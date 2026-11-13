package org.microemu.app;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import org.microemu.app.ui.swing.SwingDialogPanel;

/** Quan ly proxy: tu them proxy ban da mua (HTTP / SOCKS5), kiem tra song/chet, chon proxy se dung. */
public class ProxyPanel extends SwingDialogPanel {
	private static final long serialVersionUID = 1L;

	private final DefaultTableModel model = new DefaultTableModel(
			new Object[] { "IP", "Port", "User", "Pass", "Type", "Status" }, 0) {
		private static final long serialVersionUID = 1L;

		public boolean isCellEditable(int r, int c) {
			return false;
		}
	};
	private final JTable table = new JTable(model);
	private final JComboBox use = new JComboBox();
	private final JTextField fIp = new JTextField(12), fPort = new JTextField(5), fUser = new JTextField(8),
			fPass = new JTextField(8);
	private final JComboBox fType = new JComboBox(new String[] { "AUTO", "HTTP", "SOCKS5" });

	public ProxyPanel() {
		setLayout(new BorderLayout(6, 6));
		JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
		top.add(new JLabel("Chọn proxy sẽ dùng:"));
		top.add(use);
		JLabel hint = new JLabel("Hãy tự sử dụng proxy bạn đã mua để sử dụng.");
		JPanel north = new JPanel(new BorderLayout());
		north.add(top, BorderLayout.NORTH);
		north.add(hint, BorderLayout.SOUTH);
		add(north, BorderLayout.NORTH);

		JScrollPane sp = new JScrollPane(table);
		sp.setPreferredSize(new Dimension(560, 180));
		add(sp, BorderLayout.CENTER);

		JPanel south = new JPanel(new BorderLayout());
		JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		row1.add(new JLabel("IP"));
		row1.add(fIp);
		row1.add(new JLabel("Port"));
		row1.add(fPort);
		row1.add(new JLabel("User"));
		row1.add(fUser);
		row1.add(new JLabel("Pass"));
		row1.add(fPass);
		row1.add(fType);
		JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JButton add = new JButton("Add");
		JButton detect = new JButton("Detect");
		JButton del = new JButton("Delete");
		JButton chk = new JButton("Check All");
		row2.add(add);
		row2.add(detect);
		row2.add(del);
		row2.add(chk);
		south.add(row1, BorderLayout.NORTH);
		south.add(row2, BorderLayout.SOUTH);
		add(south, BorderLayout.SOUTH);

		add.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onAdd();
			}
		});
		detect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onDetect();
			}
		});
		del.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onDelete();
			}
		});
		chk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onCheckAll();
			}
		});
		reload();
	}

	private void reload() {
		model.setRowCount(0);
		for (ProxyManager.Proxy p : new ArrayList<ProxyManager.Proxy>(ProxyManager.all())) {
			model.addRow(new Object[] { p.host, Integer.valueOf(p.port), p.user, p.pass.length() > 0 ? "***" : "", p.type,
					p.status });
		}
		fillUse();
	}

	private void fillUse() {
		use.removeAllItems();
		use.addItem("Không dùng Proxy");
		for (ProxyManager.Proxy p : new ArrayList<ProxyManager.Proxy>(ProxyManager.all())) {
			use.addItem(p.label());
		}
		int s = Che779.getProxySel();
		use.setSelectedIndex(s < use.getItemCount() ? s : 0);
	}

	private void onAdd() {
		try {
			ProxyManager.Proxy p = new ProxyManager.Proxy();
			p.host = fIp.getText().trim();
			p.port = Integer.parseInt(fPort.getText().trim());
			if (p.host.length() == 0 || p.port < 1 || p.port > 65535) {
				throw new NumberFormatException();
			}
			p.user = fUser.getText().trim();
			p.pass = fPass.getText().trim();
			p.type = (String) fType.getSelectedItem();
			if (JOptionPane.showConfirmDialog(this, "Thêm proxy này?", "Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
				return;
			}
			ProxyManager.all().add(p);
			ProxyManager.save();
			reload();
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(this, "Port không hợp lệ");
		}
	}

	private void onDetect() {
		final String host = fIp.getText().trim();
		final int port;
		try {
			port = Integer.parseInt(fPort.getText().trim());
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(this, "Port không hợp lệ");
			return;
		}
		new Thread(new Runnable() {
			public void run() {
				final String t = ProxyManager.detect(host, port);
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						if (t != null) {
							fType.setSelectedItem(t);
						}
						JOptionPane.showMessageDialog(ProxyPanel.this, t != null ? "Detected: " + t : "DEAD");
					}
				});
			}
		}).start();
	}

	private void onDelete() {
		int r = table.getSelectedRow();
		if (r < 0) {
			return;
		}
		if (JOptionPane.showConfirmDialog(this, "Xóa proxy đã chọn?", "Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
			return;
		}
		ProxyManager.all().remove(r);
		ProxyManager.save();
		if (Che779.getProxySel() > ProxyManager.all().size()) {
			Che779.setProxySel(0);
		}
		reload();
	}

	private void onCheckAll() {
		final List<ProxyManager.Proxy> snap = new ArrayList<ProxyManager.Proxy>(ProxyManager.all());
		for (ProxyManager.Proxy p : snap) {
			p.status = "CHECKING...";
		}
		reload();
		new Thread(new Runnable() {
			public void run() {
				for (ProxyManager.Proxy p : snap) {
					p.status = ProxyManager.check(p);
				}
				ProxyManager.save();
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						reload();
					}
				});
			}
		}).start();
	}

	public boolean check(boolean showErrors) {
		Che779.setProxySel(Math.max(0, use.getSelectedIndex()));
		return true;
	}
}
