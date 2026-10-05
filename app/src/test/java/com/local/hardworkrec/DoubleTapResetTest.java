package com.local.hardworkrec;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DoubleTapResetTest {
    @Test public void onlyFastSecondTapResets() {
        DoubleTapReset guard = new DoubleTapReset();
        assertFalse(guard.tap(1000));
        assertTrue(guard.tap(1650));
        assertFalse(guard.tap(1700));
        assertFalse(guard.tap(2401));
        assertTrue(guard.tap(2500));
    }

    @Test public void slowOrNonMonotonicTapMustNotReset() {
        DoubleTapReset guard = new DoubleTapReset();
        assertFalse(guard.tap(1000));
        assertFalse(guard.tap(1651));
        assertFalse(guard.tap(900));
        assertTrue(guard.tap(950));
    }
}
