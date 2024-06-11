package net.lisstem.taint.asm.shadowint.handlers;

import net.lisstem.taint.asm.TypeHelper;
import net.lisstem.taint.asm.shadowint.DynamicShadowMethodWrapper;
import net.lisstem.taint.asm.shadowint.ShadowMethodAdapter;
import net.lisstem.taint.asm.shadowint.ShadowMethodInsnWrapper;
import net.lisstem.taint.asm.shadowint.ShadowMethodWrapper;
import net.lisstem.taint.taint.TaintBox;
import net.lisstem.taint.taint.Taintable;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

public class MethodInvocation {
    public AbstractInsnNode handleMethodInstruction(AbstractInsnNode insn, ShadowMethodAdapter adapter) {
        ShadowMethodWrapper wrapper = new ShadowMethodInsnWrapper((MethodInsnNode) insn, adapter);
        return handleMethodInvocation(insn, wrapper, adapter);
    }

    public AbstractInsnNode handleInvokeDynamicInstruction(AbstractInsnNode insn, ShadowMethodAdapter adapter) {
        ShadowMethodWrapper wrapper = new DynamicShadowMethodWrapper((InvokeDynamicInsnNode) insn, adapter);
        return  handleMethodInvocation(insn, wrapper, adapter);
    }

    public AbstractInsnNode handleMethodInvocation(AbstractInsnNode insn, ShadowMethodWrapper wrapper, ShadowMethodAdapter adapter) {
        AbstractInsnNode method;
        if (wrapper.isWrapperRequired()) {
            method = wrapper.getWrapperInsn();
            adapter.requiredFunctions.add(wrapper);
        }
        else {
            method = wrapper.getWrappedInsn();
        }

        method = adapter.replaceInstruction(insn, method);
        if (wrapper.isReturningBox()) {
            method = adapter.insertInstruction(method, new InsnNode(DUP));
            method = getDataFromBox(method, adapter, wrapper.getReturnDim());
            method = adapter.insertInstruction(method, new InsnNode(SWAP));
            method = getTaintFromBox(method, adapter, wrapper.getReturnDim());
        } else if (wrapper.isReturningInt()) {
            if (wrapper.getReturnDim() == 0)
                method = adapter.addTaint(method);
            else
                method = adapter.createTaintArray(method, TypeHelper.createArrayType(wrapper.getReturnDim(), Type.INT_TYPE));
        }

        return method;
    }

    private AbstractInsnNode getDataFromBox(AbstractInsnNode insn, ShadowMethodAdapter adapter, int dim) {
        String name = dim > 0 ? "getArray" : "getInt";
        String desc = dim > 0 ? "()" + OBJECT : "()I";
        insn = adapter.insertInstruction(insn, new MethodInsnNode(INVOKEVIRTUAL, BOX, name, desc, false));
        if (dim > 0) {
            insn = adapter.insertInstruction(insn, new TypeInsnNode(CHECKCAST,
                    TypeHelper.createArrayType(dim, Type.INT_TYPE).getDescriptor()));
        }
        return insn;
    }

    private AbstractInsnNode getTaintFromBox(AbstractInsnNode insn, ShadowMethodAdapter adapter, int dim) {
        String name = dim > 0 ? "getTaintArray" : "getTaint";
        String desc = dim > 0 ? "()" + OBJECT : "()" + Type.getDescriptor(Taintable.class);
        insn = adapter.insertInstruction(insn, new MethodInsnNode(INVOKEVIRTUAL, BOX, name, desc, false));
        String cast = dim == 0 ?
                Type.getInternalName(adapter.taintClass) :
                TypeHelper.createArrayType(dim, Type.getType(adapter.taintClass)).getDescriptor();
        insn = adapter.insertInstruction(insn, new TypeInsnNode(CHECKCAST, cast));
        return insn;
    }

    private final static String BOX = Type.getInternalName(TaintBox.class);
    private final static String OBJECT = Type.getDescriptor(Object.class);
}
