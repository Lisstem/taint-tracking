package net.lisstem.taint.asm.wrapint.handlers;

import net.lisstem.taint.asm.Instruction;
import net.lisstem.taint.asm.wrapint.TaintMethodAdapter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

public class MultiANewArray {
    private static AbstractInsnNode handleStack(MultiANewArrayInsnNode insn, TaintMethodAdapter adapter) {
        InsnList list = new InsnList();
        list.add(Instruction.loadInt(insn.dims));
        list.add(new IntInsnNode(NEWARRAY, T_INT));
        for (int i = insn.dims - 1 ; i >= 0; i--) {
            list.add(new InsnNode(DUP_X1));
            list.add(new InsnNode(SWAP));
            list.add(Instruction.loadInt(i));
            list.add(new InsnNode(SWAP));
            list.add(adapter.unbox());
            list.add(new InsnNode(IASTORE));
        }
        for (int i = 0; i < insn.dims; i++) {
            list.add(new InsnNode(DUP));
            list.add(Instruction.loadInt(i));
            list.add(new InsnNode(IALOAD));
            list.add(new InsnNode(SWAP));
        }
        list.add(new InsnNode(POP));
        return adapter.insertInstructionsBefore(insn, list);
    }

    private static AbstractInsnNode handleFrame(MultiANewArrayInsnNode insn, TaintMethodAdapter adapter) {
        InsnList list = new InsnList();
        int var = adapter.localVariables.size();
        LabelNode start = new LabelNode();
        LabelNode end = new LabelNode();
        adapter.localVariables.add(new LocalVariableNode("$temp_array", "[I", null, start, end, var));
        list.add(start);
        list.add(new IntInsnNode(SIPUSH, insn.dims));
        list.add(new IntInsnNode(NEWARRAY, T_INT));
        list.add(new VarInsnNode(ASTORE, var));
        for (int i = insn.dims - 1 ; i >= 0; i--) {
            list.add(new VarInsnNode(ALOAD, var));
            list.add(new InsnNode(SWAP));
            list.add(new IntInsnNode(SIPUSH, i));
            list.add(new InsnNode(SWAP));
            list.add(adapter.unbox());
            list.add(new InsnNode(IASTORE));
        }
        for (int i = 0; i < insn.dims; i++) {
            list.add(new VarInsnNode(ALOAD, var));
            list.add(new IntInsnNode(SIPUSH, i));
            list.add(new InsnNode(IALOAD));
        }
        list.add(end);
        return adapter.insertInstructionsBefore(insn, list);
    }

    public static AbstractInsnNode handleFrame(AbstractInsnNode insn, TaintMethodAdapter adapter) {
        return handle(insn, adapter, true);
    }

    public static AbstractInsnNode handle(AbstractInsnNode insn, TaintMethodAdapter adapter, boolean stack) {
        MultiANewArrayInsnNode aNewArrayInsn = (MultiANewArrayInsnNode) insn;
        if (aNewArrayInsn.desc.endsWith(Type.INT_TYPE.getDescriptor()))
            aNewArrayInsn.desc = aNewArrayInsn.desc.replace(Type.INT_TYPE.getDescriptor(), adapter.targetType.getDescriptor());
        return switch (aNewArrayInsn.dims) {
            case 1 -> adapter.unbox(aNewArrayInsn);
            case 2 -> adapter.unbox2(aNewArrayInsn);
            default -> stack ? handleStack(aNewArrayInsn, adapter) : handleFrame(aNewArrayInsn, adapter);
        };
    }

    public static AbstractInsnNode handleStack(AbstractInsnNode insn, TaintMethodAdapter adapter) {
        return handle(insn, adapter, true);
    }
}
