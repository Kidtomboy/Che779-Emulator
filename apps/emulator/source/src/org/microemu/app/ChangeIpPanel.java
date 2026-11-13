package org.microemu.app;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.microemu.app.ui.swing.SwingDialogPanel;

/** Hop thoai "Thay IP phien ban": chon may chu co san hoac tu nhap IP/PORT. */
public class ChangeIpPanel extends SwingDialogPanel {
	private static final long serialVersionUID = 1L;

	private final JCheckBox selectServer = new JCheckBox("Chọn sẵn máy chủ NSO (Team+Lậu)");
	private final JComboBox serverBox = new JComboBox();
	private final JTextField ip = new JTextField(22);
	private final JTextField port = new JTextField(8);

	public ChangeIpPanel() {
		setLayout(new BorderLayout(6, 6));
		JPanel form = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(4, 6, 4, 6);
		c.anchor = GridBagConstraints.WEST;
		c.gridx = 0;
		c.gridy = 0;
		form.add(new JLabel("Nhập IP:"), c);
		c.gridx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		form.add(ip, c);
		c.gridx = 0;
		c.gridy = 1;
		c.fill = GridBagConstraints.NONE;
		form.add(new JLabel("Nhập PORT:"), c);
		c.gridx = 1;
		form.add(port, c);

		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 2;
		form.add(new JLabel("(Tích bên dưới thì không cần tự nhập IP và PORT)"), c);
		c.gridy = 3;
		form.add(selectServer, c);
		c.gridy = 4;
		c.fill = GridBagConstraints.HORIZONTAL;
		form.add(serverBox, c);
		c.gridy = 5;
		form.add(new JLabel("(Các server gộp sẽ không có tác dụng do chung ip+port nhưng khác type)"), c);
		add(form, BorderLayout.CENTER);

		JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JButton help = new JButton("Hướng dẫn");
		help.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(ChangeIpPanel.this,
						"Nhập IP, Port máy chủ vào; khi phiên bản kết nối sẽ dùng IP, Port đã nhập (không can thiệp phiên bản, CÓ THỂ DÙNG LOCALHOST).\n\n"
								+ "Bạn có thể tạo file server.txt ghi ip:port, ví dụ example.com:2907, rồi nén file server.txt vào phiên bản jar "
								+ "(nếu dùng file server.txt sẽ không cần bật 'Kích hoạt thay IP'). Phiên bản sẽ tự kết nối đến ip:port trong file.\n\n"
								+ "Thứ tự ưu tiên: server.txt > Tích + chọn server > Tự nhập IP và PORT.\n\n"
								+ "Danh sách máy chủ có sẵn nằm trong file servers.txt (mỗi dòng: ip|port|tên) ở thư mục dữ liệu Che779.",
						"Hướng dẫn", JOptionPane.INFORMATION_MESSAGE);
			}
		});
		JButton reload = new JButton("Tải lại danh sách");
		reload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String u = JOptionPane.showInputDialog(ChangeIpPanel.this,
						"URL danh sách máy chủ (mỗi dòng: ip|port|tên). Để trống = chỉ dùng servers.txt có sẵn:",
						Che779.getServersUrl());
				if (u == null) {
					return;
				}
				Che779.setServersUrl(u);
				if (u.trim().length() > 0) {
					int n = Servers.fetch(u.trim());
					JOptionPane.showMessageDialog(ChangeIpPanel.this,
							n >= 0 ? "Đã tải " + n + " máy chủ." : "Không tải được danh sách.");
				}
				fillServers();
			}
		});
		south.add(help);
		south.add(reload);
		add(south, BorderLayout.SOUTH);

		fillServers();
		selectServer.setSelected(Che779.isIpSelectServer());
		ip.setText(Che779.getIpHost());
		port.setText(Che779.getIpPort());
	}

	private void fillServers() {
		serverBox.removeAllItems();
		List<Servers.Server> l = Servers.all();
		for (Servers.Server s : l) {
			serverBox.addItem(s.toString());
		}
		if (serverBox.getItemCount() > 0) {
			int i = Che779.getIpIndex();
			serverBox.setSelectedIndex(i < serverBox.getItemCount() ? i : 0);
		}
	}

	public boolean check(boolean showErrors) {
		if (!selectServer.isSelected()) {
			try {
				int p = Integer.parseInt(port.getText().trim());
				if (p < 0 || p > 65535 || ip.getText().trim().length() == 0) {
					throw new NumberFormatException();
				}
			} catch (NumberFormatException e) {
				if (showErrors) {
					JOptionPane.showMessageDialog(this, "IP hoặc PORT không hợp lệ.");
				}
				return false;
			}
		}
		Che779.setIpSelectServer(selectServer.isSelected());
		Che779.setIpIndex(Math.max(0, serverBox.getSelectedIndex()));
		Che779.setIpManual(ip.getText().trim(), port.getText().trim());
		return true;
	}
}
