package com.local.hardworkrec;

import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class BackupStringsTest {
    @Test public void preservesLiteralTextAndUnicodeWithoutCoercion() {
        for (String value : new String[]{"null", "true", "123", "2.5", "[1]", "{\"value\":1}",
                "스쿼트 🏋️", "12, 8; 6회", "", "  12  "}) {
            assertEquals(value, BackupStrings.text(value));
        }
    }

    @Test public void rejectsNullNumbersBooleansAndContainers() {
        for (Object value : new Object[]{null, 123, 2.5, true, false,
                Arrays.asList(1), Collections.singletonMap("value", 1), new Object()}) {
            assertThrows(IllegalArgumentException.class, () -> BackupStrings.text(value));
        }
    }

    @Test public void rejectsObjectsEvenWhenTheyPrintAsValidText() {
        Object value = new Object() {
            @Override public String toString() { return "12, 8"; }
        };
        assertThrows(IllegalArgumentException.class, () -> BackupStrings.text(value));
    }
}
