package com.che779.emu;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

/** Goi psapi!EmptyWorkingSet qua JNA. Chi duoc nap khi co jna.jar (>= 5.x) tren classpath. */
final class JnaClean {
    interface Psapi extends Library {
        boolean EmptyWorkingSet(Pointer hProcess);
    }

    static boolean run() {
        Psapi psapi = (Psapi) Native.load("psapi", Psapi.class);
        // Pseudo-handle cua process hien tai = (HANDLE)-1
        return psapi.EmptyWorkingSet(Pointer.createConstant(-1L));
    }
}
