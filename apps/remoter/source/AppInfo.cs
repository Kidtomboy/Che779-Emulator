using System;
using System.IO;
using System.Reflection;

[assembly: AssemblyTitle("Che779 Remoter")]
[assembly: AssemblyVersion("0.0.0.2")]
[assembly: AssemblyFileVersion("0.0.0.2")]
[assembly: AssemblyInformationalVersion("0.0.0.2 custom")]

namespace Che779.Remoter
{
    /// <summary>Phien ban + thu muc du lieu (%APPDATA%\\Che779\\...). Khong luu gi canh file exe.</summary>
    internal static class AppInfo
    {
        public const string Version = "0.0.0.2 custom";

        public static readonly string Root = Path.Combine(
            Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData), "Che779");
        public static readonly string RemoterDir = Path.Combine(Root, "Che779-Remoter");
        public static readonly string EmulatorDir = Path.Combine(Root, "Che779-Emulator");

        public static string Ensure(string dir)
        {
            try { Directory.CreateDirectory(dir); } catch (Exception) { }
            return dir;
        }
    }
}
