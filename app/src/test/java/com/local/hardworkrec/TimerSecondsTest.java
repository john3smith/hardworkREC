package com.local.hardworkrec;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class TimerSecondsTest {
    @Test public void acceptsSecondsOnly() {
        assertEquals(60, TimerSeconds.parse("60"));
        assertEquals(125, TimerSeconds.parse(" 125 "));
        assertEquals(5999, TimerSeconds.parse("5999"));
    }

    @Test public void rejectsInvalidValues() {
        assertEquals(-1, TimerSeconds.parse("1:00"));
        assertEquals(-1, TimerSeconds.parse("0"));
        assertEquals(-1, TimerSeconds.parse("6000"));
        assertEquals(-1, TimerSeconds.parse("999999999999999"));
        assertEquals(-1, TimerSeconds.parse(""));
    }
}
