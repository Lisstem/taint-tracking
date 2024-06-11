package net.lisstem.taint.test;

import net.lisstem.taint.asm.Approach;
import net.lisstem.taint.asm.wrapint.TaintClassAdapter;
import net.lisstem.taint.taint.BooleanTaint;
import net.lisstem.taint.taint.TaintedInt;
import net.lisstem.taint.util.CountBoolean;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static java.lang.System.exit;

public class TaintExamples {
    private static Path rootDir;
    private static Path inDir;
    private static Path outDir;

    private static Approach approach;

    private final static String buildDir = "build/classes/java/main";
    private final static String packageName = "net/lisstem/taint";
    private final static PrintStream log = System.out;
    private final static PrintStream err = System.err;

    private static String red(String string) {
        return "\u001B[31m" + string + "\u001B[0m";
    }
    private static String green(String string) {
        return  "\u001B[32m" + string + "\u001B[0m";
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            err.println(red("No root directory given"));
            exit(1);
        } else if (args.length < 2) {
            err.println(red("No input directory given"));
            exit(2);
        } else if (args.length < 3) {
            err.println(red("No output directory given"));
            exit(3);
        } else if (args.length < 4) {
            err.println(red("No approach given (wrap or shadow)"));
            exit(4);
        }

        rootDir = Paths.get(args[0]);
        inDir = Paths.get(args[1]);
        outDir = Paths.get(args[2]);
        approach = Approach.get(args[3]);


        log.println("Root Directory:   " + rootDir);
        log.println("Input Directory:  " + inDir);
        log.println("Output Directory: " + outDir);
        log.println("Build Directory:  " + buildDir);
        log.println("Package name:     " + packageName.replace('/', '.'));
        log.println("Adapter:          " + approach.name());

        outDir = rootDir.resolve(outDir).resolve(packageName).resolve(inDir);
        inDir = rootDir.resolve(buildDir).resolve(packageName).resolve(inDir);

        int[] values = taintAll();
        if (values[CountBoolean.COUNT] < 0) {
            err.println(red("Could not taint examples"));
            exit(42);
        }
        String result = values[CountBoolean.COUNT] + " classes, " + values[CountBoolean.TRUE] + " tainted, "
                + values[CountBoolean.FALSE] + " failed\n";
        log.println("\nResult: " + (values[CountBoolean.FALSE] == 0 ? green(result) : red(result)));
    }

    private static int[] taintAll() {
        try (Stream<Path> files = Files.walk(inDir)) {
            return files.filter(Files::isRegularFile).map(path -> inDir.relativize(path))
                    .map(TaintExamples::taintClass).collect(CountBoolean.get());
        } catch (IOException ex) {
            ex.printStackTrace(err);
            return new int[]{-1, -1, -1};
        }
    }

    private static boolean taintClass(Path relPath) {
        String className = relPath.getFileName().toString();
        Path dir = relPath.getParent() == null ? outDir : outDir.resolve(relPath.getParent());
        log.print("Tainting " + className + "  ...");
        File file = dir.toFile();
        if (!file.isDirectory() && !file.mkdirs()) {
            log.println(red("  failed :("));
            err.println(red("Could not create output directory: " + file.getPath()));
            return false;
        }
        try (FileInputStream input = new FileInputStream(inDir.resolve(relPath).toString());
        FileOutputStream log = new FileOutputStream(dir.resolve(className + ".log").toString())) {
            ClassReader classReader = new ClassReader(input);
            ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES);
            CheckClassAdapter checkClassAdapter = new CheckClassAdapter(classWriter);
            Textifier textifier = new Textifier();
            PrintWriter printWriter = new PrintWriter(log);
            TraceClassVisitor traceClassVisitor = new TraceClassVisitor(classWriter, textifier, printWriter);
            ClassVisitor adapter = approach.getAdapter(traceClassVisitor, true);
            classReader.accept(adapter, 0);
            try (FileOutputStream out = new FileOutputStream(dir.resolve(className).toString())) {
                out.write(classWriter.toByteArray());
            }
        } catch (Exception ex) {
            log.println(red("  failed :("));
            return false;
        }
        log.println(green("  successful"));
        return true;
    }
}
