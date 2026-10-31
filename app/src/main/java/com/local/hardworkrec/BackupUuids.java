package com.local.hardworkrec;

import java.util.UUID;

/** Validates backup identities before normalization or database writes. */
final class BackupUuids {
    private BackupUuids() {}

    static String canonical(Object value) {
        String input = BackupStrings.text(value);
        String canonical = UUID.fromString(input).toString();
        // Some runtimes also parse shortened groups and signed hexadecimal numbers.
        // Normalizing these silently can collide with a different record's identity.
        if (!canonical.equalsIgnoreCase(input)) {
            throw new IllegalArgumentException("백업 기록의 UUID 형식이 올바르지 않습니다.");
        }
        return canonical;
    }
}
