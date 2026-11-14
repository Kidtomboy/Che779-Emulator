package com.che779.remoter;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;

/**
 * Che779 Remoter (Java/Swing) - chay tren Linux, Windows, macOS.
 * Dieu khien nhieu Emulator V1 (moi game mot tien trinh Java) qua IPC TCP 127.0.0.1.
 * Dong bo chuot: thao tac tren khung xem truoc trong Remoter duoc nhan ra cac tab da tick.
 */
public final class Main {
    static final String VERSION = "1.2.0";
    static final File CFG_DIR = new File(System.getProperty("user.home"), ".che779");
    static final File CFG = new File(CFG_DIR, "remoter.properties");

    final Properties cfg = new Properties();
    final Core core;
    final ExecutorService io = Executors.newSingleThreadExecutor(r -> { Thread t = new Thread(r, "che779-io"); t.setDaemon(true); return t; });
    final ExecutorService bg = Executors.newCachedThreadPool(r -> { Thread t = new Thread(r, "che779-bg"); t.setDaemon(true); return t; });

    JFrame frame;
    JTable table;
    TabModel model;
    JCheckBox sync;
    JLabel status = new JLabel(" ");
    Preview preview = new Preview();
    volatile BufferedImage shot;
    volatile boolean shotBusy;

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
        try (FileInputStream in = new FileInputStream(CFG)) { cfg.load(in); } catch (Exception e) { /* lan dau */ }
        File jar = new File(cfg.getProperty("emulatorJar", findEmuJar().getPath()));
        int port = parseInt(cfg.getProperty("basePort"), 7790);
        core = new Core(jar, port);
        core.ramMb = parseInt(cfg.getProperty("ramMb"), 512);
    }

    static int parseInt(String s, int d) { try { return Integer.parseInt(s.trim()); } catch (Exception e) { return d; } }

    void saveCfg() {
        cfg.setProperty("emulatorJar", core.emuJar.getPath());
        cfg.setProperty("ramMb", String.valueOf(core.ramMb));
        cfg.setProperty("basePort", String.valueOf(core.basePort));
        try { CFG_DIR.mkdirs(); try (FileOutputStream o = new FileOutputStream(CFG)) { cfg.store(o, "Che779 Remoter"); } } catch (Exception e) { /* bo qua */ }
    }

    // ---------- bang tab ----------
    final class TabModel extends AbstractTableModel {
        final String[] cols = { "✔", "#", "Game", "Trạng thái", "Proxy", "Cổng" };
        public int getRowCount() { return core.tabs.size(); }
        public int getColumnCount() { return cols.length; }
        public String getColumnName(int c) { return cols[c]; }
        public Class<?> getColumnClass(int c) { return c == 0 ? Boolean.class : String.class; }
        public boolean isCellEditable(int r, int c) { return c == 0; }
        public Object getValueAt(int r, int c) {
            Core.Tab t = core.tabs.get(r);
            switch (c) {
                case 0: return t.checked;
                case 1: return String.valueOf(t.id);
                case 2: return t.name();
                case 3: return t.state;
                case 4: return t.proxy;
                default: return String.valueOf(t.port);
            }
        }
        public void setValueAt(Object v, int r, int c) { if (c == 0) core.tabs.get(r).checked = Boolean.TRUE.equals(v); }
    }

    Core.Tab selected() {
        int r = table.getSelectedRow();
        if (r < 0 || r >= core.tabs.size()) return core.tabs.isEmpty() ? null : core.tabs.get(0);
        return core.tabs.get(r);
    }

    /** Cac tab nhan lenh: dong bo bat thi moi tab da tick (+ tab dang chon), nguoc lai chi tab dang chon. */
    List<Core.Tab> targets() { return core.targets(selected(), sync.isSelected()); }

    void msg(String s) { SwingUtilities.invokeLater(() -> status.setText(s)); }

    void refresh() { model.fireTableDataChanged(); }

    // ---------- hanh dong ----------
    void openGames() {
        if (!core.emuJar.isFile()) { settings(); if (!core.emuJar.isFile()) return; }
        JFileChooser fc = new JFileChooser(cfg.getProperty("lastDir", System.getProperty("user.home")));
        fc.setMultiSelectionEnabled(true);
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Game J2ME (*.jar)", "jar"));
        if (fc.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) return;
        cfg.setProperty("lastDir", fc.getCurrentDirectory().getPath()); saveCfg();
        for (File g : fc.getSelectedFiles()) {
            bg.execute(() -> {
                try {
                    Core.Tab t = core.open(g);
                    SwingUtilities.invokeLater(this::refresh);
                    msg(core.waitReady(t, 20000) ? "Đã mở " + g.getName() : "Tab " + t.id + " chưa phản hồi IPC (xem log trong thư mục tạm: che779-tab" + t.id + ".log)");
                } catch (Exception e) { msg("Lỗi mở game: " + e.getMessage()); }
            });
        }
    }

    interface TabAction { void run(Core.Tab t); }

    void forTargets(String label, TabAction a) {
        List<Core.Tab> ts = targets();
        if (ts.isEmpty()) { msg("Chưa có tab nào."); return; }
        bg.execute(() -> { for (Core.Tab t : ts) a.run(t); msg(label + " (" + ts.size() + " tab)"); SwingUtilities.invokeLater(this::refresh); });
    }

    void closeSelected() {
        List<Core.Tab> ts = targets();
        bg.execute(() -> { for (Core.Tab t : ts) core.close(t, true); SwingUtilities.invokeLater(this::refresh); msg("Đã đóng " + ts.size() + " tab"); });
    }

    void proxyDialog() {
        String s = JOptionPane.showInputDialog(frame, "Proxy cho các tab đang nhận lệnh.\nĐịnh dạng host:port hoặc host:port:user:pass\n(để trống = tắt proxy)", "Proxy", JOptionPane.PLAIN_MESSAGE);
        if (s == null) return;
        forTargets("Đặt proxy", t -> core.proxy(t, s));
    }

    void settings() {
        JTextField jar = new JTextField(core.emuJar.getPath(), 28), ram = new JTextField(String.valueOf(core.ramMb), 6), port = new JTextField(String.valueOf(core.basePort), 6);
        JPanel p = new JPanel(new GridLayout(0, 2, 6, 6));
        p.add(new JLabel("Che779_Emulator.jar")); p.add(jar);
        p.add(new JLabel("RAM tối đa mỗi tab (MB)")); p.add(ram);
        p.add(new JLabel("Cổng IPC bắt đầu từ")); p.add(port);
        JButton b = new JButton("Chọn jar…");
        b.addActionListener(e -> { JFileChooser fc = new JFileChooser(); if (fc.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) jar.setText(fc.getSelectedFile().getPath()); });
        p.add(new JLabel()); p.add(b);
        if (JOptionPane.showConfirmDialog(frame, p, "Cài đặt Remoter", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        int r = Math.max(128, Math.min(4096, parseInt(ram.getText(), 512)));
        int bp = Math.max(1024, Math.min(65000, parseInt(port.getText(), 7790)));
        // Core giu emuJar/basePort bat bien -> tao lai Core khi doi: chi cho phep doi khi khong co tab dang chay
        if (!core.tabs.isEmpty() && (!new File(jar.getText()).equals(core.emuJar) || bp != core.basePort)) {
            JOptionPane.showMessageDialog(frame, "Hãy đóng hết tab trước khi đổi jar hoặc cổng."); return;
        }
        cfg.setProperty("emulatorJar", jar.getText()); cfg.setProperty("basePort", String.valueOf(bp)); cfg.setProperty("ramMb", String.valueOf(r));
        saveCfg();
        JOptionPane.showMessageDialog(frame, "Đã lưu. Khởi động lại Remoter để áp dụng jar/cổng mới; RAM áp dụng từ tab mở tiếp theo.");
        core.ramMb = r;
    }

    void snapshotTo() {
        Core.Tab t = selected(); if (t == null) return;
        JFileChooser fc = new JFileChooser(); fc.setSelectedFile(new File("che779-tab" + t.id + ".png"));
        if (fc.showSaveDialog(frame) != JFileChooser.APPROVE_OPTION) return;
        File out = fc.getSelectedFile();
        bg.execute(() -> msg(core.screenshot(t, out) ? "Đã chụp: " + out : "Chụp thất bại"));
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
            addKeyListener(new KeyAdapter() {
                public void keyPressed(KeyEvent e) { String k = mapKey(e); if (k != null) sendKey(k); }
            });
        }
        void send(String type, MouseEvent e) {
            if (shot == null || area.width <= 0 || area.height <= 0) return;
            double fx = (e.getX() - area.x) / (double) area.width, fy = (e.getY() - area.y) / (double) area.height;
            if (fx < 0 || fx > 1 || fy < 0 || fy > 1) { if (!"up".equals(type)) return; fx = Math.max(0, Math.min(1, fx)); fy = Math.max(0, Math.min(1, fy)); }
            final double x = fx, y = fy; List<Core.Tab> ts = targets();
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
        if (c == '*') return "STAR"; if (c == '#') return "POUND";
        return null;
    }

    void sendKey(String k) {
        List<Core.Tab> ts = targets();
        io.execute(() -> { for (Core.Tab t : ts) core.key(t, k); });
    }

    JPanel keypad() {
        JPanel p = new JPanel(new GridLayout(0, 3, 4, 4));
        String[][] keys = { { "F1", "SOFT1" }, { "▲", "UP" }, { "F2", "SOFT2" }, { "◀", "LEFT" }, { "OK", "FIRE" }, { "▶", "RIGHT" },
                { "1", "1" }, { "▼", "DOWN" }, { "2", "2" }, { "3", "3" }, { "4", "4" }, { "5", "5" }, { "6", "6" }, { "7", "7" }, { "8", "8" },
                { "9", "9" }, { "*", "STAR" }, { "0", "0" }, { "#", "POUND" } };
        for (String[] k : keys) { JButton b = new JButton(k[0]); b.setFocusable(false); b.setMargin(new Insets(2, 2, 2, 2)); b.addActionListener(e -> sendKey(k[1])); p.add(b); }
        return p;
    }

    // ---------- giao dien ----------
    void build() {
        frame = new JFrame("Che779 Remoter " + VERSION);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() { public void windowClosing(WindowEvent e) { quit(); } });
        model = new TabModel(); table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setMaxWidth(36); table.getColumnModel().getColumn(1).setMaxWidth(40);

        JToolBar tb = new JToolBar(); tb.setFloatable(false);
        add(tb, "Mở game…", e -> openGames());
        add(tb, "Mở lại", e -> { Core.Tab t = selected(); if (t != null) bg.execute(() -> { try { core.restart(t); core.waitReady(t, 20000); msg("Đã mở lại tab " + t.id); } catch (Exception ex) { msg("Lỗi: " + ex.getMessage()); } SwingUtilities.invokeLater(this::refresh); }); });
        add(tb, "Đóng", e -> closeSelected());
        add(tb, "Đóng hết", e -> bg.execute(() -> { core.closeAll(); SwingUtilities.invokeLater(this::refresh); msg("Đã đóng hết"); }));
        tb.addSeparator();
        add(tb, "Hiện", e -> forTargets("Hiện", t -> core.show(t, true)));
        add(tb, "Ẩn", e -> forTargets("Ẩn", t -> core.show(t, false)));
        add(tb, "Ngủ", e -> forTargets("Ngủ", t -> core.sleep(t, true)));
        add(tb, "Thức", e -> forTargets("Thức", t -> core.sleep(t, false)));
        add(tb, "Dọn RAM", e -> forTargets("Dọn RAM", t -> core.cleanRam(t)));
        add(tb, "Proxy…", e -> proxyDialog());
        add(tb, "Chụp…", e -> snapshotTo());
        tb.addSeparator();
        add(tb, "Cài đặt", e -> settings());
        sync = new JCheckBox("Đồng bộ các tab đã tick", true); tb.add(sync);

        JPanel right = new JPanel(new BorderLayout(4, 4));
        right.add(preview, BorderLayout.CENTER);
        JPanel kp = keypad(); kp.setPreferredSize(new Dimension(360, 130)); right.add(kp, BorderLayout.SOUTH);

        JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(table), right);
        sp.setResizeWeight(0.55);
        frame.add(tb, BorderLayout.NORTH); frame.add(sp, BorderLayout.CENTER);
        status.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8)); frame.add(status, BorderLayout.SOUTH);
        frame.setSize(980, 620); frame.setLocationByPlatform(true); frame.setVisible(true);
        msg("Emulator: " + core.emuJar + (core.emuJar.isFile() ? "" : "  (CHƯA TÌM THẤY - bấm Cài đặt)"));

        new javax.swing.Timer(1500, e -> tick()).start();
    }

    void add(JToolBar tb, String text, ActionListener a) { JButton b = new JButton(text); b.setFocusable(false); b.addActionListener(a); tb.add(b); }

    void tick() {
        List<Core.Tab> all = new java.util.ArrayList<>(core.tabs);
        bg.execute(() -> { for (Core.Tab t : all) core.poll(t); SwingUtilities.invokeLater(() -> { int r = table.getSelectedRow(); refresh(); if (r >= 0 && r < model.getRowCount()) table.setRowSelectionInterval(r, r); }); });
        Core.Tab t = selected();
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
        if (!core.tabs.isEmpty() && JOptionPane.showConfirmDialog(frame, "Đóng hết các tab game và thoát?", "Thoát", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        saveCfg(); core.closeAll(); System.exit(0);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) { /* mac dinh */ }
            Main m = new Main(); m.build();
            for (String a : args) if (a.toLowerCase().endsWith(".jar")) { File g = new File(a); m.bg.execute(() -> { try { m.core.open(g); SwingUtilities.invokeLater(m::refresh); } catch (Exception e) { m.msg("Lỗi: " + e.getMessage()); } }); }
        });
    }
}
