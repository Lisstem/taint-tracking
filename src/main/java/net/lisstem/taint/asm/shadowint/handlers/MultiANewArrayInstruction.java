package net.lisstem.taint.asm.shadowint.handlers;

import net.lisstem.taint.asm.Instruction;
import net.lisstem.taint.asm.TypeHelper;
import net.lisstem.taint.asm.shadowint.ShadowMethodAdapter;
import net.lisstem.taint.asm.shadowint.TaintArray;
import net.lisstem.taint.taint.Taintable;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

public class MultiANewArrayInstruction {
    public static AbstractInsnNode handle(AbstractInsnNode insn, ShadowMethodAdapter adapter) {
        MultiANewArrayInsnNode multiANewArrayInsnNode = (MultiANewArrayInsnNode) insn;
        Type type = Type.getType(multiANewArrayInsnNode.desc);
        if (type.getElementType().equals(Type.INT_TYPE)) {
            return handleIntArray(multiANewArrayInsnNode, adapter);
        } else {
            return handleNormalArray(multiANewArrayInsnNode, adapter);
        }
    }

    private static AbstractInsnNode handleNormalArray(MultiANewArrayInsnNode insn, ShadowMethodAdapter adapter) {
        return switch (insn.dims) {
            case 1 -> adapter.popTaint(insn);
            case 2 -> adapter.pop2Taints(insn);
            default -> handleHighDim(insn, adapter);
        };
    }

    private static AbstractInsnNode handleIntArray(MultiANewArrayInsnNode insn, ShadowMethodAdapter adapter) {
        return adapter.createTaintArray(handleNormalArray(insn, adapter), Type.getType(insn.desc));
    }

    private static AbstractInsnNode handleHighDim(MultiANewArrayInsnNode insn, ShadowMethodAdapter adapter) {
        InsnList list = new InsnList();
        list.add(Instruction.loadInt(insn.dims));
        list.add(new IntInsnNode(NEWARRAY, T_INT));
        for (int i = 0; i < insn.dims; i++) {
            list.add(new InsnNode(SWAP));
            list.add(new InsnNode(POP));
            list.add(new InsnNode(DUP_X1));
            list.add(new InsnNode(SWAP));
            list.add(Instruction.loadInt(i));
            list.add(new InsnNode(SWAP));
            list.add(new InsnNode(IASTORE));
        }
        for (int i = insn.dims - 1; i >= 0; i--) {
            list.add(new InsnNode(DUP));
            list.add(Instruction.loadInt(i));
            list.add(new InsnNode(IALOAD));
            list.add(new InsnNode(SWAP));
        }
        list.add(new InsnNode(POP));
        return adapter.insertInstructionsBefore(insn, list);
    }
}
