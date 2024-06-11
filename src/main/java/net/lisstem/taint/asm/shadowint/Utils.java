package net.lisstem.taint.asm.shadowint;

import net.lisstem.taint.asm.MethodHelper;
import net.lisstem.taint.asm.TypeHelper;
import net.lisstem.taint.taint.TaintBox;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.ArrayList;
import java.util.List;

import static org.objectweb.asm.tree.AbstractInsnNode.*;

public class Utils {
    public static String shadowVariableName(String name) {
        return "$shadow_" + name;
    }

    public static Type shadowType(Type toShadow, Type shadowType) {
        return switch (toShadow.getSort()) {
            case Type.INT, Type.BYTE, Type.BOOLEAN, Type.SHORT, Type.CHAR -> shadowType;
            case Type.ARRAY -> {
                if (toShadow.getElementType().equals(Type.INT_TYPE)) {
                    yield TypeHelper.createArrayType(toShadow.getDimensions(), shadowType);
                }
                yield null;
            }
            default -> null;
        };
    }

    public static boolean isShadowType(Type type, Type shadowType) {
        return type.equals(shadowType) || TypeHelper.isArrayOf(type, shadowType);
    }

    public static Type shadowType(LocalVariableNode toShadow, Type shadowType) {
        return shadowType(Type.getType(toShadow.desc), shadowType);
    }
    public static Type shadowedMethodType(Type methodType, Type shadowType) {
        return shadowMethodType(methodType, shadowType, null, true);
    }

    public static Type shadowMethodType(Type methodType, Type shadowType, Type instanceType, boolean replaceReturnType) {
        Type[] args = methodType.getArgumentTypes();
        List<Type> newArgs = new ArrayList<>(args.length * 2 + 1);
        if (instanceType != null)
            newArgs.add(instanceType);

        for (Type type : args) {
            newArgs.add(type);
            if (TypeHelper.isIntegerType(type))
                newArgs.add(shadowType);
            else if (TypeHelper.isIntArray(type)) {
                newArgs.add(TypeHelper.createArrayType(type.getDimensions(), shadowType));
            }
        }
        Type returnType = methodType.getReturnType();
        if (replaceReturnType && TypeHelper.isIntTypeOrArray(returnType)) {
            returnType = Type.getType(TaintBox.class);
        }
        return Type.getMethodType(returnType, newArgs.toArray(new Type[0]));
    }

    public static Type wrappedMethodType(Type methodType, Type shadowType, Type instanceType) {
        return shadowMethodType(methodType, shadowType, instanceType, false);
    }

    public static String shadowedMethodDesc(Type methodType, Type shadowType) {
        return shadowedMethodType(methodType, shadowType).getDescriptor();
    }

    public static boolean returnsTaintedInt(MethodInsnNode insn) {
        return MethodHelper.returnsInt(insn) && !TypeHelper.isJdkClass(insn.owner);
    }

    public static boolean returnsTaintedInt(AbstractInsnNode insn) {
        if (insn.getType() == METHOD_INSN) {
            return returnsTaintedInt((MethodInsnNode) insn);
        }
        return false;
    }

    public static int compareVariables(LocalVariableNode a, LocalVariableNode b) {
        if (a.index < b.index)
            return -1;
        if (a.index > b.index)
            return 1;
        boolean isAInt = TypeHelper.isIntegerOrIntArray(a);
        boolean isBInt = TypeHelper.isIntegerOrIntArray(b);
        if (isAInt & !isBInt) {
            return -1;
        } else if (!isAInt & !isBInt) {
            return 1;
        }
        return 0;
    }
}
