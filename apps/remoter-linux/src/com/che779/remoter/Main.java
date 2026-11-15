package com.che779.remoter;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;

/**
 * Che779 Remoter (Java/Swing) - Linux (chay duoc ca Windows/macOS co Java).
 * Bo cuc 6 khu vuc theo Remoter Windows: duong dan, cau hinh tab, hanh dong, danh sach tab, dong bo, tien ich.
 * Dieu khien nhieu Emulator V1 (moi game mot tien trinh Java) qua IPC TCP 127.0.0.1.
 */
public final class Main {
    static final String VERSION = "1.2.0";
    static final File HOME = new File(System.getProperty("user.home"), ".che779");
    static final File REMOTER_DIR = new File(HOME, "Che779-Remoter");
    static final File CFG = new File(REMOTER_DIR, "che779_remoter.ini");

    // ---------- chu de ----------
    static final class Theme {
        Color bg, card, text, muted, accent, accentText, border, field, gridAlt, danger;
        static Theme dark() {
            Theme t = new Theme();
            t.bg = new Color(0x1B1C21); t.card = new Color(0x25272E); t.text = new Color(0xE8EAEF); t.muted = new Color(0x9AA0AC);
            t.accent = new Color(0xFF7AA8); t.accentText = new Color(0x251018); t.border = new Color(0x3A3D47);
            t.field = new Color(0x1F2127); t.gridAlt = new Color(0x292B33); t.danger = new Color(0xFF6B6B);
            return t;
        }
        static Theme light() {
            Theme t = new Theme();
            t.bg = new Color(0xF3F4F7); t.card = Color.WHITE; t.text = new Color(0x22252B); t.muted = new Color(0x6B7280);
            t.accent = new Color(0xE8588B); t.accentText = Color.WHITE; t.border = new Color(0xD9DCE3);
            t.field = Color.WHITE; t.gridAlt = new Color(0xF8F9FB); t.danger = new Color(0xD63B3B);
            return t;
        }
    }

    Theme th = Theme.dark();
    final Properties cfg = new Properties();
    final Core core;
    final ExecutorService io = Executors.newSingleThreadExecutor(r -> { Thread t = new Thread(r, "che779-io"); t.setDaemon(true); return t; });
    final ExecutorService bg = Executors.newCachedThreadPool(r -> { Thread t = new Thread(r, "che779-bg"); t.setDaemon(true); return t; });

    JFrame frame;
    JTable table;
    TabModel model;
    JTextField tfEmu, tfGame, tfJava;
    JSpinner spRam, spTabs, spWait, spW, spH, spCols, spRows, spCleanMin, spGuardPct, spTimerMin;
    JCheckBox cbAuto, cbSep, cbSyncTab, cbSyncKey, cbSyncMouse, cbDark, cbRestart, cbAutoStart, cbCleanAuto, cbGuard, cbTimer, cbTop;
    JLabel status = new JLabel(" "), sysInfo = new JLabel(" "), runInfo = new JLabel(" ");
    JPanel tabGrid;
    JDialog previewDlg;
    Preview preview = new Preview();
    volatile BufferedImage shot;
    volatile boolean shotBusy;
    final List<JComponent> cards = new ArrayList<JComponent>();
    long lastClean = System.currentTimeMillis(), timerDeadline = 0;
    boolean guardSlept;

    // ---------- cau hinh ----------
    static File findEmuJar() {
        String[] c = { "Che779_Emulator.jar", "../emulator/source/Che779_Emulator.jar", "../emulator/prebuilt/Che779_Emulator.jar",
                "apps/emulator/source/Che779_Emulator.jar", "apps/emulator/prebuilt/Che779_Emulator.jar" };
        File here = null;
        try { here = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile(); } catch (Exception e) { /* bo qua */ }
        for (String s : c) {
            File f = new File(s); if (f.isFile()) return f;
            if (here != null) { f = new File(here, s); if (f.isFile()) return f; }
        }
        return new File("Che779_Emulator.jar");
    }

    Main() {
        try (FileInputStream in = new FileInputStream(CFG)) { cfg.load(new InputStreamReader(in, "UTF-8")); } catch (Exception e) { /* lan dau */ }
        File jar = new File(cfg.getProperty("emu", findEmuJar().getPath()));
        core = new Core(jar, 7790);
    }

    static int parseInt(String s, int d) { try { return Integer.parseInt(s.trim()); } catch (Exception e) { return d; } }
    int geti(String k, int d) { return parseInt(cfg.getProperty(k, ""), d); }
    boolean getb(String k, boolean d) { String v = cfg.getProperty(k, ""); return v.equals("1") ? true : v.equals("0") ? false : d; }
    static int val(JSpinner s) { return ((Number) s.getValue()).intValue(); }

    /** Doc gia tri tu cac o nhap vao cfg. */
    void collect() {
        cfg.setProperty("emu", tfEmu.getText().trim()); cfg.setProperty("game", tfGame.getText().trim()); cfg.setProperty("java", tfJava.getText().trim());
        cfg.setProperty("xmx", String.valueOf(val(spRam))); cfg.setProperty("tabs", String.valueOf(val(spTabs))); cfg.setProperty("delay", String.valueOf(val(spWait)));
        cfg.setProperty("w", String.valueOf(val(spW))); cfg.setProperty("h", String.valueOf(val(spH)));
        cfg.setProperty("cols", String.valueOf(val(spCols))); cfg.setProperty("rows", String.valueOf(val(spRows)));
        cfg.setProperty("cleanmin", String.valueOf(val(spCleanMin))); cfg.setProperty("guardpct", String.valueOf(val(spGuardPct))); cfg.setProperty("timermin", String.valueOf(val(spTimerMin)));
        setb("auto", cbAuto); setb("sep", cbSep); setb("synctab", cbSyncTab); setb("synckey", cbSyncKey); setb("syncmouse", cbSyncMouse); setb("dark", cbDark);
        setb("restart", cbRestart); setb("autostart", cbAutoStart); setb("cleanauto", cbCleanAuto); setb("guard", cbGuard); setb("timer", cbTimer); setb("top", cbTop);
    }
    void setb(String k, JCheckBox c) { cfg.setProperty(k, c.isSelected() ? "1" : "0"); }

    /** Dat lai cac o nhap tu cfg. */
    void applyCfg() {
        tfEmu.setText(cfg.getProperty("emu", core.emuJar.getPath())); tfGame.setText(cfg.getProperty("game", "")); tfJava.setText(cfg.getProperty("java", defaultJava()));
        spRam.setValue(geti("xmx", 512)); spTabs.setValue(geti("tabs", 12)); spWait.setValue(geti("delay", 2000));
        spW.setValue(geti("w", 226)); spH.setValue(geti("h", 334)); spCols.setValue(geti("cols", 6)); spRows.setValue(geti("rows", 2));
        spCleanMin.setValue(geti("cleanmin", 15)); spGuardPct.setValue(geti("guardpct", 90)); spTimerMin.setValue(geti("timermin", 60));
        cbAuto.setSelected(getb("auto", true)); cbSep.setSelected(getb("sep", false)); cbSyncTab.setSelected(getb("synctab", true));
        cbSyncKey.setSelected(getb("synckey", true)); cbSyncMouse.setSelected(getb("syncmouse", false)); cbDark.setSelected(getb("dark", true));
        cbRestart.setSelected(getb("restart", false)); cbAutoStart.setSelected(getb("autostart", false)); cbCleanAuto.setSelected(getb("cleanauto", false));
        cbGuard.setSelected(getb("guard", false)); cbTimer.setSelected(getb("timer", false)); cbTop.setSelected(getb("top", false));
        syncCore();
    }

    static String defaultJava() { return System.getProperty("java.home") + File.separator + "bin" + File.separator + "java"; }

    /** Day cac gia tri vao Core (RAM, cho, tach du lieu, java). */
    void syncCore() {
        core.ramMb = Math.max(64, val(spRam));
        core.waitMs = Math.max(0, val(spWait));
        core.separateData = cbSep.isSelected();
        String j = tfJava.getText().trim();
        if (!j.isEmpty()) core.javaExe = new File(j);
        frame.setAlwaysOnTop(cbTop.isSelected());
    }

    void saveCfg() {
        collect();
        try { REMOTER_DIR.mkdirs(); try (OutputStream o = new FileOutputStream(CFG)) { cfg.store(new OutputStreamWriter(o, "UTF-8"), "Che779 Remoter"); } } catch (Exception e) { /* bo qua */ }
    }

    // ---------- bang tab ----------
    final class TabModel extends AbstractTableModel {
        final String[] cols = { "", "Tab", "Game", "Trạng thái", "Cổng", "CPU", "RAM", "Ngủ", "PID" };
        public int getRowCount() { return core.tabs.size(); }
        public int getColumnCount() { return cols.length; }
        public String getColumnName(int c) { return cols[c]; }
        public Class<?> getColumnClass(int c) { return c == 0 ? Boolean.class : String.class; }
        public boolean isCellEditable(int r, int c) { return c == 0; }
        public Object getValueAt(int r, int c) {
            if (r >= core.tabs.size()) return "";
            Core.Tab t = core.tabs.get(r);
            switch (c) {
                case 0: return t.checked;
                case 1: return String.valueOf(r + 1);
                case 2: return t.name();
                case 3: return t.state;
                case 4: return t.alive() ? String.valueOf(t.port) : "-";
                case 5: return t.alive() && t.cpu >= 0 ? String.format("%.0f%%", t.cpu) : "-";
                case 6: return t.alive() && t.rssMb >= 0 ? t.rssMb + " MB" : "-";
                case 7: return t.alive() ? (t.sleeping ? "Ngủ" : "-") : "-";
                default: return t.alive() && t.pid > 0 ? String.valueOf(t.pid) : "-";
            }
        }
        public void setValueAt(Object v, int r, int c) { if (c == 0 && r < core.tabs.size()) core.tabs.get(r).checked = Boolean.TRUE.equals(v); }
    }

    Core.Tab selected() {
        int r = table.getSelectedRow();
        if (r < 0 || r >= core.tabs.size()) return core.tabs.isEmpty() ? null : core.tabs.get(0);
        return core.tabs.get(r);
    }

    /** Cac tab nhan lenh: "Dong bo Tab" bat thi moi tab da tick (+ tab dang chon), nguoc lai chi tab dang chon. */
    List<Core.Tab> targets() { return core.targets(selected(), cbSyncTab.isSelected()); }

    void msg(String s) { SwingUtilities.invokeLater(() -> status.setText(s)); }
    void refresh() {
        int r = table.getSelectedRow();
        model.fireTableDataChanged();
        if (r >= 0 && r < model.getRowCount()) table.setRowSelectionInterval(r, r);
        refreshGrid();
    }

    // ---------- hanh dong ----------
    interface TabAction { void run(Core.Tab t); }

    void forTargets(String label, TabAction a) {
        List<Core.Tab> ts = targets();
        if (ts.isEmpty()) { msg("Chưa có tab nào."); return; }
        bg.execute(() -> { for (Core.Tab t : ts) a.run(t); msg(label + " (" + ts.size() + " tab)"); SwingUtilities.invokeLater(this::refresh); });
    }

    File gameFile() { String g = tfGame.getText().trim(); return g.isEmpty() ? null : new File(g); }

    /** Bao dam co du dong tab theo "So tab", roi chay cac tab chua chay. */
    void onStart() {
        syncCore();
        core.emuJar_set(new File(tfEmu.getText().trim()));
        if (!core.emuJar.isFile()) { msg("Chưa chọn đúng Che779_Emulator.jar."); return; }
        File g = gameFile();
        int want = val(spTabs);
        while (core.tabs.size() < want) { if (g == null) break; core.addRow(g); }
        if (core.tabs.isEmpty()) { msg("Chưa chọn game (hoặc dùng \"Thêm game…\")."); return; }
        for (Core.Tab t : core.tabs) if (t.game == null && g != null) t.game = g;
        refresh();
        startTabs(new ArrayList<Core.Tab>(core.tabs));
    }

    void startTabs(List<Core.Tab> list) {
        if (cbTimer.isSelected()) timerDeadline = System.currentTimeMillis() + val(spTimerMin) * 60000L;
        bg.execute(() -> {
            int n = 0;
            for (Core.Tab t : list) {
                if (t.alive()) continue;
                try { core.start(t); n++; msg("Đang mở tab " + t.id + "…"); core.waitReady(t, 20000); }
                catch (Exception e) { msg("Lỗi tab " + t.id + ": " + e.getMessage()); }
                SwingUtilities.invokeLater(this::refresh);
                try { Thread.sleep(core.waitMs); } catch (InterruptedException ie) { return; }
            }
            msg("Đã chạy " + n + " tab");
            if (cbAuto.isSelected()) arrange();
        });
    }

    void onStop() { bg.execute(() -> { for (Core.Tab t : core.tabs) core.close(t, false); SwingUtilities.invokeLater(this::refresh); msg("Đã dừng các tab"); }); }

    void onCloseAll() { bg.execute(() -> { core.closeAll(); SwingUtilities.invokeLater(this::refresh); msg("Đã đóng hết"); }); }

    void onAddGames() {
        JFileChooser fc = new JFileChooser(cfg.getProperty("lastDir", System.getProperty("user.home")));
        fc.setMultiSelectionEnabled(true);
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Game J2ME (*.jar)", "jar"));
        if (fc.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) return;
        cfg.setProperty("lastDir", fc.getCurrentDirectory().getPath());
        List<Core.Tab> add = new ArrayList<Core.Tab>();
        for (File g : fc.getSelectedFiles()) add.add(core.addRow(g));
        refresh();
        syncCore(); core.emuJar_set(new File(tfEmu.getText().trim()));
        startTabs(add);
    }

    void onAddN() {
        String s = JOptionPane.showInputDialog(frame, "Thêm bao nhiêu tab (cùng game đang chọn)?", "1");
        if (s == null) return;
        int n = Math.max(0, Math.min(60, parseInt(s, 0)));
        File g = gameFile();
        if (g == null || !g.isFile()) { msg("Chưa chọn game ở mục 1."); return; }
        List<Core.Tab> add = new ArrayList<Core.Tab>();
        for (int i = 0; i < n; i++) add.add(core.addRow(g));
        refresh();
        syncCore(); core.emuJar_set(new File(tfEmu.getText().trim()));
        startTabs(add);
    }

    void onRemoveRows() {
        int[] rows = table.getSelectedRows();
        List<Core.Tab> del = new ArrayList<Core.Tab>();
        for (int r : rows) if (r >= 0 && r < core.tabs.size()) del.add(core.tabs.get(r));
        if (del.isEmpty()) { msg("Chọn dòng cần xóa."); return; }
        bg.execute(() -> { for (Core.Tab t : del) core.close(t, true); SwingUtilities.invokeLater(this::refresh); msg("Đã xóa " + del.size() + " dòng"); });
    }

    /** Sap xep cua so theo luoi bang xdotool (X11). Khong co xdotool/X11: bao ro, khong gia vo. */
    void arrange() {
        int w = val(spW), h = val(spH), cols = Math.max(1, val(spCols));
        List<Core.Tab> alive = new ArrayList<Core.Tab>();
        for (Core.Tab t : core.tabs) if (t.alive()) alive.add(t);
        if (alive.isEmpty()) { msg("Chưa có tab đang chạy để sắp xếp."); return; }
        if (!hasCmd("xdotool")) { msg("Sắp xếp cửa sổ cần xdotool (X11): sudo apt install xdotool. Wayland thuần chưa hỗ trợ."); return; }
        int ok = 0, i = 0;
        for (Core.Tab t : alive) {
            core.poll(t);
            int x = (i % cols) * w, y = (i / cols) * h; i++;
            if (t.pid <= 0) continue;
            String ids = run("xdotool", "search", "--pid", String.valueOf(t.pid));
            for (String id : ids.split("\\s+")) {
                if (id.isEmpty()) continue;
                run("xdotool", "windowmove", id, String.valueOf(x), String.valueOf(y));
                run("xdotool", "windowsize", id, String.valueOf(w), String.valueOf(h));
                ok++;
            }
        }
        msg(ok > 0 ? "Đã sắp xếp " + ok + " cửa sổ" : "Không tìm thấy cửa sổ để sắp xếp (cần X11 và window manager).");
    }

    static boolean hasCmd(String c) {
        for (String d : System.getenv().getOrDefault("PATH", "/usr/bin:/bin").split(File.pathSeparator)) if (new File(d, c).canExecute()) return true;
        return false;
    }

    static String run(String... cmd) {
        try {
            Process p = new ProcessBuilder(cmd).redirectErrorStream(true).start();
            StringBuilder sb = new StringBuilder();
            try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream(), "UTF-8"))) { String l; while ((l = r.readLine()) != null) sb.append(l).append('\n'); }
            p.waitFor(5, java.util.concurrent.TimeUnit.SECONDS);
            return sb.toString();
        } catch (Exception e) { return ""; }
    }

    void autoGrid() {
        int w = Math.max(1, val(spW)), n = Math.max(1, val(spTabs));
        int sw = Toolkit.getDefaultToolkit().getScreenSize().width;
        int cols = Math.max(1, Math.min(n, sw / w));
        spCols.setValue(cols); spRows.setValue((n + cols - 1) / cols);
    }

    void browse(JTextField tf, String title, boolean jarOnly) {
        JFileChooser fc = new JFileChooser(tf.getText().trim().isEmpty() ? cfg.getProperty("lastDir", System.getProperty("user.home")) : new File(tf.getText().trim()).getParent());
        fc.setDialogTitle(title);
        if (jarOnly) fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Tệp jar (*.jar)", "jar"));
        if (fc.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) { tf.setText(fc.getSelectedFile().getPath()); cfg.setProperty("lastDir", fc.getCurrentDirectory().getPath()); }
    }

    void proxyDialog() {
        String s = JOptionPane.showInputDialog(frame, "Proxy cho các tab đang nhận lệnh.\nĐịnh dạng host:port hoặc host:port:user:pass\n(để trống = tắt proxy)", "Proxy", JOptionPane.PLAIN_MESSAGE);
        if (s == null) return;
        forTargets("Đặt proxy", t -> core.proxy(t, s));
    }

    void snapshotTo() {
        Core.Tab t = selected(); if (t == null) return;
        JFileChooser fc = new JFileChooser(); fc.setSelectedFile(new File("che779-tab" + t.id + ".png"));
        if (fc.showSaveDialog(frame) != JFileChooser.APPROVE_OPTION) return;
        File out = fc.getSelectedFile();
        bg.execute(() -> msg(core.screenshot(t, out) ? "Đã chụp: " + out : "Chụp thất bại"));
    }

    // ---------- ho so du lieu ----------
    void saveProfile() {
        collect();
        JFileChooser fc = new JFileChooser(REMOTER_DIR); fc.setSelectedFile(new File(REMOTER_DIR, "hoso.ini"));
        if (fc.showSaveDialog(frame) != JFileChooser.APPROVE_OPTION) return;
        try (OutputStream o = new FileOutputStream(fc.getSelectedFile())) { cfg.store(new OutputStreamWriter(o, "UTF-8"), "Ho so Che779 Remoter"); msg("Đã lưu hồ sơ: " + fc.getSelectedFile()); }
        catch (Exception e) { msg("Lỗi lưu hồ sơ: " + e.getMessage()); }
    }

    void loadProfile() {
        REMOTER_DIR.mkdirs();
        JFileChooser fc = new JFileChooser(REMOTER_DIR);
        if (fc.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) return;
        try (InputStream in = new FileInputStream(fc.getSelectedFile())) { Properties p = new Properties(); p.load(new InputStreamReader(in, "UTF-8")); cfg.putAll(p); applyCfg(); restyle(); msg("Đã nạp hồ sơ: " + fc.getSelectedFile()); }
        catch (Exception e) { msg("Lỗi nạp hồ sơ: " + e.getMessage()); }
    }

    void openDataDir() {
        core.dataRoot.mkdirs();
        try { Desktop.getDesktop().open(core.dataRoot); }
        catch (Throwable e) {
            if (hasCmd("xdg-open")) { try { new ProcessBuilder("xdg-open", core.dataRoot.getPath()).start(); return; } catch (Exception ex) { /* roi xuong thong bao */ } }
            msg("Thư mục dữ liệu: " + core.dataRoot);
        }
    }

    void clearData() {
        for (Core.Tab t : core.tabs) if (t.alive()) { JOptionPane.showMessageDialog(frame, "Hãy dừng hết tab trước khi xóa dữ liệu."); return; }
        if (JOptionPane.showConfirmDialog(frame, "Xóa toàn bộ dữ liệu Emulator (bản ghi game, cấu hình) trong:\n" + core.dataRoot + "\nKhông thể hoàn tác.", "Xóa dữ liệu", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) return;
        deleteTree(core.dataRoot); msg("Đã xóa dữ liệu Emulator");
    }

    static void deleteTree(File f) {
        File[] k = f.listFiles();
        if (k != null) for (File c : k) deleteTree(c);
        f.delete();
    }

    // ---------- khung xem truoc + dong bo chuot ----------
    final class Preview extends JPanel {
        Rectangle area = new Rectangle();
        Preview() {
            setBackground(Color.BLACK); setPreferredSize(new Dimension(360, 480)); setFocusable(true);
            MouseAdapter m = new MouseAdapter() {
                public void mousePressed(MouseEvent e) { requestFocusInWindow(); send("down", e); }
                public void mouseReleased(MouseEvent e) { send("up", e); }
                public void mouseDragged(MouseEvent e) { send("move", e); }
            };
            addMouseListener(m); addMouseMotionListener(m);
        }
        void send(String type, MouseEvent e) {
            if (shot == null || area.width <= 0 || area.height <= 0) return;
            double fx = (e.getX() - area.x) / (double) area.width, fy = (e.getY() - area.y) / (double) area.height;
            if (fx < 0 || fx > 1 || fy < 0 || fy > 1) { if (!"up".equals(type)) return; fx = Math.max(0, Math.min(1, fx)); fy = Math.max(0, Math.min(1, fy)); }
            final double x = fx, y = fy;
            List<Core.Tab> ts = core.targets(selected(), cbSyncMouse.isSelected());
            io.execute(() -> { for (Core.Tab t : ts) core.mouse(t, type, x, y); });
        }
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            BufferedImage im = shot;
            if (im == null) { g.setColor(Color.GRAY); g.drawString("Chọn một tab đang chạy để xem trước", 20, 30); return; }
            double s = Math.min(getWidth() / (double) im.getWidth(), getHeight() / (double) im.getHeight());
            int w = (int) (im.getWidth() * s), h = (int) (im.getHeight() * s);
            area = new Rectangle((getWidth() - w) / 2, (getHeight() - h) / 2, w, h);
            g.drawImage(im, area.x, area.y, w, h, null);
        }
    }

    static String mapKey(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP: return "UP"; case KeyEvent.VK_DOWN: return "DOWN";
            case KeyEvent.VK_LEFT: return "LEFT"; case KeyEvent.VK_RIGHT: return "RIGHT";
            case KeyEvent.VK_ENTER: return "FIRE"; case KeyEvent.VK_F1: return "SOFT1"; case KeyEvent.VK_F2: return "SOFT2";
            case KeyEvent.VK_BACK_SPACE: return "BACKSPACE"; case KeyEvent.VK_SPACE: return "SPACE";
            default:
        }
        char c = e.getKeyChar();
        if (c >= '0' && c <= '9') return String.valueOf(c);
        if (c >= 'a' && c <= 'z') return String.valueOf((char) (c - 32));
        if (c >= 'A' && c <= 'Z') return String.valueOf(c);
        if (c == '*') return "STAR"; if (c == '#') return "POUND";
        return null;
    }

    void sendKey(String k) {
        List<Core.Tab> ts = targets();
        io.execute(() -> { for (Core.Tab t : ts) core.key(t, k); });
    }

    JPanel keypad() {
        JPanel p = new JPanel(new GridLayout(0, 3, 4, 4)); p.setOpaque(false);
        String[][] keys = { { "F1", "SOFT1" }, { "▲", "UP" }, { "F2", "SOFT2" }, { "◀", "LEFT" }, { "OK", "FIRE" }, { "▶", "RIGHT" },
                { "1", "1" }, { "▼", "DOWN" }, { "2", "2" }, { "3", "3" }, { "4", "4" }, { "5", "5" }, { "6", "6" }, { "7", "7" }, { "8", "8" },
                { "9", "9" }, { "*", "STAR" }, { "0", "0" }, { "#", "POUND" } };
        for (String[] k : keys) { RBtn b = new RBtn(k[0]); b.addActionListener(e -> sendKey(k[1])); p.add(b); }
        return p;
    }

    void openPreview() {
        if (previewDlg == null) {
            previewDlg = new JDialog(frame, "Xem trước & bàn phím đầy đủ", false);
            JPanel root = new JPanel(new BorderLayout(6, 6)); root.setBorder(new EmptyBorder(8, 8, 8, 8)); root.setBackground(th.bg);
            root.add(preview, BorderLayout.CENTER);
            JPanel kp = keypad(); kp.setPreferredSize(new Dimension(360, 130)); root.add(kp, BorderLayout.SOUTH);
            previewDlg.setContentPane(root); previewDlg.pack(); previewDlg.setLocationRelativeTo(frame);
            restyle();
        }
        previewDlg.setVisible(true);
    }

    // ---------- thanh phan giao dien ----------
    final class RBtn extends JButton {
        boolean accent, danger;
        RBtn(String text) { super(text); setFocusable(false); setContentAreaFilled(false); setBorderPainted(false); setOpaque(false); setMargin(new Insets(6, 10, 6, 10)); setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); }
        RBtn accent() { accent = true; return this; }
        RBtn danger() { danger = true; return this; }
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            boolean hot = getModel().isRollover(), down = getModel().isPressed();
            Color bgc = accent ? th.accent : th.field;
            if (hot) bgc = accent ? bgc.brighter() : th.gridAlt;
            if (down) bgc = accent ? bgc.darker() : th.border;
            g.setColor(bgc); g.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 8, 8));
            if (!accent) { g.setColor(th.border); g.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 8, 8)); }
            g.setColor(accent ? th.accentText : (danger ? th.danger : th.text));
            g.setFont(getFont());
            FontMetrics fm = g.getFontMetrics();
            g.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            g.dispose();
        }
        public Dimension getPreferredSize() { FontMetrics fm = getFontMetrics(getFont()); return new Dimension(fm.stringWidth(getText()) + 24, fm.getHeight() + 16); }
    }

    final class Card extends JPanel {
        Card(int n, String title, JComponent body) {
            super(new BorderLayout(0, 8)); setOpaque(false); setBorder(new EmptyBorder(12, 14, 12, 14));
            JPanel head = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0)); head.setOpaque(false);
            JLabel badge = new JLabel(String.valueOf(n), SwingConstants.CENTER) {
                protected void paintComponent(Graphics g0) {
                    Graphics2D g = (Graphics2D) g0.create(); g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g.setColor(th.accent); g.fillOval(0, 0, getWidth() - 1, getHeight() - 1); g.setColor(th.accentText); g.setFont(getFont());
                    FontMetrics fm = g.getFontMetrics(); g.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2, (getHeight() - fm.getHeight()) / 2 + fm.getAscent()); g.dispose();
                }
            };
            badge.setPreferredSize(new Dimension(24, 24)); badge.setFont(badge.getFont().deriveFont(Font.BOLD, 12f));
            JLabel tl = new JLabel(title); tl.setFont(tl.getFont().deriveFont(Font.BOLD, 14f)); tl.putClientProperty("role", "title");
            head.add(badge); head.add(tl);
            add(head, BorderLayout.NORTH); add(body, BorderLayout.CENTER);
            cards.add(this);
        }
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create(); g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(th.card); g.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 12, 12));
            g.setColor(th.border); g.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 12, 12)); g.dispose();
        }
    }

    JLabel lbl(String s) { JLabel l = new JLabel(s); l.putClientProperty("role", "label"); return l; }
    JLabel muted(String s) { JLabel l = new JLabel(s); l.putClientProperty("role", "muted"); return l; }
    JCheckBox chk(String s, boolean v) { JCheckBox c = new JCheckBox(s, v); c.setOpaque(false); c.setFocusable(false); return c; }
    JSpinner spin(int v, int min, int max, int step) { JSpinner s = new JSpinner(new SpinnerNumberModel(v, min, max, step)); ((JSpinner.DefaultEditor) s.getEditor()).getTextField().setColumns(4); return s; }
    RBtn btn(String s, ActionListener a) { RBtn b = new RBtn(s); b.addActionListener(a); return b; }

    JPanel pathRow(String label, JTextField tf, ActionListener browse) {
        JPanel p = new JPanel(new BorderLayout(8, 0)); p.setOpaque(false);
        JLabel l = lbl(label); l.setPreferredSize(new Dimension(70, 24));
        p.add(l, BorderLayout.WEST); p.add(tf, BorderLayout.CENTER); p.add(btn("Duyệt", browse), BorderLayout.EAST);
        return p;
    }

    void refreshGrid() {
        if (tabGrid == null) return;
        tabGrid.removeAll();
        int n = Math.max(1, Math.min(24, Math.max(val(spTabs), core.tabs.size())));
        for (int i = 0; i < n; i++) {
            final int idx = i;
            boolean alive = i < core.tabs.size() && core.tabs.get(i).alive();
            RBtn b = btn(String.valueOf(i + 1), e -> { if (idx < core.tabs.size()) { table.setRowSelectionInterval(idx, idx); Core.Tab t = core.tabs.get(idx); bg.execute(() -> core.show(t, true)); } });
            if (alive) b.accent();
            tabGrid.add(b);
        }
        tabGrid.revalidate(); tabGrid.repaint();
    }

    // ---------- giao dien ----------
    void build() {
        frame = new JFrame("Che779 Remoter v" + VERSION);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() { public void windowClosing(WindowEvent e) { quit(); } });
        tfEmu = new JTextField(core.emuJar.getPath()); tfGame = new JTextField(); tfJava = new JTextField(defaultJava());
        spRam = spin(512, 64, 4096, 64); spTabs = spin(12, 1, 60, 1); spWait = spin(2000, 0, 60000, 100); spW = spin(226, 100, 2000, 2); spH = spin(334, 100, 2000, 2);
        spCols = spin(6, 1, 30, 1); spRows = spin(2, 1, 30, 1); spCleanMin = spin(15, 1, 600, 1); spGuardPct = spin(90, 50, 99, 1); spTimerMin = spin(60, 1, 1440, 5);
        cbAuto = chk("Tự sắp xếp khi mở tab", true); cbSep = chk("Tách dữ liệu từng tab", false);
        cbSyncTab = chk("Đồng bộ Tab", true); cbSyncKey = chk("Đồng bộ Phím", true); cbSyncMouse = chk("Đồng bộ Chuột (nhấp / kéo)", false); cbDark = chk("Giao diện tối", true);
        cbRestart = chk("Tự mở lại tab khi bị tắt/crash", false); cbAutoStart = chk("Tự bắt đầu khi mở Remoter", false);
        cbCleanAuto = chk("Tự Clean RAM mỗi", false); cbGuard = chk("Tự cho ngủ khi RAM ≥", false); cbTimer = chk("Hẹn giờ đóng hết sau", false); cbTop = chk("Remoter luôn nằm trên cùng", false);

        // 1: duong dan
        JPanel p1 = new JPanel(new GridLayout(3, 1, 0, 6)); p1.setOpaque(false);
        p1.add(pathRow("Emulator", tfEmu, e -> browse(tfEmu, "Chọn Che779_Emulator.jar", true)));
        p1.add(pathRow("Game", tfGame, e -> browse(tfGame, "Chọn game J2ME (.jar)", true)));
        p1.add(pathRow("Java", tfJava, e -> browse(tfJava, "Chọn java", false)));

        // 2: cau hinh tab
        JPanel cfgL = new JPanel(new GridBagLayout()); cfgL.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints(); c.insets = new Insets(3, 2, 3, 5); c.anchor = GridBagConstraints.WEST;
        c.gridy = 0; c.gridx = 0; cfgL.add(lbl("RAM tối đa (MB)"), c); c.gridx = 1; cfgL.add(spRam, c);
        c.gridx = 2; cfgL.add(lbl("Số tab"), c); c.gridx = 3; cfgL.add(spTabs, c);
        c.gridx = 4; cfgL.add(lbl("Chờ (ms)"), c); c.gridx = 5; cfgL.add(spWait, c);
        c.gridx = 6; cfgL.add(lbl("Ô tab"), c); c.gridx = 7; cfgL.add(spW, c); c.gridx = 8; cfgL.add(lbl("×"), c); c.gridx = 9; cfgL.add(spH, c);
        c.gridy = 1; c.gridx = 0; cfgL.add(lbl("Lưới: cột"), c); c.gridx = 1; cfgL.add(spCols, c); c.gridx = 2; cfgL.add(lbl("hàng"), c); c.gridx = 3; cfgL.add(spRows, c);
        c.gridx = 4; c.gridwidth = 2; cfgL.add(btn("Tự tính", e -> autoGrid()), c); c.gridwidth = 1;
        c.gridy = 2; c.gridx = 0; c.gridwidth = 3; cfgL.add(cbAuto, c); c.gridx = 3; c.gridwidth = 4; cfgL.add(cbSep, c);
        tabGrid = new JPanel(new GridLayout(0, 6, 6, 6)); tabGrid.setOpaque(false);
        JPanel p2 = new JPanel(new BorderLayout(10, 0)); p2.setOpaque(false); p2.add(cfgL, BorderLayout.CENTER);
        JPanel gridWrap = new JPanel(new BorderLayout()); gridWrap.setOpaque(false); gridWrap.setPreferredSize(new Dimension(236, 80)); gridWrap.add(tabGrid, BorderLayout.NORTH); p2.add(gridWrap, BorderLayout.EAST);

        // 3: hanh dong
        JPanel p3 = new JPanel(new GridLayout(2, 6, 8, 8)); p3.setOpaque(false);
        p3.add(btn("▶  Bắt đầu", e -> onStart()).accent()); p3.add(btn("■  Dừng", e -> onStop()).danger());
        p3.add(btn("Ẩn", e -> forTargets("Ẩn", t -> core.show(t, false)))); p3.add(btn("Hiện", e -> forTargets("Hiện", t -> core.show(t, true))));
        p3.add(btn("Ngủ", e -> forTargets("Ngủ", t -> core.sleep(t, true)))); p3.add(btn("Thức", e -> forTargets("Thức", t -> core.sleep(t, false))));
        p3.add(btn("Clean RAM", e -> forTargets("Clean RAM", t -> core.cleanRam(t)))); p3.add(btn("Sắp xếp tab", e -> bg.execute(this::arrange)));
        p3.add(btn("Thêm game…", e -> onAddGames())); p3.add(btn("+  Thêm N tab", e -> onAddN()));
        p3.add(btn("Xóa dòng", e -> onRemoveRows())); p3.add(btn("✕  Đóng hết", e -> onCloseAll()).danger());

        // 4: danh sach tab
        model = new TabModel(); table = new JTable(model);
        table.setRowHeight(26); table.setShowGrid(false); table.setIntercellSpacing(new Dimension(0, 0)); table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        int[] wd = { 34, 40, 260, 100, 60, 56, 70, 50, 70 };
        for (int i = 0; i < wd.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(wd[i]);
        table.setDefaultRenderer(String.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int cc) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, false, r, cc);
                l.setBackground(sel ? th.accent : (r % 2 == 1 ? th.gridAlt : th.card)); l.setForeground(sel ? th.accentText : th.text); l.setBorder(new EmptyBorder(0, 6, 0, 6)); return l;
            }
        });
        JScrollPane sc = new JScrollPane(table); sc.getViewport().setBackground(th.card); sc.setBorder(BorderFactory.createLineBorder(th.border));
        JPanel p4 = new JPanel(new BorderLayout(0, 6)); p4.setOpaque(false); p4.add(sc, BorderLayout.CENTER);
        JPanel st = new JPanel(new GridLayout(2, 1)); st.setOpaque(false); st.add(sysInfo); st.add(runInfo); p4.add(st, BorderLayout.SOUTH);

        // 5: dong bo & ban phim
        JPanel p5 = new JPanel(); p5.setOpaque(false); p5.setLayout(new BoxLayout(p5, BoxLayout.Y_AXIS));
        for (JCheckBox cb : new JCheckBox[] { cbSyncTab, cbSyncKey, cbSyncMouse, cbDark }) { cb.setAlignmentX(0); p5.add(cb); }
        p5.add(Box.createVerticalStrut(8));
        RBtn kb = btn("⌨  Mở bàn phím đầy đủ / xem trước", e -> openPreview()).accent(); kb.setAlignmentX(0); p5.add(kb);
        p5.add(Box.createVerticalStrut(6));
        JLabel hint = muted("<html>Phím thật khi bật Đồng bộ Phím: mũi tên, Enter=OK, F1/F2 = phím mềm, 0-9, A-Z, Space, Backspace.</html>"); hint.setAlignmentX(0); hint.setPreferredSize(new Dimension(300, 48)); p5.add(hint);

        // 6: tien ich
        JPanel p6 = new WidthPanel(); p6.setOpaque(false); p6.setLayout(new BoxLayout(p6, BoxLayout.Y_AXIS));
        JLabel h1 = muted("HỒ SƠ DỮ LIỆU"); h1.setAlignmentX(0); p6.add(h1);
        JPanel pf = new JPanel(new GridLayout(2, 2, 6, 6)); pf.setOpaque(false); pf.setAlignmentX(0);
        pf.add(btn("Lưu hồ sơ", e -> saveProfile())); pf.add(btn("Nạp hồ sơ", e -> loadProfile())); pf.add(btn("Thư mục dữ liệu", e -> openDataDir())); pf.add(btn("Xóa dữ liệu", e -> clearData()).danger());
        p6.add(pf); p6.add(Box.createVerticalStrut(8));
        JLabel h2 = muted("TỰ ĐỘNG"); h2.setAlignmentX(0); p6.add(h2);
        cbRestart.setAlignmentX(0); cbAutoStart.setAlignmentX(0); cbTop.setAlignmentX(0);
        p6.add(cbRestart); p6.add(cbAutoStart);
        p6.add(row(cbCleanAuto, spCleanMin, "phút")); p6.add(row(cbGuard, spGuardPct, "%")); p6.add(row(cbTimer, spTimerMin, "phút"));
        p6.add(cbTop); p6.add(Box.createVerticalStrut(8));
        JPanel extra = new JPanel(new GridLayout(1, 3, 6, 6)); extra.setOpaque(false); extra.setAlignmentX(0);
        extra.add(btn("Proxy…", e -> proxyDialog())); extra.add(btn("Chụp…", e -> snapshotTo()));
        extra.add(btn("Mở lại", e -> { Core.Tab t = selected(); if (t != null) bg.execute(() -> { try { syncCore(); core.restart(t); core.waitReady(t, 20000); msg("Đã mở lại tab " + t.id); } catch (Exception ex) { msg("Lỗi: " + ex.getMessage()); } SwingUtilities.invokeLater(this::refresh); }); }));
        p6.add(extra);
        JScrollPane s6 = new JScrollPane(p6); s6.setBorder(null); s6.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER); s6.setOpaque(false); s6.getViewport().setOpaque(false); s6.getVerticalScrollBar().setUnitIncrement(14);

        JPanel left = new JPanel(new GridBagLayout()); left.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints(); g.fill = GridBagConstraints.BOTH; g.weightx = 1; g.gridx = 0; g.insets = new Insets(0, 0, 10, 0);
        g.gridy = 0; g.weighty = 0; left.add(new Card(1, "Đường dẫn: Emulator · Game · Java", p1), g);
        g.gridy = 1; left.add(new Card(2, "Cấu hình tab & lưới sắp xếp", p2), g);
        g.gridy = 2; left.add(new Card(3, "Hành động", p3), g);
        g.gridy = 3; g.weighty = 1; g.insets = new Insets(0, 0, 0, 0); left.add(new Card(4, "Danh sách tab & trạng thái hệ thống", p4), g);
        JPanel right = new JPanel(new GridBagLayout()); right.setOpaque(false); right.setPreferredSize(new Dimension(370, 100));
        GridBagConstraints r = new GridBagConstraints(); r.fill = GridBagConstraints.BOTH; r.weightx = 1; r.gridx = 0;
        r.gridy = 0; r.weighty = 0; r.insets = new Insets(0, 0, 10, 0); right.add(new Card(5, "Đồng bộ & bàn phím", p5), r);
        r.gridy = 1; r.weighty = 1; r.insets = new Insets(0, 0, 0, 0); right.add(new Card(6, "Tiện ích", s6), r);

        JPanel root = new JPanel(new GridBagLayout()); root.setBorder(new EmptyBorder(12, 14, 4, 14)); root.setName("root");
        GridBagConstraints m = new GridBagConstraints(); m.fill = GridBagConstraints.BOTH; m.gridy = 0; m.weighty = 1;
        m.gridx = 0; m.weightx = 1; m.insets = new Insets(0, 0, 0, 12); root.add(left, m);
        m.gridx = 1; m.weightx = 0; m.insets = new Insets(0, 0, 0, 0); root.add(right, m);
        status.setBorder(new EmptyBorder(3, 16, 6, 16));
        frame.add(root, BorderLayout.CENTER); frame.add(status, BorderLayout.SOUTH);

        applyCfg();
        spTabs.addChangeListener(e -> refreshGrid());
        cbDark.addActionListener(e -> { th = cbDark.isSelected() ? Theme.dark() : Theme.light(); restyle(); });
        cbTop.addActionListener(e -> frame.setAlwaysOnTop(cbTop.isSelected()));
        cbSep.addActionListener(e -> core.separateData = cbSep.isSelected());
        th = cbDark.isSelected() ? Theme.dark() : Theme.light();
        refreshGrid(); restyle();
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (e.getID() != KeyEvent.KEY_PRESSED || !cbSyncKey.isSelected()) return false;
            Component f = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
            if (f instanceof javax.swing.text.JTextComponent || f instanceof JTable) return false;
            Window w = f == null ? null : SwingUtilities.getWindowAncestor(f);
            if (w != frame && w != previewDlg) return false;
            String k = mapKey(e); if (k != null) { sendKey(k); return true; }
            return false;
        });
        frame.setSize(1300, 740); frame.setMinimumSize(new Dimension(1180, 680)); frame.setLocationByPlatform(true); frame.setVisible(true);
        msg("Emulator: " + core.emuJar + (core.emuJar.isFile() ? "" : "  (CHƯA TÌM THẤY - hãy chọn ở mục 1)"));
        new javax.swing.Timer(1500, e -> tick()).start();
        if (cbAutoStart.isSelected() && gameFile() != null && gameFile().isFile()) SwingUtilities.invokeLater(this::onStart);
    }

    /** Panel cuon doc, luon co chieu rong bang khung nhin (khong cuon ngang). */
    static final class WidthPanel extends JPanel implements Scrollable {
        public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 14; }
        public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return 60; }
        public boolean getScrollableTracksViewportWidth() { return true; }
        public boolean getScrollableTracksViewportHeight() { return false; }
    }

    JPanel row(JCheckBox cb, JSpinner sp, String unit) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2)); p.setOpaque(false); p.setAlignmentX(0);
        p.add(cb); p.add(sp); p.add(muted(unit)); return p;
    }

    /** Ap mau theo chu de len toan bo cay thanh phan. */
    void restyle() {
        frame.getContentPane().setBackground(th.bg);
        style(frame.getContentPane());
        if (previewDlg != null) { previewDlg.getContentPane().setBackground(th.bg); style(previewDlg.getContentPane()); }
        frame.repaint();
    }

    void style(Component c) {
        if (c instanceof JPanel && "root".equals(c.getName())) { ((JPanel) c).setOpaque(true); c.setBackground(th.bg); }
        if (c instanceof JLabel) {
            JLabel l = (JLabel) c; Object role = l.getClientProperty("role");
            l.setForeground("muted".equals(role) ? th.muted : th.text);
        } else if (c instanceof JCheckBox) { c.setForeground(th.text); ((JCheckBox) c).setOpaque(false); }
        else if (c instanceof javax.swing.text.JTextComponent) {
            JTextField f = (JTextField) c; f.setBackground(th.field); f.setForeground(th.text); f.setCaretColor(th.text);
            f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(th.border), new EmptyBorder(4, 6, 4, 6)));
        } else if (c instanceof JSpinner) { c.setBackground(th.field); ((JSpinner) c).setBorder(BorderFactory.createLineBorder(th.border)); }
        else if (c instanceof JTable) {
            JTable t = (JTable) c; t.setBackground(th.card); t.setForeground(th.text);
            JTableHeader h = t.getTableHeader(); h.setBackground(th.card); h.setForeground(th.muted); h.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, th.border));
            h.setDefaultRenderer(new DefaultTableCellRenderer() {
                public Component getTableCellRendererComponent(JTable tb, Object v, boolean s, boolean f, int r, int cc) {
                    JLabel l = (JLabel) super.getTableCellRendererComponent(tb, v, false, false, r, cc);
                    l.setBackground(th.card); l.setForeground(th.muted); l.setBorder(new EmptyBorder(4, 6, 4, 6)); return l;
                }
            });
        } else if (c instanceof JScrollPane) { ((JScrollPane) c).getViewport().setBackground(th.card); }
        if (c instanceof Container) for (Component k : ((Container) c).getComponents()) style(k);
        if (c instanceof JSpinner) { for (Component k : ((JSpinner) c).getComponents()) if (k instanceof JSpinner.DefaultEditor) { JTextField f = ((JSpinner.DefaultEditor) k).getTextField(); f.setBackground(th.field); f.setForeground(th.text); f.setCaretColor(th.text); f.setBorder(new EmptyBorder(2, 4, 2, 4)); } }
        if (c == status || c == sysInfo || c == runInfo) c.setForeground(th.muted);
        if (c == table) c.repaint();
    }

    // ---------- vong lap 1,5 giay ----------
    void tick() {
        List<Core.Tab> all = new ArrayList<Core.Tab>(core.tabs);
        bg.execute(() -> {
            for (Core.Tab t : all) core.poll(t);
            // tu mo lai tab bi tat/crash (toi da 5 lan moi tab)
            if (cbRestart.isSelected()) for (Core.Tab t : all)
                if (t.proc != null && !t.alive() && !t.stopRequested && t.restarts < 5) {
                    t.restarts++;
                    try { core.start(t); t.stopRequested = false; msg("Tự mở lại tab " + t.id + " (lần " + t.restarts + ")"); } catch (Exception ex) { msg("Không mở lại được tab " + t.id + ": " + ex.getMessage()); }
                }
            long now = System.currentTimeMillis();
            if (cbCleanAuto.isSelected() && now - lastClean >= val(spCleanMin) * 60000L) {
                lastClean = now; for (Core.Tab t : all) if (t.alive()) core.cleanRam(t); msg("Tự Clean RAM");
            }
            long[] mem = Core.sysMem();
            if (cbGuard.isSelected() && mem != null) {
                int pct = (int) (mem[0] * 100 / mem[1]);
                if (pct >= val(spGuardPct) && !guardSlept) { guardSlept = true; for (Core.Tab t : all) if (t.alive()) core.sleep(t, true); msg("RAM " + pct + "% — đã cho các tab ngủ"); }
                else if (pct < val(spGuardPct) - 5) guardSlept = false;
            }
            if (cbTimer.isSelected() && timerDeadline > 0 && now >= timerDeadline) { timerDeadline = 0; core.closeAll(); msg("Hết giờ hẹn — đã đóng hết"); }
            double cpu = core.sysCpu(); int run = 0; double tcpu = 0; long trss = 0;
            for (Core.Tab t : all) if (t.alive()) { run++; if (t.cpu >= 0) tcpu += t.cpu; if (t.rssMb >= 0) trss += t.rssMb; }
            final String s1 = "Hệ thống: CPU " + (cpu >= 0 ? String.format("%.1f%%", cpu) : "-") + " | RAM " + (mem != null ? mem[0] + " MB / " + mem[1] + " MB (" + (mem[0] * 100 / mem[1]) + "%)" : "-");
            final String s2 = "Đang chạy " + run + " / " + core.tabs.size() + " tab: CPU " + String.format("%.1f%%", tcpu) + " | RAM " + trss + " MB";
            SwingUtilities.invokeLater(() -> { sysInfo.setText(s1); runInfo.setText(s2); refresh(); });
        });
        Core.Tab t = selected();
        if (previewDlg == null || !previewDlg.isVisible()) return;
        if (t == null || !t.alive() || t.hidden || shotBusy) { if (t == null || !t.alive()) { shot = null; preview.repaint(); } return; }
        shotBusy = true;
        bg.execute(() -> {
            try {
                File f = File.createTempFile("che779-prev", ".png"); f.deleteOnExit();
                if (core.screenshot(t, f)) { BufferedImage im = ImageIO.read(f); if (im != null) shot = im; }
                f.delete();
            } catch (Exception ex) { /* bo qua khung nay */ }
            finally { shotBusy = false; SwingUtilities.invokeLater(preview::repaint); }
        });
    }

    void quit() {
        boolean any = false; for (Core.Tab t : core.tabs) if (t.alive()) any = true;
        if (any && JOptionPane.showConfirmDialog(frame, "Đóng hết các tab game và thoát?", "Thoát", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        saveCfg(); core.closeAll(); System.exit(0);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); } catch (Exception e) { /* mac dinh */ }
            Main m = new Main(); m.build();
            for (String a : args) if (a.toLowerCase().endsWith(".jar")) { File g = new File(a); m.tfGame.setText(g.getPath()); m.bg.execute(() -> { try { m.core.open(g); SwingUtilities.invokeLater(m::refresh); } catch (Exception e) { m.msg("Lỗi: " + e.getMessage()); } }); }
        });
    }
}
