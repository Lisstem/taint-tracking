package net.lisstem.taint.asm;

import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import static org.objectweb.asm.Opcodes.*;
public class AccessHelper {
    public static boolean isStatic(int access) {
        return (access & ACC_STATIC) != 0;
    }

    public static boolean isStatic(MethodNode method) {
        return isStatic(method.access);
    }

    public static boolean isStatic(FieldNode field) {
        return isStatic(field.access);
    }
    
    public static boolean isNotStatic(int access) {
        return !isStatic(access);
    }
    
    public static boolean isNotStatic(MethodNode method) {
        return isNotStatic(method.access);
    }

    public static boolean isNotStatic(FieldNode field) {
        return isNotStatic(field.access);
    }
}
