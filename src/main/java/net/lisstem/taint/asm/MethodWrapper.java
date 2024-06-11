package net.lisstem.taint.asm;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

public abstract class MethodWrapper {
    protected MethodInsnNode wrapper;
    protected boolean wrapperRequired;



    protected int returnDim;
    protected MethodWrapper() {
        wrapper = null;
        wrapperRequired = false;
    }

    public abstract @NotNull AbstractInsnNode getWrappedInsn();

    public MethodInsnNode getWrapperInsn() {
        return wrapper;
    }


    public int getReturnDim() {
        return returnDim;
    }

    public boolean isWrapperRequired() {
        return wrapperRequired;
    }

    abstract public void createWrappingMethod(MethodVisitor mv);


    protected Type getWrappedSignature() {
        AbstractInsnNode wrapped = getWrappedInsn();
        return switch (wrapped.getType()) {
            case AbstractInsnNode.METHOD_INSN -> Type.getMethodType(((MethodInsnNode)wrapped).desc);
            case AbstractInsnNode.INVOKE_DYNAMIC_INSN -> Type.getMethodType(((InvokeDynamicInsnNode)wrapped).desc);
            default -> throw new IllegalStateException("Wrapped method is not a method");
        };
    }
}
