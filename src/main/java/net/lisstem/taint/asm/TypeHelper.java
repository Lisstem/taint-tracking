package net.lisstem.taint.asm;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.LocalVariableNode;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.objectweb.asm.Opcodes.*;

public class TypeHelper {

    public static @NotNull Type createArrayType(int dim, @NotNull Type elementType) {
        return Type.getType("[".repeat(dim) + elementType.getDescriptor());
    }

    public static Type reduceArrayType(@NotNull Type arrayType) {
        if (arrayType.getSort() != Type.ARRAY)
            return null;

        if (arrayType.getDimensions() == 1)
            return arrayType.getElementType();
        else
            return createArrayType(arrayType.getDimensions() - 1, arrayType.getElementType());
    }

    public static Type replaceIntVar(Type var, Type targetType) {
        if (var == Type.INT_TYPE)
            return targetType;
        if (isIntArray(var))
            return createArrayType(var.getDimensions(), targetType);

        return var;
    }

    public static boolean isComputationalType2(Type type) {
        return Type.LONG_TYPE.equals(type) || Type.DOUBLE_TYPE.equals(type);
    }

    public static boolean isComputationalType2(int sort) {
        return sort == Type.DOUBLE || sort == Type.LONG;
    }

    public static boolean isComputationalType2(LocalVariableNode var) {
        return isComputationalType2(Type.getType(var.desc));
    }

    public static int returnFor(Type type) {
        return switch (type.getSort()) {
            case Type.VOID -> RETURN;
            case Type.BOOLEAN, Type.CHAR, Type.BYTE, Type.SHORT, Type.INT -> IRETURN;
            case Type.FLOAT -> FRETURN;
            case Type.LONG -> LRETURN;
            case Type.DOUBLE -> DRETURN;
            case Type.OBJECT, Type.ARRAY -> ARETURN;
            default -> -1;
        };
    }


    public static @NotNull Type replaceIntegerVar(@NotNull Type var, @NotNull Type targetType) {
        if (isIntegerType(var))
            return targetType;
        if (isIntArray(var))
            return createArrayType(var.getDimensions(), targetType);

        return var;
    }

    static Set<String> JDKPackages = new HashSet<>();
    static {
        JDKPackages.addAll(Arrays.asList("sun/",
                "com/sun/",
                "jdk/",
                "java/",
                "sun/",
                "javax/",
                "joptsimple/",
                "net/sf/jopt-simple/",
                "org/objectweb/asm/",
                "org/openjdk/jmh/",
                "org/apache/commons/commons-math3/",
                "org/apache/commons/math3",
                "net/lisstem/taint/jmh",
                "net/lisstem/taint/jdk",
                "net/lisstem/taint/taint"));
    }


    public static boolean isJdkClass(@NotNull String clazz) {
        return JDKPackages.stream().anyMatch(clazz::startsWith);
    }

    public static boolean requiresUnboxing(@NotNull Type method) {
        for (Type param: method.getArgumentTypes()) {
            if (TypeHelper.isIntegerOrIntArray(param))
                return true;
        }
        return false;
    }

    public static boolean isIntegerType(Type type) {
        return type == Type.INT_TYPE || type == Type.BYTE_TYPE ||
                type == Type.SHORT_TYPE || type == Type.BOOLEAN_TYPE ||
                type == Type.CHAR_TYPE;
    }

    public static boolean isIntegerType(String descriptor) {
        return isIntegerType(Type.getType(descriptor));
    }

    public static boolean isIntegerType(LocalVariableNode var) {
        return isIntegerType(Type.getType(var.desc));
    }

    public static boolean isArrayOf(Type toTest, Type elementType) {
        return toTest != null && toTest.getSort() == Type.ARRAY && elementType.equals(toTest.getElementType());
    }

    public static boolean isArrayOf(LocalVariableNode var, Type elementType) {
        return isArrayOf(Type.getType(var.desc), elementType);
    }

    public static boolean isTypeOrArrayOf(Type toTest, Type type) {
        return type.equals(toTest) || isArrayOf(toTest, type);
    }

    public static boolean isIntTypeOrArray(Type type) {
        return isTypeOrArrayOf(type, Type.INT_TYPE);
    }

    public static boolean isIntegerOrIntArray(Type type) {
        return isIntegerType(type) || isIntArray(type);
    }

    public static boolean isIntegerOrIntArray(LocalVariableNode var) {
        return isIntegerOrIntArray(Type.getType(var.desc));
    }

    public static boolean isIntArray(Type type) {
        return isArrayOf(type, Type.INT_TYPE);
    }

    public static @NotNull String intDesc(@NotNull Type type) {
        return switch (type.getSort()) {
            case Type.INT, Type.BOOLEAN, Type.CHAR, Type.LONG, Type.FLOAT, Type.SHORT, Type.BYTE, Type.DOUBLE
                    -> type.getDescriptor();
            case Type.OBJECT -> "A";
            case Type.ARRAY -> intDesc(type.getElementType()) + type.getDimensions();
            case Type.METHOD -> filterInts(type);
            default -> "V";
        };
    }

    @Contract("_ -> new")
    public static @NotNull String filterInts(@NotNull Type type) {
        return Arrays.stream(type.getArgumentTypes()).map(TypeHelper::intDesc).collect(Collectors.joining())
                + "_" + intDesc(type.getReturnType());
    }

    public static @NotNull String MakeWrappedName(@NotNull String owner, @NotNull String name, @NotNull Type replaced, @NotNull Type original) {
        return "$wrapped_" + owner.replace('/', '_') + '$' + name + filterInts(original) + "__" + filterInts(replaced);
    }

    public static boolean isIntegerType(FieldNode field) {
        return isIntegerType(field.desc);
    }

    public static Type replaceIntegerMethod(@NotNull Type method, @NotNull Type targetType) {
        Type ret = replaceIntVar(method.getReturnType(), targetType);
        Type[] args = method.getArgumentTypes();
        for (int i = 0; i < args.length; i++) {
            args[i] = TypeHelper.replaceIntegerVar(args[i], targetType);
        }
        return Type.getMethodType(ret, args);
    }


    public static boolean isInt(LocalVariableNode variable) {
        return isInt(variable.desc);
    }

    public static boolean isInt(String typeDescriptor) {
        return Type.getType(typeDescriptor).equals(Type.INT_TYPE);
    }

    public static boolean isInt(FieldNode field) {
        return isInt(field.desc);
    }
}
