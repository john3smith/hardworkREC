package com.local.hardworkrec;

final class TimerSeconds {
    static final int MAX = 99 * 60 + 59;

    private TimerSeconds() {}

    static int parse(String raw) {
        if (raw == null) return -1;
        String value = raw.trim();
        if (value.isEmpty() || !value.matches("[0-9]+")) return -1;
        try {
            int seconds = Integer.parseInt(value);
            return seconds >= 1 && seconds <= MAX ? seconds : -1;
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }
}
