package net.lisstem.taint.asm.shadowint.handlers;

import net.lisstem.taint.asm.TypeHelper;
import net.lisstem.taint.asm.insnhandler.DelegateInsnByOpcode;
import net.lisstem.taint.asm.shadowint.ShadowMethodAdapter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import static org.objectweb.asm.Opcodes.*;

public class VarInstruction extends DelegateInsnByOpcode<ShadowMethodAdapter> {
    public VarInstruction() {
        addHandler(ILOAD, VarInstruction::handleILoad);
        addHandler(ISTORE, VarInstruction::handleIStore);
        addHandler(ALOAD, VarInstruction::handleALoad);
        addHandler(ASTORE, VarInstruction::handleAStore);
        setDefault(VarInstruction::mapIndex);
    }

    private static AbstractInsnNode handleILoad(AbstractInsnNode insn, ShadowMethodAdapter adapter) {
        VarInsnNode varInsn = (VarInsnNode)insn;
        mapVarIndex(varInsn, adapter);
        if (Type.INT_TYPE.equals(getLoadType(varInsn, adapter)))
            return addTaintLoad(varInsn, adapter);
        else
            return adapter.addTaint(insn);
    }

    private static AbstractInsnNode handleIStore(AbstractInsnNode insn, ShadowMethodAdapter adapter) {
        VarInsnNode varInsn = (VarInsnNode)insn;
        mapVarIndex(varInsn, adapter);
        if (adapter.isTaintOnTop())
            return addTaintStore(varInsn, adapter);
        else
            return adapter.popTaint(insn);
    }

    private static Type getLoadType(VarInsnNode insn, ShadowMethodAdapter adapter) {
        return adapter.stack.getCurrent().getLocal(insn.var);
    }


    private static void mapVarIndex(VarInsnNode insn, ShadowMethodAdapter adapter) {
        adapter.map(insn);
    }

    private static AbstractInsnNode mapIndex(AbstractInsnNode insn, ShadowMethodAdapter adapter) {
        mapVarIndex((VarInsnNode)insn, adapter);
        return insn;
    }

    private static AbstractInsnNode handleALoad(AbstractInsnNode insn, ShadowMethodAdapter adapter) {
        VarInsnNode varInsn = (VarInsnNode)insn;
        mapVarIndex(varInsn, adapter);
        if (TypeHelper.isIntArray(getLoadType(varInsn, adapter)))
            insn = addTaintLoad(varInsn, adapter);
        return insn;
    }

    private static AbstractInsnNode handleAStore(AbstractInsnNode insn, ShadowMethodAdapter adapter) {
        VarInsnNode varInsn = (VarInsnNode)insn;
        mapVarIndex(varInsn, adapter);
        if (adapter.isTaintOnTop())
            insn = addTaintStore(varInsn, adapter);
        return insn;
    }

    private static AbstractInsnNode addTaintLoad(VarInsnNode insn, ShadowMethodAdapter adapter) {
        return adapter.insertInstruction(insn, adapter.taintLoad(insn));
    }

    private static AbstractInsnNode addTaintStore(VarInsnNode insn, ShadowMethodAdapter adapter) {
        return adapter.insertInstructionBefore(insn, adapter.taintStore(insn));
    }
}
