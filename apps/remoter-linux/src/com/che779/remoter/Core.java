package com.che779.remoter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Quan ly cac tien trinh Emulator V1 (mot game = mot JVM) va gui lenh IPC. Khong phu thuoc giao dien. */
final class Core {
    static final class Tab {
        final int id;
        final File game;
        final int port;
        volatile Process proc;
        volatile String state = "Khoi dong";
        volatile String proxy = "none";
        volatile boolean sleeping;
        volatile boolean checked = true;
        volatile long pid = -1;
        volatile boolean hidden;
        Tab(int id, File game, int port) { this.id = id; this.game = game; this.port = port; }
        boolean alive() { Process p = proc; return p != null && p.isAlive(); }
        String name() { return game.getName(); }
    }

    final List<Tab> tabs = new CopyOnWriteArrayList<Tab>();
    final File emuJar;
    final int basePort;
    int ramMb = 512;
    private int nextId = 1;

    Core(File emuJar, int basePort) { this.emuJar = emuJar; this.basePort = basePort; }

    private synchronized int freePort() {
        for (int p = basePort; p < basePort + 400; p++) {
            boolean used = false;
            for (Tab t : tabs) if (t.port == p && t.alive()) used = true;
            if (used) continue;
            try (java.net.ServerSocket ss = new java.net.ServerSocket(p, 1, java.net.InetAddress.getByName("127.0.0.1"))) {
                return p;
            } catch (Exception e) { /* ban */ }
        }
        throw new IllegalStateException("Het cong IPC trong dai " + basePort + "+400");
    }

    /** Mo mot tab moi chay game. Tra ve tab hoac nem loi. */
    synchronized Tab open(File game) throws Exception {
        if (!emuJar.isFile()) throw new IllegalStateException("Khong thay Che779_Emulator.jar: " + emuJar);
        if (!game.isFile()) throw new IllegalStateException("Khong thay game: " + game);
        int port = freePort();
        Tab t = new Tab(nextId++, game, port);
        start(t);
        tabs.add(t);
        return t;
    }

    private void start(Tab t) throws Exception {
        List<String> cmd = new ArrayList<String>();
        cmd.add(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java");
        cmd.add("-Xmx" + ramMb + "m");
        cmd.add("-XX:+UseSerialGC");
        cmd.add("-Djava.net.preferIPv4Stack=true");
        cmd.add("-Dche779.port=" + t.port);
        cmd.add("-jar");
        cmd.add(emuJar.getAbsolutePath());
        cmd.add(t.game.getAbsolutePath());
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        pb.redirectOutput(ProcessBuilder.Redirect.to(new File(System.getProperty("java.io.tmpdir"), "che779-tab" + t.id + ".log")));
        t.proc = pb.start();
        t.state = "Khoi dong";
        t.hidden = false;
    }

    /** Khoi dong lai tab (neu da thoat hoac treo). */
    synchronized void restart(Tab t) throws Exception {
        close(t, false);
        start(t);
    }

    /** Doi cong IPC san sang, toi da waitMs. */
    boolean waitReady(Tab t, int waitMs) {
        long end = System.currentTimeMillis() + waitMs;
        while (System.currentTimeMillis() < end) {
            if (!t.alive()) return false;
            if (Ipc.ok(Ipc.call(t.port, "{\"action\":\"ping\"}", 400))) return true;
            try { Thread.sleep(250); } catch (InterruptedException e) { return false; }
        }
        return false;
    }

    /** Cap nhat trang thai tu IPC. */
    void poll(Tab t) {
        if (!t.alive()) { t.state = "Da thoat"; return; }
        String r = Ipc.call(t.port, "{\"action\":\"status\"}", 700);
        if (!Ipc.ok(r)) { t.state = "Khong phan hoi"; return; }
        t.sleeping = "true".equals(Ipc.field(r, "sleeping"));
        String pid = Ipc.field(r, "pid");
        try { if (pid != null) t.pid = Long.parseLong(pid.trim()); } catch (NumberFormatException ignore) {}
        t.state = t.hidden ? "An" : (t.sleeping ? "Ngu" : "Dang chay");
        String px = Ipc.call(t.port, "{\"action\":\"proxy_get\"}", 700);
        String d = Ipc.field(px, "proxy");
        if (d != null) t.proxy = d;
    }

    boolean send(Tab t, String json) { return Ipc.ok(Ipc.call(t.port, json, 1200)); }

    boolean key(Tab t, String key) { return send(t, Ipc.json("action", "keypress", "key", key)); }

    boolean mouse(Tab t, String type, double fx, double fy) {
        return send(t, Ipc.json("action", "mouse", "type", type, "fx", "#" + fx, "fy", "#" + fy));
    }

    boolean show(Tab t, boolean visible) {
        boolean r = send(t, Ipc.json("action", visible ? "show" : "hide"));
        if (r) t.hidden = !visible;
        return r;
    }

    boolean sleep(Tab t, boolean on) { return send(t, Ipc.json("action", "sleep", "state", on ? "on" : "off")); }

    boolean cleanRam(Tab t) { return send(t, "{\"action\":\"clean_ram\"}"); }

    /** spec: "host:port" hoac "host:port:user:pass"; null/rong = tat proxy. */
    boolean proxy(Tab t, String spec) {
        if (spec == null || spec.trim().isEmpty()) return send(t, "{\"action\":\"proxy\",\"mode\":\"none\"}");
        String[] p = spec.trim().split(":");
        if (p.length < 2) return false;
        return send(t, Ipc.json("action", "proxy", "mode", "set", "host", p[0], "port", p[1],
                "user", p.length > 2 ? p[2] : "", "pass", p.length > 3 ? p[3] : "", "type", "AUTO"));
    }

    boolean screenshot(Tab t, File out) {
        return send(t, Ipc.json("action", "screenshot", "path", out.getAbsolutePath()));
    }

    /** Dong tab: quit qua IPC, neu khong thoat thi huy tien trinh. remove=true thi go khoi danh sach. */
    void close(Tab t, boolean remove) {
        if (t.alive()) {
            Ipc.call(t.port, "{\"action\":\"quit\"}", 800);
            Process p = t.proc;
            try { if (p != null && !p.waitFor(2, java.util.concurrent.TimeUnit.SECONDS)) p.destroyForcibly(); }
            catch (InterruptedException e) { if (p != null) p.destroyForcibly(); }
        }
        t.state = "Da thoat";
        if (remove) tabs.remove(t);
    }

    void closeAll() { for (Tab t : new ArrayList<Tab>(tabs)) close(t, true); }

    /** Cac tab duoc chon: neu sync bat thi toan bo tab da tick, nguoc lai chi tab chinh. */
    List<Tab> targets(Tab main, boolean sync) {
        List<Tab> r = new ArrayList<Tab>();
        if (main != null) r.add(main);
        if (sync) for (Tab t : tabs) if (t != main && t.checked && t.alive()) r.add(t);
        return r;
    }
}
