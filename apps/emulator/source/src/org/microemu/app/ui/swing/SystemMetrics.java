package org.microemu.app.ui.swing;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

/** Do CPU/RAM cua chinh tien trinh emulator (cap nhat toi da 1 lan/giay). */
public final class SystemMetrics {
	private static final MemoryMXBean MEM = ManagementFactory.getMemoryMXBean();
	private static long lastCpuTime = -1, lastNano, lastCpuCall, lastRamCall;
	private static double lastCpu, lastRam;

	private SystemMetrics() {
	}

	public static synchronized double getRamUsedMB() {
		long now = System.currentTimeMillis();
		if (now - lastRamCall >= 1000) {
			MemoryUsage h = MEM.getHeapMemoryUsage();
			MemoryUsage n = MEM.getNonHeapMemoryUsage();
			lastRam = (h.getUsed() + n.getUsed()) / 1024.0 / 1024.0;
			lastRamCall = now;
		}
		return lastRam;
	}

	public static synchronized double getCpuUsage() {
		long now = System.currentTimeMillis();
		if (now - lastCpuCall < 1000) {
			return lastCpu;
		}
		try {
			java.lang.management.OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
			long cpu = ((com.sun.management.OperatingSystemMXBean) os).getProcessCpuTime();
			long nano = System.nanoTime();
			if (lastCpuTime >= 0 && nano > lastNano) {
				lastCpu = (double) (cpu - lastCpuTime) / (double) (nano - lastNano) / os.getAvailableProcessors() * 100.0;
			}
			lastCpuTime = cpu;
			lastNano = nano;
			lastCpuCall = now;
		} catch (Throwable t) {
			lastCpu = 0;
		}
		return lastCpu;
	}
}
