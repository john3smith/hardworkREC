package com.local.hardworkrec;

final class DoubleTapReset {
    private static final long WINDOW_MS = 650;
    private long firstTapAt = -1;

    boolean tap(long elapsedRealtimeMs) {
        if (firstTapAt >= 0 && elapsedRealtimeMs >= firstTapAt
                && elapsedRealtimeMs - firstTapAt <= WINDOW_MS) {
            firstTapAt = -1;
            return true;
        }
        firstTapAt = elapsedRealtimeMs;
        return false;
    }
}
