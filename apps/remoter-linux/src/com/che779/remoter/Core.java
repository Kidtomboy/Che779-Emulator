package com.che779.remoter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Quan ly cac tien trinh Emulator V1 (mot game = mot JVM) va gui lenh IPC. Khong phu thuoc giao dien. */
final class Core {
    static final class Tab {
        final int id;
        volatile File game;
        volatile int port;
        volatile boolean stopRequested;
        volatile int restarts;
        volatile double cpu = -1;
        volatile long rssMb = -1;
        long lastTicks = -1, lastAt;
        volatile Process proc;
        volatile String state = "Chưa chạy";
        volatile String proxy = "none";
        volatile boolean sleeping;
        volatile boolean checked = true;
        volatile long pid = -1;
        volatile boolean hidden;
        Tab(int id, File game) { this.id = id; this.game = game; }
        boolean alive() { Process p = proc; return p != null && p.isAlive(); }
        String name() { return game == null ? "(chưa chọn game)" : game.getName(); }
    }

    final List<Tab> tabs = new CopyOnWriteArrayList<Tab>();
    volatile File emuJar;
    final int basePort;
    static final long CLK = 100;
    int ramMb = 512;
    int waitMs = 2000;
    boolean separateData;
    File javaExe = new File(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java");
    File dataRoot = new File(System.getProperty("user.home"), ".che779" + File.separator + "Che779-Emulator");
    private int nextId = 1;

    void emuJar_set(File f) { this.emuJar = f; }

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

    /** Them mot dong tab (chua chay). */
    synchronized Tab addRow(File game) { Tab t = new Tab(nextId++, game); tabs.add(t); return t; }

    /** Them dong va chay ngay (dung cho tham so dong lenh). */
    synchronized Tab open(File game) throws Exception { Tab t = addRow(game); start(t); return t; }

    /** Chay (hoac chay lai) mot dong tab. */
    synchronized void start(Tab t) throws Exception {
        if (!emuJar.isFile()) throw new IllegalStateException("Không thấy Che779_Emulator.jar: " + emuJar);
        if (t.game == null || !t.game.isFile()) { t.state = "Thiếu file game"; throw new IllegalStateException("Không thấy game: " + t.game); }
        if (t.alive()) return;
        t.port = freePort();
        t.stopRequested = false;
        launch(t);
    }

    private void launch(Tab t) throws Exception {
        List<String> cmd = new ArrayList<String>();
        cmd.add(javaExe.getPath());
        cmd.add("-Xmx" + ramMb + "m");
        cmd.add("-Xms8m");
        cmd.add("-XX:+UseSerialGC");
        cmd.add("-Djava.net.preferIPv4Stack=true");
        cmd.add("-Dche779.port=" + t.port);
        File data = separateData ? new File(dataRoot, "tab" + t.id) : dataRoot;
        data.mkdirs();
        cmd.add("-Dche779.data=" + data.getAbsolutePath());
        cmd.add("-jar");
        cmd.add(emuJar.getAbsolutePath());
        cmd.add(t.game.getAbsolutePath());
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        pb.redirectOutput(ProcessBuilder.Redirect.to(new File(System.getProperty("java.io.tmpdir"), "che779-tab" + t.id + ".log")));
        t.proc = pb.start();
        t.state = "Đang mở...";
        t.hidden = false;
        t.pid = -1; t.cpu = -1; t.rssMb = -1; t.lastTicks = -1;
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
        if (!t.alive()) { if (t.proc != null) t.state = t.stopRequested ? "Đã dừng" : "Đã thoát"; t.cpu = -1; t.rssMb = -1; return; }
        String r = Ipc.call(t.port, "{\"action\":\"status\"}", 700);
        if (!Ipc.ok(r)) { t.state = "Không phản hồi"; return; }
        t.sleeping = "true".equals(Ipc.field(r, "sleeping"));
        String pid = Ipc.field(r, "pid");
        try { if (pid != null) t.pid = Long.parseLong(pid.trim()); } catch (NumberFormatException ignore) {}
        t.state = t.hidden ? "Ẩn" : (t.sleeping ? "Ngủ" : "Đang chạy");
        procStats(t);
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
        t.stopRequested = true;
        if (t.alive()) {
            Ipc.call(t.port, "{\"action\":\"quit\"}", 800);
            Process p = t.proc;
            try { if (p != null && !p.waitFor(2, java.util.concurrent.TimeUnit.SECONDS)) p.destroyForcibly(); }
            catch (InterruptedException e) { if (p != null) p.destroyForcibly(); }
        }
        t.stopRequested = true;
        t.state = remove ? "Đã đóng" : "Đã dừng";
        t.cpu = -1; t.rssMb = -1;
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

    /** CPU/RAM cua tien trinh tab qua /proc (chi Linux); khong co thi de -1. */
    void procStats(Tab t) {
        long pid = t.pid;
        if (pid <= 0) return;
        try {
            String st = new String(java.nio.file.Files.readAllBytes(new File("/proc/" + pid + "/stat").toPath()), "UTF-8");
            String[] f = st.substring(st.lastIndexOf(')') + 2).split(" ");
            long ticks = Long.parseLong(f[11]) + Long.parseLong(f[12]);
            long now = System.nanoTime();
            if (t.lastTicks >= 0 && now > t.lastAt) t.cpu = Math.max(0, (ticks - t.lastTicks) * 1e9 / CLK / (now - t.lastAt) * 100.0);
            t.lastTicks = ticks; t.lastAt = now;
            for (String line : java.nio.file.Files.readAllLines(new File("/proc/" + pid + "/status").toPath(), java.nio.charset.StandardCharsets.UTF_8))
                if (line.startsWith("VmRSS:")) t.rssMb = Long.parseLong(line.replaceAll("[^0-9]", "")) / 1024;
        } catch (Exception e) { /* khong phai Linux hoac tien trinh da thoat */ }
    }

    /** RAM he thong (usedMb, totalMb) tu /proc/meminfo; null neu khong doc duoc. */
    static long[] sysMem() {
        try {
            long total = -1, avail = -1;
            for (String l : java.nio.file.Files.readAllLines(new File("/proc/meminfo").toPath(), java.nio.charset.StandardCharsets.UTF_8)) {
                if (l.startsWith("MemTotal:")) total = Long.parseLong(l.replaceAll("[^0-9]", "")) / 1024;
                else if (l.startsWith("MemAvailable:")) avail = Long.parseLong(l.replaceAll("[^0-9]", "")) / 1024;
            }
            return total > 0 && avail >= 0 ? new long[] { total - avail, total } : null;
        } catch (Exception e) { return null; }
    }

    private long[] lastCpu;
    /** CPU he thong 0..100 theo /proc/stat (so voi lan goi truoc); -1 neu khong co. */
    synchronized double sysCpu() {
        try {
            String[] f = java.nio.file.Files.readAllLines(new File("/proc/stat").toPath(), java.nio.charset.StandardCharsets.UTF_8).get(0).trim().split("\\s+");
            long idle = Long.parseLong(f[4]) + Long.parseLong(f[5]), total = 0;
            for (int i = 1; i < f.length && i < 9; i++) total += Long.parseLong(f[i]);
            double r = -1;
            if (lastCpu != null && total > lastCpu[1]) r = 100.0 * (1.0 - (idle - lastCpu[0]) / (double) (total - lastCpu[1]));
            lastCpu = new long[] { idle, total };
            return r;
        } catch (Exception e) { return -1; }
    }
}
