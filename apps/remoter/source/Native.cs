using System;
using System.Runtime.InteropServices;

namespace Che779.Remoter
{
    internal static class Native
    {
        [DllImport("psapi.dll")]
        public static extern bool EmptyWorkingSet(IntPtr hProcess);

        [DllImport("user32.dll")]
        public static extern bool MoveWindow(IntPtr hWnd, int x, int y, int w, int h, bool repaint);

        [StructLayout(LayoutKind.Sequential)]
        public struct RECT { public int Left, Top, Right, Bottom; }

        public delegate bool EnumProc(IntPtr h, IntPtr lp);

        [DllImport("user32.dll")]
        public static extern bool EnumWindows(EnumProc cb, IntPtr lp);
        [DllImport("user32.dll")]
        public static extern bool IsWindowVisible(IntPtr h);
        [DllImport("user32.dll")]
        public static extern uint GetWindowThreadProcessId(IntPtr h, out uint pid);
        [DllImport("user32.dll")]
        public static extern bool GetWindowRect(IntPtr h, out RECT r);
        [DllImport("user32.dll", CharSet = CharSet.Auto)]
        public static extern int GetWindowTextLength(IntPtr h);

        /// <summary>Cua so chinh (hien hoac an) cua tien trinh pid: cua so top-level lon nhat co tieu de.</summary>
        public static IntPtr FindMainWindow(int pid, bool visibleOnly)
        {
            IntPtr best = IntPtr.Zero;
            long bestArea = 0;
            EnumWindows(delegate (IntPtr h, IntPtr lp)
            {
                uint p;
                GetWindowThreadProcessId(h, out p);
                if ((int)p != pid) return true;
                if (visibleOnly && !IsWindowVisible(h)) return true;
                if (GetWindowTextLength(h) == 0) return true;
                RECT r;
                if (!GetWindowRect(h, out r)) return true;
                long area = (long)(r.Right - r.Left) * (r.Bottom - r.Top);
                if (area > bestArea) { bestArea = area; best = h; }
                return true;
            }, IntPtr.Zero);
            return best;
        }

        [StructLayout(LayoutKind.Sequential, CharSet = CharSet.Auto)]
        public class MEMORYSTATUSEX
        {
            public uint dwLength;
            public uint dwMemoryLoad;
            public ulong ullTotalPhys;
            public ulong ullAvailPhys;
            public ulong ullTotalPageFile;
            public ulong ullAvailPageFile;
            public ulong ullTotalVirtual;
            public ulong ullAvailVirtual;
            public ulong ullAvailExtendedVirtual;
            public MEMORYSTATUSEX()
            {
                dwLength = (uint)Marshal.SizeOf(typeof(MEMORYSTATUSEX));
            }
        }

        [DllImport("kernel32.dll", SetLastError = true)]
        [return: MarshalAs(UnmanagedType.Bool)]
        public static extern bool GlobalMemoryStatusEx([In, Out] MEMORYSTATUSEX lpBuffer);
    }
}
