import java.io.File;
/** Kiem thu Core cua Remoter Linux voi Emulator V1 + game that. Chay: java -cp Che779_Remoter.jar tests/RemoterCoreTest.java <emulator.jar> <game.jar> */
public class RemoterCoreTest {
    static int fail = 0;
    static void check(String n, boolean ok) { System.out.println((ok ? "OK   " : "FAIL ") + n); if (!ok) fail++; }
    public static void main(String[] a) throws Exception {
        java.lang.reflect.Constructor<?> k = Class.forName("com.che779.remoter.Core").getDeclaredConstructors()[0]; k.setAccessible(true);
        Object core = k.newInstance(new File(a[0]), 7900);
        Class<?> C = core.getClass();
        java.lang.reflect.Method addRow = C.getDeclaredMethod("addRow", File.class), start = C.getDeclaredMethod("start", Class.forName("com.che779.remoter.Core$Tab")),
            poll = C.getDeclaredMethod("poll", Class.forName("com.che779.remoter.Core$Tab")), ready = C.getDeclaredMethod("waitReady", Class.forName("com.che779.remoter.Core$Tab"), int.class),
            sleep = C.getDeclaredMethod("sleep", Class.forName("com.che779.remoter.Core$Tab"), boolean.class), close = C.getDeclaredMethod("close", Class.forName("com.che779.remoter.Core$Tab"), boolean.class),
            clean = C.getDeclaredMethod("cleanRam", Class.forName("com.che779.remoter.Core$Tab"));
        for (java.lang.reflect.Method m : new java.lang.reflect.Method[] { addRow, start, poll, ready, sleep, close, clean }) m.setAccessible(true);
        java.lang.reflect.Field sep = C.getDeclaredField("separateData"); sep.setAccessible(true); sep.setBoolean(core, true);
        java.lang.reflect.Field dr = C.getDeclaredField("dataRoot"); dr.setAccessible(true); File data = new File("/tmp/rc-data"); dr.set(core, data);
        Object t = addRow.invoke(core, new File(a[1]));
        Class<?> T = t.getClass(); java.lang.reflect.Field st = T.getDeclaredField("state"), pid = T.getDeclaredField("pid"), rss = T.getDeclaredField("rssMb"); st.setAccessible(true); pid.setAccessible(true); rss.setAccessible(true);
        check("dong moi: Chua chay", "Chưa chạy".equals(st.get(t)));
        start.invoke(core, t);
        check("IPC san sang", (Boolean) ready.invoke(core, t, 25000));
        poll.invoke(core, t); Thread.sleep(1200); poll.invoke(core, t);
        check("trang thai Dang chay", "Đang chạy".equals(st.get(t)));
        check("co PID", (Long) pid.get(t) > 0);
        check("doc RAM tien trinh", (Long) rss.get(t) > 0);
        check("du lieu tach theo tab (tab1)", new File(data, "tab1").isDirectory());
        check("clean_ram", (Boolean) clean.invoke(core, t));
        check("sleep on", (Boolean) sleep.invoke(core, t, true)); poll.invoke(core, t);
        check("trang thai Ngu", "Ngủ".equals(st.get(t)));
        check("sleep off", (Boolean) sleep.invoke(core, t, false));
        close.invoke(core, t, false); poll.invoke(core, t);
        check("Dung giu dong, trang thai Da dung", "Đã dừng".equals(st.get(t)));
        start.invoke(core, t); check("chay lai sau khi dung", (Boolean) ready.invoke(core, t, 25000));
        close.invoke(core, t, true);
        System.out.println(fail == 0 ? "DAT: Core Remoter Linux" : "LOI: " + fail); System.exit(fail == 0 ? 0 : 1);
    }
}
