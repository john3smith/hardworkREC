package com.local.hardworkrec;

import java.util.Locale;

/** Digits are entered continuously: 125 -> 1:25, 1234 -> 12:34. */
public final class TimeFormat {
    private TimeFormat() {}

    public static String displayDigits(String input) {
        String digits = input.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) return "";
        if (digits.length() <= 2) return "0:" + String.format(Locale.US, "%02d", Integer.parseInt(digits));
        String minutes = digits.substring(0, digits.length() - 2).replaceFirst("^0+(?!$)", "");
        return minutes + ":" + digits.substring(digits.length() - 2);
    }

    /** Returns -1 when empty, malformed, or seconds are outside 0..59. */
    public static int parseSeconds(String display) {
        if (display == null || !display.matches("[0-9]+:[0-9]{2}")) return -1;
        String[] parts = display.split(":", 2);
        try {
            long minutes = Long.parseLong(parts[0]);
            int seconds = Integer.parseInt(parts[1]);
            long total = minutes * 60L + seconds;
            return seconds <= 59 && total <= Integer.MAX_VALUE ? (int) total : -1;
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    public static String readable(int seconds) {
        return String.format(Locale.KOREA, "%d분 %02d초", seconds / 60, seconds % 60);
    }
}
