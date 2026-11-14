package com.che779.remoter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/** Cung cap client IPC (JSON mot dong, TCP loopback) cho Emulator V1. Moi lenh mot ket noi, co timeout. */
final class Ipc {
    private Ipc() {}

    static String esc(String s) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '"' || c == '\\') b.append('\\').append(c);
            else if (c == '\n') b.append("\\n");
            else if (c == '\r') b.append("\\r");
            else if (c < 0x20) b.append(' ');
            else b.append(c);
        }
        return b.toString();
    }

    /** Dung chuoi JSON phang tu cac cap khoa/gia tri (gia tri la chuoi, tru khi bat dau bang '#': so). */
    static String json(String... kv) {
        StringBuilder b = new StringBuilder("{");
        for (int i = 0; i + 1 < kv.length; i += 2) {
            if (i > 0) b.append(',');
            b.append('"').append(esc(kv[i])).append("\":");
            String v = kv[i + 1];
            if (v.startsWith("#")) b.append(v.substring(1));
            else b.append('"').append(esc(v)).append('"');
        }
        return b.append('}').toString();
    }

    /** Gui mot lenh; tra ve dong phan hoi hoac null neu khong ket noi duoc. */
    static String call(int port, String request, int timeoutMs) {
        try (Socket s = new Socket()) {
            s.connect(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), port), timeoutMs);
            s.setSoTimeout(timeoutMs);
            OutputStream o = s.getOutputStream();
            o.write((request + "\n").getBytes(StandardCharsets.UTF_8));
            o.flush();
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
            return in.readLine();
        } catch (Exception e) {
            return null;
        }
    }

    static boolean ok(String response) {
        return response != null && response.contains("\"ok\":true");
    }

    /** Lay gia tri so/chuoi don gian tu phan hoi phang; null neu khong co. */
    static String field(String response, String key) {
        if (response == null) return null;
        int i = response.indexOf("\"" + key + "\":");
        if (i < 0) return null;
        i += key.length() + 3;
        if (i < response.length() && response.charAt(i) == '"') {
            int j = response.indexOf('"', i + 1);
            return j < 0 ? null : response.substring(i + 1, j);
        }
        int j = i;
        while (j < response.length() && ",}".indexOf(response.charAt(j)) < 0) j++;
        return response.substring(i, j);
    }
}
