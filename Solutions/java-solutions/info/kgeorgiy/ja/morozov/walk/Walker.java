package info.kgeorgiy.ja.morozov.walk;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.EnumSet;

public class Walker {
    private static final Charset charset = StandardCharsets.UTF_8;

    public static void walk(String[] args, int depth) {
        if (args == null || args.length < 2 || args[0] == null || args[1] == null) {
            System.out.println("Can't find input or output file name");
            return;
        }

        Path inputFile, outputFile;

        try {
            inputFile = Path.of(args[0]);
        } catch (InvalidPathException e) {
            System.out.println("Invalid input path");
            return;
        }

        try {
            outputFile = Path.of(args[1]);

            if (!Files.exists(outputFile)) {
                if (outputFile.getParent() != null) {
                    Files.createDirectories(outputFile.getParent());
                }
                Files.createFile(outputFile);
            }
        } catch (InvalidPathException e) {
            System.out.println("Invalid output path");
            return;
        } catch (IOException e) {
            System.out.println("Can't create output file");
            return;
        }

        try {
            if (Files.isSameFile(inputFile, outputFile)) {
                System.out.println("Input and output files match");
                return;
            }
        } catch (IOException e) {
            System.out.println("Invalid input path");
        }

        try (BufferedReader in = Files.newBufferedReader(inputFile, charset)) {
            try (BufferedWriter out = Files.newBufferedWriter(outputFile, charset)) {
                String line;
                while ((line = in.readLine()) != null) {
                    SHA256FileEncryptor fileEncryptor = new SHA256FileEncryptor();

                    try {
                        walkFileOrDir(out, outputFile, Path.of(line), depth);
                    } catch (IOException e) {
                        System.out.println(
                                "Error walking through file/directory " + line + ": " + e.getMessage());
                    } catch (InvalidPathException ignored) {
                        out.write(fileEncryptor.getDefaultEncryptionResult() + " " + line);
                        out.newLine();
                    }
                }
            } catch (IOException e) {
                System.out.println("Can't write in output file " + outputFile + ": " + e.getMessage());
            }
        } catch (IOException e) {
            System.out.println("Can't read input file " + inputFile + ": " + e.getMessage());
        }
    }

    private static void walkFileOrDir(BufferedWriter out, Path outputFile, Path start, int depth)
            throws IOException {

        Files.walkFileTree(start,
                EnumSet.noneOf(FileVisitOption.class), depth,
                new CustomVisitor(outputFile, out));
    }
}
