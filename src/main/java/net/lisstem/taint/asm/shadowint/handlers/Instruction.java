package net.lisstem.taint.asm.shadowint.handlers;

import net.lisstem.taint.asm.insnhandler.DelegateInsnByOpcode;
import net.lisstem.taint.asm.insnhandler.InsnHandler;
import net.lisstem.taint.asm.shadowint.ShadowMethodAdapter;
import net.lisstem.taint.asm.shadowint.ShadowedArray;
import net.lisstem.taint.taint.TaintBox;
import net.lisstem.taint.taint.Taintable;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import static net.lisstem.taint.asm.MethodHelper.CONSTRUCTOR_NAME;
import static org.objectweb.asm.Opcodes.*;

public class Instruction extends DelegateInsnByOpcode<ShadowMethodAdapter> { 
    public Instruction() {
        super();
        for (int opcode : new int[]{IADD, ISUB, IMUL, IDIV, IREM, ISHL, ISHR, IUSHR, IAND, IOR, IXOR}) {
            addHandler(opcode, (insn, adapter) -> adapter.combineTaint(insn));
        }
        for (int opcode: new int[]{I2B, I2C, I2S, INEG}) {
            addHandler(opcode, Instruction::handleOneIntOp);
        }
        for (int opcode: new int[]{ ICONST_M1, ICONST_0, ICONST_1, ICONST_2, ICONST_3, ICONST_4, ICONST_5, L2I, D2I, F2I }) {
            addHandler(opcode, (insn, adapter) -> adapter.addTaint(insn));
        }
        for (int opcode: new int[]{LALOAD, FALOAD, DALOAD, I2D, I2F, I2L}) {
            addHandler(opcode, (insn, adapter) -> adapter.popTaint(insn));
        }
        for (int opcode: new int[]{BALOAD, CALOAD, SALOAD}) {
            addHandler(opcode, (insn, adapter) -> adapter.addTaint(adapter.popTaint(insn)));
        }
        for (int opcode: new int[]{LASTORE, DASTORE}) {
            addHandler(opcode, (insn, adapter) -> adapter.popSecondTaintW(insn));
        }
        for (int opcode: new int[]{FASTORE}) {
            addHandler(opcode, (insn, adapter) -> adapter.popSecondTaint(insn));
        }
        for (int opcode: new int[]{BASTORE, CASTORE, SASTORE}) {
            addHandler(opcode, (insn, adapter) -> adapter.pop2Taints(insn));
        }
        addHandler(IASTORE, Instruction::handleIAStore);
        addHandler(RETURN, Instruction::printUntaintedReturn);
        addHandler(IRETURN, Instruction::handleIntReturn);
        addHandler(ARETURN, Instruction::handleObjectReturn);
        addHandler(IALOAD, Instruction::handleIntLoad);
        addHandler(ARRAYLENGTH, Instruction::handleArrayLength);
        addHandler(AALOAD, Instruction::handleAALoad);
        addHandler(AASTORE, Instruction::handleAAStore);

        InsnHandler<ShadowMethodAdapter> stackManipulation = new StackManipulation();
        for (int opcode: new int[]{POP, POP2, DUP, DUP_X1, DUP_X2, DUP2, DUP2_X1, DUP2_X2, SWAP}) {
            addHandler(opcode, stackManipulation);
        }
        /* NOP, ACONST_NULL, LCONST_0, LCONST_1, FCONST_0, FCONST_1, FCONST_2, DCONST_0, DCONST_1,
           LADD, FADD, DADD, LSUB, FSUB, DSUB, LMUL, FMUL, DMUL, LDIV, FDIV, DDIV, LREM, FREM, DREM,
           LNEG, FNEG, DNEG, LSHL, LSHR, LUSHR, LAND, LOR, LXOR, L2F, L2D, F2L, F2D, D2L, D2F,
           LCMP, FCMPL, FCMPG, DCMPL, DCMPG, LRETURN, FRETURN, DRETURN, ARETURN, RETURN,
           ATHROW, MONITORENTER, MONITOREXIT do nothing*/
    }

    private static AbstractInsnNode handleAAStore(AbstractInsnNode insn, ShadowMethodAdapter adapter) {
        if (adapter.isTaintOnTop()) {
            InsnList list = new InsnList();
            // int[][], taint[][], int, taint, int[], taint[]
            list.add(new InsnNode(SWAP));
            adapter.swap2(list);
            // int[][], taint[][], taint[], int[], int, taint
            list.add(new InsnNode(POP));
            list.add(adapter.storeInt());
            // int[][], taint[][], taint[], int[], stored: int
            list.add(new InsnNode(DUP_X2));
            list.add(new InsnNode(POP));
            // int[][], int[], taint[][], taint[], stored: int
            list.add(adapter.loadInt());
            list.add(new InsnNode(SWAP));
            // int[][], int[], taint[][], int, taint[], stored: int
            list.add(new InsnNode(AASTORE));
            // int[][], int[], stored: int
            list.add(adapter.loadInt());
            list.add(new InsnNode(SWAP));
            // int[][], int, int[], stored: int
            list.add(new InsnNode(AASTORE));
            // stored: int
            return adapter.replaceInstruction(insn, list);
        }
        return adapter.popSecondTaint(insn);
    }

    private static AbstractInsnNode handleIAStore(AbstractInsnNode insn, ShadowMethodAdapter adapter) {
        InsnList list = new InsnList();
        // int[], taint[], int1, taint1, int2, taint2
        list.add(new InsnNode(SWAP));
        adapter.swap2(list);
        // int[], taint[], taint2, int2, int1, taint1
        list.add(new InsnNode(POP));
        list.add(adapter.storeInt());
        // int[], taint[], taint2, int2, stored: int1
        list.add(new InsnNode(DUP_X2));
        list.add(new InsnNode(POP));
        // int[], int2, taint[], taint2, stored: int1
        list.add(adapter.loadInt());
        list.add(new InsnNode(SWAP));
        // int[], int2, taint[], int1, taint, stored: int1
        list.add(new InsnNode(AASTORE));
        // int[], int2, stored: int1
        list.add(adapter.loadInt());
        list.add(new InsnNode(SWAP));
        // int[], int1, int2, stored: int1
        list.add(new InsnNode(IASTORE));
        // stored: int1
        return adapter.replaceInstruction(insn, list);
    }

    private static AbstractInsnNode handleAALoad(AbstractInsnNode insn, ShadowMethodAdapter adapter) {
        if (adapter.isTaintOnTop(3)) {
            InsnList list = new InsnList();
            list.add(adapter.popTaint());
            list.add(new InsnNode(DUP_X1));
            list.add(new InsnNode(AALOAD));
            list.add(new InsnNode(DUP_X2));
            list.add(new InsnNode(POP));
            list.add(new InsnNode(AALOAD));
            list.add(new InsnNode(SWAP));
            return adapter.replaceInstruction(insn, list);
        }
        return adapter.popTaint(insn);
    }

    private static AbstractInsnNode handleOneIntOp(AbstractInsnNode insn, ShadowMethodAdapter adapter) {
        adapter.insertInstructionBefore(insn, new InsnNode(SWAP));
        return adapter.insertInstruction(insn, new InsnNode(SWAP));
    }

    private static AbstractInsnNode handleIntLoad(AbstractInsnNode insn, ShadowMethodAdapter adapter) {
        adapter.popTaint(insn);
        adapter.insertInstructionBefore(insn, new InsnNode(DUP_X1));
        adapter.insertInstructionBefore(insn, new InsnNode(AALOAD));
        adapter.insertInstructionBefore(insn, new InsnNode(DUP_X2));
        adapter.popTaint(insn);
        return adapter.insertInstruction(insn, new InsnNode(SWAP));
    }

    private static AbstractInsnNode handleArrayLength(AbstractInsnNode insn, ShadowMethodAdapter adapter) {
        if (adapter.isTaintOnTop()) {
            insn = adapter.popTaint(insn);
        }
        return adapter.addTaint(insn);
    }

    private static AbstractInsnNode printUntaintedReturn(AbstractInsnNode insn, ShadowMethodAdapter adapter) {
        return adapter.printTaints ? adapter.print(insn, "return without taint") : insn;
    }


    private static AbstractInsnNode handleIntReturn(AbstractInsnNode insn, ShadowMethodAdapter adapter) {
        if (!adapter.isTaintOnTop())
            return printUntaintedReturn(insn, adapter);

        if (!adapter.returnsBox) {
            insn = adapter.popTaint(insn);
            return printUntaintedReturn(insn, adapter);
        }

        if (adapter.taintsResult)
            insn = adapter.setTaint(insn, true);

        if (adapter.printTaints)
            insn = adapter.printTaint(insn, "taint of return value: ");


        InsnList list = newTaintBox();
        list.add(new MethodInsnNode(INVOKESPECIAL,
                Type.getInternalName(TaintBox.class), CONSTRUCTOR_NAME,
                Type.getMethodDescriptor(Type.VOID_TYPE, Type.INT_TYPE, Type.getType(Taintable.class))));
        list.add(new InsnNode(ARETURN));
        return adapter.replaceInstruction(insn, list);
    }

    private static AbstractInsnNode handleObjectReturn(AbstractInsnNode insn, ShadowMethodAdapter adapter) {
        if (!adapter.returnsBox) {
            if (adapter.isTaintOnTop())
                insn = adapter.popTaint(insn);

            return printUntaintedReturn(insn, adapter);
        }
        if (adapter.printTaints)
            insn = adapter.printTaint(insn, "Taint of return value: ");

        InsnList list = newTaintBox();
        Type object = Type.getType(Object.class);
        list.add(new MethodInsnNode(INVOKESPECIAL,
                Type.getInternalName(TaintBox.class), CONSTRUCTOR_NAME,
                Type.getMethodDescriptor(Type.VOID_TYPE, object, object)));
        return adapter.insertInstructionsBefore(insn, list);
    }

    private static InsnList newTaintBox() {
        InsnList list = new InsnList();
        list.add(new TypeInsnNode(NEW, Type.getInternalName(TaintBox.class)));
        list.add(new InsnNode(DUP_X2));
        list.add(new InsnNode(DUP_X2));
        list.add(new InsnNode(POP));
        return list;
    }
}
