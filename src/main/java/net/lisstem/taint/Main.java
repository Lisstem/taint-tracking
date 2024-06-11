package net.lisstem.taint;

import net.lisstem.taint.asm.Approach;
import net.lisstem.taint.examples.IntFields;
import net.lisstem.taint.examples.benchmark.PrimeFactorization;
import net.lisstem.taint.examples.methodinvocation.JDKIntArray;
import net.lisstem.taint.examples.methodinvocation.StaticIntArray;
import net.lisstem.taint.taint.TaintedInt;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;
import org.objectweb.asm.util.Textifier;

import java.io.*;


public class Main {
    public static void main(String[] args) throws IOException {
        ClassReader classReader = new ClassReader(Type.getInternalName(IntFields.class));
        ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES);
        CheckClassAdapter checkClassAdapter = new CheckClassAdapter(classWriter);
        Textifier textifier = new Textifier();
        PrintWriter printWriter = new PrintWriter(System.out);
        TraceClassVisitor traceClassVisitor = new TraceClassVisitor(classWriter, textifier, printWriter);
        ClassVisitor adapter = Approach.BOX.getAdapter(traceClassVisitor, false);
        classReader.accept(adapter, 0);
        try (FileOutputStream out = new FileOutputStream("/home/lisstem/Documents/ba/java/out/net/lisstem/taint/examples/methodinvocation/StaticIntArray.class")) {
            out.write(classWriter.toByteArray());
        }
        TaintedInt foo = new TaintedInt(5);
        System.out.println(foo);
    }
}
