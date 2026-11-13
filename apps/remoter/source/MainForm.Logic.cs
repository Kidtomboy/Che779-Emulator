using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Drawing;
using System.IO;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading;
using System.Windows.Forms;

namespace Che779.Remoter
{
    internal class MacroStep
    {
        public int Delay;
        public bool IsText;
        public string Value;
    }

    internal class ArrangeCfg
    {
        public int Cols, Rows, W, H;
        public bool Auto;
    }

    internal class LaunchSpec
    {
        public string Java, Emu;
        public int Xmx;
        public bool Sep;
        public ArrangeCfg Arr;
    }

    internal partial class MainForm
    {
        private volatile bool autoRestart;
        private LaunchSpec lastSpec;
        private DateTime lastClean = DateTime.UtcNow;
        private DateTime lastGuard = DateTime.MinValue;

        // macro
        private readonly List<MacroStep> macro = new List<MacroStep>();
        private bool recording;
        private Stopwatch recWatch = new Stopwatch();
        private volatile bool macroPlaying;

        // =====================================================================  chon dong muc tieu
        private List<EmuTab> Targets()
        {
            if (chkSyncTab.Checked) return new List<EmuTab>(tabs);
            List<EmuTab> res = new List<EmuTab>();
            foreach (DataGridViewRow row in grid.Rows)
            {
                EmuTab t = row.Tag as EmuTab;
                if (t != null && IsChecked(row)) res.Add(t);
            }
            if (res.Count == 0)
            {
                foreach (DataGridViewRow row in grid.SelectedRows)
                {
                    EmuTab t = row.Tag as EmuTab;
                    if (t != null) res.Add(t);
                }
            }
            if (res.Count == 0) res.AddRange(tabs);
            return res;
        }

        private static bool IsChecked(DataGridViewRow row)
        {
            object v = row.Cells[0].Value;
            return v is bool && (bool)v;
        }

        private static List<EmuTab> RunningOf(List<EmuTab> list)
        {
            List<EmuTab> res = new List<EmuTab>();
            foreach (EmuTab t in list) if (t.Running) res.Add(t);
            return res;
        }

        // =====================================================================  them / xoa dong
        private EmuTab AddTab(string game)
        {
            EmuTab t = new EmuTab();
            t.Index = tabs.Count + 1;
            t.GamePath = game;
            tabs.Add(t);
            int r = grid.Rows.Add();
            DataGridViewRow row = grid.Rows[r];
            row.Tag = t;
            row.Cells[0].Value = true;
            UpdateRow(row, t);
            UpdatePreview();
            return t;
        }

        private void OnAddN(object s, EventArgs e)
        {
            if (txtGame.Text.Trim().Length == 0)
            {
                MessageBox.Show(this, "Hãy chọn file game .jar trước.", AppTitle);
                return;
            }
            int n = (int)numTabs.Value;
            for (int i = 0; i < n; i++) AddTab(txtGame.Text.Trim());
        }

        private void OnAddGame(object s, EventArgs e)
        {
            OpenFileDialog dlg = new OpenFileDialog();
            dlg.Title = "Thêm game (.jar)";
            dlg.Filter = "Java archive (*.jar)|*.jar";
            dlg.Multiselect = true;
            if (dlg.ShowDialog(this) != DialogResult.OK) return;
            foreach (string f in dlg.FileNames) AddTab(f);
            if (txtGame.Text.Trim().Length == 0) txtGame.Text = dlg.FileNames[0];
        }

        private void OnRemoveRows(object s, EventArgs e)
        {
            List<DataGridViewRow> del = new List<DataGridViewRow>();
            foreach (DataGridViewRow row in grid.SelectedRows) del.Add(row);
            foreach (DataGridViewRow row in del)
            {
                EmuTab t = row.Tag as EmuTab;
                if (t != null && t.Running) continue; // khong xoa tab dang chay
                if (t != null) tabs.Remove(t);
                grid.Rows.Remove(row);
            }
            for (int i = 0; i < tabs.Count; i++) tabs[i].Index = i + 1;
            UpdatePreview();
        }

        // =====================================================================  luoi / sap xep
        private ArrangeCfg ReadArrange()
        {
            ArrangeCfg a = new ArrangeCfg();
            a.Cols = (int)numCols.Value;
            a.Rows = (int)numRows.Value;
            a.W = (int)numW.Value;
            a.H = (int)numH.Value;
            a.Auto = chkAuto.Checked;
            return a;
        }

        private void OnAutoGrid(object s, EventArgs e)
        {
            Rectangle area = Screen.PrimaryScreen.WorkingArea;
            int w = Math.Max(1, (int)numW.Value);
            int h = Math.Max(1, (int)numH.Value);
            numCols.Value = Clamp(Math.Max(1, area.Width / w), numCols);
            numRows.Value = Clamp(Math.Max(1, area.Height / h), numRows);
            UpdatePreview();
        }

        private static Point SlotPos(ArrangeCfg a, int slot)
        {
            Rectangle area = Screen.PrimaryScreen.WorkingArea;
            int per = Math.Max(1, a.Cols * a.Rows);
            int wrap = slot / per;
            int s = slot % per;
            int c = s % Math.Max(1, a.Cols);
            int r = s / Math.Max(1, a.Cols);
            return new Point(area.Left + c * a.W + wrap * 24, area.Top + r * a.H + wrap * 24);
        }

        /// <summary>Cho cua so cua tab xuat hien roi dat vao o luoi cua no (dat 2 lan de thang ca vi tri app tu dat).</summary>
        private void PlaceWindow(EmuTab t, ArrangeCfg a, bool wait)
        {
            int tries = wait ? 60 : 1;
            for (int i = 0; i < tries; i++)
            {
                try
                {
                    if (!t.Running) return;
                    t.Proc.Refresh();
                    IntPtr h = t.Proc.MainWindowHandle;
                    if (h != IntPtr.Zero)
                    {
                        Point p = SlotPos(a, t.Index - 1);
                        Native.MoveWindow(h, p.X, p.Y, a.W, a.H, true);
                        if (wait)
                        {
                            Thread.Sleep(1500);
                            t.Proc.Refresh();
                            h = t.Proc.MainWindowHandle;
                            if (h != IntPtr.Zero) Native.MoveWindow(h, p.X, p.Y, a.W, a.H, true);
                        }
                        return;
                    }
                }
                catch (Exception) { }
                if (wait) Thread.Sleep(250);
            }
        }

        private void OnArrange(object s, EventArgs e)
        {
            ArrangeCfg a = ReadArrange();
            foreach (EmuTab t in RunningOf(tabs))
            {
                t.HasSaved = false;
                EmuTab tab = t;
                ThreadPool.QueueUserWorkItem(delegate { PlaceWindow(tab, a, false); });
            }
        }

        private void UpdatePreview()
        {
            if (preview == null) return;
            int cols = (int)numCols.Value;
            int rows = (int)numRows.Value;
            int n = cols * rows;
            int[] st = new int[n];
            for (int i = 0; i < tabs.Count; i++)
            {
                int slot = i % Math.Max(1, n);
                int v = tabs[i].Running ? 2 : 1;
                if (v > st[slot]) st[slot] = v;
            }
            preview.Cols = cols;
            preview.Rows = rows;
            preview.States = st;
            preview.Invalidate();
        }

        // =====================================================================  start / stop
        private int PickPort()
        {
            Dictionary<int, bool> used = new Dictionary<int, bool>();
            foreach (EmuTab t in tabs) if (t.Port > 0 && t.Running) used[t.Port] = true;
            for (int p = BasePort; p < BasePort + 300; p++)
            {
                if (used.ContainsKey(p)) continue;
                if (IsPortFree(p)) return p;
            }
            return BasePort;
        }

        private static bool IsPortFree(int port)
        {
            try
            {
                TcpListener l = new TcpListener(IPAddress.Loopback, port);
                l.Start();
                l.Stop();
                return true;
            }
            catch (Exception) { return false; }
        }

        private void OnStart(object s, EventArgs e)
        {
            string emu = txtEmu.Text.Trim();
            string java = txtJava.Text.Trim();
            if (!File.Exists(emu))
            {
                MessageBox.Show(this, "Chưa chọn đúng file Che779_Emulator.jar.", AppTitle);
                return;
            }
            if (tabs.Count == 0)
            {
                if (txtGame.Text.Trim().Length == 0)
                {
                    MessageBox.Show(this, "Hãy chọn file game .jar rồi bấm Bắt đầu.", AppTitle);
                    return;
                }
                for (int i = 0; i < (int)numTabs.Value; i++) AddTab(txtGame.Text.Trim());
            }
            SaveSettings();

            LaunchSpec spec = new LaunchSpec();
            spec.Java = java;
            spec.Emu = emu;
            spec.Xmx = (int)numXmx.Value;
            spec.Sep = chkSep.Checked;
            spec.Arr = ReadArrange();
            lastSpec = spec;
            int delay = (int)numDelay.Value;

            List<EmuTab> todo = new List<EmuTab>();
            foreach (EmuTab t in Targets())
            {
                if (t.Running) continue;
                if (t.GamePath == null || !File.Exists(t.GamePath))
                {
                    t.Status = "Thiếu file game";
                    continue;
                }
                t.StopRequested = false;
                t.Restarts = 0;
                t.Port = PickPort();
                t.Status = "Đang mở...";
                todo.Add(t);
            }
            ThreadPool.QueueUserWorkItem(delegate
            {
                foreach (EmuTab t in todo)
                {
                    LaunchTab(t, spec);
                    if (delay > 0) Thread.Sleep(delay);
                }
            });
        }

        private void LaunchTab(EmuTab t, LaunchSpec spec)
        {
            try
            {
                if (!IsPortFree(t.Port)) t.Port = PickPort();

                string args = "-Xmx" + spec.Xmx + "m -Xms8m -XX:+UseSerialGC -Dche779.port=" + t.Port;
                // du lieu emulator nam trong %APPDATA%\\Che779\\Che779-Emulator (tach rieng theo tab neu bat)
                string dataDir = spec.Sep
                    ? Path.Combine(AppInfo.EmulatorDir, "tab" + t.Index)
                    : AppInfo.EmulatorDir;
                AppInfo.Ensure(dataDir);
                args += " \"-Dche779.data=" + dataDir + "\"";
                args += " -jar \"" + spec.Emu + "\" \"" + t.GamePath + "\"";

                ProcessStartInfo psi = new ProcessStartInfo(spec.Java, args);
                psi.UseShellExecute = false;
                psi.CreateNoWindow = true;
                psi.RedirectStandardOutput = true;
                psi.RedirectStandardError = true;
                psi.WorkingDirectory = Path.GetDirectoryName(spec.Emu);

                Process p = new Process();
                p.StartInfo = psi;
                p.EnableRaisingEvents = true;
                EmuTab tab = t;
                p.OutputDataReceived += delegate(object sender, DataReceivedEventArgs ev)
                {
                    if (ev.Data == null) return;
                    Match m = Regex.Match(ev.Data, @"CHE779_PORT=(\d+)");
                    if (m.Success)
                    {
                        int port = int.Parse(m.Groups[1].Value);
                        tab.Port = port;
                        if (tab.Ipc != null) { tab.Ipc.Port = port; tab.Ipc.Close(); }
                    }
                };
                p.ErrorDataReceived += delegate { };
                p.Exited += delegate
                {
                    tab.Status = "Đã dừng";
                    tab.Sleeping = false;
                    tab.Hidden = false;
                    tab.Cpu = 0;
                    tab.RamMb = 0;
                    if (tab.Ipc != null) tab.Ipc.Close();
                    if (autoRestart && !tab.StopRequested && tab.Restarts < 5 && lastSpec != null)
                    {
                        tab.Restarts++;
                        tab.Status = "Mở lại (" + tab.Restarts + "/5)...";
                        ThreadPool.QueueUserWorkItem(delegate
                        {
                            Thread.Sleep(3000);
                            if (!tab.Running && !tab.StopRequested && autoRestart) LaunchTab(tab, lastSpec);
                        });
                    }
                };
                p.Start();
                p.BeginOutputReadLine();
                p.BeginErrorReadLine();
                t.Proc = p;
                t.Ipc = new IpcClient(t.Port);
                t.LastCpu = TimeSpan.Zero;
                t.LastSample = DateTime.MinValue;
                t.Sleeping = false;
                t.Hidden = false;
                t.Status = "Đang chạy";
                if (spec.Arr != null && spec.Arr.Auto) PlaceWindow(t, spec.Arr, true);
            }
            catch (Exception ex)
            {
                t.Status = "Lỗi: " + ex.Message;
            }
        }

        private void OnStop(object s, EventArgs e)
        {
            List<EmuTab> list = RunningOf(Targets());
            ThreadPool.QueueUserWorkItem(delegate { foreach (EmuTab t in list) StopTab(t); });
        }

        private static void StopTab(EmuTab t)
        {
            try
            {
                t.StopRequested = true;
                t.Status = "Đang dừng...";
                if (t.Ipc != null) t.Ipc.Send("{\"action\":\"quit\"}");
                if (t.Proc != null && !t.Proc.WaitForExit(2500))
                {
                    try { t.Proc.Kill(); } catch (Exception) { }
                }
            }
            catch (Exception) { }
        }

        private void OnCloseAll(object s, EventArgs e)
        {
            List<EmuTab> list = RunningOf(tabs);
            ThreadPool.QueueUserWorkItem(delegate { foreach (EmuTab t in list) StopTab(t); });
        }

        // =====================================================================  an / hien / ngu / clean ram
        private static void Broadcast(List<EmuTab> list, string json)
        {
            foreach (EmuTab t in RunningOf(list))
            {
                EmuTab tab = t;
                ThreadPool.QueueUserWorkItem(delegate
                {
                    if (tab.Ipc != null) tab.Ipc.Send(json);
                });
            }
        }

        private void OnHide(object s, EventArgs e)
        {
            List<EmuTab> l = Targets();
            // ghi nho vi tri/kich thuoc that cua tung cua so TRUOC khi an
            foreach (EmuTab t in RunningOf(l))
            {
                if (t.Hidden) continue;
                try
                {
                    IntPtr h = Native.FindMainWindow(t.Pid, true);
                    Native.RECT r;
                    if (h != IntPtr.Zero && Native.GetWindowRect(h, out r) && r.Right > r.Left)
                    {
                        t.SavedX = r.Left; t.SavedY = r.Top;
                        t.SavedW = r.Right - r.Left; t.SavedH = r.Bottom - r.Top;
                        t.HasSaved = true;
                    }
                }
                catch (Exception) { }
            }
            Broadcast(l, "{\"action\":\"hide\"}");
            foreach (EmuTab t in l) t.Hidden = true;
        }

        private volatile bool showing;

        /// <summary>Hien tung tab mot theo thu tu: hien -> tra ve dung cho cu -> cho -> tab ke tiep.</summary>
        private void OnShow(object s, EventArgs e)
        {
            if (showing) return;
            List<EmuTab> l = Targets();
            l.Sort(delegate (EmuTab x, EmuTab y) { return x.Index.CompareTo(y.Index); });
            ArrangeCfg a = ReadArrange();
            int gap = 350;
            showing = true;
            ThreadPool.QueueUserWorkItem(delegate
            {
                try
                {
                    foreach (EmuTab t in l)
                    {
                        if (!t.Running) { t.Hidden = false; continue; }
                        if (t.Ipc != null) t.Ipc.Send("{\"action\":\"show\"}");
                        t.Hidden = false;
                        RestoreWindow(t, a);
                        Thread.Sleep(gap);
                    }
                }
                finally { showing = false; }
            });
        }

        private void RestoreWindow(EmuTab t, ArrangeCfg a)
        {
            for (int i = 0; i < 24; i++)
            {
                try
                {
                    if (!t.Running) return;
                    IntPtr h = Native.FindMainWindow(t.Pid, true);
                    if (h != IntPtr.Zero)
                    {
                        int x, y, w, hh;
                        if (t.HasSaved) { x = t.SavedX; y = t.SavedY; w = t.SavedW; hh = t.SavedH; }
                        else { Point p = SlotPos(a, t.Index - 1); x = p.X; y = p.Y; w = a.W; hh = a.H; }
                        Native.MoveWindow(h, x, y, w, hh, true);
                        Thread.Sleep(400);
                        Native.MoveWindow(h, x, y, w, hh, true);   // lan 2: thang vi tri app tu dat
                        return;
                    }
                }
                catch (Exception) { }
                Thread.Sleep(250);
            }
        }

        private void OnSleep(object s, EventArgs e)
        {
            SleepTabs(Targets(), true);
        }

        private void OnWake(object s, EventArgs e)
        {
            SleepTabs(Targets(), false);
        }

        private static void SleepTabs(List<EmuTab> l, bool on)
        {
            Broadcast(l, "{\"action\":\"sleep\",\"state\":\"" + (on ? "on" : "off") + "\"}");
            foreach (EmuTab t in l) if (t.Running) t.Sleeping = on;
        }

        private void OnCleanRam(object s, EventArgs e)
        {
            CleanRamTabs(RunningOf(Targets()));
        }

        private static void CleanRamTabs(List<EmuTab> l)
        {
            Broadcast(l, "{\"action\":\"clean_ram\"}");
            ThreadPool.QueueUserWorkItem(delegate
            {
                Thread.Sleep(900); // de emulator kip GC truoc khi thu working set
                foreach (EmuTab t in l)
                {
                    try { if (t.Running) Native.EmptyWorkingSet(t.Proc.Handle); } catch (Exception) { }
                }
                try { Native.EmptyWorkingSet(Process.GetCurrentProcess().Handle); } catch (Exception) { }
            });
        }

        // =====================================================================  gui phim / chu
        private static string JsonEscape(string s)
        {
            StringBuilder sb = new StringBuilder();
            foreach (char c in s)
            {
                if (c == '\\') sb.Append("\\\\");
                else if (c == '"') sb.Append("\\\"");
                else if (c == '\n') sb.Append("\\n");
                else if (c == '\t') sb.Append("\\t");
                else if (c == '\r') { }
                else sb.Append(c);
            }
            return sb.ToString();
        }

        private static void SendJson(List<EmuTab> list, string json)
        {
            foreach (EmuTab t in list)
            {
                EmuTab tab = t;
                ThreadPool.QueueUserWorkItem(delegate
                {
                    if (tab.Ipc != null) tab.Ipc.Send(json);
                });
            }
        }

        private static string KeyJson(string key)
        {
            return "{\"action\":\"keypress\",\"key\":\"" + JsonEscape(key) + "\"}";
        }

        private static string TextJson(string text)
        {
            return "{\"action\":\"text\",\"text\":\"" + JsonEscape(text) + "\"}";
        }

        /// <summary>Phim bam tren ban phim ao: gui toi cac tab theo quy tac dong bo tab.</summary>
        internal void SendKeyToTargets(string key)
        {
            SendJson(RunningOf(Targets()), KeyJson(key));
            Record(false, key);
        }

        /// <summary>Gui ca doan chu toi cac tab.</summary>
        internal void SendText(string text)
        {
            if (string.IsNullOrEmpty(text)) return;
            SendJson(RunningOf(Targets()), TextJson(text));
            Record(true, text);
        }

        internal bool SyncKeyOn
        {
            get { return chkSyncKey != null && chkSyncKey.Checked; }
        }

        /// <summary>Phim that tren may tinh (khi bat Dong bo Phim): gui toi TAT CA tab dang chay.</summary>
        internal bool TryPhysicalKey(Keys keyData)
        {
            string name = MapKey(keyData);
            if (name == null) return false;
            SendJson(RunningOf(tabs), KeyJson(name));
            Record(false, name);
            return true;
        }

        private static string MapKey(Keys keyData)
        {
            Keys code = keyData & Keys.KeyCode;
            bool shift = (keyData & Keys.Shift) == Keys.Shift;
            bool ctrlAlt = (keyData & (Keys.Control | Keys.Alt)) != Keys.None;
            if (ctrlAlt) return null;
            switch (code)
            {
                case Keys.Up: return "UP";
                case Keys.Down: return "DOWN";
                case Keys.Left: return "LEFT";
                case Keys.Right: return "RIGHT";
                case Keys.Enter: return "FIRE";
                case Keys.F1: return "SOFT_LEFT";
                case Keys.F2: return "SOFT_RIGHT";
                case Keys.Space: return "SPACE";
                case Keys.Back: return "BACKSPACE";
                case Keys.Delete: return "DELETE";
                case Keys.Multiply: return "STAR";
                case Keys.Divide: return "POUND";
            }
            if (code == Keys.D8 && shift) return "STAR";
            if (code == Keys.D3 && shift) return "POUND";
            if (!shift && code >= Keys.D0 && code <= Keys.D9) return ((int)(code - Keys.D0)).ToString();
            if (code >= Keys.NumPad0 && code <= Keys.NumPad9) return ((int)(code - Keys.NumPad0)).ToString();
            if (code >= Keys.A && code <= Keys.Z) return ((char)('A' + (int)(code - Keys.A))).ToString();
            return null;
        }

        private bool TextInputFocused()
        {
            Control c = ActiveControl;
            while (c is ContainerControl && ((ContainerControl)c).ActiveControl != null)
                c = ((ContainerControl)c).ActiveControl;
            return c is TextBox || c is NumericUpDown || c is ComboBox;
        }

        protected override bool ProcessCmdKey(ref Message msg, Keys keyData)
        {
            // chi bat phim khi Remoter dang la cua so hoat dong (ProcessCmdKey chi chay khi co focus)
            if (SyncKeyOn && Form.ActiveForm == this && !TextInputFocused())
            {
                if (TryPhysicalKey(keyData)) return true;
            }
            return base.ProcessCmdKey(ref msg, keyData);
        }

        private void OnToggleKeypad(object s, EventArgs e)
        {
            if (keypad == null || keypad.IsDisposed)
            {
                keypad = new KeypadForm(this);
                // khong dat Owner: thu nho Remoter thi ban phim van hien (noi tren cung) de dong bo tab
                StyleExternal(keypad);
                keypad.Location = new Point(Math.Max(0, Left + Width - keypad.Width - 40), Math.Max(0, Top + 120));
                keypad.Show();
            }
            else if (keypad.Visible) keypad.Hide();
            else keypad.Show();
        }

        // =====================================================================  macro
        private void Record(bool isText, string value)
        {
            if (!recording) return;
            MacroStep st = new MacroStep();
            st.Delay = (int)recWatch.ElapsedMilliseconds;
            st.IsText = isText;
            st.Value = value;
            macro.Add(st);
            recWatch.Reset();
            recWatch.Start();
            UpdateMacroUi();
        }

        private void OnMacroRec(object s, EventArgs e)
        {
            if (macroPlaying) return;
            macro.Clear();
            recording = true;
            recWatch.Reset();
            recWatch.Start();
            UpdateMacroUi();
        }

        private void OnMacroRecStop(object s, EventArgs e)
        {
            recording = false;
            recWatch.Stop();
            UpdateMacroUi();
        }

        private void OnMacroPlay(object s, EventArgs e)
        {
            if (macroPlaying || recording || macro.Count == 0) return;
            List<EmuTab> snap = RunningOf(Targets());
            if (snap.Count == 0) return;
            List<MacroStep> steps = new List<MacroStep>(macro);
            int loops = (int)numLoops.Value;
            macroPlaying = true;
            UpdateMacroUi();
            Thread th = new Thread(delegate()
            {
                try
                {
                    int n = 0;
                    while (macroPlaying && (loops == 0 || n < loops))
                    {
                        foreach (MacroStep st in steps)
                        {
                            if (!macroPlaying) break;
                            Thread.Sleep(Math.Max(30, st.Delay));
                            string json = st.IsText ? TextJson(st.Value) : KeyJson(st.Value);
                            foreach (EmuTab t in snap)
                            {
                                if (t.Running && t.Ipc != null) t.Ipc.Send(json);
                            }
                        }
                        n++;
                    }
                }
                finally
                {
                    macroPlaying = false;
                    try { if (!IsDisposed) BeginInvoke(new MethodInvoker(UpdateMacroUi)); } catch (Exception) { }
                }
            });
            th.IsBackground = true;
            th.Start();
        }

        private void OnMacroPlayStop(object s, EventArgs e)
        {
            macroPlaying = false;
        }

        private void UpdateMacroUi()
        {
            if (InvokeRequired)
            {
                try { BeginInvoke(new MethodInvoker(UpdateMacroUi)); } catch (Exception) { }
                return;
            }
            string state = recording ? "đang ghi" : (macroPlaying ? "đang phát" : "sẵn sàng");
            lblMacro.Text = "Macro: " + macro.Count + " bước · " + state;
            btnRec.Enabled = !recording && !macroPlaying;
            btnRecStop.Enabled = recording;
            btnPlay.Enabled = !recording && !macroPlaying && macro.Count > 0;
            btnPlayStop.Enabled = macroPlaying;
        }

        private void SaveMacroToCfg()
        {
            StringBuilder sb = new StringBuilder();
            foreach (MacroStep st in macro)
                sb.Append(st.Delay).Append('\t').Append(st.IsText ? "T" : "K").Append('\t').Append(st.Value.Replace("\n", " ")).Append('\n');
            cfg.Set("macro", B64(sb.ToString()));
        }

        private void LoadMacroFromCfg()
        {
            macro.Clear();
            string raw = UnB64(cfg.Get("macro", ""));
            foreach (string line in raw.Split('\n'))
            {
                string[] p = line.Split('\t');
                if (p.Length < 3) continue;
                int d;
                if (!int.TryParse(p[0], out d)) continue;
                MacroStep st = new MacroStep();
                st.Delay = d;
                st.IsText = p[1] == "T";
                st.Value = p[2];
                macro.Add(st);
            }
            UpdateMacroUi();
        }

        // =====================================================================  ho so + danh sach tab
        private void SaveTabsToCfg()
        {
            StringBuilder sb = new StringBuilder();
            foreach (EmuTab t in tabs) sb.Append(t.GamePath).Append('\n');
            cfg.Set("tabs_games", B64(sb.ToString()));
        }

        private void LoadTabsFromCfg()
        {
            foreach (string line in UnB64(cfg.Get("tabs_games", "")).Split('\n'))
            {
                string g = line.Trim();
                if (g.Length > 0) AddTab(g);
            }
        }

        private void OnSaveProfile(object s, EventArgs e)
        {
            SaveFileDialog dlg = new SaveFileDialog();
            dlg.Title = "Lưu hồ sơ Che779";
            dlg.Filter = "Hồ sơ Che779 (*.che779)|*.che779";
            dlg.FileName = "ho-so.che779";
            if (dlg.ShowDialog(this) != DialogResult.OK) return;
            SaveSettings();
            cfg.SaveTo(dlg.FileName);
        }

        private void OnLoadProfile(object s, EventArgs e)
        {
            if (RunningOf(tabs).Count > 0)
            {
                MessageBox.Show(this, "Hãy dừng các tab đang chạy trước khi nạp hồ sơ.", AppTitle);
                return;
            }
            OpenFileDialog dlg = new OpenFileDialog();
            dlg.Title = "Nạp hồ sơ Che779";
            dlg.Filter = "Hồ sơ Che779 (*.che779)|*.che779|Tất cả|*.*";
            if (dlg.ShowDialog(this) != DialogResult.OK) return;
            cfg.Clear();
            cfg.LoadFrom(dlg.FileName);
            tabs.Clear();
            grid.Rows.Clear();
            LoadSettingsToUi();
            ApplyTheme();
        }

        // =====================================================================  cap nhat bang
        private void UpdateRow(DataGridViewRow row, EmuTab t)
        {
            row.Cells[1].Value = t.Index;
            row.Cells[2].Value = t.GamePath == null ? "" : Path.GetFileName(t.GamePath);
            row.Cells[3].Value = t.Status;
            row.Cells[4].Value = t.Port > 0 ? t.Port.ToString() : "";
            row.Cells[5].Value = t.Running ? t.Cpu.ToString("0.0") + "%" : "-";
            row.Cells[6].Value = t.Running ? t.RamMb + " MB" : "-";
            row.Cells[7].Value = t.Running ? (t.Sleeping ? "Bật" : "Tắt") : "-";
            row.Cells[8].Value = t.Running ? t.Pid.ToString() : "-";
        }

        private void SampleTab(EmuTab t)
        {
            if (!t.Running) return;
            try
            {
                t.Proc.Refresh();
                DateTime now = DateTime.UtcNow;
                TimeSpan cpu = t.Proc.TotalProcessorTime;
                if (t.LastSample != DateTime.MinValue)
                {
                    double dt = (now - t.LastSample).TotalMilliseconds;
                    if (dt > 0)
                    {
                        double used = (cpu - t.LastCpu).TotalMilliseconds;
                        t.Cpu = Math.Max(0, Math.Min(100, used / dt / Environment.ProcessorCount * 100.0));
                    }
                }
                t.LastCpu = cpu;
                t.LastSample = now;
                t.RamMb = t.Proc.WorkingSet64 / (1024 * 1024);
            }
            catch (Exception) { }
        }

        private void RefreshGrid()
        {
            double totCpu = 0;
            long totRam = 0;
            int running = 0;
            foreach (DataGridViewRow row in grid.Rows)
            {
                EmuTab t = row.Tag as EmuTab;
                if (t == null) continue;
                SampleTab(t);
                UpdateRow(row, t);
                if (t.Running) { running++; totCpu += t.Cpu; totRam += t.RamMb; }
            }
            UpdatePreview();

            string sys = "Hệ thống: ";
            try
            {
                if (cpuCounter != null) sys += "CPU " + cpuCounter.NextValue().ToString("0.0") + "% | ";
            }
            catch (Exception) { }
            uint memLoad = 0;
            try
            {
                Native.MEMORYSTATUSEX ms = new Native.MEMORYSTATUSEX();
                if (Native.GlobalMemoryStatusEx(ms))
                {
                    long total = (long)(ms.ullTotalPhys / (1024 * 1024));
                    long free = (long)(ms.ullAvailPhys / (1024 * 1024));
                    memLoad = ms.dwMemoryLoad;
                    sys += "RAM " + (total - free) + " MB / " + total + " MB (" + ms.dwMemoryLoad + "%)";
                }
            }
            catch (Exception) { }
            lblSys.Text = sys;
            lblAll.Text = "Đang chạy " + running + " / " + tabs.Count + " tab: CPU " + totCpu.ToString("0.0") + "% | RAM " + totRam + " MB";

            // tien ich tu dong
            uint guardPct = (uint)numGuard.Value;
            bool high = chkGuard.Checked && memLoad >= guardPct;
            lblSys.ForeColor = high ? theme.Danger : theme.Text;
            DateTime now2 = DateTime.UtcNow;
            if (high && (now2 - lastGuard).TotalSeconds > 60)
            {
                lastGuard = now2;
                SleepTabs(RunningOf(tabs), true);
            }
            if (chkCleanAuto.Checked && (now2 - lastClean).TotalMinutes >= (double)numCleanMin.Value)
            {
                lastClean = now2;
                CleanRamTabs(RunningOf(tabs));
            }
        }

        private void HandleClosing(object sender, FormClosingEventArgs e)
        {
            macroPlaying = false;
            if (mouseSync != null) mouseSync.Stop();
            SaveSettings();
            List<EmuTab> run = RunningOf(tabs);
            if (run.Count > 0)
            {
                DialogResult r = MessageBox.Show(this, "Đóng luôn " + run.Count + " tab đang chạy?",
                    AppTitle, MessageBoxButtons.YesNoCancel, MessageBoxIcon.Question);
                if (r == DialogResult.Cancel) { e.Cancel = true; return; }
                if (r == DialogResult.Yes)
                {
                    foreach (EmuTab t in run) StopTab(t);
                }
                else
                {
                    autoRestart = false; // de tab o lai khong bi mo lai
                }
            }
        }
    }
}
