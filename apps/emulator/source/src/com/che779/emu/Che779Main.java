package com.che779.emu;

import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.lang.management.ManagementFactory;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.SwingUtilities;

/**
 * Che779 Emulator - diem vao (launcher): mo may chu IPC cho Remoter, tu dong Clean RAM (tuy chon),
 * roi chay emulator (org.microemu.app.Main).
 */
public final class Che779Main {
    private static final String PROP_PORT = "che779.port";
    private static final String PROP_AUTOCLEAN = "che779.autoclean";
    static final ScheduledExecutorService SCHED = Executors.newScheduledThreadPool(2, new java.util.concurrent.ThreadFactory() {
        public Thread newThread(Runnable r) { Thread t = new Thread(r, "che779-sched"); t.setDaemon(true); return t; }
    });
    static volatile int port = -1;

    private Che779Main() {}

    public static void main(String[] args) throws Exception {
        int base = Integer.getInteger(PROP_PORT, 7790);
        try {
            port = new IpcServer(base).start();
            System.out.println("CHE779_VERSION=" + org.microemu.app.Che779.VERSION);
            System.out.println("CHE779_PORT=" + port);
            System.out.println("CHE779_DATA=" + org.microemu.app.Che779.dataDir());
            System.out.flush();
        } catch (Throwable t) {
            System.err.println("[Che779] IPC khong khoi dong duoc: " + t);
        }
        int auto = Integer.getInteger(PROP_AUTOCLEAN, 0);
        if (auto > 0) {
            SCHED.scheduleWithFixedDelay(new Runnable() { public void run() { CleanRam.run(); } },
                    auto, auto, TimeUnit.SECONDS);
        }
        org.microemu.app.Main.main(args);
    }

    static Frame findFrame() {
        return org.microemu.app.Main.instance;
    }

    static Component findByClass(Component c, String simpleName) {
        if (c.getClass().getName().endsWith("." + simpleName)) return c;
        if (c instanceof Container) {
            for (Component k : ((Container) c).getComponents()) {
                Component r = findByClass(k, simpleName);
                if (r != null) return r;
            }
        }
        return null;
    }

    static void runCleanAsync() {
        SCHED.execute(new Runnable() { public void run() { CleanRam.run(); } });
    }

    static String pid() { return ManagementFactory.getRuntimeMXBean().getName().split("@")[0]; }

    /** Gui phim thang vao KeyListener cua man hinh thiet bi (khong can focus, hoat dong ca khi cua so an). */
    static boolean sendKey(final String name, final boolean press, final boolean release, final int holdMs) {
        final Keys.K k = Keys.get(name);
        final Frame f = findFrame();
        if (k == null || f == null) return false;
        final Component dev = findByClass(f, "SwingDeviceComponent");
        if (dev == null) return false;
        Runnable down = new Runnable() { public void run() { fire(dev, KeyEvent.KEY_PRESSED, k); } };
        final Runnable up = new Runnable() { public void run() { fire(dev, KeyEvent.KEY_RELEASED, k); } };
        if (press) SwingUtilities.invokeLater(down);
        if (release) {
            if (press && holdMs > 0) {
                SCHED.schedule(new Runnable() { public void run() { SwingUtilities.invokeLater(up); } }, holdMs, TimeUnit.MILLISECONDS);
            } else {
                SwingUtilities.invokeLater(up);
            }
        }
        return true;
    }

    private static void fire(Component dev, int id, Keys.K k) {
        KeyEvent ev = new KeyEvent(dev, id, System.currentTimeMillis(), 0, k.vk, k.ch);
        for (KeyListener l : dev.getKeyListeners()) {
            if (id == KeyEvent.KEY_PRESSED) l.keyPressed(ev); else l.keyReleased(ev);
        }
    }

    /** Go ca doan chu: moi ky tu = pressed + typed + released vao man hinh thiet bi. */
    static boolean typeText(final String text) {
        final Frame f = findFrame();
        if (f == null || text == null) return false;
        final Component dev = findByClass(f, "SwingDeviceComponent");
        if (dev == null) return false;
        SCHED.execute(new Runnable() { public void run() {
            for (int i = 0; i < text.length(); i++) {
                final char ch = text.charAt(i);
                try {
                    SwingUtilities.invokeAndWait(new Runnable() { public void run() {
                        int vk = KeyEvent.getExtendedKeyCodeForChar(ch);
                        KeyEvent p = new KeyEvent(dev, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, vk, ch);
                        KeyEvent t = new KeyEvent(dev, KeyEvent.KEY_TYPED, System.currentTimeMillis(), 0, KeyEvent.VK_UNDEFINED, ch);
                        KeyEvent r = new KeyEvent(dev, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, vk, ch);
                        for (KeyListener l : dev.getKeyListeners()) {
                            if (TEXT_MODE != 2) l.keyPressed(p);
                            if (TEXT_MODE != 1) l.keyTyped(t);
                            if (TEXT_MODE != 2 && TEXT_MODE != 3) l.keyReleased(r);
                        }
                    }});
                    Thread.sleep(25);
                } catch (Exception ignore) { return; }
            }
        }});
        return true;
    }
    /** 0 = pressed+typed+released, 1 = pressed+released, 2 = typed only (thu nghiem de chon). */
    static int TEXT_MODE = Integer.getInteger("che779.textmode", 0);


    /** state: "on"/"off"/null(chuyen doi). */
    static boolean setSleep(String state) {
        boolean want = state == null ? !org.microemu.app.Che779.sleeping : state.equalsIgnoreCase("on");
        org.microemu.app.Main.setSleepFromOutside(want);
        return true;
    }

    /** Chup cua so emulator ra file PNG (Remoter: Chup man hinh). */
    static boolean screenshot(final String path) {
        final Frame f = findFrame();
        if (f == null || path == null || path.length() == 0) return false;
        try {
            final boolean[] ok = { false };
            SwingUtilities.invokeAndWait(new Runnable() { public void run() {
                try {
                    java.awt.image.BufferedImage bi = new java.awt.image.BufferedImage(
                            Math.max(1, f.getWidth()), Math.max(1, f.getHeight()), java.awt.image.BufferedImage.TYPE_INT_RGB);
                    java.awt.Graphics2D g = bi.createGraphics();
                    f.paintAll(g);
                    g.dispose();
                    java.io.File out = new java.io.File(path);
                    if (out.getParentFile() != null) out.getParentFile().mkdirs();
                    ok[0] = javax.imageio.ImageIO.write(bi, "png", out);
                } catch (Throwable t) { ok[0] = false; }
            }});
            return ok[0];
        } catch (Throwable t) {
            return false;
        }
    }

    static boolean isSleeping() { return org.microemu.app.Che779.sleeping; }

    private static volatile java.awt.Rectangle savedBounds;

    static boolean setVisible(final boolean vis) {
        final Frame f = findFrame();
        if (f == null) return false;
        SwingUtilities.invokeLater(new Runnable() { public void run() {
            if (!vis) {
                if (f.isVisible()) savedBounds = f.getBounds();
                f.setVisible(false);
            } else {
                java.awt.Rectangle b = savedBounds;
                f.setVisible(true);
                if (b != null) f.setBounds(b);   // tra dung vi tri/kich thuoc truoc khi an
                f.setState(Frame.NORMAL);
                f.toFront();
            }
        }});
        return true;
    }

    /** Dong bo chuot: toa do tuong doi (0..1) trong cua so; type = down|up|move. */
    static boolean mouse(final String type, final double fx, final double fy) {
        final Frame f = findFrame();
        if (f == null || type == null) return false;
        SwingUtilities.invokeLater(new Runnable() { public void run() {
            try {
                java.awt.Container cp = f;
                java.awt.Rectangle fb = f.getBounds();
                java.awt.Insets in = f.getInsets();
                int cx = (int) Math.round(fx * fb.width);
                int cy = (int) Math.round(fy * fb.height);
                Component c = javax.swing.SwingUtilities.getDeepestComponentAt(cp, cx, cy);
                if (c == null) return;
                java.awt.Point p = javax.swing.SwingUtilities.convertPoint(cp, cx, cy, c);
                int id; int mods = 0; int clicks = 0; boolean pop = false;
                if (type.equals("down")) { id = java.awt.event.MouseEvent.MOUSE_PRESSED; mods = java.awt.event.InputEvent.BUTTON1_MASK; clicks = 1; }
                else if (type.equals("up")) { id = java.awt.event.MouseEvent.MOUSE_RELEASED; mods = java.awt.event.InputEvent.BUTTON1_MASK; clicks = 1; }
                else if (type.equals("move")) { id = java.awt.event.MouseEvent.MOUSE_DRAGGED; mods = java.awt.event.InputEvent.BUTTON1_MASK; }
                else return;
                long now = System.currentTimeMillis();
                java.awt.event.MouseEvent ev = new java.awt.event.MouseEvent(c, id, now, mods, p.x, p.y, clicks, pop, java.awt.event.MouseEvent.BUTTON1);
                c.dispatchEvent(ev);
                if (type.equals("up")) {
                    c.dispatchEvent(new java.awt.event.MouseEvent(c, java.awt.event.MouseEvent.MOUSE_CLICKED, now, 0, p.x, p.y, 1, false, java.awt.event.MouseEvent.BUTTON1));
                }
                org.microemu.app.Che779.touch();
            } catch (Throwable t) { /* bo qua */ }
        }});
        return true;
    }
}
