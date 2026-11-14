using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Text;
using System.Threading;
using System.Windows.Forms;

namespace Che779.Remoter
{
    /// <summary>Cac tien ich moi o muc 6: thu muc du lieu, hen gio, chup man hinh, mo lai tab, sao chep danh sach, phien ban.</summary>
    internal partial class MainForm
    {
        private DateTime timerDeadline = DateTime.MinValue;

        private static void OpenFolder(string dir)
        {
            try
            {
                AppInfo.Ensure(dir);
                Process.Start("explorer.exe", "\"" + dir + "\"");
            }
            catch (Exception) { }
        }

        private void OnOpenDataDir(object s, EventArgs e)
        {
            OpenFolder(AppInfo.Root);
        }

        private void OnWipeData(object s, EventArgs e)
        {
            if (RunningOf(tabs).Count > 0)
            {
                MessageBox.Show(this, "Hãy dừng tất cả tab trước khi xóa dữ liệu emulator.", AppTitle);
                return;
            }
            DialogResult r = MessageBox.Show(this,
                "Xóa TOÀN BỘ dữ liệu emulator (cấu hình, vị trí cửa sổ, dữ liệu lưu của game) trong:\n"
                + AppInfo.EmulatorDir + "\n\nCấu hình Remoter được giữ nguyên. Tiếp tục?",
                AppTitle, MessageBoxButtons.YesNo, MessageBoxIcon.Warning);
            if (r != DialogResult.Yes) return;
            try
            {
                if (Directory.Exists(AppInfo.EmulatorDir)) Directory.Delete(AppInfo.EmulatorDir, true);
                AppInfo.Ensure(AppInfo.EmulatorDir);
                MessageBox.Show(this, "Đã xóa dữ liệu emulator.", AppTitle);
            }
            catch (Exception ex)
            {
                MessageBox.Show(this, "Không xóa hết được: " + ex.Message, AppTitle);
            }
        }

        // ------------------------------------------------------------ hen gio dong het
        private void OnTimerToggle()
        {
            if (chkTimer.Checked) timerDeadline = DateTime.UtcNow.AddMinutes((double)numTimer.Value);
            else { timerDeadline = DateTime.MinValue; lblTimer.Text = ""; }
        }

        /// <summary>Goi moi giay: cap nhat dong hen gio, tu dong dong het khi het gio.</summary>
        private void TickExtras()
        {
            if (!chkTimer.Checked || timerDeadline == DateTime.MinValue) return;
            TimeSpan left = timerDeadline - DateTime.UtcNow;
            if (left.TotalSeconds <= 0)
            {
                chkTimer.Checked = false;
                lblTimer.Text = "Đã hết giờ: đóng các tab.";
                OnCloseAll(null, EventArgs.Empty);
                return;
            }
            lblTimer.Text = "Còn " + ((int)left.TotalMinutes) + " phút " + left.Seconds + " giây sẽ đóng hết";
        }

        // ------------------------------------------------------------ tu bat dau
        private void AutoStartIfWanted()
        {
            try
            {
                if (chkAutoStart.Checked && File.Exists(txtEmu.Text.Trim()) && (tabs.Count > 0 || txtGame.Text.Trim().Length > 0))
                    OnStart(null, EventArgs.Empty);
            }
            catch (Exception) { }
        }

        // ------------------------------------------------------------ mo lai tab
        private LaunchSpec CurrentSpec()
        {
            LaunchSpec spec = new LaunchSpec();
            spec.Java = txtJava.Text.Trim();
            spec.Emu = txtEmu.Text.Trim();
            spec.Xmx = (int)numXmx.Value;
            spec.Sep = chkSep.Checked;
            spec.Arr = ReadArrange();
            return spec;
        }

        private void OnRestartTabs(object s, EventArgs e)
        {
            List<EmuTab> list = RunningOf(Targets());
            if (list.Count == 0) return;
            LaunchSpec spec = CurrentSpec();
            lastSpec = spec;
            int delay = (int)numDelay.Value;
            ThreadPool.QueueUserWorkItem(delegate
            {
                foreach (EmuTab t in list)
                {
                    StopTab(t);
                    t.StopRequested = false;
                    t.Restarts = 0;
                    t.Port = PickPort();
                    t.Status = "Mở lại...";
                    LaunchTab(t, spec);
                    if (delay > 0) Thread.Sleep(delay);
                }
            });
        }

        // ------------------------------------------------------------ chup man hinh
        private void OnScreenshot(object s, EventArgs e)
        {
            List<EmuTab> list = RunningOf(Targets());
            if (list.Count == 0) return;
            string dir = Path.Combine(Path.Combine(
                Environment.GetFolderPath(Environment.SpecialFolder.MyPictures), "Che779"),
                DateTime.Now.ToString("yyyyMMdd_HHmmss"));
            AppInfo.Ensure(dir);
            foreach (EmuTab t in list)
            {
                EmuTab tab = t;
                string file = Path.Combine(dir, "tab" + tab.Index + ".png");
                ThreadPool.QueueUserWorkItem(delegate
                {
                    if (tab.Ipc != null) tab.Ipc.Send("{\"action\":\"screenshot\",\"path\":\"" + JsonEscape(file) + "\"}");
                });
            }
            string folder = dir;
            ThreadPool.QueueUserWorkItem(delegate
            {
                Thread.Sleep(1500);
                OpenFolder(folder);
            });
        }

        // ------------------------------------------------------------ sao chep danh sach
        private void OnCopyList(object s, EventArgs e)
        {
            StringBuilder sb = new StringBuilder();
            sb.AppendLine("Tab\tGame\tTrạng thái\tCổng\tCPU\tRAM\tNgủ\tPID");
            foreach (DataGridViewRow row in grid.Rows)
            {
                List<string> cells = new List<string>();
                for (int i = 1; i < row.Cells.Count; i++) cells.Add(Convert.ToString(row.Cells[i].Value));
                sb.AppendLine(string.Join("\t", cells.ToArray()));
            }
            try { Clipboard.SetText(sb.ToString()); }
            catch (Exception) { }
        }

        // ------------------------------------------------------------ phien ban
        private void OnShowVersions(object s, EventArgs e)
        {
            StringBuilder sb = new StringBuilder();
            sb.AppendLine("Che779 Remoter: " + AppInfo.Version);
            sb.AppendLine("Dữ liệu: " + AppInfo.Root);
            sb.AppendLine();
            foreach (EmuTab t in RunningOf(tabs))
            {
                string reply = t.Ipc != null ? t.Ipc.Send("{\"action\":\"status\"}") : null;
                string ver = "?";
                if (reply != null)
                {
                    System.Text.RegularExpressions.Match m =
                        System.Text.RegularExpressions.Regex.Match(reply, "\"version\":\"([^\"]*)\"");
                    if (m.Success) ver = m.Groups[1].Value;
                }
                sb.AppendLine("Tab " + t.Index + ": Emulator " + ver);
            }
            MessageBox.Show(this, sb.ToString(), AppTitle);
        }
    }
}
