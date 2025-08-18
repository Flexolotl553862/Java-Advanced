package info.kgeorgiy.ja.morozov.walk;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA256FileEncryptor {

    private final int bufferLength = 256;

    private final String defaultEncryptionResult = "0".repeat(16);

    public String encrypt(Path file) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        String hash = defaultEncryptionResult;

        try (InputStream reader = Files.newInputStream(file)) {
            byte[] buffer = new byte[bufferLength];
            int realBufferLength = reader.read(buffer);
            while (realBufferLength >= 0) {
                digest.update(buffer, 0, realBufferLength);
                realBufferLength = reader.read(buffer);
            }

            byte[] encryptionResult = digest.digest();

            long hashValue = 0;

            for (int i = 0; i < 8; i++) {
                hashValue = (hashValue << 8) | (encryptionResult[i] & 0xFF);
            }

            hash = String.format("%016x", hashValue);

        } catch(IOException ignored) {}

        return hash;
    }

    public String getDefaultEncryptionResult() {
        return defaultEncryptionResult;
    }
}
