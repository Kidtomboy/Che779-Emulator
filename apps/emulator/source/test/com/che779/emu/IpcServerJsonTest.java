package com.che779.emu;

import java.util.Map;

public final class IpcServerJsonTest {
    private static void check(boolean ok, String message) {
        if (!ok) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        Map<String, String> m = IpcServer.parse(
                "{\"action\":\"text\",\"text\":\"quote: \\\" slash: \\\\ newline:\\n tab:\\t unicode:\\u0041\",\"hold\":60,\"enabled\":true}");
        check("text".equals(m.get("action")), "action");
        check("quote: \" slash: \\ newline:\n tab:\t unicode:A".equals(m.get("text")),
                "escaped string decoding: " + m.get("text"));
        check("60".equals(m.get("hold")), "number");
        check("true".equals(m.get("enabled")), "boolean");

        check(IpcServer.parse("{\"action\":\"ping\",}").isEmpty(), "trailing comma rejected");
        check(IpcServer.parse("{\"action\":\"ping\"} garbage").isEmpty(), "trailing data rejected");
        check(IpcServer.parse("{\"action\":unquoted}").isEmpty(), "unquoted string rejected");
        check(IpcServer.parse("{\"action\":\"ping\",\"nested\":{\"x\":1}}").isEmpty(),
                "nested value rejected by flat protocol");
        check("".equals(IpcServer.parse("{\"action\":\"\"}").get("action")), "empty string");
        System.out.println("IpcServerJsonTest: PASS");
    }
}
