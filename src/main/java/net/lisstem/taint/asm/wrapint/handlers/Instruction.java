package net.lisstem.taint.asm.wrapint.handlers;

import net.lisstem.taint.asm.TypeHelper;
import net.lisstem.taint.asm.insnhandler.DelegateInsnByOpcode;
import net.lisstem.taint.asm.shadowint.ShadowMethodAdapter;
import net.lisstem.taint.asm.wrapint.TaintMethodAdapter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import static org.objectweb.asm.Opcodes.*;

public class Instruction extends DelegateInsnByOpcode<TaintMethodAdapter> {
    public Instruction() {
        for (int opcode: new int[]{ IADD, ISUB, IMUL, IDIV, IREM, ISHL, ISHR, IUSHR, IAND, IOR, IXOR }) {
            addHandler(opcode, (insn, adapter) ->
                    adapter.replaceInstruction(insn, opcodeToMethodSecondary(insn.getOpcode(), adapter.targetType)));
        }
        for (int opcode: new int[] { ICONST_M1, ICONST_0, ICONST_1, ICONST_2, ICONST_3, ICONST_4, ICONST_5, L2I, D2I, F2I, ARRAYLENGTH }) {
            addHandler(opcode, (insn, adapter) -> adapter.box(insn));
        }
        for (int opcode: new int[] { I2B, I2C, I2S, INEG }) {
            addHandler(opcode, (insn, adapter) ->
                    adapter.replaceInstruction(insn, opcodeToMethodMono(insn.getOpcode(), adapter.targetType)));
        }
        for (int opcode: new int[] { LALOAD, FALOAD, DALOAD, AALOAD, I2D, I2F, I2L }) {
            addHandler(opcode, (insn, adapter) -> adapter.unbox(insn));
        }
        for (int opcode: new int[] { BALOAD, CALOAD, SALOAD }) {
            addHandler(opcode, (insn, adapter) -> adapter.box(adapter.unbox(insn)));
        }
        for (int opcode: new int[] { FASTORE, AASTORE }) {
            addHandler(opcode, (insn, adapter) -> adapter.unboxSecond(insn));
        }
        for (int opcode: new int[] { LASTORE, DASTORE }) {
            addHandler(opcode, Instruction::handleType2AStore);
        }
        for (int opcode: new int[] { BASTORE, CASTORE, SASTORE }) {
            addHandler(opcode, (insn, adapter) -> adapter.unbox2(insn));
        }
        addHandler(RETURN, Instruction::printUntaintedReturn);
        addHandler(IRETURN, Instruction::handleIReturn);
        addHandler(ARETURN, Instruction::handleAReturn);
        addHandler(IASTORE, (insn, adapter) -> adapter.unboxSecond(adapter.replaceInstruction(insn, new InsnNode(AASTORE))));
        addHandler(IALOAD, (insn, adapter) -> adapter.unbox(adapter.replaceInstruction(insn, new InsnNode(AALOAD))));
    }

    private static AbstractInsnNode handleType2AStore(AbstractInsnNode insn, TaintMethodAdapter adapter) {
        InsnList list = new InsnList();
        list.add(new InsnNode(DUP2_X1));
        list.add(new InsnNode(POP2));
        list.add(adapter.unbox());
        list.add(new InsnNode(DUP_X2));
        list.add(new InsnNode(POP));
        return adapter.insertInstructionsBefore(insn, list);
    }

    private static AbstractInsnNode printUntaintedReturn(AbstractInsnNode insn, TaintMethodAdapter adapter) {
        return adapter.printTaints ? adapter.print(insn, "return without taint") : insn;
    }

    private static AbstractInsnNode handleAReturn(AbstractInsnNode insn, TaintMethodAdapter adapter) {
        if (!TypeHelper.isArrayOf(Type.getMethodType(adapter.desc).getReturnType(), adapter.targetType))
            return printUntaintedReturn(insn, adapter);

        if (adapter.printTaints)
            return adapter.printTaint(insn, "taint of return value: ", true);

        return insn;
    }

    private static AbstractInsnNode handleIReturn(AbstractInsnNode insn, TaintMethodAdapter adapter) {
        if (adapter.returnsTarget) {
            if (adapter.taintsResult)
                insn = adapter.taintResult(insn);
            if (adapter.printTaints)
                insn = adapter.printTaint(insn, "taint of return value: ", false);
            insn = adapter.replaceInstruction(insn, new InsnNode(ARETURN));
        } else {
            insn = printUntaintedReturn(adapter.unbox(insn), adapter);
        }
        return insn;
    }

    private static MethodInsnNode opcodeToMethodMono(int opcode, Type targetType) {
        String desc = "()" +  targetType.getDescriptor();
        return new MethodInsnNode(INVOKEVIRTUAL, targetType.getInternalName(), opcodeToMethodname(opcode), desc);
    }
    private static MethodInsnNode opcodeToMethodSecondary(int opcode, Type targetType) {
        String desc = "(" + targetType.getDescriptor() + ")" + targetType.getDescriptor();
        return new MethodInsnNode(INVOKEVIRTUAL, targetType.getInternalName(), opcodeToMethodname(opcode), desc);
    }


    private static String opcodeToMethodname(int opcode) {
        return switch (opcode) {
            case IADD -> "add";
            case ISUB -> "sub";
            case IMUL -> "mul";
            case IDIV -> "div";
            case IREM -> "rem";
            case INEG -> "neg";
            case ISHL -> "shl";
            case ISHR -> "shr";
            case IUSHR -> "ushr";
            case IAND -> "and";
            case IOR -> "or";
            case IXOR -> "xor";
            case I2B -> "i2b";
            case I2C -> "i2c";
            case I2S -> "i2s";
            default -> "error";
        };
    }
}
