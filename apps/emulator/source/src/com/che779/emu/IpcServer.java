package com.che779.emu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

/** Socket server localhost: moi dong la 1 lenh JSON don gian, tra ve 1 dong JSON. Chay nen, khong block UI. */
final class IpcServer {
    private static final int CLIENT_TIMEOUT_MS = 5000;
    private static final int MAX_LINE_CHARS = 65536;
    private static final Semaphore CLIENT_SLOTS = new Semaphore(16);
    private final int basePort;
    private ServerSocket server;

    IpcServer(int basePort) { this.basePort = basePort; }

    /** Bind 127.0.0.1; neu cong ban thi thu cong ke tiep (toi da 200 cong). @return cong that su. */
    int start() throws IOException {
        IOException last = null;
        for (int p = basePort; p < basePort + 200; p++) {
            try {
                server = new ServerSocket(p, 16, InetAddress.getByName("127.0.0.1"));
                break;
            } catch (IOException e) { last = e; }
        }
        if (server == null) throw last != null ? last : new IOException("no port");
        Thread t = new Thread(new Runnable() { public void run() { acceptLoop(); } }, "che779-ipc");
        t.setDaemon(true);
        t.start();
        return server.getLocalPort();
    }

    private void acceptLoop() {
        while (!server.isClosed()) {
            try {
                final Socket s = server.accept();
                if (!CLIENT_SLOTS.tryAcquire()) {
                    try { s.close(); } catch (IOException ignore) {}
                    continue;
                }
                Thread c = new Thread(new Runnable() { public void run() { serve(s); } }, "che779-ipc-client");
                c.setDaemon(true);
                try {
                    c.start();
                } catch (RuntimeException e) {
                    CLIENT_SLOTS.release();
                    try { s.close(); } catch (IOException ignore) {}
                }
            } catch (IOException e) { if (server.isClosed()) return; }
        }
    }

    private void serve(Socket s) {
        try {
            s.setSoTimeout(CLIENT_TIMEOUT_MS);
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), "UTF-8"));
            PrintWriter out = new PrintWriter(new java.io.OutputStreamWriter(s.getOutputStream(), "UTF-8"), true);
            String line;
            while ((line = readBoundedLine(in)) != null) {
                line = line.trim();
                if (line.length() == 0) continue;
                out.println(handle(line));
            }
        } catch (IOException ignore) {
        } finally {
            try { s.close(); } catch (IOException ignore) {}
            CLIENT_SLOTS.release();
        }
    }

    /** Read one protocol line without allowing an unbounded allocation. */
    private static String readBoundedLine(BufferedReader in) throws IOException {
        StringBuilder line = new StringBuilder();
        int ch;
        while ((ch = in.read()) != -1) {
            if (ch == '\n') return line.toString();
            if (ch != '\r') {
                if (line.length() >= MAX_LINE_CHARS) {
                    throw new IOException("IPC line exceeds " + MAX_LINE_CHARS + " characters");
                }
                line.append((char) ch);
            }
        }
        return line.length() == 0 ? null : line.toString();
    }

    /**
     * Parse the flat JSON object used by the Remoter protocol. Unlike the former
     * regex scanner this honours JSON string boundaries, escapes and primitive
     * values, and rejects malformed/trailing input instead of guessing fields.
     */
    static Map<String, String> parse(String json) {
        Map<String, String> result = new HashMap<String, String>();
        if (json == null) return result;
        try {
            JsonObjectReader reader = new JsonObjectReader(json);
            result.putAll(reader.read());
        } catch (IllegalArgumentException e) {
            result.clear();
        }
        return result;
    }

    private static final class JsonObjectReader {
        private final String input;
        private int pos;

        JsonObjectReader(String input) { this.input = input; }

        Map<String, String> read() {
            Map<String, String> values = new HashMap<String, String>();
            ws();
            expect('{');
            ws();
            if (take('}')) { ws(); end(); return values; }
            while (true) {
                ws();
                String key = string();
                ws();
                expect(':');
                ws();
                String value = scalar();
                values.put(key, value);
                ws();
                if (take('}')) break;
                expect(',');
            }
            ws();
            end();
            return values;
        }

        private String scalar() {
            if (peek() == '"') return string();
            int begin = pos;
            while (pos < input.length()) {
                char c = input.charAt(pos);
                if (c == ',' || c == '}' || Character.isWhitespace(c)) break;
                pos++;
            }
            if (begin == pos) throw error();
            String token = input.substring(begin, pos);
            if ("true".equals(token) || "false".equals(token) || "null".equals(token)
                    || token.matches("-?(0|[1-9][0-9]*)(\\.[0-9]+)?([eE][+-]?[0-9]+)?")) {
                return "null".equals(token) ? null : token;
            }
            throw error();
        }

        private String string() {
            expect('"');
            StringBuilder out = new StringBuilder();
            while (pos < input.length()) {
                char c = input.charAt(pos++);
                if (c == '"') return out.toString();
                if (c < 0x20) throw error();
                if (c != '\\') { out.append(c); continue; }
                if (pos >= input.length()) throw error();
                char e = input.charAt(pos++);
                switch (e) {
                    case '"': out.append('"'); break;
                    case '\\': out.append('\\'); break;
                    case '/': out.append('/'); break;
                    case 'b': out.append('\b'); break;
                    case 'f': out.append('\f'); break;
                    case 'n': out.append('\n'); break;
                    case 'r': out.append('\r'); break;
                    case 't': out.append('\t'); break;
                    case 'u':
                        if (pos + 4 > input.length()) throw error();
                        try { out.append((char) Integer.parseInt(input.substring(pos, pos + 4), 16)); }
                        catch (NumberFormatException ex) { throw error(); }
                        pos += 4;
                        break;
                    default: throw error();
                }
            }
            throw error();
        }

        private char peek() { return pos < input.length() ? input.charAt(pos) : '\0'; }
        private void ws() { while (pos < input.length() && Character.isWhitespace(input.charAt(pos))) pos++; }
        private boolean take(char c) { if (peek() == c) { pos++; return true; } return false; }
        private void expect(char c) { if (!take(c)) throw error(); }
        private void end() { if (pos != input.length()) throw error(); }
        private IllegalArgumentException error() { return new IllegalArgumentException("Invalid IPC JSON at " + pos); }
    }

    static String handle(String json) {
        Map<String, String> m = parse(json);
        String a = m.get("action");
        if (a == null) return "{\"ok\":false,\"error\":\"missing action\"}";
        a = a.toLowerCase();
        boolean ok;
        if (a.equals("keypress")) {
            int hold = 60;
            try { if (m.get("hold") != null) hold = Integer.parseInt(m.get("hold")); } catch (NumberFormatException ignore) {}
            ok = Che779Main.sendKey(m.get("key"), true, true, hold);
        } else if (a.equals("keydown")) {
            ok = Che779Main.sendKey(m.get("key"), true, false, 0);
        } else if (a.equals("keyup")) {
            ok = Che779Main.sendKey(m.get("key"), false, true, 0);
        } else if (a.equals("text")) {
            String v = m.get("text") != null ? m.get("text") : m.get("value");
            ok = Che779Main.typeText(v);
        } else if (a.equals("clean_ram")) {
            Che779Main.runCleanAsync(); ok = true;
        } else if (a.equals("sleep")) {
            ok = Che779Main.setSleep(m.get("state"));
        } else if (a.equals("hide")) {
            ok = Che779Main.setVisible(false);
        } else if (a.equals("show")) {
            ok = Che779Main.setVisible(true);
        } else if (a.equals("mouse")) {
            double fx = 0, fy = 0;
            try { fx = Double.parseDouble(m.get("fx")); fy = Double.parseDouble(m.get("fy")); } catch (Exception e) { return "{\"ok\":false,\"error\":\"bad coords\"}"; }
            ok = Che779Main.mouse(m.get("type"), fx, fy);
        } else if (a.equals("proxy")) {
            String mode = m.get("mode");
            if ("none".equals(mode)) {
                org.microemu.app.ProxyManager.setOverride(null);
            } else {
                org.microemu.app.ProxyManager.Proxy px = org.microemu.app.ProxyManager.parseSpec(
                        m.get("host") + "|" + m.get("port") + "|" + (m.get("user") == null ? "" : m.get("user"))
                        + "|" + (m.get("pass") == null ? "" : m.get("pass")) + "|" + (m.get("type") == null ? "AUTO" : m.get("type")));
                if (px == null) return "{\"ok\":false,\"error\":\"bad proxy\"}";
                org.microemu.app.ProxyManager.setOverride(px);
            }
            org.microemu.app.Main.statusMessage("Proxy: " + org.microemu.app.ProxyManager.describeActive());
            ok = true;
        } else if (a.equals("proxy_get")) {
            return "{\"ok\":true,\"proxy\":\"" + org.microemu.app.ProxyManager.describeActive().replace("\"", "") + "\"}";
        } else if (a.equals("ping")) {
            ok = true;
        } else if (a.equals("status")) {
            return "{\"ok\":true,\"pid\":" + Che779Main.pid() + ",\"port\":" + Che779Main.port
                    + ",\"sleeping\":" + Che779Main.isSleeping()
                    + ",\"version\":\"" + org.microemu.app.Che779.VERSION + "\"}";
        } else if (a.equals("screenshot")) {
            ok = Che779Main.screenshot(m.get("path"));
        } else if (a.equals("version")) {
            return "{\"ok\":true,\"version\":\"" + org.microemu.app.Che779.VERSION + "\"}";
        } else if (a.equals("quit") || a.equals("close")) {
            new Thread(new Runnable() { public void run() {
                try { Thread.sleep(150); } catch (InterruptedException ignore) {}
                System.exit(0);
            }}).start();
            ok = true;
        } else {
            return "{\"ok\":false,\"error\":\"unknown action\"}";
        }
        return ok ? "{\"ok\":true}" : "{\"ok\":false,\"error\":\"failed\"}";
    }
}
