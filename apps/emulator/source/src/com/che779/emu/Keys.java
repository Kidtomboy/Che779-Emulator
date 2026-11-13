package com.che779.emu;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

/** Ten phim kieu dien thoai Nokia -> ma phim AWT ma emulator hieu. */
final class Keys {
    static final class K { final int vk; final char ch; K(int vk, char ch) { this.vk = vk; this.ch = ch; } }
    private static final Map<String, K> MAP = new HashMap<String, K>();
    static {
        put("UP", KeyEvent.VK_UP, KeyEvent.CHAR_UNDEFINED);
        put("DOWN", KeyEvent.VK_DOWN, KeyEvent.CHAR_UNDEFINED);
        put("LEFT", KeyEvent.VK_LEFT, KeyEvent.CHAR_UNDEFINED);
        put("RIGHT", KeyEvent.VK_RIGHT, KeyEvent.CHAR_UNDEFINED);
        put("FIRE", KeyEvent.VK_ENTER, '\n');
        put("OK", KeyEvent.VK_ENTER, '\n');
        put("SELECT", KeyEvent.VK_ENTER, '\n');
        put("SOFT_LEFT", KeyEvent.VK_F1, KeyEvent.CHAR_UNDEFINED);
        put("SOFT1", KeyEvent.VK_F1, KeyEvent.CHAR_UNDEFINED);
        put("SOFT_RIGHT", KeyEvent.VK_F2, KeyEvent.CHAR_UNDEFINED);
        put("SOFT2", KeyEvent.VK_F2, KeyEvent.CHAR_UNDEFINED);
        for (char c = '0'; c <= '9'; c++) put(String.valueOf(c), KeyEvent.VK_0 + (c - '0'), c);
        for (char c = 'A'; c <= 'Z'; c++) put(String.valueOf(c), KeyEvent.VK_A + (c - 'A'), Character.toLowerCase(c));
        put("SPACE", KeyEvent.VK_SPACE, ' ');
        put("BACKSPACE", KeyEvent.VK_BACK_SPACE, '\b');
        put("CLEAR", KeyEvent.VK_BACK_SPACE, '\b');
        put("STAR", KeyEvent.VK_MULTIPLY, '*');
        put("*", KeyEvent.VK_MULTIPLY, '*');
        put("POUND", KeyEvent.VK_DIVIDE, '#');
        put("DELETE", KeyEvent.VK_BACK_SPACE, '\b');
        for (char c = 'A'; c <= 'Z'; c++) put("LETTER_" + c, KeyEvent.VK_A + (c - 'A'), Character.toLowerCase(c));
        put("#", KeyEvent.VK_DIVIDE, '#');
    }
    private static void put(String n, int vk, char ch) { MAP.put(n, new K(vk, ch)); }
    static K get(String name) {
        if (name != null && name.length() == 1 && Character.isLetter(name.charAt(0))) {
            char u = Character.toUpperCase(name.charAt(0));
            if (u >= 'A' && u <= 'Z') return MAP.get("LETTER_" + u);
        } return name == null ? null : MAP.get(name.trim().toUpperCase()); }
}
