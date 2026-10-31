package com.local.hardworkrec;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class BackupUuidsTest {
    @Test public void preservesFullWidthIdentityAndNormalizesCase() {
        String lower = "abcdefab-cdef-abcd-efab-cdefabcdefab";
        for (String value : new String[]{lower, "ABCDEFAB-CDEF-ABCD-EFAB-CDEFABCDEFAB",
                "AbCdEfAb-cDeF-aBcD-eFaB-cDeFaBcDeFaB"}) {
            assertEquals(lower, BackupUuids.canonical(value));
        }
        for (String value : new String[]{"00000000-0000-0000-0000-000000000000",
                "ffffffff-ffff-ffff-ffff-ffffffffffff"}) {
            assertEquals(value, BackupUuids.canonical(value));
        }
    }

    @Test public void rejectsShortenedAndSignedGroupsRatherThanAliasingOtherIds() {
        for (String value : new String[]{"1-1-1-1-1", "0000001-0001-0001-0001-000000000001",
                "00000001-001-0001-0001-000000000001", "00000001-0001-001-0001-000000000001",
                "00000001-0001-0001-001-000000000001", "00000001-0001-0001-0001-00000000001",
                "+0000001-0001-0001-0001-000000000001", "00000001-+001-0001-0001-000000000001",
                "00000001-0001-+001-0001-000000000001", "00000001-0001-0001-+001-000000000001",
                "00000001-0001-0001-0001-+00000000001"}) {
            assertThrows(IllegalArgumentException.class, () -> BackupUuids.canonical(value));
        }
    }

    @Test public void rejectsMalformedTextWhitespaceAndNonTextValues() {
        for (Object value : new Object[]{null, 123, true, "", "garbage",
                "0000000g-0001-0001-0001-000000000001", "00000001000100010001000000000001",
                " 00000001-0001-0001-0001-000000000001", "00000001-0001-0001-0001-000000000001 ",
                "00000001-0001-0001-0001-000000000001-extra"}) {
            assertThrows(IllegalArgumentException.class, () -> BackupUuids.canonical(value));
        }
    }
}
