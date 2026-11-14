/*
 * Che779 Emulator - trang thai & cai dat rieng cua Che779 (sleep, toc do, FPS, mau, giam do hoa).
 * Luu trong file cau hinh che779.xml (nut <che779>).
 */
package org.microemu.app;

import nanoxml.XMLElement;

public final class Che779 {
	public static final String NAME = "Che779 Emulator";
	/** Phien ban (ban custom). Hien trong About, log, IPC status va Remoter. */
	public static final String VERSION = "1.2.0";

	/**
	 * Thu muc du lieu: -Dche779.data=... (Remoter truyen vao) > %APPDATA%\Che779\Che779-Emulator > ~/.che779
	 */
	public static java.io.File dataDir() {
		try {
			String p = System.getProperty("che779.data");
			if (p != null && p.trim().length() > 0) {
				return new java.io.File(p);
			}
			String appdata = System.getenv("APPDATA");
			if (appdata != null && appdata.length() > 0) {
				return new java.io.File(new java.io.File(appdata, "Che779"), "Che779-Emulator");
			}
			return new java.io.File(System.getProperty("user.home"), ".che779");
		} catch (SecurityException e) {
			return null;
		}
	}
	public static final String FOUNDER = "Người sáng lập: Cherry - Che779 Team";

	public static final String[] COLOR_NAMES = { "Bình thường", "Hình ảnh màu Xám", "Hình ảnh màu Đen" };
	public static final int[] COLOR_NUMCOLORS = { 0, 256, 2 }; // 0 = khong doi (mau day du)
	public static final String[] FPS_NAMES = { "Bình thường", "Giảm cấp 1", "Giảm cấp 2", "Giảm cấp 3", "Giảm cấp 4", "Giảm cấp 5" };
	public static final String[] GFX_NAMES = { "Không giảm đồ họa", "Giảm đồ họa cấp 1", "Giảm đồ họa cấp 2",
			"Giảm đồ họa cấp 3", "Giảm đồ họa cấp 4", "Giảm đồ họa cấp 5" };
	private static final float[] GFX_QUALITY = { 0.0f, 0.1f, 0.07f, 0.05f, 0.03f, 0.01f };
	public static final String[] AUTOSLEEP_NAMES = { "Không tự Sleep", "Mỗi 1 phút", "Mỗi 5 phút", "Mỗi 15 phút" };
	private static final long[] AUTOSLEEP_MS = { Long.MAX_VALUE, 60000L, 300000L, 900000L };

	public static volatile boolean sleeping = false;
	public static volatile long lastActivity = System.currentTimeMillis();

	private Che779() {
	}

	// ------------------------------------------------------------------ settings (che779.xml)
	private static XMLElement node() {
		return Config.getChe779Node();
	}

	private static int getInt(String key, int def, int max) {
		try {
			int v = node().getIntAttribute(key, def);
			return v < 0 || v > max ? def : v;
		} catch (Throwable t) {
			return def;
		}
	}

	private static void setInt(String key, int v) {
		node().setIntAttribute(key, v);
		Config.saveConfig();
	}

	public static int getColor() {
		return getInt("color", 0, COLOR_NAMES.length - 1);
	}

	public static void setColor(int v) {
		setInt("color", v);
	}

	public static int getFps() {
		return getInt("fps", 0, FPS_NAMES.length - 1);
	}

	public static void setFps(int v) {
		setInt("fps", v);
	}

	public static int getGfx() {
		return getInt("gfx", 0, GFX_NAMES.length - 1);
	}

	public static void setGfx(int v) {
		setInt("gfx", v);
	}

	public static int getAutoSleep() {
		return getInt("autosleep", 0, AUTOSLEEP_NAMES.length - 1);
	}

	public static void setAutoSleep(int v) {
		setInt("autosleep", v);
		touch();
	}

	/** Toc do game: 1.0 = binh thuong, >1 nhanh hon, <1 cham hon. */
	public static volatile float speed = 1.0f;

	public static void loadSpeed() {
		try {
			speed = Float.parseFloat(node().getStringAttribute("speed", "1.0"));
			if (!(speed >= 0.1f && speed <= 100f)) {
				speed = 1.0f;
			}
		} catch (Throwable t) {
			speed = 1.0f;
		}
	}

	public static void setSpeed(float v) {
		if (v < 0.1f) {
			v = 0.1f;
		}
		if (v > 100f) {
			v = 100f;
		}
		speed = Math.round(v * 10f) / 10f;
		node().setAttribute("speed", Float.toString(speed));
		Config.saveConfig();
	}

	// ------------------------------------------------------------------ mang: thay IP / proxy
	/** IP:port doc tu server.txt trong file jar cua game (uu tien cao nhat). */
	public static volatile String ipTemp, portTemp;

	private static String getStr(String key, String def) {
		try {
			return node().getStringAttribute(key, def);
		} catch (Throwable t) {
			return def;
		}
	}

	private static void setStr(String key, String v) {
		node().setAttribute(key, v == null ? "" : v);
		Config.saveConfig();
	}

	public static boolean isIpActive() {
		return getInt("ip_active", 0, 1) == 1;
	}

	public static void setIpActive(boolean b) {
		setInt("ip_active", b ? 1 : 0);
	}

	public static boolean isIpSelectServer() {
		return getInt("ip_select", 0, 1) == 1;
	}

	public static void setIpSelectServer(boolean b) {
		setInt("ip_select", b ? 1 : 0);
	}

	public static int getIpIndex() {
		return getInt("ip_index", 0, 100000);
	}

	public static void setIpIndex(int i) {
		setInt("ip_index", i);
	}

	public static String getIpHost() {
		return getStr("ip_host", "");
	}

	public static String getIpPort() {
		return getStr("ip_port", "");
	}

	public static void setIpManual(String host, String port) {
		node().setAttribute("ip_host", host);
		node().setAttribute("ip_port", port);
		Config.saveConfig();
	}

	/** 0 = khong dung proxy, i = proxy thu i trong danh sach. */
	public static int getProxySel() {
		return getInt("proxy_sel", 0, 100000);
	}

	public static void setProxySel(int i) {
		setInt("proxy_sel", i);
	}

	public static String getServersUrl() {
		return getStr("servers_url", Servers.DEFAULT_URL);
	}

	public static void setServersUrl(String u) {
		setStr("servers_url", u == null ? "" : u.trim());
	}

	/**
	 * Quyet dinh ip:port that su de noi. Thu tu uu tien: server.txt trong jar > (kich hoat thay IP:
	 * chon may chu co san > tu nhap IP/PORT) > ip:port game yeu cau.
	 */
	public static String[] route(String host, int port) {
		String h = host;
		int p = port;
		try {
			if (isIpActive()) {
				Servers.Server s = isIpSelectServer() ? Servers.get(getIpIndex()) : null;
				if (s != null) {
					h = s.ip;
					p = s.port;
				} else if (!isIpSelectServer() && getIpHost().length() > 0 && getIpPort().length() > 0) {
					h = getIpHost();
					p = Integer.parseInt(getIpPort());
				}
			}
			if (ipTemp != null && ipTemp.length() > 0 && portTemp != null && portTemp.length() > 0) {
				h = ipTemp;
				p = Integer.parseInt(portTemp);
			}
		} catch (Throwable t) {
			// giu nguyen dich goc
		}
		return new String[] { h, Integer.toString(p) };
	}

	// ------------------------------------------------------------------ sleep
	public static void touch() {
		lastActivity = System.currentTimeMillis();
	}

	public static long autoSleepMillis() {
		return AUTOSLEEP_MS[getAutoSleep()];
	}

	/** True neu da het thoi gian khong thao tac (tu dong sleep). */
	public static boolean isAutoSleepNow() {
		long d = autoSleepMillis();
		return d != Long.MAX_VALUE && System.currentTimeMillis() - lastActivity >= d;
	}

	public static boolean isSleepNow() {
		return sleeping || isAutoSleepNow();
	}

	/** Con bao nhieu ms nua se tu sleep (Long.MAX_VALUE neu khong dung tu sleep). */
	public static long autoSleepRemaining() {
		long d = autoSleepMillis();
		if (d == Long.MAX_VALUE) {
			return Long.MAX_VALUE;
		}
		return d - (System.currentTimeMillis() - lastActivity);
	}

	public static void setSleep(boolean on) {
		sleeping = on;
		touch();
	}

	// ------------------------------------------------------------------ do hoa / toc do
	public static float gfxQuality() {
		return GFX_QUALITY[getGfx()];
	}

	/** Thoi gian ngu thuc te theo toc do game (thay cho Thread.sleep trong game). */
	public static long scaleSleep(long ms) {
		float s = speed;
		if (s != 1.0f && ms > 1) {
			ms = (long) (ms / s);
			if (ms <= 0) {
				ms = 1;
			}
		}
		return ms;
	}
}
