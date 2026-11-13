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

import java.io.IOException;

import org.microemu.cldc.ClosedConnection;

public class Connection implements ClosedConnection {

	public javax.microedition.io.Connection open(String name) throws IOException {

		if (!org.microemu.cldc.http.Connection.isAllowNetworkConnection()) {
			throw new IOException("No network");
		}

		int port = -1;
		int portSepIndex = name.lastIndexOf(':');
		if (portSepIndex == -1) {
			throw new IllegalArgumentException("Port missing");
		}
		String portToParse = name.substring(portSepIndex + 1);
		if (portToParse.length() > 0) {
			port = Integer.parseInt(portToParse);
		}
		String host = name.substring("socket://".length(), portSepIndex);

		if (host.length() > 0) {
			if (port == -1) {
				throw new IllegalArgumentException("Port missing");
			}
			// Che779: thay IP / proxy
			String[] rt = org.microemu.app.Che779.route(host, port);
			String h2 = rt[0];
			int p2 = Integer.parseInt(rt[1]);
			org.microemu.app.Main.statusMessage("Kết nối đến máy chủ: " + h2 + ":" + p2);
			org.microemu.app.ProxyManager.Proxy px = org.microemu.app.ProxyManager.selected();
			if (px == null) {
				return new SocketConnection(h2, p2);
			}
			String type = px.type;
			if ("AUTO".equals(type)) {
				type = org.microemu.app.ProxyManager.detect(px.host, px.port);
			}
			return new SocketConnection(h2, p2, px.host, px.port, px.user, px.pass, "HTTP".equals(type));
		} else {
			if (port == -1) {
				return new ServerSocketConnection();
			} else {
				return new ServerSocketConnection(port);
			}
		}
	}

	public void close() throws IOException {
		// Implemented in SocketConnection or ServerSocketConnection
	}

}
