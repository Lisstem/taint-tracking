package net.lisstem.taint.asm;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import static org.objectweb.asm.Opcodes.*;

public final class MethodHelper {
    public static final String EMPTY_DESC = "()V";
    public static final String CONSTRUCTOR_NAME = "<init>";
    public static final String CLASS_INITIALIZER_NAME = "<clinit>";
    public static final String CLASS_INITIALIZER_DESC = EMPTY_DESC;

    public static boolean isConstructor(String name) {
        return CONSTRUCTOR_NAME.equals(name);
    }
    public static boolean isConstructor(MethodNode method) {
        return isConstructor(method.name);
    }

    public static MethodNode classInitializer() {
        MethodNode classInitializer = new MethodNode(ASM9, ACC_STATIC, CLASS_INITIALIZER_NAME, CLASS_INITIALIZER_DESC, null, null);
        classInitializer.instructions.add(new InsnNode(RETURN));
        classInitializer.maxStack = 0;
        classInitializer.maxLocals = 0;
        return classInitializer;
    }

    public static boolean isClassInitializer(String name) {
        return CLASS_INITIALIZER_NAME.equals(name);
    }
    public static boolean isClassInitializer(MethodNode method) {
        return isClassInitializer(method.name);
    }

    public static boolean isStatic(MethodNode method) {
        return AccessHelper.isStatic(method);
    }

    public static boolean returnsInt(Type methodType) {
        return Type.INT_TYPE.equals(methodType.getReturnType());
    }

    public static boolean returnsInt(String methodDescriptor) {
        return Type.INT_TYPE.equals(Type.getMethodType(methodDescriptor).getReturnType());
    }

    public static boolean returnsInt(MethodInsnNode method) {
        return returnsInt(method.desc);
    }

    public static boolean returnsInteger(@NotNull Type methodType) {
        return TypeHelper.isIntegerType(methodType.getReturnType());
    }

    public static boolean hasIntArg(@NotNull Type methodType) {
        for (Type type: methodType.getArgumentTypes()) {
            if (type.equals(Type.INT_TYPE))
                return true;
        }
        return false;
    }

    public static final Type OBJECT = Type.getType(Object.class);

    public static boolean hasIntegerArgs(@NotNull Type methodType) {
        for (Type arg: methodType.getArgumentTypes()) {
            if (TypeHelper.isIntegerType(arg) ||  TypeHelper.isIntArray(arg) ||
                    OBJECT.equals(arg) || TypeHelper.isArrayOf(arg, OBJECT))
                return true;
        }
        return false;
    }

    public static boolean hasNonIntIntegerArgs(Type methodType) {
        for (Type type: methodType.getArgumentTypes()) {
            if (TypeHelper.isIntegerType(type) && !Type.INT_TYPE.equals(methodType))
                return true;
        }
        return false;
    }

    public static boolean invokesOnObject(@NotNull MethodInsnNode method) {
        return invokesOnObject(method.getOpcode());
    }

    public static boolean invokesOnObject(int opcode) {
        return switch (opcode) {
            case INVOKEVIRTUAL, INVOKEINTERFACE, INVOKESPECIAL -> true;
            case INVOKEDYNAMIC, INVOKESTATIC ->  false;
            default -> throw new IllegalArgumentException("Not an invocation");
        };
    }
}
