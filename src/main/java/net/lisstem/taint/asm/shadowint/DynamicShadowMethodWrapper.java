package net.lisstem.taint.asm.shadowint;

import net.lisstem.taint.asm.MethodHelper;
import net.lisstem.taint.asm.TypeHelper;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.List;
import java.util.Objects;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

public class DynamicShadowMethodWrapper extends ShadowMethodWrapper {
    private InvokeDynamicInsnNode wrapped;

    public DynamicShadowMethodWrapper(@NotNull InvokeDynamicInsnNode method, @NotNull ShadowMethodAdapter adapter) {
        super(Type.getType(adapter.taintClass));
        wrap(method, adapter);
    }

    private void wrap(@NotNull InvokeDynamicInsnNode method, @NotNull ShadowMethodAdapter adapter) {
        // TODO: Support dynamic invocation for non jdk classes
        Type type = Type.getMethodType(method.desc);

        returnsInt = TypeHelper.isIntegerOrIntArray(type.getReturnType());
        returnDim = (type.getReturnType().getSort() == Type.ARRAY) ? type.getReturnType().getDimensions() : 0;
        returnsBox = false;

        if (!TypeHelper.isJdkClass(method.bsm.getOwner()))
            System.out.println("Warning dynamic invocation of non jdk classes is not supported");

        if (MethodHelper.hasIntegerArgs(type)) {
            wrapperRequired = true;
            wrapped = (InvokeDynamicInsnNode) method.clone(null);
            List<Type> args = adapter.getTopElements(adapter.stackConsumption(type, false))
                    .stream().map(t -> TypeHelper.isIntegerType(t) ? Type.INT_TYPE : t).toList();
            Type replaced = Type.getMethodType(type.getReturnType(), args.toArray(new Type[0]));
            wrapper = new MethodInsnNode(INVOKESTATIC, adapter.className, TypeHelper.MakeWrappedName("", method.name, replaced, type),
                    replaced.getDescriptor(), false);
        } else {
            wrapper = null;
            wrapped = method;
        }
    }

    @Override
    public @NotNull AbstractInsnNode getWrappedInsn() {
        return wrapped;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DynamicShadowMethodWrapper that = (DynamicShadowMethodWrapper) o;
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
