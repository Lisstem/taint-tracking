package net.lisstem.taint.asm.shadowint;

import net.lisstem.taint.asm.MethodHelper;
import net.lisstem.taint.asm.TypeHelper;
import net.lisstem.taint.taint.TaintBox;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.objectweb.asm.Opcodes.*;

public class ShadowMethodInsnWrapper extends ShadowMethodWrapper {
    private MethodInsnNode wrapped;
    public ShadowMethodInsnWrapper(@NotNull MethodInsnNode method,
                                   @NotNull ShadowMethodAdapter adapter) {
        super(Type.getType(adapter.taintClass));
        wrap(method, adapter);
    }

    private void wrap(@NotNull MethodInsnNode method, @NotNull ShadowMethodAdapter adapter) {
        Type type = Type.getMethodType(method.desc);
        Type instance = MethodHelper.invokesOnObject(method) ? Type.getObjectType(method.owner) : null;
        Type replaced = type;

        if (!TypeHelper.isJdkClass(method.owner)) {
            replaced = Utils.shadowedMethodType(type, shadowType);
            method.desc = replaced.getDescriptor();
        }

        if (adapter.fitsStackToMethod(replaced, instance)) {
            wrapperRequired = false;
            wrapped = method;
            wrapper = null;
        } else {
            wrapperRequired = true;
            wrapped = (MethodInsnNode) method.clone(null);
            wrapper = method;
            wrapper.setOpcode(INVOKESTATIC);
            List<Type> args = adapter.getTopElements(adapter.stackConsumption(replaced, instance != null));
            if (MethodHelper.invokesOnObject(method)) {
                args.add(0, Type.getObjectType(method.owner));
            }
            if (TypeHelper.isJdkClass(method.owner)) {
                args = args.stream().map(t -> TypeHelper.isIntegerType(t) ? Type.INT_TYPE : t).toList();
            }
            replaced = Type.getMethodType(replaced.getReturnType(), args.toArray(new Type[0]));
            wrapper.desc = replaced.getDescriptor();
            wrapper.name = TypeHelper.MakeWrappedName(method.owner, method.name, replaced, type);
            wrapper.owner = adapter.className;
            wrapper.itf = false;
        }

        returnsInt = TypeHelper.isIntegerOrIntArray(replaced.getReturnType());
        returnsBox = Type.getType(TaintBox.class).equals(replaced.getReturnType());
        returnDim = (type.getReturnType().getSort() == Type.ARRAY) ? type.getReturnType().getDimensions() : 0;
    }

    @Override
    protected List<Type> getArgs(Type[] args) {
        List<Type> ret = new ArrayList<>(args.length + 1);
        if (MethodHelper.invokesOnObject(wrapped))
            ret.add(Type.getObjectType(wrapped.owner));
        ret.addAll(Arrays.asList(args));
        return ret;
    }

    @Override
    public @NotNull AbstractInsnNode getWrappedInsn() {
        return wrapped;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShadowMethodInsnWrapper that = (ShadowMethodInsnWrapper) o;
        return AreMethodsEqual(wrapper, that.wrapper);
    }

    @Override
    public int hashCode() {
        return Objects.hash(wrapped.getOpcode(), wrapped.owner, wrapped.desc, wrapped.name, wrapped.itf);
    }

    private static boolean AreMethodsEqual(MethodInsnNode first, MethodInsnNode second) {
        return first != null && second != null && first.name.equals(second.name);
    }
}
