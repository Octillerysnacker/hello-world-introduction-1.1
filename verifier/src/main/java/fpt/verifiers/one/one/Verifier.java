package fpt.verifiers.one.one;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;

import fpt.verifiers.core.IVerifier;
import fpt.verifiers.core.VerifierResult;
import fpt.verifiers.core.FPTDiagnostic.DiagnosticKind;
import fpt.verifiers.core.tools.DefaultFPTCompilerFactory;
import fpt.verifiers.core.FPTDiagnostic;
import fpt.verifiers.core.FPTDiagnosticUtil;

public class Verifier implements IVerifier {
    @Override
    public VerifierResult Verify(Path userDirectory, Path projectDirectory) throws IOException {
        // Start programming here!
        var factory = new DefaultFPTCompilerFactory();
        var compiler = factory.CreateCompiler();
        var compiledDirectory = userDirectory.resolve("compiled");
        if (compiler.compileToDir(projectDirectory, compiledDirectory)) {
            try (var loader = new URLClassLoader(new URL[] { compiledDirectory.toUri().toURL() })) {
                try {
                    var exerciseClass = loader.loadClass("Exercise");
                    var mainMethod = exerciseClass.getMethod("main", String[].class);
                    try (var testBaos = new ByteArrayOutputStream()) {
                        try (var testOut = new PrintStream(testBaos)) {
                            var actualOut = System.out;
                            System.setOut(testOut);
                            mainMethod.invoke(null, new Object[] { null });
                            System.setOut(actualOut);
                        }
                        var result = testBaos.toString();
                        if (result.trim().toLowerCase().equals("hello world!")) {
                            return new VerifierResult(true);
                        } else {
                            return new VerifierResult(false, new FPTDiagnostic[] { new FPTDiagnostic(
                                    "A line that was not \"System.out.print(\"Hello World!\");\" was placed in Exercise.java. Please delete that code and try again.",
                                    DiagnosticKind.Error) });
                        }
                    }

                } catch (ClassNotFoundException e) {
                    return new VerifierResult(false, new FPTDiagnostic[] { new FPTDiagnostic(
                            "Could not find the class \"Exercise\". The class name may have been changed. Please reset Exercise.java.",
                            DiagnosticKind.Error) });
                } catch (NoSuchMethodException e) {
                    return new VerifierResult(false, new FPTDiagnostic[] { new FPTDiagnostic(
                            "Could not find the main method. The method may have been deleted or renamed. Please reset Exercise.java.",
                            DiagnosticKind.Error) });
                } catch (SecurityException e) {
                    return new VerifierResult(false, new FPTDiagnostic[] { new FPTDiagnostic(
                            "Exercise.main is not public, indicating that the method signature may have been changed. Please reset Exercise.java.",
                            DiagnosticKind.Error) });
                } catch (IllegalAccessException e) {
                    return new VerifierResult(false, new FPTDiagnostic[] { new FPTDiagnostic(
                            "Exercise is corrupted and attempting to access unknown classes. Please reset Exercise.java or ask your mentor for help.",
                            DiagnosticKind.Error) });
                } catch (IllegalArgumentException e) {
                    return new VerifierResult(false,
                            new FPTDiagnostic[] { new FPTDiagnostic(
                                    "The exercise was unable to be verified. Please ask your mentor for help.",
                                    DiagnosticKind.Error) });
                } catch (InvocationTargetException e) {
                    return new VerifierResult(false,
                            new FPTDiagnostic[] { new FPTDiagnostic(
                                    "Exercise.main threw an exceptin with the following message: \"" + e.getMessage()
                                            + "\". Please reset Exercise.java or ask your mentor for help.",
                                    DiagnosticKind.Error) });
                }
            }
        } else {
            return new VerifierResult(false,
                    FPTDiagnosticUtil.fromDiagnosticCollector(factory.getDefaultDiagnosticCollector()));
        }
    }
}