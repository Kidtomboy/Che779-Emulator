using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Drawing;
using System.IO;
using System.Text;
using System.Windows.Forms;

namespace Che779.Remoter
{
    internal partial class MainForm : Form
    {
        private const int BasePort = 7790;
        private const string AppTitle = "Che779 Remoter v" + AppInfo.Version;

        private readonly Settings cfg = new Settings();
        private readonly List<EmuTab> tabs = new List<EmuTab>();
        private Theme theme;

        // (1) duong dan
        private TextBox txtEmu, txtGame, txtJava;
        // (2) cau hinh + luoi
        private NumericUpDown numXmx, numTabs, numDelay, numW, numH, numCols, numRows;
        private CheckBox chkSep, chkAuto;
        private GridPreview preview;
        // (4) bang tab
        private DataGridView grid;
        private Label lblSys, lblAll;
        // (5) dong bo
        private CheckBox chkSyncTab, chkSyncKey, chkDark;
        // (6) tien ich
        private CheckBox chkRestart, chkCleanAuto, chkGuard;
        private NumericUpDown numCleanMin, numGuard, numLoops;
        private Button btnRec, btnRecStop, btnPlay, btnPlayStop;
        private Label lblMacro;
        // (6) tien ich moi
        private CheckBox chkAutoStart, chkTop, chkTimer;
        private NumericUpDown numTimer;
        private Label lblTimer;

        private System.Windows.Forms.Timer uiTimer;
        private PerformanceCounter cpuCounter;
        private readonly List<Button> primaryButtons = new List<Button>();
        private readonly List<Button> dangerButtons = new List<Button>();
        private readonly List<CardPanel> cards = new List<CardPanel>();
        private KeypadForm keypad;

        private static readonly Font BtnFont = new Font("Segoe UI Emoji", 9f);

        public MainForm()
        {
            cfg.Load();
            Text = AppTitle;
            Font = new Font("Segoe UI", 9f);
            MinimumSize = new Size(1240, 760);
            Size = new Size(1300, 820);
            StartPosition = FormStartPosition.CenterScreen;
            KeyPreview = true;
            try { Icon = Icon.ExtractAssociatedIcon(Application.ExecutablePath); } catch (Exception) { }

            BuildUi();
            LoadSettingsToUi();
            ApplyTheme();

            try { cpuCounter = new PerformanceCounter("Processor", "% Processor Time", "_Total"); cpuCounter.NextValue(); }
            catch (Exception) { cpuCounter = null; }

            uiTimer = new System.Windows.Forms.Timer();
            uiTimer.Interval = 1000;
            uiTimer.Tick += delegate { RefreshGrid(); TickExtras(); };
            uiTimer.Start();

            FormClosing += HandleClosing;
            Shown += delegate { AutoStartIfWanted(); };
        }

        // =====================================================================  helpers tao control
        private Label MakeLabel(string text)
        {
            Label l = new Label();
            l.Text = text;
            l.AutoSize = true;
            l.Anchor = AnchorStyles.Left;
            l.Margin = new Padding(0, 7, 8, 3);
            return l;
        }

        private Button MakeButton(string text, EventHandler onClick, int width)
        {
            Button b = new Button();
            b.Text = text;
            b.Font = BtnFont;
            b.Size = new Size(width, 36);
            b.Margin = new Padding(0, 0, 8, 8);
            b.FlatStyle = FlatStyle.Flat;
            b.FlatAppearance.BorderSize = 1;
            b.Cursor = Cursors.Hand;
            b.UseVisualStyleBackColor = false;
            if (onClick != null) b.Click += onClick;
            return b;
        }

        private NumericUpDown MakeNum(int min, int max, int val, int width)
        {
            NumericUpDown n = new NumericUpDown();
            n.Minimum = min;
            n.Maximum = max;
            n.Value = val;
            n.Width = width;
            n.Margin = new Padding(0, 4, 14, 3);
            n.BorderStyle = BorderStyle.FixedSingle;
            return n;
        }

        private CheckBox MakeCheck(string text)
        {
            CheckBox c = new CheckBox();
            c.Text = text;
            c.AutoSize = true;
            c.Margin = new Padding(0, 6, 14, 3);
            return c;
        }

        private FlowLayoutPanel MakeRow()
        {
            FlowLayoutPanel f = new FlowLayoutPanel();
            f.AutoSize = true;
            f.AutoSizeMode = AutoSizeMode.GrowAndShrink;
            f.WrapContents = false;
            f.Margin = new Padding(0);
            return f;
        }

        private CardPanel MakeCard(int number, string title)
        {
            CardPanel c = new CardPanel(number, title);
            c.Dock = DockStyle.Fill;
            c.Margin = new Padding(0, 0, 0, 10);
            cards.Add(c);
            return c;
        }

        // =====================================================================  UI
        private void BuildUi()
        {
            TableLayoutPanel root = new TableLayoutPanel();
            root.Dock = DockStyle.Fill;
            root.ColumnCount = 2;
            root.RowCount = 1;
            root.Padding = new Padding(14, 14, 14, 4);
            root.ColumnStyles.Add(new ColumnStyle(SizeType.Percent, 100f));
            root.ColumnStyles.Add(new ColumnStyle(SizeType.Absolute, 316f));
            Controls.Add(root);

            // ------------------------------------------------ cot chinh: 1, 2, 3, 4
            TableLayoutPanel main = new TableLayoutPanel();
            main.Dock = DockStyle.Fill;
            main.ColumnCount = 1;
            main.RowCount = 4;
            main.Margin = new Padding(0, 0, 12, 0);
            main.RowStyles.Add(new RowStyle(SizeType.Absolute, 168f));
            main.RowStyles.Add(new RowStyle(SizeType.Absolute, 172f));
            main.RowStyles.Add(new RowStyle(SizeType.Absolute, 158f));
            main.RowStyles.Add(new RowStyle(SizeType.Percent, 100f));
            root.Controls.Add(main, 0, 0);

            main.Controls.Add(BuildCard1(), 0, 0);
            main.Controls.Add(BuildCard2(), 0, 1);
            main.Controls.Add(BuildCard3(), 0, 2);
            main.Controls.Add(BuildCard4(), 0, 3);

            // ------------------------------------------------ cot ben: 5, 6
            TableLayoutPanel side = new TableLayoutPanel();
            side.Dock = DockStyle.Fill;
            side.ColumnCount = 1;
            side.RowCount = 2;
            side.Margin = new Padding(0);
            side.RowStyles.Add(new RowStyle(SizeType.Absolute, 340f));
            side.RowStyles.Add(new RowStyle(SizeType.Percent, 100f));
            root.Controls.Add(side, 1, 0);
            side.Controls.Add(BuildCard5(), 0, 0);
            side.Controls.Add(BuildCard6(), 0, 1);
        }

        // ---------------- (1) Emulator, Game, Java
        private CardPanel BuildCard1()
        {
            CardPanel card = MakeCard(1, "Đường dẫn: Emulator · Game · Java");
            TableLayoutPanel t = new TableLayoutPanel();
            t.Dock = DockStyle.Fill;
            t.ColumnCount = 3;
            t.RowCount = 3;
            t.ColumnStyles.Add(new ColumnStyle(SizeType.Absolute, 80f));
            t.ColumnStyles.Add(new ColumnStyle(SizeType.Percent, 100f));
            t.ColumnStyles.Add(new ColumnStyle(SizeType.Absolute, 92f));
            for (int i = 0; i < 3; i++) t.RowStyles.Add(new RowStyle(SizeType.Percent, 33.3f));
            txtEmu = AddPathRow(t, 0, "Emulator", "Chọn Che779_Emulator.jar", "jar");
            txtGame = AddPathRow(t, 1, "Game", "Chọn file game .jar", "jar");
            txtJava = AddPathRow(t, 2, "Java", "Chọn javaw.exe / java.exe", "exe");
            card.Controls.Add(t);
            return card;
        }

        private TextBox AddPathRow(TableLayoutPanel t, int row, string label, string dialogTitle, string kind)
        {
            Label l = MakeLabel(label);
            l.Anchor = AnchorStyles.Left;
            TextBox tb = new TextBox();
            tb.Dock = DockStyle.Fill;
            tb.BorderStyle = BorderStyle.FixedSingle;
            tb.Margin = new Padding(0, 5, 8, 5);
            Button b = MakeButton("📂 Duyệt", null, 86);
            b.Margin = new Padding(0, 2, 0, 2);
            b.Height = 30;
            string title = dialogTitle;
            string k = kind;
            TextBox target = tb;
            b.Click += delegate
            {
                OpenFileDialog dlg = new OpenFileDialog();
                dlg.Title = title;
                dlg.Filter = k == "exe" ? "Java (*.exe)|*.exe|Tất cả|*.*" : "Java archive (*.jar)|*.jar|Tất cả|*.*";
                try { if (target.Text.Length > 0) dlg.InitialDirectory = Path.GetDirectoryName(target.Text); } catch (Exception) { }
                if (dlg.ShowDialog(this) == DialogResult.OK) target.Text = dlg.FileName;
            };
            t.Controls.Add(l, 0, row);
            t.Controls.Add(tb, 1, row);
            t.Controls.Add(b, 2, row);
            return tb;
        }

        // ---------------- (2) RAM, so tab, cho, o tab, luoi
        private CardPanel BuildCard2()
        {
            CardPanel card = MakeCard(2, "Cấu hình tab & lưới sắp xếp");
            TableLayoutPanel t = new TableLayoutPanel();
            t.Dock = DockStyle.Fill;
            t.ColumnCount = 2;
            t.RowCount = 1;
            t.ColumnStyles.Add(new ColumnStyle(SizeType.Percent, 100f));
            t.ColumnStyles.Add(new ColumnStyle(SizeType.Absolute, 250f));

            TableLayoutPanel left = new TableLayoutPanel();
            left.Dock = DockStyle.Fill;
            left.ColumnCount = 1;
            left.RowCount = 3;
            left.RowStyles.Add(new RowStyle(SizeType.Absolute, 36f));
            left.RowStyles.Add(new RowStyle(SizeType.Absolute, 36f));
            left.RowStyles.Add(new RowStyle(SizeType.Absolute, 36f));

            numXmx = MakeNum(16, 2048, 64, 66);
            numTabs = MakeNum(1, 64, 2, 54);
            numDelay = MakeNum(0, 60000, 1000, 72);
            numW = MakeNum(100, 3000, 300, 64);
            numH = MakeNum(100, 3000, 300, 64);
            FlowLayoutPanel r1 = MakeRow();
            r1.Dock = DockStyle.Fill;
            r1.Controls.Add(MakeLabel("RAM tối đa (MB)"));
            r1.Controls.Add(numXmx);
            r1.Controls.Add(MakeLabel("Số tab"));
            r1.Controls.Add(numTabs);
            r1.Controls.Add(MakeLabel("Chờ (ms)"));
            r1.Controls.Add(numDelay);
            r1.Controls.Add(MakeLabel("Ô tab"));
            r1.Controls.Add(numW);
            r1.Controls.Add(MakeLabel("×"));
            r1.Controls.Add(numH);

            numCols = MakeNum(1, 24, 4, 52);
            numRows = MakeNum(1, 24, 2, 52);
            numCols.ValueChanged += delegate { UpdatePreview(); };
            numRows.ValueChanged += delegate { UpdatePreview(); };
            FlowLayoutPanel r2 = MakeRow();
            r2.Dock = DockStyle.Fill;
            r2.Controls.Add(MakeLabel("Lưới: cột"));
            r2.Controls.Add(numCols);
            r2.Controls.Add(MakeLabel("hàng"));
            r2.Controls.Add(numRows);
            Button autoCalc = MakeButton("🧮 Tự tính", OnAutoGrid, 100);
            autoCalc.Margin = new Padding(0, 1, 8, 1);
            autoCalc.Height = 30;
            r2.Controls.Add(autoCalc);

            FlowLayoutPanel r3 = MakeRow();
            r3.Dock = DockStyle.Fill;
            chkAuto = MakeCheck("Tự sắp xếp khi mở tab");
            chkSep = MakeCheck("Tách dữ liệu từng tab");
            r3.Controls.Add(chkAuto);
            r3.Controls.Add(chkSep);

            left.Controls.Add(r1, 0, 0);
            left.Controls.Add(r2, 0, 1);
            left.Controls.Add(r3, 0, 2);

            preview = new GridPreview();
            preview.Dock = DockStyle.Fill;
            preview.Margin = new Padding(8, 0, 0, 0);

            t.Controls.Add(left, 0, 0);
            t.Controls.Add(preview, 1, 0);
            card.Controls.Add(t);
            return card;
        }

        // ---------------- (3) cac nut hanh dong
        private CardPanel BuildCard3()
        {
            CardPanel card = MakeCard(3, "Hành động");
            // luoi deu 2 hang x 6 cot: moi nut cung kich thuoc, khong bao gio bi vo/ cat
            TableLayoutPanel g = new TableLayoutPanel();
            g.Dock = DockStyle.Fill;
            g.ColumnCount = 6;
            g.RowCount = 2;
            for (int i = 0; i < 6; i++) g.ColumnStyles.Add(new ColumnStyle(SizeType.Percent, 100f / 6f));
            g.RowStyles.Add(new RowStyle(SizeType.Percent, 50f));
            g.RowStyles.Add(new RowStyle(SizeType.Percent, 50f));

            Button bStart = GridButton("▶️  Bắt đầu", OnStart);
            primaryButtons.Add(bStart);
            Button bStop = GridButton("⏹️  Dừng", OnStop);
            dangerButtons.Add(bStop);
            Button bAll = GridButton("❌  Đóng hết", OnCloseAll);
            dangerButtons.Add(bAll);

            g.Controls.Add(bStart, 0, 0);
            g.Controls.Add(bStop, 1, 0);
            g.Controls.Add(GridButton("🙈  Ẩn", OnHide), 2, 0);
            g.Controls.Add(GridButton("👁️  Hiện", OnShow), 3, 0);
            g.Controls.Add(GridButton("😴  Ngủ", OnSleep), 4, 0);
            g.Controls.Add(GridButton("☀️  Thức", OnWake), 5, 0);
            g.Controls.Add(GridButton("🧹  Clean RAM", OnCleanRam), 0, 1);
            g.Controls.Add(GridButton("🧩  Sắp xếp tab", OnArrange), 1, 1);
            g.Controls.Add(GridButton("🎮  Thêm game...", OnAddGame), 2, 1);
            g.Controls.Add(GridButton("➕  Thêm N tab", OnAddN), 3, 1);
            g.Controls.Add(GridButton("🗑️  Xóa dòng", OnRemoveRows), 4, 1);
            g.Controls.Add(bAll, 5, 1);
            card.Controls.Add(g);
            return card;
        }

        /// <summary>Nut lap day o luoi (Dock = Fill).</summary>
        private Button GridButton(string text, EventHandler onClick)
        {
            Button b = MakeButton(text, onClick, 100);
            b.Dock = DockStyle.Fill;
            b.Margin = new Padding(0, 0, 8, 8);
            return b;
        }

        // ---------------- (4) bang tab + trang thai he thong
        private CardPanel BuildCard4()
        {
            CardPanel card = MakeCard(4, "Danh sách tab & trạng thái hệ thống");
            grid = new DataGridView();
            grid.Dock = DockStyle.Fill;
            grid.AllowUserToAddRows = false;
            grid.AllowUserToDeleteRows = false;
            grid.AllowUserToResizeRows = false;
            grid.RowHeadersVisible = false;
            grid.SelectionMode = DataGridViewSelectionMode.FullRowSelect;
            grid.MultiSelect = true;
            grid.BorderStyle = BorderStyle.None;
            grid.CellBorderStyle = DataGridViewCellBorderStyle.SingleHorizontal;
            grid.EnableHeadersVisualStyles = false;
            grid.AutoSizeColumnsMode = DataGridViewAutoSizeColumnsMode.Fill;
            grid.RowTemplate.Height = 28;
            grid.ColumnHeadersHeight = 30;
            grid.ColumnHeadersHeightSizeMode = DataGridViewColumnHeadersHeightSizeMode.DisableResizing;

            DataGridViewCheckBoxColumn cChk = new DataGridViewCheckBoxColumn();
            cChk.HeaderText = "";
            cChk.FillWeight = 8;
            grid.Columns.Add(cChk);
            AddCol("Tab", 8);
            AddCol("Game", 28);
            AddCol("Trạng thái", 18);
            AddCol("Cổng", 10);
            AddCol("CPU", 10);
            AddCol("RAM", 12);
            AddCol("Ngủ", 9);
            AddCol("PID", 12);
            for (int i = 1; i < grid.Columns.Count; i++) grid.Columns[i].ReadOnly = true;
            grid.CurrentCellDirtyStateChanged += delegate
            {
                if (grid.IsCurrentCellDirty) grid.CommitEdit(DataGridViewDataErrorContexts.Commit);
            };

            Panel bottom = new Panel();
            bottom.Dock = DockStyle.Bottom;
            bottom.Height = 44;
            lblAll = new Label();
            lblAll.Dock = DockStyle.Bottom;
            lblAll.Height = 20;
            lblAll.Text = "Tất cả tab: ...";
            lblSys = new Label();
            lblSys.Dock = DockStyle.Bottom;
            lblSys.Height = 22;
            lblSys.Text = "Hệ thống: ...";
            bottom.Controls.Add(lblSys);
            bottom.Controls.Add(lblAll);

            card.Controls.Add(grid);
            card.Controls.Add(bottom);
            return card;
        }

        private void AddCol(string header, float weight)
        {
            DataGridViewTextBoxColumn c = new DataGridViewTextBoxColumn();
            c.HeaderText = header;
            c.FillWeight = weight;
            grid.Columns.Add(c);
        }

        // ---------------- (5) dong bo + ban phim
        private CardPanel BuildCard5()
        {
            CardPanel card = MakeCard(5, "Đồng bộ & bàn phím");
            FlowLayoutPanel f = new FlowLayoutPanel();
            f.Dock = DockStyle.Fill;
            f.FlowDirection = FlowDirection.TopDown;
            f.WrapContents = false;
            chkSyncTab = MakeCheck("Đồng bộ Tab");
            chkSyncKey = MakeCheck("Đồng bộ Phím");
            chkSyncMouse = MakeCheck("Đồng bộ Chuột (nhấp / kéo)");
            chkSyncMouse.CheckedChanged += OnSyncMouseChanged;
            chkDark = MakeCheck("Giao diện tối");
            chkDark.CheckedChanged += delegate { ApplyTheme(); };
            Button kb = MakeButton("⌨️ Mở bàn phím đầy đủ", OnToggleKeypad, 262);
            primaryButtons.Add(kb);
            kb.Margin = new Padding(0, 8, 0, 4);
            Label tip = new Label();
            tip.AutoSize = false;
            tip.Size = new Size(262, 44);
            tip.Text = "Phím thật khi bật Đồng bộ Phím: mũi tên, Enter=OK, F1/F2 = phím mềm, 0-9, A-Z, Space, Backspace.";
            tip.Tag = "muted";
            f.Controls.Add(chkSyncTab);
            f.Controls.Add(chkSyncKey);
            f.Controls.Add(chkSyncMouse);
            f.Controls.Add(chkDark);
            f.Controls.Add(kb);
            f.Controls.Add(tip);
            card.Controls.Add(f);
            return card;
        }

        // ---------------- (6) tien ich
        private Label Section(string text)
        {
            Label l = MakeLabel(text);
            l.Tag = "muted";
            l.Font = new Font("Segoe UI Semibold", 8.5f);
            l.Margin = new Padding(0, 10, 0, 4);
            return l;
        }

        private FlowLayoutPanel BtnRow(Button a, Button b)
        {
            FlowLayoutPanel r = MakeRow();
            a.Width = 128;
            b.Width = 128;
            a.Margin = new Padding(0, 0, 6, 6);
            b.Margin = new Padding(0, 0, 0, 6);
            r.Controls.Add(a);
            r.Controls.Add(b);
            return r;
        }

        private CardPanel BuildCard6()
        {
            CardPanel card = MakeCard(6, "Tiện ích");
            FlowLayoutPanel f = new FlowLayoutPanel();
            f.Dock = DockStyle.Fill;
            f.FlowDirection = FlowDirection.TopDown;
            f.WrapContents = false;
            f.AutoScroll = true;

            // ---- Ho so & du lieu
            f.Controls.Add(Section("HỒ SƠ & DỮ LIỆU"));
            f.Controls.Add(BtnRow(MakeButton("💾 Lưu hồ sơ", OnSaveProfile, 128), MakeButton("📂 Nạp hồ sơ", OnLoadProfile, 128)));
            f.Controls.Add(BtnRow(MakeButton("📁 Thư mục dữ liệu", OnOpenDataDir, 128), MakeButton("🗑️ Xóa dữ liệu", OnWipeData, 128)));

            // ---- Tu dong
            f.Controls.Add(Section("TỰ ĐỘNG"));
            chkRestart = MakeCheck("♻️ Tự mở lại tab khi bị tắt/crash");
            chkRestart.CheckedChanged += delegate { autoRestart = chkRestart.Checked; };
            f.Controls.Add(chkRestart);

            chkAutoStart = MakeCheck("🚀 Tự bắt đầu khi mở Remoter");
            f.Controls.Add(chkAutoStart);

            numCleanMin = MakeNum(1, 600, 15, 56);
            chkCleanAuto = MakeCheck("🧹 Tự Clean RAM mỗi");
            chkCleanAuto.Margin = new Padding(0, 6, 4, 3);
            FlowLayoutPanel clean = MakeRow();
            clean.Controls.Add(chkCleanAuto);
            clean.Controls.Add(numCleanMin);
            clean.Controls.Add(MakeLabel("phút"));
            f.Controls.Add(clean);

            numGuard = MakeNum(50, 99, 90, 52);
            chkGuard = MakeCheck("🛡️ Tự cho ngủ khi RAM ≥");
            chkGuard.Margin = new Padding(0, 6, 4, 3);
            FlowLayoutPanel guard = MakeRow();
            guard.Controls.Add(chkGuard);
            guard.Controls.Add(numGuard);
            guard.Controls.Add(MakeLabel("%"));
            f.Controls.Add(guard);

            numTimer = MakeNum(1, 1440, 60, 56);
            chkTimer = MakeCheck("⏰ Hẹn giờ đóng hết sau");
            chkTimer.Margin = new Padding(0, 6, 4, 3);
            chkTimer.CheckedChanged += delegate { OnTimerToggle(); };
            FlowLayoutPanel tmr = MakeRow();
            tmr.Controls.Add(chkTimer);
            tmr.Controls.Add(numTimer);
            tmr.Controls.Add(MakeLabel("phút"));
            f.Controls.Add(tmr);
            lblTimer = new Label();
            lblTimer.AutoSize = false;
            lblTimer.Size = new Size(262, 18);
            lblTimer.Tag = "muted";
            lblTimer.Text = "";
            f.Controls.Add(lblTimer);

            chkTop = MakeCheck("📌 Remoter luôn nằm trên cùng");
            chkTop.CheckedChanged += delegate { TopMost = chkTop.Checked; };
            f.Controls.Add(chkTop);

            // ---- Cong cu tab
            f.Controls.Add(Section("CÔNG CỤ TAB (theo quy tắc Đồng bộ Tab)"));
            f.Controls.Add(BtnRow(MakeButton("🔄 Mở lại tab", OnRestartTabs, 128), MakeButton("📸 Chụp màn hình", OnScreenshot, 128)));
            f.Controls.Add(BtnRow(MakeButton("📋 Sao chép DS tab", OnCopyList, 128), MakeButton("ℹ️ Phiên bản", OnShowVersions, 128)));

            // ---- Macro
            Label macroTitle = Section("MACRO PHÍM (ghi → phát lặp)");

            btnRec = MakeButton("⏺️ Ghi", OnMacroRec, 90);
            btnRecStop = MakeButton("⏹️ Dừng ghi", OnMacroRecStop, 112);
            btnPlay = MakeButton("▶️ Phát", OnMacroPlay, 90);
            btnPlayStop = MakeButton("⏹️ Dừng phát", OnMacroPlayStop, 112);
            FlowLayoutPanel m1 = MakeRow();
            btnRec.Width = 128; btnRecStop.Width = 128;
            btnRec.Margin = new Padding(0, 0, 6, 6); btnRecStop.Margin = new Padding(0, 0, 0, 6);
            m1.Controls.Add(btnRec);
            m1.Controls.Add(btnRecStop);
            FlowLayoutPanel m2 = MakeRow();
            btnPlay.Width = 128; btnPlayStop.Width = 128;
            btnPlay.Margin = new Padding(0, 0, 6, 6); btnPlayStop.Margin = new Padding(0, 0, 0, 6);
            m2.Controls.Add(btnPlay);
            m2.Controls.Add(btnPlayStop);

            numLoops = MakeNum(0, 99999, 0, 62);
            FlowLayoutPanel m3 = MakeRow();
            m3.Controls.Add(MakeLabel("Lặp"));
            m3.Controls.Add(numLoops);
            m3.Controls.Add(MakeLabel("lần (0 = mãi mãi)"));

            lblMacro = new Label();
            lblMacro.AutoSize = false;
            lblMacro.Size = new Size(262, 20);
            lblMacro.Text = "Macro: trống";
            lblMacro.Tag = "muted";

            f.Controls.Add(macroTitle);
            f.Controls.Add(m1);
            f.Controls.Add(m2);
            f.Controls.Add(m3);
            f.Controls.Add(lblMacro);

            Label ver = new Label();
            ver.AutoSize = false;
            ver.Size = new Size(262, 34);
            ver.Tag = "muted";
            ver.Margin = new Padding(0, 10, 0, 0);
            ver.Text = "Che779 Remoter " + AppInfo.Version + "\nDữ liệu: %APPDATA%\\Che779";
            f.Controls.Add(ver);

            card.Controls.Add(f);
            return card;
        }

        // =====================================================================  theme
        private void ApplyTheme()
        {
            theme = chkDark != null && chkDark.Checked ? Theme.Dark() : Theme.Light();
            BackColor = theme.Bg;
            ForeColor = theme.Text;
            StyleTree(this, theme.Bg);

            grid.BackgroundColor = theme.Card;
            grid.GridColor = theme.Border;
            grid.DefaultCellStyle.BackColor = theme.Card;
            grid.DefaultCellStyle.ForeColor = theme.Text;
            grid.DefaultCellStyle.SelectionBackColor = theme.Accent;
            grid.DefaultCellStyle.SelectionForeColor = theme.AccentText;
            grid.AlternatingRowsDefaultCellStyle.BackColor = theme.GridAlt;
            grid.AlternatingRowsDefaultCellStyle.SelectionBackColor = theme.Accent;
            grid.AlternatingRowsDefaultCellStyle.SelectionForeColor = theme.AccentText;
            grid.ColumnHeadersDefaultCellStyle.BackColor = theme.Card;
            grid.ColumnHeadersDefaultCellStyle.ForeColor = theme.Muted;
            grid.ColumnHeadersDefaultCellStyle.SelectionBackColor = theme.Card;
            grid.ColumnHeadersDefaultCellStyle.SelectionForeColor = theme.Muted;
            grid.ColumnHeadersBorderStyle = DataGridViewHeaderBorderStyle.None;

            preview.BackFill = theme.Card;
            preview.EmptyColor = theme.GridAlt;
            preview.StoppedColor = theme.Border;
            preview.RunColor = theme.Accent;
            preview.TextColor = theme.Muted;
            preview.RunTextColor = theme.AccentText;

            if (keypad != null) StyleExternal(keypad);
            Invalidate(true);
        }

        /// <summary>Ap theme hien tai cho cua so ngoai (ban phim).</summary>
        internal void StyleExternal(Form f)
        {
            if (theme == null) return;
            f.BackColor = theme.Card;
            f.ForeColor = theme.Text;
            StyleTree(f, theme.Card);
        }

        private void StyleTree(Control c, Color bg)
        {
            foreach (Control ch in c.Controls)
            {
                if (ch is CardPanel)
                {
                    CardPanel cp = (CardPanel)ch;
                    cp.OuterColor = theme.Bg;
                    cp.CardColor = theme.Card;
                    cp.BorderColor = theme.Border;
                    cp.BadgeColor = theme.Accent;
                    cp.BadgeTextColor = theme.AccentText;
                    cp.TitleColor = theme.Text;
                    cp.BackColor = theme.Card;
                    cp.Invalidate();
                    StyleTree(cp, theme.Card);
                    continue;
                }
                if (ch is Button)
                {
                    Button b = (Button)ch;
                    bool primary = primaryButtons.Contains(b);
                    bool danger = dangerButtons.Contains(b);
                    b.BackColor = primary ? theme.Accent : theme.Field;
                    b.ForeColor = primary ? theme.AccentText : (danger ? theme.Danger : theme.Text);
                    b.FlatAppearance.BorderColor = primary ? theme.Accent : theme.Border;
                    b.FlatAppearance.MouseOverBackColor = primary ? theme.Accent : theme.GridAlt;
                    b.FlatAppearance.MouseDownBackColor = theme.Border;
                }
                else if (ch is TextBox || ch is NumericUpDown)
                {
                    ch.BackColor = theme.Field;
                    ch.ForeColor = theme.Text;
                    continue; // khong de quy vao control con ben trong
                }
                else if (ch is Label)
                {
                    ch.ForeColor = "muted".Equals(ch.Tag) ? theme.Muted : theme.Text;
                    ch.BackColor = bg;
                }
                else if (ch is CheckBox)
                {
                    ch.ForeColor = theme.Text;
                    ch.BackColor = bg;
                }
                else if (ch is DataGridView || ch is GridPreview)
                {
                    continue;
                }
                else
                {
                    ch.BackColor = bg;
                    ch.ForeColor = theme.Text;
                }
                StyleTree(ch, bg);
            }
        }

        // =====================================================================  settings
        private void LoadSettingsToUi()
        {
            string baseDir = AppDomain.CurrentDomain.BaseDirectory;
            string emu = cfg.Get("emu", "");
            if (emu.Length == 0 || !File.Exists(emu))
            {
                string guess = Path.Combine(baseDir, "Che779_Emulator.jar");
                if (File.Exists(guess)) emu = guess;
            }
            txtEmu.Text = emu;
            txtGame.Text = cfg.Get("game", "");
            string java = cfg.Get("java", "");
            if (java.Length == 0 || !File.Exists(java)) java = DetectJava();
            txtJava.Text = java;
            numXmx.Value = Clamp(cfg.GetInt("xmx", 64), numXmx);
            numTabs.Value = Clamp(cfg.GetInt("tabs", 2), numTabs);
            numDelay.Value = Clamp(cfg.GetInt("delay", 1000), numDelay);
            numW.Value = Clamp(cfg.GetInt("w", 300), numW);
            numH.Value = Clamp(cfg.GetInt("h", 300), numH);
            numCols.Value = Clamp(cfg.GetInt("cols", 4), numCols);
            numRows.Value = Clamp(cfg.GetInt("rows", 2), numRows);
            chkSep.Checked = cfg.GetBool("sep", false);
            chkAuto.Checked = cfg.GetBool("auto", true);
            chkSyncTab.Checked = cfg.GetBool("synctab", true);
            chkSyncKey.Checked = cfg.GetBool("synckey", true);
            chkDark.Checked = cfg.GetBool("dark", true);
            chkRestart.Checked = cfg.GetBool("restart", false);
            autoRestart = chkRestart.Checked;
            chkCleanAuto.Checked = cfg.GetBool("cleanauto", false);
            numCleanMin.Value = Clamp(cfg.GetInt("cleanmin", 15), numCleanMin);
            chkGuard.Checked = cfg.GetBool("guard", false);
            numGuard.Value = Clamp(cfg.GetInt("guardpct", 90), numGuard);
            numLoops.Value = Clamp(cfg.GetInt("loops", 0), numLoops);
            chkAutoStart.Checked = cfg.GetBool("autostart", false);
            chkTop.Checked = cfg.GetBool("top", false);
            numTimer.Value = Clamp(cfg.GetInt("timermin", 60), numTimer);
            LoadSyncExtras();
            LoadMacroFromCfg();
            LoadTabsFromCfg();
            UpdatePreview();
        }

        private static decimal Clamp(int v, NumericUpDown n)
        {
            if (v < n.Minimum) return n.Minimum;
            if (v > n.Maximum) return n.Maximum;
            return v;
        }

        private void SaveSettings()
        {
            cfg.Set("emu", txtEmu.Text);
            cfg.Set("game", txtGame.Text);
            cfg.Set("java", txtJava.Text);
            cfg.SetInt("xmx", (int)numXmx.Value);
            cfg.SetInt("tabs", (int)numTabs.Value);
            cfg.SetInt("delay", (int)numDelay.Value);
            cfg.SetInt("w", (int)numW.Value);
            cfg.SetInt("h", (int)numH.Value);
            cfg.SetInt("cols", (int)numCols.Value);
            cfg.SetInt("rows", (int)numRows.Value);
            cfg.SetBool("sep", chkSep.Checked);
            cfg.SetBool("auto", chkAuto.Checked);
            cfg.SetBool("synctab", chkSyncTab.Checked);
            cfg.SetBool("synckey", chkSyncKey.Checked);
            cfg.SetBool("dark", chkDark.Checked);
            cfg.SetBool("restart", chkRestart.Checked);
            cfg.SetBool("cleanauto", chkCleanAuto.Checked);
            cfg.SetInt("cleanmin", (int)numCleanMin.Value);
            cfg.SetBool("guard", chkGuard.Checked);
            cfg.SetInt("guardpct", (int)numGuard.Value);
            cfg.SetInt("loops", (int)numLoops.Value);
            cfg.SetBool("autostart", chkAutoStart.Checked);
            cfg.SetBool("top", chkTop.Checked);
            cfg.SetInt("timermin", (int)numTimer.Value);
            SaveSyncExtras();
            SaveMacroToCfg();
            SaveTabsToCfg();
            cfg.Save();
        }

        private static string DetectJava()
        {
            List<string> roots = new List<string>();
            string jh = Environment.GetEnvironmentVariable("JAVA_HOME");
            if (!string.IsNullOrEmpty(jh)) roots.Add(jh);
            foreach (string pf in new string[] {
                Environment.GetEnvironmentVariable("ProgramFiles(x86)"),
                Environment.GetEnvironmentVariable("ProgramFiles") })
            {
                if (string.IsNullOrEmpty(pf)) continue;
                try
                {
                    string j = Path.Combine(pf, "Java");
                    if (Directory.Exists(j)) roots.AddRange(Directory.GetDirectories(j));
                }
                catch (Exception) { }
            }
            foreach (string r in roots)
            {
                string p = Path.Combine(Path.Combine(r, "bin"), "javaw.exe");
                if (File.Exists(p)) return p;
            }
            return "javaw.exe"; // dua vao PATH
        }

        private static string B64(string s)
        {
            return Convert.ToBase64String(Encoding.UTF8.GetBytes(s ?? ""));
        }

        private static string UnB64(string s)
        {
            try { return Encoding.UTF8.GetString(Convert.FromBase64String(s ?? "")); }
            catch (Exception) { return ""; }
        }
    }
}
