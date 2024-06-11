package net.lisstem.taint;

import net.lisstem.taint.examples.FieldsWithValues;
import net.lisstem.taint.examples.IntFields;
import net.lisstem.taint.examples.benchmark.Fibonacci;
import net.lisstem.taint.examples.benchmark.PrimeFactorization;
import net.lisstem.taint.examples.benchmark.SumEven;
import net.lisstem.taint.examples.methodinvocation.StaticIntArray;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.IOException;
import java.io.PrintWriter;

public class DisplayClass {
    public static void main(String[] args) throws IOException {
        ClassReader classReader = new ClassReader(Type.getInternalName(Fibonacci.class));
        PrintWriter printWriter = new PrintWriter(System.out);
        TraceClassVisitor traceClassVisitor = new TraceClassVisitor(null, new Textifier(), printWriter);
        classReader.accept(traceClassVisitor, 0);
    }
}
