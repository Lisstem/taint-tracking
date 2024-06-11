package net.lisstem.taint;

import org.objectweb.asm.*;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import static org.objectweb.asm.Opcodes.*;

public class TestWriter {

    private static void visitMain(ClassVisitor cv, String name) {
        MethodVisitor mv = cv.visitMethod(ACC_PUBLIC | ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
        mv.visitCode();
        mv.visitFieldInsn(GETSTATIC, name, "bool1", "Z");
        mv.visitIntInsn(BIPUSH, 42);
        mv.visitInsn(IMUL);
        mv.visitFieldInsn(PUTSTATIC, name, "int1", "I");
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        mv.visitFieldInsn(GETSTATIC, name, "int1", "I");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V", false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(2, 1);
        mv.visitEnd();
    }

    private static String multiplyBoolean(ClassVisitor cv) {
        String name = "MultiplyBoolean";
        cv.visit(V1_7, ACC_PUBLIC, name, null, "java/lang/Object", new String[]{});
        cv.visitField(ACC_PRIVATE | ACC_STATIC, "bool1", "Z", null, true);
        cv.visitField(ACC_PRIVATE | ACC_STATIC, "int1", "I", null, 0);
        visitMain(cv, name);
        cv.visitEnd();

        return name;
    }

    public static void main(String[] args) {
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        CheckClassAdapter checkClassAdapter = new CheckClassAdapter(classWriter);
        Textifier textifier = new Textifier();
        PrintWriter printWriter = new PrintWriter(System.out);
        TraceClassVisitor traceClassVisitor = new TraceClassVisitor(classWriter, textifier, printWriter);

        String name = multiplyBoolean(traceClassVisitor);

        try (FileOutputStream out = new FileOutputStream("/home/lisstem/Documents/ba/java/out/" + name + ".class")) {
            out.write(classWriter.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
