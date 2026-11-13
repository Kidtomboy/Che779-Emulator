package org.microemu.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Danh sach proxy do nguoi dung tu them (HTTP / SOCKS5) + kiem tra song/chet. Luu o proxies.txt. */
public final class ProxyManager {
	public static final class Proxy {
		public String host = "", user = "", pass = "", type = "AUTO";
		public int port;
		public String status = "READY";

		public String label() {
			return host + ":" + port + " [" + type + "]";
		}
	}

	private static final List<Proxy> list = Collections.synchronizedList(new ArrayList<Proxy>());

	private ProxyManager() {
	}

	private static File file() {
		File d = Che779.dataDir();
		return d == null ? null : new File(d, "proxies.txt");
	}

	public static List<Proxy> all() {
		return list;
	}

	public static void load() {
		File f = file();
		if (f == null || !f.exists()) {
			return;
		}
		try {
			BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
			try {
				List<Proxy> tmp = new ArrayList<Proxy>();
				String line;
				while ((line = r.readLine()) != null) {
					String[] p = line.split("\\|", -1);
					if (p.length >= 5) {
						Proxy x = new Proxy();
						x.host = p[0];
						try {
							x.port = Integer.parseInt(p[1]);
						} catch (NumberFormatException e) {
							continue;
						}
						x.user = p[2];
						x.pass = p[3];
						x.type = p[4];
						tmp.add(x);
					}
				}
				synchronized (list) {
					list.clear();
					list.addAll(tmp);
				}
			} finally {
				r.close();
			}
		} catch (IOException e) {
			// bo qua
		}
	}

	public static void save() {
		File f = file();
		if (f == null) {
			return;
		}
		try {
			f.getParentFile().mkdirs();
			StringBuilder sb = new StringBuilder();
			synchronized (list) {
				for (Proxy x : list) {
					sb.append(x.host).append('|').append(x.port).append('|').append(x.user).append('|').append(x.pass)
							.append('|').append(x.type).append('\n');
				}
			}
			OutputStream o = new FileOutputStream(f);
			try {
				o.write(sb.toString().getBytes("UTF-8"));
			} finally {
				o.close();
			}
		} catch (IOException e) {
			// bo qua
		}
	}

	private static volatile boolean overrideOn;
	private static volatile Proxy overrideProxy;

	/** Remoter ep proxy cho tab nay (px = null: khong dung proxy). */
	public static void setOverride(Proxy px) {
		overrideProxy = px;
		overrideOn = true;
	}

	/** Doc -Dche779.proxy=host|port|user|pass|type luc khoi dong ("none" = khong proxy). */
	public static void initOverrideFromProperty() {
		String v = System.getProperty("che779.proxy");
		if (v == null || v.length() == 0) {
			return;
		}
		setOverride(parseSpec(v));
	}

	public static Proxy parseSpec(String v) {
		if (v == null || v.equalsIgnoreCase("none")) {
			return null;
		}
		String[] p = v.split("\\|", -1);
		if (p.length < 2) {
			return null;
		}
		try {
			Proxy x = new Proxy();
			x.host = p[0];
			x.port = Integer.parseInt(p[1].trim());
			x.user = p.length > 2 ? p[2] : "";
			x.pass = p.length > 3 ? p[3] : "";
			x.type = p.length > 4 && p[4].length() > 0 ? p[4] : "AUTO";
			return x.host.length() > 0 ? x : null;
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public static String describeActive() {
		Proxy p = selected();
		return p == null ? "none" : p.label();
	}

	/** Proxy dang chon (null = khong dung proxy). */
	public static Proxy selected() {
		if (overrideOn) {
			return overrideProxy;
		}
		int s = Che779.getProxySel();
		synchronized (list) {
			return s >= 1 && s <= list.size() ? list.get(s - 1) : null;
		}
	}

	/** Doan loai proxy: "SOCKS5", "HTTP" hoac null. */
	public static String detect(String host, int port) {
		try {
			Socket s = new Socket();
			try {
				s.connect(new InetSocketAddress(host, port), 3000);
				s.setSoTimeout(3000);
				OutputStream o = s.getOutputStream();
				InputStream in = s.getInputStream();
				o.write(new byte[] { 5, 1, 0 });
				o.flush();
				byte[] b = new byte[2];
				if (in.read(b) == 2 && b[0] == 5) {
					return "SOCKS5";
				}
			} finally {
				s.close();
			}
		} catch (Throwable t) {
			// thu HTTP
		}
		try {
			Socket s = new Socket();
			try {
				s.connect(new InetSocketAddress(host, port), 3000);
				s.setSoTimeout(3000);
				s.getOutputStream().write("CONNECT example.com:80 HTTP/1.1\r\n\r\n".getBytes("UTF-8"));
				s.getOutputStream().flush();
				byte[] b = new byte[64];
				int n = s.getInputStream().read(b);
				if (n > 0 && new String(b, 0, n, "UTF-8").contains("HTTP")) {
					return "HTTP";
				}
			} finally {
				s.close();
			}
		} catch (Throwable t) {
			// khong phai proxy
		}
		return null;
	}

	/** Kiem tra proxy co mo duoc ket noi toi example.com:80 khong. @return "ALIVE" hoac "DEAD" */
	public static String check(Proxy p) {
		try {
			String type = p.type;
			if ("AUTO".equals(type)) {
				type = detect(p.host, p.port);
				if (type == null) {
					return "DEAD";
				}
				p.type = type;
			}
			org.microemu.cldc.socket.SocketConnection c = new org.microemu.cldc.socket.SocketConnection("example.com", 80,
					p.host, p.port, p.user, p.pass, "HTTP".equals(type));
			c.close();
			return "ALIVE";
		} catch (Throwable t) {
			return "DEAD";
		}
	}
}
