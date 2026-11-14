using System;
using System.Collections.Generic;
using System.Globalization;
using System.Threading;
using System.Windows.Forms;

namespace Che779.Remoter
{
    /// <summary>Dong bo chuot.</summary>
    internal partial class MainForm
    {
        private CheckBox chkSyncMouse;
        private MouseSync mouseSync;

        private static string JsonEsc(string s)
        {
            return (s ?? "").Replace("\\", "\\\\").Replace("\"", "\\\"");
        }

        // ------------------------------------------------------------ chuot
        private void OnSyncMouseChanged(object s, EventArgs e)
        {
            if (chkSyncMouse.Checked)
            {
                if (mouseSync == null)
                {
                    mouseSync = new MouseSync();
                    mouseSync.Mouse += OnMouseEvent;
                }
                if (!mouseSync.Start())
                {
                    chkSyncMouse.Checked = false;
                    MessageBox.Show(this, "Không bật được đồng bộ chuột (hook chuột bị chặn).", AppTitle);
                }
            }
            else if (mouseSync != null)
            {
                mouseSync.Stop();
            }
        }

        private void OnMouseEvent(int pid, double fx, double fy, string type)
        {
            // WH_MOUSE_LL invokes this callback on the hook/message thread. All tab
            // collections and WinForms state must be read on the UI thread.
            if (IsDisposed || !IsHandleCreated) return;
            if (InvokeRequired)
            {
                try { BeginInvoke(new Action<int, double, double, string>(OnMouseEvent), pid, fx, fy, type); }
                catch (InvalidOperationException) { }
                return;
            }

            EmuTab src = null;
            foreach (EmuTab t in tabs) if (t.Running && t.Pid == pid) { src = t; break; }
            if (src == null) return;
            List<EmuTab> targets = Targets();
            string json = "{\"action\":\"mouse\",\"type\":\"" + type + "\",\"fx\":"
                + fx.ToString("0.#####", CultureInfo.InvariantCulture) + ",\"fy\":"
                + fy.ToString("0.#####", CultureInfo.InvariantCulture) + "}";
            foreach (EmuTab t in RunningOf(targets))
            {
                if (t == src) continue;
                EmuTab tab = t;
                ThreadPool.QueueUserWorkItem(delegate { if (tab.Ipc != null) tab.Ipc.Send(json); });
            }
        }

        private void LoadSyncExtras()
        {
            chkSyncMouse.Checked = cfg.GetBool("syncmouse", false);
        }

        private void SaveSyncExtras()
        {
            cfg.SetBool("syncmouse", chkSyncMouse.Checked);
        }
    }
}
