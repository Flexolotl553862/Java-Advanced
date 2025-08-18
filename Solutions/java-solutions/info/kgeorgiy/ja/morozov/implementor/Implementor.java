package info.kgeorgiy.ja.morozov.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.tools.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.jar.JarOutputStream;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

/**
 * This class implements {@link Impler} and {@link JarImpler} using reflection.
 * This class can be used to generate implementation for other classes or interfaces that can be implemented.
 * Implementation can be saved as `.java` or `.jar` file.
 *
 * @see info.kgeorgiy.java.advanced.implementor.Impler
 * @see info.kgeorgiy.java.advanced.implementor.tools.JarImpler
 */
public class Implementor implements Impler, JarImpler {
    /**
     * Whitespace character that will be used in class/interface implementation.
     */
    private static final String WS = " ";
    /**
     * Line separator that will be used in class/interface implementation.
     */
    private static final String SEPARATOR = System.lineSeparator();
    /**
     * Tabulation symbol that will be used in class/interface implementation.
     */
    private static final String TAB = WS.repeat(4);

    /**
     * Default constructor for class.
     */
    public Implementor() {
    }


    /**
     * Returns a header of function, constructor or method with given name, modifiers,
     * parameters, returning value and thrown exceptions. Header ends with "{".
     *
     * @param modifiersAndReturnValue A string contained modifiers and return value separated by whitespaces
     * @param name                    A name of function/method/constructor
     * @param parameters              Parameters of function/method/constructor.
     * @param exceptions              Exceptions that can be thrown
     * @return A string representing the header of function/method/constructor
     */
    private String createFunctionHeader(String modifiersAndReturnValue, String name, Parameter[] parameters, Class<?>[] exceptions) {
        String createParameters = Arrays.stream(parameters)
                .map(p -> p.getType().getCanonicalName() + WS + p.getName())
                .collect(Collectors.joining(", "));

        String createExceptions = "";
        if (exceptions.length > 0) {
            createExceptions = "throws " + Arrays.stream(exceptions)
                    .map(Class::getCanonicalName)
                    .collect(Collectors.joining(", ")) + " ";
        }

        return modifiersAndReturnValue + WS +
                name +
                "(" + createParameters + ")" + WS +
                createExceptions + "{";
    }

    /**
     * Creates a header of method {@code method} with given modifiers, name and signature.
     * This method invokes {@link Implementor#createFunctionHeader} with parameters derived from {@param method}.
     *
     * @param method The method that has to be created.
     * @return A string representing the method's header.
     */
    private String createMethodHeader(Method method) {
        String modifiers = Modifier.toString(method.getModifiers()
                & ~Modifier.ABSTRACT & ~Modifier.NATIVE & ~Modifier.TRANSIENT);

        return createFunctionHeader(modifiers + WS + method.getReturnType().getCanonicalName(),
                method.getName(), method.getParameters(), method.getExceptionTypes());
    }

    /**
     * Creates a method/constructor/function depending on header and body of function.
     * This method combines strings and inserts the necessary tabs and line separators.
     *
     * @param header header of method/constructor/function. For example, "public void foo(int x) throws IOException {"
     * @param body   body of method/constructor/function.
     * @return A string representing java code of method/constructor/function
     */
    private String createFunction(String header, String body) {
        return TAB + header + SEPARATOR +
                TAB + TAB + body + SEPARATOR +
                TAB + "}";
    }

    /**
     * Creates a header of constructor {@code constructor} with given modifiers and signature.
     * This method invokes {@link Implementor#createFunctionHeader} with parameters derived from {@param constructor}.
     *
     * @param constructor The constructor that has to be created.
     * @return A string representing the constructor's header.
     */
    private String createConstructorHeader(Constructor<?> constructor) {
        return createFunctionHeader("public",
                constructor.getDeclaringClass().getSimpleName() + "Impl",
                constructor.getParameters(),
                constructor.getExceptionTypes());
    }

    /**
     * Creates body of {@code constructor} that call's the parent constructor with parameters
     * given by {@link Constructor#getParameters()}.
     *
     * @param constructor The constructor that has to be created.
     * @return A body of constructor with signature given by {@link Constructor#getParameters()}.
     */
    private String createConstructorBody(Constructor<?> constructor) {
        String parameters = Arrays.stream(constructor.getParameters())
                .map(Parameter::getName)
                .collect(Collectors.joining(", "));
        return "super (" + parameters + ");";
    }

    /**
     * Returns a string in the form "return T;", where T depends on the provided token.
     * T is:
     * "" for {@code void},
     * "false" for {@link Boolean} or {@code bool},
     * "0" for other primitives,
     * "null" for reference types.
     *
     * @param token The type of the function's return value, used to determine the default return value.
     * @return A string representing the return statement.
     */
    private String createReturnValue(Class<?> token) {
        if (token == void.class) {
            return "return;";
        }
        if (token == Boolean.class || token == boolean.class) {
            return "return false;";
        }
        if (token.isPrimitive()) {
            return "return 0;";
        }
        return "return null;";
    }

    /**
     * Creates a class that implements or extends the specified {@code token}.
     *
     * @param token The class or interface for which an implementation or extension is to be generated.
     * @return A string representing the Java code of a class that implements or extends the specified {@code token}.
     * @throws ImplerException If an error occurs while generating the implementation of the class or interface.
     */
    private String generateImplementation(Class<?> token) throws ImplerException {
        if (Modifier.isFinal(token.getModifiers())
                || Modifier.isPrivate(token.getModifiers())
                || token == Enum.class
                || token == Record.class
                || token.isPrimitive()
                || token.isArray()
                || token.isSealed()) {
            throw new ImplerException("Can't implement " + token.getCanonicalName());
        }

        String impl = "extends";
        if (Modifier.isInterface(token.getModifiers())) {
            impl = "implements";
        }

        StringBuilder body = new StringBuilder();
        String className = token.getSimpleName() + "Impl";

        if (token.getPackageName().startsWith("java.")
                || token.getPackageName().startsWith("javax.")
                || token.getPackageName().startsWith("sun.")) {
            throw new ImplerException(
                    "Can't implement " + token.getCanonicalName() + " . It contains in system package"
            );
        }
        body.append("package ").append(token.getPackage().getName()).append(";").append(SEPARATOR).append(SEPARATOR);

        body.append("public")
                .append(WS).append("class")
                .append(WS).append(className)
                .append(WS).append(impl)
                .append(WS).append(token.getCanonicalName()).append(" {").append(SEPARATOR);

        boolean hasConstructor = false;
        for (Constructor<?> constructor : token.getDeclaredConstructors()) {
            if (!Modifier.isPrivate(constructor.getModifiers())) {
                body.append(createFunction(createConstructorHeader(constructor), createConstructorBody(constructor)))
                        .append(SEPARATOR).append(SEPARATOR);
                hasConstructor = true;
            }
        }

        if (!hasConstructor && !Modifier.isInterface(token.getModifiers())) {
            throw new ImplerException("Can't implement " + token.getCanonicalName());
        }

        HashSet<Method> methods = new HashSet<>(Arrays.asList(token.getMethods()));
        methods.addAll(Arrays.asList(token.getDeclaredMethods()));
        for (Method method : methods) {
            if (Modifier.isAbstract(method.getModifiers())) {
                body.append(createFunction(createMethodHeader(method), createReturnValue(method.getReturnType())))
                        .append(SEPARATOR).append(SEPARATOR);
            }
        }
        body.append("}");

        return body.toString();
    }

    /**
     * Returns the path to the `.java` file that contains the implementation of the specified {@code token}.
     *
     * @param token The class or interface that needs to be implemented.
     * @param root  Root directory of package.
     * @return The path to the `.java` file that implements {@code token}.
     */
    Path getPathToFile(Class<?> token, Path root) {
        return root.resolve(token.getPackageName().replace('.', File.separatorChar))
                .resolve(token.getSimpleName() + "Impl.java");
    }

    /**
     * Converts string to unicode.
     *
     * @param str the input string.
     * @return the unicode representation of the string.
     */
    private String toUnicode(String str) {
        StringBuilder unicodeString = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            unicodeString.append(String.format("\\u%04X", (int) str.charAt(i)));
        }
        return unicodeString.toString();
    }

    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        try {
            Path outputFile = getPathToFile(token, root);

            if (!Files.exists(outputFile)) {
                if (outputFile.getParent() != null) {
                    Files.createDirectories(outputFile.getParent());
                }
                Files.createFile(outputFile);
                Files.write(outputFile, toUnicode(generateImplementation(token)).getBytes());
            }
        } catch (InvalidPathException e) {
            throw new ImplerException("Invalid root path: " + root, e);
        } catch (IOException e) {
            throw new ImplerException("Can't create output file: " + root, e);
        }
    }

    /**
     * Compiles the source `.java` file at the given path {@code file} to a class file.
     *
     * @param token The class or interface that is being implemented.
     * @param file  The path to the `.java` source file that contains the implementation of the class or interface.
     * @throws ImplerException If an error occurs while compiling the source file into a class file.
     */
    private void compile(final Class<?> token, Path file) throws ImplerException {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new ImplerException("Could not find java compiler, include tools.jar to classpath");
        }

        final Path classpath;

        try {
            classpath = Path.of(token.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (URISyntaxException e) {
            throw new ImplerException("Could not get path to dependency " + token.getCanonicalName(), e);
        }

        final int exitCode = compiler.run(
                null,
                null,
                null,
                "-cp",
                classpath.toString(),
                "-encoding",
                StandardCharsets.UTF_8.name(),
                file.toString()
        );
        if (exitCode != 0) {
            throw new ImplerException("Compiler exit code is " + exitCode, null);
        }
    }

    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        Path root = jarFile.getParent();

        implement(token, root);
        compile(token, getPathToFile(token, root));

        Path pathToClassFile = Path.of(
                getPathToFile(token, root).getParent().resolve(token.getSimpleName() + "Impl") + ".class");

        try (JarOutputStream outputStream = new JarOutputStream(new FileOutputStream(jarFile.toFile()))) {

            outputStream.putNextEntry(new ZipEntry(
                    token.getPackageName().replace('.', '/') +
                            "/" + token.getSimpleName() + "Impl.class"));

            Files.copy(pathToClassFile, outputStream);
        } catch (FileNotFoundException | SecurityException e) {
            throw new ImplerException("Can't create output file: " + pathToClassFile, e);
        } catch (IOException e) {
            throw new RuntimeException("Other exception", e);
        }
    }

    /**
     * Creates a `.jar` or `.java` file with the implementation of the given class or interface
     * and places it at the specified path.
     *
     * <p>The method can create either a `.jar` file or a `.java` file, depending on the provided arguments.</p>
     *
     * @param args The command-line arguments:
     *             <ul>
     *             <li>First: "-jar" if a `.jar` file should be created, otherwise leave empty for a `.java` file.</li>
     *             <li>Second: The name of the class or interface to implement.</li>
     *             <li>Third: The path where the implementation file should be placed. Use the root directory for a `.java` file, or specify the desired path for a `.jar` file.</li>
     *             </ul>
     */
    public static void main(String[] args) {
        if (args == null || args.length < 2) {
            System.out.println("Usage: java -jar implementor.jar <classname> <outputfile.jar> or " +
                    "java implementor.jar <classname> <root-directory>");
            return;
        }

        Implementor implementor = new Implementor();

        try {
            if (args[0].equals("-jar") && args.length == 3) {
                implementor.implementJar(Class.forName(args[1]), Path.of(args[2]));
            } else if (args.length == 2) {
                implementor.implement(Class.forName(args[0]), Path.of(args[1]));
            } else {
                System.out.println("Unexpected arguments: " + Arrays.toString(args));
            }
        } catch (ClassNotFoundException e) {
            System.out.println("Can't find class " + args[1]);
        } catch (ImplerException e) {
            System.out.println(e.getMessage());
        }
    }
}