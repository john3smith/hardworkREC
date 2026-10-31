package com.local.hardworkrec;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

public class BackupInputTest {
    @Test public void preservesUnicodeBomAndLiteralReplacementAcrossSingleByteReads() throws Exception {
        for (String text : new String[]{"", "\uFEFF{\"운동명\":\"스쿼트 🏋️ �\"}", "12, 8회"}) {
            InputStream stream = new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)) {
                @Override public synchronized int read(byte[] b, int off, int len) {
                    return super.read(b, off, Math.min(len, 1));
                }
            };
            assertEquals(text, BackupInput.read(stream));
        }
    }

    @Test public void rejectsMalformedUtf8InsteadOfReplacingIt() {
        int[][] invalid = {{0x80}, {0xC0, 0xAF}, {0xC2}, {0xE2, 0x82},
                {0xED, 0xA0, 0x80}, {0xF4, 0x90, 0x80, 0x80}, {0xFF}, {0xE2, 0x28, 0xA1}};
        for (int[] values : invalid) {
            byte[] bytes = new byte[values.length];
            for (int i = 0; i < values.length; i++) bytes[i] = (byte) values[i];
            IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                    () -> BackupInput.read(new ByteArrayInputStream(bytes)));
            assertTrue(error.getMessage().contains("UTF-8"));
        }
    }

    @Test public void enforcesOriginalByteLimitWithExactBoundary() throws Exception {
        assertEquals(10_000_000, BackupInput.read(spaces(10_000_000)).length());
        assertThrows(IllegalArgumentException.class, () -> BackupInput.read(spaces(10_000_001)));
    }

    @Test public void leavesStreamClosureToCallerAndPropagatesReadFailure() throws Exception {
        final boolean[] closed = {false};
        InputStream stream = new ByteArrayInputStream(new byte[]{'a'}) {
            @Override public void close() { closed[0] = true; }
        };
        try (InputStream owned = stream) {
            assertEquals("a", BackupInput.read(owned));
            assertFalse(closed[0]);
        }
        assertTrue(closed[0]);
        InputStream broken = new InputStream() {
            @Override public int read() throws IOException { throw new IOException("read failed"); }
        };
        assertEquals("read failed", assertThrows(IOException.class, () -> BackupInput.read(broken)).getMessage());
    }

    private static InputStream spaces(int size) {
        return new InputStream() {
            private int left = size;
            @Override public int read() { return left-- > 0 ? ' ' : -1; }
            @Override public int read(byte[] b, int off, int len) {
                if (left == 0) return -1;
                int n = Math.min(len, left);
                Arrays.fill(b, off, off + n, (byte) ' ');
                left -= n;
                return n;
            }
        };
    }
}
