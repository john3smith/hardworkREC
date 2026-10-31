package com.local.hardworkrec;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

/** Reads a bounded UTF-8 backup without silently replacing corrupt bytes. */
final class BackupInput {
    private BackupInput() {}

    // The caller owns the stream and closes it, including on validation failure.
    static String read(InputStream stream) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int read;
        while ((read = stream.read(buffer)) != -1) {
            if (bytes.size() + read > 10_000_000) {
                throw new IllegalArgumentException("백업 파일이 너무 큽니다.");
            }
            bytes.write(buffer, 0, read);
        }
        try {
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes.toByteArray())).toString();
        } catch (CharacterCodingException ex) {
            throw new IllegalArgumentException("백업 파일의 UTF-8 인코딩이 올바르지 않습니다.", ex);
        }
    }
}
