package org.microemu.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Danh sach may chu co san cho "Thay IP phien ban". Luu o servers.txt trong thu muc du lieu,
 * moi dong: ip|port|ten. Co the tai lai tu URL (mac dinh la URL cu cua nguon danh sach, sua duoc trong hop thoai).
 */
public final class Servers {
	public static final class Server {
		public final String name, ip;
		public final int port;

		public Server(String name, String ip, int port) {
			this.name = name;
			this.ip = ip;
			this.port = port;
		}

		public String toString() {
			return name + " (" + ip + ":" + port + ")";
		}
	}

	public static final String DEFAULT_URL = "https://tool.vpsgame.net/public/api/zmicropro/servers_nso.php";

	private static final List<Server> list = Collections.synchronizedList(new ArrayList<Server>());

	private Servers() {
	}

	private static File file() {
		File d = Che779.dataDir();
		return d == null ? null : new File(d, "servers.txt");
	}

	public static List<Server> all() {
		synchronized (list) {
			return new ArrayList<Server>(list);
		}
	}

	public static Server get(int i) {
		synchronized (list) {
			return i >= 0 && i < list.size() ? list.get(i) : null;
		}
	}

	public static void load() {
		File f = file();
		if (f == null || !f.exists()) {
			return;
		}
		try {
			BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
			try {
				List<Server> tmp = new ArrayList<Server>();
				parse(r, tmp);
				synchronized (list) {
					list.clear();
					list.addAll(tmp);
				}
			} finally {
				r.close();
			}
		} catch (IOException e) {
			// bo qua: danh sach rong
		}
	}

	private static void parse(BufferedReader r, List<Server> out) throws IOException {
		String line;
		while ((line = r.readLine()) != null) {
			line = line.trim();
			if (line.length() == 0 || line.startsWith("#")) {
				continue;
			}
			String[] p = line.split("\\|");
			if (p.length >= 3) {
				try {
					out.add(new Server(p[2].trim(), p[0].trim(), Integer.parseInt(p[1].trim())));
				} catch (NumberFormatException e) {
					// dong hong: bo qua
				}
			}
		}
	}

	/** Tai danh sach tu URL, luu vao servers.txt. @return so server, hoac -1 neu loi. */
	public static int fetch(String url) {
		try {
			HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
			c.setConnectTimeout(5000);
			c.setReadTimeout(8000);
			InputStream in = c.getInputStream();
			byte[] data;
			try {
				java.io.ByteArrayOutputStream bo = new java.io.ByteArrayOutputStream();
				byte[] buf = new byte[4096];
				int n;
				while ((n = in.read(buf)) > 0) {
					bo.write(buf, 0, n);
				}
				data = bo.toByteArray();
			} finally {
				in.close();
			}
			List<Server> tmp = new ArrayList<Server>();
			BufferedReader r = new BufferedReader(new InputStreamReader(new java.io.ByteArrayInputStream(data), "UTF-8"));
			parse(r, tmp);
			if (tmp.isEmpty()) {
				return -1;
			}
			File f = file();
			if (f != null) {
				f.getParentFile().mkdirs();
				OutputStream o = new FileOutputStream(f);
				try {
					o.write(data);
				} finally {
					o.close();
				}
			}
			synchronized (list) {
				list.clear();
				list.addAll(tmp);
			}
			return tmp.size();
		} catch (Throwable t) {
			return -1;
		}
	}

	/** Nap file va (neu thieu hoac cu > 1 ngay) tai lai nen trong luc khoi dong. */
	public static void initAsync() {
		load();
		final File f = file();
		final String url = Che779.getServersUrl();
		if (url.length() == 0) {
			return;
		}
		if (f != null && f.exists() && System.currentTimeMillis() - f.lastModified() < 24L * 3600 * 1000) {
			return;
		}
		Thread t = new Thread(new Runnable() {
			public void run() {
				fetch(url);
			}
		}, "che779-servers");
		t.setDaemon(true);
		t.start();
	}
}
