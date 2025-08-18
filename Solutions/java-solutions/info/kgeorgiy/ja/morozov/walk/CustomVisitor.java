package info.kgeorgiy.ja.morozov.walk;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

public class CustomVisitor extends SimpleFileVisitor<Path> {
    private final Path outputFile;
    private final BufferedWriter out;
    private final SHA256FileEncryptor fileEncryptor = new SHA256FileEncryptor();

    public CustomVisitor(Path outputFile, BufferedWriter out) {
        this.outputFile = outputFile;
        this.out = out;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Objects.requireNonNull(file);
        Objects.requireNonNull(attrs);

        try {
            if (Files.isSameFile(outputFile, file)) {
                throw new IOException("File " + outputFile + " is already open for writing, can't read this file");
            }
        } catch (IOException ignored) {}

        String hash;

        try {
            hash = fileEncryptor.encrypt(file);
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("Can't find encryption algorithm: " + e.getMessage());
        }

        out.write(hash + " " + file);
        out.newLine();

        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException ignored) throws IOException {
        Objects.requireNonNull(file);

        out.write(fileEncryptor.getDefaultEncryptionResult() + " " + file);
        out.newLine();

        return FileVisitResult.CONTINUE;
    }
}
