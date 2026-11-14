using System;
using System.Diagnostics;
using System.Runtime.InteropServices;

namespace Che779.Remoter
{
    /// <summary>Hook chuot toan cuc: bat nhap chuot/keo tren cua so 1 tab emulator va bao toa do tuong doi (0..1) de nhan ban sang tab khac.</summary>
    internal class MouseSync : IDisposable
    {
        private delegate IntPtr HookProc(int nCode, IntPtr wParam, IntPtr lParam);

        [StructLayout(LayoutKind.Sequential)]
        private struct POINT { public int X, Y; }

        [StructLayout(LayoutKind.Sequential)]
        private struct RECT { public int Left, Top, Right, Bottom; }

        [StructLayout(LayoutKind.Sequential)]
        private struct MSLLHOOKSTRUCT
        {
            public POINT pt;
            public uint mouseData, flags, time;
            public IntPtr extra;
        }

        [DllImport("user32.dll", SetLastError = true)]
        private static extern IntPtr SetWindowsHookEx(int id, HookProc fn, IntPtr mod, uint thread);
        [DllImport("user32.dll")]
        private static extern bool UnhookWindowsHookEx(IntPtr hook);
        [DllImport("user32.dll")]
        private static extern IntPtr CallNextHookEx(IntPtr hook, int code, IntPtr w, IntPtr l);
        [DllImport("kernel32.dll", CharSet = CharSet.Auto)]
        private static extern IntPtr GetModuleHandle(string name);
        [DllImport("user32.dll")]
        private static extern IntPtr WindowFromPoint(POINT p);
        [DllImport("user32.dll")]
        private static extern IntPtr GetAncestor(IntPtr h, uint flags);
        [DllImport("user32.dll")]
        private static extern uint GetWindowThreadProcessId(IntPtr h, out uint pid);
        [DllImport("user32.dll")]
        private static extern bool GetWindowRect(IntPtr h, out RECT r);

        private const int WH_MOUSE_LL = 14;
        private const int WM_MOUSEMOVE = 0x200, WM_LBUTTONDOWN = 0x201, WM_LBUTTONUP = 0x202;

        /// <summary>pid nguon, toa do tuong doi x, y, loai (down|up|move).</summary>
        public event Action<int, double, double, string> Mouse;

        private HookProc proc;
        private IntPtr hook = IntPtr.Zero;
        private bool dragging;
        private IntPtr dragWin = IntPtr.Zero;
        private int dragPid;
        private int lastMove;

        public bool Start()
        {
            if (hook != IntPtr.Zero) return true;
            proc = new HookProc(Callback);
            using (Process p = Process.GetCurrentProcess())
            using (ProcessModule m = p.MainModule)
            {
                hook = SetWindowsHookEx(WH_MOUSE_LL, proc, GetModuleHandle(m.ModuleName), 0);
            }
            return hook != IntPtr.Zero;
        }

        public void Stop()
        {
            if (hook != IntPtr.Zero) UnhookWindowsHookEx(hook);
            hook = IntPtr.Zero;
            dragging = false;
        }

        public void Dispose() { Stop(); }

        private IntPtr Callback(int code, IntPtr w, IntPtr l)
        {
            if (code >= 0)
            {
                try
                {
                    int msg = w.ToInt32();
                    if (msg == WM_LBUTTONDOWN || msg == WM_LBUTTONUP || msg == WM_MOUSEMOVE)
                    {
                        MSLLHOOKSTRUCT st = (MSLLHOOKSTRUCT)Marshal.PtrToStructure(l, typeof(MSLLHOOKSTRUCT));
                        Handle(msg, st.pt);
                    }
                }
                catch (Exception) { }
            }
            return CallNextHookEx(hook, code, w, l);
        }

        private void Handle(int msg, POINT pt)
        {
            if (msg == WM_LBUTTONDOWN)
            {
                IntPtr root = GetAncestor(WindowFromPoint(pt), 2);
                if (root == IntPtr.Zero) return;
                uint pid;
                GetWindowThreadProcessId(root, out pid);
                dragging = true;
                dragWin = root;
                dragPid = (int)pid;
                Emit(pt, "down");
            }
            else if (msg == WM_LBUTTONUP)
            {
                if (!dragging) return;
                Emit(pt, "up");
                dragging = false;
            }
            else if (dragging)
            {
                int now = Environment.TickCount;
                if (now - lastMove < 40) return;
                lastMove = now;
                Emit(pt, "move");
            }
        }

        private void Emit(POINT pt, string type)
        {
            RECT r;
            if (!GetWindowRect(dragWin, out r)) return;
            int w = r.Right - r.Left, h = r.Bottom - r.Top;
            if (w <= 0 || h <= 0) return;
            double fx = (double)(pt.X - r.Left) / w;
            double fy = (double)(pt.Y - r.Top) / h;
            if (fx < 0) fx = 0; if (fx > 1) fx = 1;
            if (fy < 0) fy = 0; if (fy > 1) fy = 1;
            Action<int, double, double, string> h2 = Mouse;
            if (h2 != null) h2(dragPid, fx, fy, type);
        }
    }
}
