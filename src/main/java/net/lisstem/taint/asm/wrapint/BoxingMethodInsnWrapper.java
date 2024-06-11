package net.lisstem.taint.asm.wrapint;

import net.lisstem.taint.asm.MethodHelper;
import net.lisstem.taint.asm.TypeHelper;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.objectweb.asm.Opcodes.*;

public class BoxingMethodInsnWrapper extends BoxingMethodWrapper {
    private MethodInsnNode wrapped;
    public BoxingMethodInsnWrapper(@NotNull MethodInsnNode method, @NotNull Type targetType, @NotNull String className) {
        super(targetType);
        wrap(method, className);
    }

    @Override
    public @NotNull AbstractInsnNode getWrappedInsn() {
        return wrapped;
    }



    private void wrap(@NotNull MethodInsnNode method, @NotNull String className) {
        Type type = Type.getMethodType(method.desc);
        Type original = type;
        Type replaced = TypeHelper.replaceIntegerMethod(type, targetType);

        if (!TypeHelper.isJdkClass(method.owner)) {
            type = replaced;
        }

        boxingRequired = TypeHelper.isIntegerOrIntArray(type.getReturnType());
        wrapperRequired = TypeHelper.requiresUnboxing(type);
        returnDim = type.getReturnType().getSort() == Type.ARRAY ? type.getReturnType().getDimensions() : 0;

        if (wrapperRequired) {
            wrapped = (MethodInsnNode) method.clone(null);
            wrapper = method;
            if (MethodHelper.invokesOnObject(method))
                wrapper.desc = "(" + Type.getObjectType(method.owner).getDescriptor() + replaced.getDescriptor().substring(1);
            else
                wrapper.desc = replaced.getDescriptor();
            wrapper.setOpcode(INVOKESTATIC);
            wrapper.name = TypeHelper.MakeWrappedName(method.owner, method.name, Type.getMethodType(wrapped.desc), original);
            wrapper.owner = className;
            wrapper.itf = false;
        } else {
            wrapped = method;
        }

        wrapped.desc = type.getDescriptor();
    }

    @Override
    protected List<Type> GetParams(Type[] params) {
        List<Type> list = new ArrayList<>(params.length + 1);
        if (MethodHelper.invokesOnObject(wrapped)) {
            list.add(Type.getObjectType(wrapped.owner));
        }
        list.addAll(Arrays.asList(params));
        return list;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BoxingMethodInsnWrapper that = (BoxingMethodInsnWrapper) o;
        return AreMethodsEqual(wrapped, that.wrapped);
    }

    @Override
    public int hashCode() {
        return Objects.hash(wrapped.getOpcode(), wrapped.owner, wrapped.desc, wrapped.name, wrapped.itf);
    }

    private static boolean AreMethodsEqual(MethodInsnNode first, MethodInsnNode second) {
        return first != null && second != null && first.name.equals(second.name);
    }
}
