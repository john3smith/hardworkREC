package com.local.hardworkrec;

/** Reads JSON text without converting null, numbers, booleans or containers to text. */
final class BackupStrings {
    private BackupStrings() {}

    static String text(Object value) {
        if (!(value instanceof String)) {
            throw new IllegalArgumentException("백업의 문자열 형식이 올바르지 않습니다.");
        }
        return (String) value;
    }
}
