package com.che779.emu;

import java.lang.management.ManagementFactory;

/** Giai phong working set cua chinh process nay (EmptyWorkingSet / psapi.dll). */
public final class CleanRam {
    private CleanRam() {}

    /** @return mo ta cach da dung, hoac "failed". */
    public static synchronized String run() {
        System.gc();
        System.runFinalization();
        if (!System.getProperty("os.name", "").toLowerCase().contains("win")) {
            return "skipped (not windows)";
        }
        // 1) JNA (neu co lib/jna.jar tren classpath)
        try {
            if (JnaClean.run()) return "jna";
        } catch (Throwable ignore) { /* khong co JNA -> fallback */ }
        // 2) Fallback khong can thu vien: PowerShell P/Invoke psapi.dll
        try {
            String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
            String script =
                "$s='[DllImport(\"psapi.dll\")] public static extern bool EmptyWorkingSet(IntPtr h);';" +
                "Add-Type -MemberDefinition $s -Name W -Namespace P;" +
                "$p=[System.Diagnostics.Process]::GetProcessById(" + pid + ");" +
                "[void][P.W]::EmptyWorkingSet($p.Handle)";
            Process pr = new ProcessBuilder("powershell", "-NoProfile", "-NonInteractive",
                    "-ExecutionPolicy", "Bypass", "-Command", script)
                    .redirectErrorStream(true).start();
            pr.getOutputStream().close();
            byte[] buf = new byte[512];
            while (pr.getInputStream().read(buf) > 0) { /* drain */ }
            return pr.waitFor() == 0 ? "powershell" : "failed";
        } catch (Throwable t) {
            return "failed: " + t;
        }
    }
}
