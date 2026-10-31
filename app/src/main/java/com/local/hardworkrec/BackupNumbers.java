package com.local.hardworkrec;

/** Reads JSON integer values without JSONObject's lossy numeric coercions. */
final class BackupNumbers {
    private BackupNumbers() {}

    static long longInteger(Object value) {
        if (!(value instanceof Integer) && !(value instanceof Long)) {
            throw new IllegalArgumentException("백업의 숫자 값은 정수여야 합니다.");
        }
        return ((Number) value).longValue();
    }

    static int integer(Object value) {
        long number = longInteger(value);
        if (number < Integer.MIN_VALUE || number > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("백업의 숫자 값이 허용 범위를 벗어났습니다.");
        }
        return (int) number;
    }
}
