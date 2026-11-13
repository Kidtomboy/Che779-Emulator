/*
 *  MicroEmulator
 *  Copyright (C) 2001-2003 Bartek Teodorczyk <barteo@barteo.net>
 *
 *  It is licensed under the following two licenses as alternatives:
 *    1. GNU Lesser General Public License (the "LGPL") version 2.1 or any newer version
 *    2. Apache License (the "AL") Version 2.0
 *
 *  You may not use this file except in compliance with at least one of
 *  the above two licenses.
 *
 *  You may obtain a copy of the LGPL at
 *      http://www.gnu.org/licenses/old-licenses/lgpl-2.1.txt
 *
 *  You may obtain a copy of the AL at
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the LGPL or the AL for the specific language governing permissions and
 *  limitations.
 */

package org.microemu.cldc.socket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class SocketConnection implements javax.microedition.io.SocketConnection {

	protected Socket socket;
	
	public SocketConnection() {		
	}

	public SocketConnection(String host, int port) throws IOException {
		this.socket = new Socket(host, port);
	}
	
	/**
	 * Ket noi qua proxy. HTTP: CONNECT + Basic auth. SOCKS5: java.net.Proxy (xac thuc user/pass bang Authenticator).
	 */
	public SocketConnection(String host, int port, String proxyHost, int proxyPort, String user, String pass,
			boolean http) throws IOException {
		if (http) {
			Socket s = new Socket(proxyHost, proxyPort);
			s.setSoTimeout(15000);
			java.io.OutputStream out = s.getOutputStream();
			String auth = "";
			if (user != null && user.length() > 0) {
				auth = "Proxy-Authorization: Basic " + java.util.Base64.getEncoder().encodeToString(
						(user + ":" + pass).getBytes("UTF-8")) + "\r\n";
			}
			out.write(("CONNECT " + host + ":" + port + " HTTP/1.1\r\nHost: " + host + ":" + port + "\r\n" + auth + "\r\n")
					.getBytes("UTF-8"));
			out.flush();
			java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(s.getInputStream(), "UTF-8"));
			String status = br.readLine();
			if (status == null || !status.contains(" 200")) {
				s.close();
				throw new IOException("Proxy CONNECT failed: " + status);
			}
			String line;
			while ((line = br.readLine()) != null && line.length() > 0) {
				// bo qua header tra loi
			}
			s.setSoTimeout(0);
			this.socket = s;
		} else {
			final String u = user, p = pass;
			if (u != null && u.length() > 0) {
				java.net.Authenticator.setDefault(new java.net.Authenticator() {
					protected java.net.PasswordAuthentication getPasswordAuthentication() {
						return new java.net.PasswordAuthentication(u, p.toCharArray());
					}
				});
			}
			java.net.Proxy px = new java.net.Proxy(java.net.Proxy.Type.SOCKS, new java.net.InetSocketAddress(proxyHost, proxyPort));
			Socket s = new Socket(px);
			s.connect(new java.net.InetSocketAddress(host, port), 15000);
			this.socket = s;
		}
	}

	public SocketConnection(Socket socket) {
		this.socket = socket;
	}

	public String getAddress() throws IOException {
		if (socket == null || socket.isClosed()) {
			throw new IOException();
		}

		return socket.getInetAddress().toString();
	}

	public String getLocalAddress() throws IOException {
		if (socket == null || socket.isClosed()) {
			throw new IOException();
		}

		return socket.getLocalAddress().toString();
	}

	public int getLocalPort() throws IOException {
		if (socket == null || socket.isClosed()) {
			throw new IOException();
		}

		return socket.getLocalPort();
	}

	public int getPort() throws IOException {
		if (socket == null || socket.isClosed()) {
			throw new IOException();
		}

		return socket.getPort();
	}

	public int getSocketOption(byte option) throws IllegalArgumentException,
			IOException {
		if (socket != null && socket.isClosed()) {
			throw new IOException();
		}
		switch (option) {
		case DELAY:
			if (socket.getTcpNoDelay()) {
				return 1;
			} else {
				return 0;
			}
		case LINGER:
			int value = socket.getSoLinger();
			if (value == -1) {
				return 0;
			} else {
				return value;
			}
		case KEEPALIVE:
			if (socket.getKeepAlive()) {
				return 1;
			} else {
				return 0;
			}
		case RCVBUF:
			return socket.getReceiveBufferSize();
		case SNDBUF:
			return socket.getSendBufferSize();
		default:
			throw new IllegalArgumentException();
		}
	}

	public void setSocketOption(byte option, int value)
			throws IllegalArgumentException, IOException {
		if (socket.isClosed()) {
			throw new IOException();
		}
		switch (option) {
		case DELAY:
			int delay;
			if (value == 0) {
				delay = 0;
			} else {
				delay = 1;
			}
			socket.setTcpNoDelay(delay == 0 ? false : true);
			break;
		case LINGER:
			if (value < 0) {
				throw new IllegalArgumentException();
			}
			socket.setSoLinger(value == 0 ? false : true, value);
			break;
		case KEEPALIVE:
			int keepalive;
			if (value == 0) {
				keepalive = 0;
			} else {
				keepalive = 1;
			}
			socket.setKeepAlive(keepalive == 0 ? false : true);
			break;
		case RCVBUF:
			if (value <= 0) {
				throw new IllegalArgumentException();
			}
			socket.setReceiveBufferSize(value);
			break;
		case SNDBUF:
			if (value <= 0) {
				throw new IllegalArgumentException();
			}
			socket.setSendBufferSize(value);
			break;
		default:
			throw new IllegalArgumentException();
		}
	}

	public void close() throws IOException {
		// TODO fix differences between Java ME and Java SE
		
		socket.close();
	}

	public InputStream openInputStream() throws IOException {
		return socket.getInputStream();
	}

	public DataInputStream openDataInputStream() throws IOException {
		return new DataInputStream(openInputStream());
	}

	public OutputStream openOutputStream() throws IOException {
		return socket.getOutputStream();
	}

	public DataOutputStream openDataOutputStream() throws IOException {
		return new DataOutputStream(openOutputStream());
	}

}
