package net.lisstem.taint.asm.wrapint.handlers;

import net.lisstem.taint.asm.TypeHelper;
import net.lisstem.taint.asm.wrapint.*;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

public class MethodInvocation {
    public static AbstractInsnNode handleMethodInstruction(AbstractInsnNode insn, TaintMethodAdapter adapter) {
        MethodInsnNode invocation = (MethodInsnNode)insn;
        BoxingMethodInsnWrapper wrapper = new BoxingMethodInsnWrapper(invocation, adapter.targetType, adapter.className);
        return wrap(insn, adapter, wrapper);
    }

    public static AbstractInsnNode handleInvokeDynamicInstruction(AbstractInsnNode insn, TaintMethodAdapter adapter) {
        InvokeDynamicInsnNode invocation = (InvokeDynamicInsnNode)insn;
        // TODO Refactor
        BoxingMethodWrapper wrapper = new DynamicBoxingMethodWrapper(invocation, adapter.targetType, adapter.className);
        return wrap(insn, adapter, wrapper);
    }

    private static AbstractInsnNode wrap(AbstractInsnNode insn, TaintMethodAdapter adapter, BoxingMethodWrapper wrapper) {
        if (wrapper.isWrapperRequired()) {
            adapter.requiredFunctions.add(wrapper);
            insn = adapter.replaceInstruction(insn, wrapper.getWrapperInsn());
        } else {
            insn = wrapper.getWrappedInsn();
        }

        if (wrapper.isBoxingRequired()) {
            if (wrapper.getReturnDim() == 0) {
                insn = adapter.box(insn);
            } else {
                insn = adapter.insertInstruction(insn, new MethodInsnNode(INVOKESTATIC, Type.getInternalName(TaintedIntArray.class), "box",
                        Type.getMethodDescriptor(TypeHelper.createArrayType(wrapper.getReturnDim(), adapter.targetType),
                                TypeHelper.createArrayType(wrapper.getReturnDim(), Type.INT_TYPE)), false));
            }
        }

        return insn;
    }
}
