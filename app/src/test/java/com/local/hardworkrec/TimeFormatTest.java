package com.local.hardworkrec;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class TimeFormatTest {
    @Test public void formatsContinuousDigits() {
        assertEquals("", TimeFormat.displayDigits(""));
        assertEquals("0:05", TimeFormat.displayDigits("5"));
        assertEquals("0:25", TimeFormat.displayDigits("25"));
        assertEquals("1:25", TimeFormat.displayDigits("125"));
        assertEquals("12:34", TimeFormat.displayDigits("1234"));
        assertEquals("12:34", TimeFormat.displayDigits("12:34"));
    }

    @Test public void validatesSecondsAndOverflow() {
        assertEquals(754, TimeFormat.parseSeconds("12:34"));
        assertEquals(5, TimeFormat.parseSeconds("0:05"));
        assertEquals(-1, TimeFormat.parseSeconds("0:90"));
        assertEquals(-1, TimeFormat.parseSeconds(""));
        assertEquals(-1, TimeFormat.parseSeconds("999999999999:00"));
    }
}
