using System;
using System.Diagnostics;

namespace Che779.Remoter
{
    internal class EmuTab
    {
        public int Index;
        public string GamePath;
        public Process Proc;
        public int Port;
        public IpcClient Ipc;
        public volatile string Status = "Chưa chạy";
        public volatile bool Sleeping;
        public volatile bool Hidden;
        public bool HasSaved;
        public int SavedX, SavedY, SavedW, SavedH;
        public volatile bool StopRequested;
        public int Restarts;

        // do CPU / RAM
        public TimeSpan LastCpu;
        public DateTime LastSample;
        public double Cpu;
        public long RamMb;

        public bool Running
        {
            get
            {
                try { return Proc != null && !Proc.HasExited; }
                catch (Exception) { return false; }
            }
        }

        public int Pid
        {
            get
            {
                try { return Proc != null ? Proc.Id : 0; }
                catch (Exception) { return 0; }
            }
        }
    }
}
