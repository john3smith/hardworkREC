package com.local.hardworkrec;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class BackupNumbersTest {
    @Test public void acceptsJsonIntegerRepresentationsWithoutLoss() {
        assertEquals(2, BackupNumbers.integer(2));
        assertEquals(60, BackupNumbers.integer(60L));
        assertEquals(0, BackupNumbers.integer(0));
        assertEquals(1700000000000L, BackupNumbers.longInteger(1700000000000L));
    }

    @Test public void acceptsExactIntegerBoundaries() {
        assertEquals(Integer.MIN_VALUE, BackupNumbers.integer((long) Integer.MIN_VALUE));
        assertEquals(Integer.MAX_VALUE, BackupNumbers.integer((long) Integer.MAX_VALUE));
        assertEquals(Long.MIN_VALUE, BackupNumbers.longInteger(Long.MIN_VALUE));
        assertEquals(Long.MAX_VALUE, BackupNumbers.longInteger(Long.MAX_VALUE));
    }

    @Test public void rejectsOverflowInsteadOfWrappingToValidValues() {
        for (long value : new long[]{4294967298L, 4294967356L, 4294967308L,
                (long) Integer.MAX_VALUE + 1, (long) Integer.MIN_VALUE - 1, Long.MAX_VALUE, Long.MIN_VALUE}) {
            assertThrows(IllegalArgumentException.class, () -> BackupNumbers.integer(value));
        }
    }

    @Test public void rejectsFractionalAndFloatingPointNumbers() {
        for (Object value : new Object[]{2.5, 60.5, 1700000000000.5, 2.0,
                60.0f, Double.NaN, Double.POSITIVE_INFINITY, Math.scalb(1.0, 63)}) {
            assertThrows(IllegalArgumentException.class, () -> BackupNumbers.longInteger(value));
        }
    }

    @Test public void rejectsNumericStringsWithoutCoercion() {
        for (String value : new String[]{"2", "60", "1700000000000", "2e0", "", " 2 "}) {
            assertThrows(IllegalArgumentException.class, () -> BackupNumbers.integer(value));
            assertThrows(IllegalArgumentException.class, () -> BackupNumbers.longInteger(value));
        }
    }

    @Test public void rejectsNullBooleansAndNonNumberObjects() {
        for (Object value : new Object[]{null, true, false, new Object()}) {
            assertThrows(IllegalArgumentException.class, () -> BackupNumbers.integer(value));
            assertThrows(IllegalArgumentException.class, () -> BackupNumbers.longInteger(value));
        }
    }
}
