using System;
using System.Drawing;
using System.Windows.Forms;

namespace Che779.Remoter
{
    /// <summary>Ban phim noi: phim dien thoai Nokia + ban phim may tinh (chu, xoa, space, enter, ky tu) + o go chu.</summary>
    internal class KeypadForm : Form
    {
        private readonly MainForm main;
        private TextBox txt;
        private static readonly Font KeyFont = new Font("Segoe UI Emoji", 9.5f);

        public KeypadForm(MainForm owner)
        {
            main = owner;
            Text = "⌨️ Bàn phím Che779";
            FormBorderStyle = FormBorderStyle.FixedToolWindow;
            StartPosition = FormStartPosition.Manual;
            ShowInTaskbar = false;
            TopMost = true;
            KeyPreview = true;
            Font = new Font("Segoe UI", 9f);
            Build();
            FormClosing += delegate(object s, FormClosingEventArgs e)
            {
                if (e.CloseReason == CloseReason.UserClosing) { e.Cancel = true; Hide(); }
            };
        }

        protected override bool ShowWithoutActivation
        {
            get { return true; }
        }

        private Button MakeBtn(string text, int w, int h)
        {
            Button b = new Button();
            b.Text = text;
            b.Font = KeyFont;
            b.Size = new Size(w, h);
            b.Margin = new Padding(2);
            b.FlatStyle = FlatStyle.Flat;
            b.FlatAppearance.BorderSize = 1;
            b.Cursor = Cursors.Hand;
            b.UseVisualStyleBackColor = false;
            b.TabStop = false;
            return b;
        }

        /// <summary>Phim that (UP, 5, A, SPACE...) gui bang lenh keypress.</summary>
        private Button Key(string text, string key, int w, int h)
        {
            Button b = MakeBtn(text, w, h);
            string k = key;
            b.Click += delegate { main.SendKeyToTargets(k); };
            return b;
        }

        /// <summary>Ky tu dac biet gui bang lenh text.</summary>
        private Button CharKey(string text, int w, int h)
        {
            Button b = MakeBtn(text, w, h);
            string ch = text;
            b.Click += delegate { main.SendText(ch); };
            return b;
        }

        private void Build()
        {
            TableLayoutPanel root = new TableLayoutPanel();
            root.Dock = DockStyle.Fill;
            root.ColumnCount = 2;
            root.RowCount = 2;
            root.Padding = new Padding(10);
            root.ColumnStyles.Add(new ColumnStyle(SizeType.Absolute, 214f));
            root.ColumnStyles.Add(new ColumnStyle(SizeType.Percent, 100f));
            root.RowStyles.Add(new RowStyle(SizeType.Percent, 100f));
            root.RowStyles.Add(new RowStyle(SizeType.Absolute, 48f));
            Controls.Add(root);

            // ----- ben trai: phim Nokia
            TableLayoutPanel nokia = new TableLayoutPanel();
            nokia.Dock = DockStyle.Fill;
            nokia.ColumnCount = 3;
            nokia.RowCount = 7;
            for (int i = 0; i < 3; i++) nokia.ColumnStyles.Add(new ColumnStyle(SizeType.Percent, 33.3f));
            for (int i = 0; i < 7; i++) nokia.RowStyles.Add(new RowStyle(SizeType.Percent, 14.2f));
            string[][] rows = new string[][]
            {
                new string[] { "SOFT_LEFT", "UP", "SOFT_RIGHT" },
                new string[] { "LEFT", "FIRE", "RIGHT" },
                new string[] { "", "DOWN", "" },
                new string[] { "1", "2", "3" },
                new string[] { "4", "5", "6" },
                new string[] { "7", "8", "9" },
                new string[] { "STAR", "0", "POUND" }
            };
            for (int r = 0; r < rows.Length; r++)
            {
                for (int c = 0; c < 3; c++)
                {
                    string key = rows[r][c];
                    if (key.Length == 0) continue;
                    Button b = Key(Caption(key), key, 60, 38);
                    b.Dock = DockStyle.Fill;
                    nokia.Controls.Add(b, c, r);
                }
            }
            root.Controls.Add(nokia, 0, 0);

            // ----- ben phai: ban phim may tinh
            FlowLayoutPanel pc = new FlowLayoutPanel();
            pc.Dock = DockStyle.Fill;
            pc.FlowDirection = FlowDirection.TopDown;
            pc.WrapContents = false;
            pc.Padding = new Padding(10, 0, 0, 0);

            string[] letters = new string[] { "QWERTYUIOP", "ASDFGHJKL", "ZXCVBNM" };
            for (int r = 0; r < letters.Length; r++)
            {
                FlowLayoutPanel row = NewRow();
                if (r == 1) row.Padding = new Padding(18, 0, 0, 0);
                if (r == 2) row.Padding = new Padding(40, 0, 0, 0);
                foreach (char ch in letters[r])
                    row.Controls.Add(Key(ch.ToString(), ch.ToString(), 40, 38));
                if (r == 2) row.Controls.Add(Key("⌫ Xóa", "BACKSPACE", 66, 38));
                pc.Controls.Add(row);
            }

            FlowLayoutPanel sym = NewRow();
            foreach (string s in new string[] { ".", ",", "?", "!", "@", "-", "_", "/", ":", "'" })
                sym.Controls.Add(CharKey(s, 40, 34));
            pc.Controls.Add(sym);

            FlowLayoutPanel bottom = NewRow();
            bottom.Controls.Add(Key("␣ Space", "SPACE", 150, 38));
            bottom.Controls.Add(Key("↵ Enter", "FIRE", 90, 38));
            bottom.Controls.Add(Key("⌦ Del", "DELETE", 70, 38));
            pc.Controls.Add(bottom);

            Label hint = new Label();
            hint.AutoSize = false;
            hint.Size = new Size(430, 34);
            hint.Margin = new Padding(2, 6, 0, 0);
            hint.Text = "Gửi tới các tab theo quy tắc Đồng bộ Tab. Bật Đồng bộ Phím để dùng cả bàn phím thật khi cửa sổ này được chọn.";
            hint.Tag = "muted";
            pc.Controls.Add(hint);
            root.Controls.Add(pc, 1, 0);

            // ----- hang duoi: o go chu
            TableLayoutPanel send = new TableLayoutPanel();
            send.Dock = DockStyle.Fill;
            send.ColumnCount = 3;
            send.RowCount = 1;
            send.ColumnStyles.Add(new ColumnStyle(SizeType.Percent, 100f));
            send.ColumnStyles.Add(new ColumnStyle(SizeType.Absolute, 84f));
            send.ColumnStyles.Add(new ColumnStyle(SizeType.Absolute, 104f));
            txt = new TextBox();
            txt.Dock = DockStyle.Fill;
            txt.BorderStyle = BorderStyle.FixedSingle;
            txt.Margin = new Padding(0, 8, 8, 6);
            Button bSend = MakeBtn("📤 Gửi", 80, 32);
            bSend.Dock = DockStyle.Fill;
            bSend.Margin = new Padding(0, 6, 4, 4);
            Button bSendEnter = MakeBtn("📤 Gửi + ↵", 100, 32);
            bSendEnter.Dock = DockStyle.Fill;
            bSendEnter.Margin = new Padding(0, 6, 0, 4);
            bSend.Click += delegate { DoSend(false); };
            bSendEnter.Click += delegate { DoSend(true); };
            txt.KeyDown += delegate(object s, KeyEventArgs e)
            {
                if (e.KeyCode == Keys.Enter) { e.SuppressKeyPress = true; DoSend(false); }
            };
            send.Controls.Add(txt, 0, 0);
            send.Controls.Add(bSend, 1, 0);
            send.Controls.Add(bSendEnter, 2, 0);
            root.SetColumnSpan(send, 2);
            root.Controls.Add(send, 0, 1);

            ClientSize = new Size(700, 372);
        }

        private void DoSend(bool thenEnter)
        {
            string t = txt.Text;
            if (t.Length == 0 && !thenEnter) return;
            if (t.Length > 0) main.SendText(t);
            if (thenEnter) main.SendKeyToTargets("FIRE");
            txt.Clear();
            txt.Focus();
        }

        private static FlowLayoutPanel NewRow()
        {
            FlowLayoutPanel f = new FlowLayoutPanel();
            f.AutoSize = true;
            f.AutoSizeMode = AutoSizeMode.GrowAndShrink;
            f.WrapContents = false;
            f.Margin = new Padding(0, 0, 0, 2);
            return f;
        }

        private static string Caption(string key)
        {
            switch (key)
            {
                case "SOFT_LEFT": return "◄ Soft";
                case "SOFT_RIGHT": return "Soft ►";
                case "UP": return "▲";
                case "DOWN": return "▼";
                case "LEFT": return "◀";
                case "RIGHT": return "▶";
                case "FIRE": return "OK";
                case "STAR": return "*";
                case "POUND": return "#";
                default: return key;
            }
        }

        protected override bool ProcessCmdKey(ref Message msg, Keys keyData)
        {
            if (main.SyncKeyOn && !(ActiveControl is TextBox))
            {
                if (main.TryPhysicalKey(keyData)) return true;
            }
            return base.ProcessCmdKey(ref msg, keyData);
        }
    }
}
