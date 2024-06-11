package net.lisstem.taint.agent;

import net.lisstem.taint.asm.Approach;
import net.lisstem.taint.asm.TypeHelper;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.FileOutputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

public class Agent implements ClassFileTransformer {

    private final Approach approach;
    private static final boolean OUTPUT = true;

    private static final boolean USE_ANNOTATIONS = true;

    public Agent(String approach) {
        this.approach = Approach.get(approach);
    }

    public boolean transforms() {
        return approach != Approach.NONE;
    }



    public static void premain(String args, Instrumentation inst) {
        System.out.println("Starting application with Taint Tracking!");
        Agent agent = new Agent(args);
        System.out.println("Using taint method: " + agent.getApproach().name());
        if (agent.transforms())
            inst.addTransformer(agent);
    }

    public Approach getApproach() {
        return approach;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (TypeHelper.isJdkClass(className) || approach == Approach.NONE) {
            System.out.println(className + " is a JDK class");
            return classfileBuffer;
        }
        if (OUTPUT) System.out.println("transforming class " + className);
        try {
            ClassReader cr = new ClassReader(classfileBuffer);
            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
            ClassVisitor adapter = approach.getAdapter(cw, !USE_ANNOTATIONS);
            cr.accept(adapter, 0);
            if (OUTPUT) {
                String path = approach.name() + className.replace('/', '-') + ".class";
                try (FileOutputStream out = new FileOutputStream(path)) {
                    out.write(cw.toByteArray());
                }
            }
            return cw.toByteArray();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new IllegalClassFormatException(className + " : " + ex.getMessage());
        }
    }
}
