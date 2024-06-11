package net.lisstem.taint.asm.wrapint;

import net.lisstem.taint.asm.TypeHelper;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.Objects;

import static org.objectweb.asm.Opcodes.*;

public class DynamicBoxingMethodWrapper extends BoxingMethodWrapper {
    private InvokeDynamicInsnNode wrapped;
    public DynamicBoxingMethodWrapper(@NotNull InvokeDynamicInsnNode method, @NotNull Type targetType, @NotNull String className) {
        super(targetType);

        wrap(method, className);
    }

    @Override
    public @NotNull AbstractInsnNode getWrappedInsn() {
        return wrapped;
    }

    private void wrap(@NotNull InvokeDynamicInsnNode method, String className) {
        // TODO: Support dynamic invocation for non jdk classes
        Type type = Type.getMethodType(method.desc);

        boxingRequired = TypeHelper.isIntegerType(type.getReturnType());
        wrapperRequired = TypeHelper.requiresUnboxing(type);
        returnDim = type.getReturnType().getSort() == Type.ARRAY ? type.getReturnType().getDimensions() : 0;

        if (!TypeHelper.isJdkClass(method.bsm.getOwner()))
            System.out.println("Warning dynamic invocation of non jdk classes is not supported");

        if (wrapperRequired) {
            wrapped = (InvokeDynamicInsnNode) method.clone(null);
            Type replaced = TypeHelper.replaceIntegerMethod(type, targetType);
            wrapper = new MethodInsnNode(INVOKESTATIC, className, TypeHelper.MakeWrappedName("", method.name, replaced, type), replaced.getDescriptor(), false);
        } else {
            wrapper = null;
            wrapped = method;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DynamicBoxingMethodWrapper that = (DynamicBoxingMethodWrapper) o;
        return AreMethodsEqual(wrapper, that.wrapper);
    }

    @Override
    public int hashCode() {
        return Objects.hash(wrapped.name, wrapped.desc);
    }

    private static boolean AreMethodsEqual(MethodInsnNode first, MethodInsnNode second) {
        return first != null && second != null && first.name.equals(second.name);
    }
}
