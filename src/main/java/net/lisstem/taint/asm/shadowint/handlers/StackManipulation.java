package net.lisstem.taint.asm.shadowint.handlers;

import net.lisstem.taint.asm.TypeHelper;
import net.lisstem.taint.asm.insnhandler.DelegateInsnByOpcode;
import net.lisstem.taint.asm.shadowint.ShadowMethodAdapter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Opcodes.SWAP;

public class StackManipulation extends DelegateInsnByOpcode<ShadowMethodAdapter> {
    public StackManipulation() {
        super();
        addHandler(POP, (insn, adapter) -> adapter.isTaintOnTop() ? adapter.replaceInstruction(insn, new InsnNode(POP2)) : insn);
        addHandler(DUP, (insn, adapter) -> adapter.isTaintOnTop() ? adapter.replaceInstruction(insn, new InsnNode(DUP2)) : insn);
        addHandler(POP2, StackManipulation::handlePop2);
        addHandler(DUP_X1, StackManipulation::handleDupX1);
        addHandler(DUP_X2, StackManipulation::handleDupX2);
        addHandler(SWAP, StackManipulation::handleSwap);
        addHandler(DUP2, StackManipulation::handleDup2);
        addHandler(DUP2_X1, Dup2X1::handle);
        addHandler(DUP2_X2, Dup2X2::handle);
    }


    private static AbstractInsnNode handleDupX1(AbstractInsnNode insn, ShadowMethodAdapter adapter) {
        if (adapter.isTaintOnTop()) {
            // ?, int, taint
            return adapter.replaceInstruction(insn, new InsnNode(adapter.isTaintOnTop(3) ?
                    DUP2_X2 : // int1, taint1, int2, taint2
                    DUP2_X1)); // val, int, taint
        } else if (adapter.isTaintOnTop(2)) {
            // int, taint, val
            return adapter.replaceInstruction(insn, new InsnNode(DUP_X2));
        }
        // val, val
        return insn;
    }

    private static AbstractInsnNode handleDupX2(AbstractInsnNode insn, ShadowMethodAdapter adapter) {
        InsnList list = new InsnList();
        if (adapter.isTaintOnTop()) {
            // ?, ?, int, taint
            if (adapter.isTaintOnTop(3)) {
                // ?, int, taint, int, taint
                adapter.swapTaints(list);
                adapter.storeIntAndTaint(list);
                if (adapter.isTaintOnTop(5)) {
                    // int, taint, int, taint, int, taint
                    list.add(new InsnNode(DUP2_X2));
                } else {
                    // val, int, taint, int, taint
                    list.add(new InsnNode(DUP2_X1));
                }
                adapter.loadIntAndTaint(list);
                adapter.swapTaints(list);
            } else {
                // ?, val, int, taint
                if (TypeHelper.isComputationalType2(adapter.getTop(3)) || !adapter.isTaintOnTop(4)) {
                    // val, val, int, taint or val, int, taint
                    list.add(new InsnNode(DUP2_X2));
                } else {
                    // int, taint, val, int, taint
                    adapter.storeIntAndTaint(list);
                    list.add(new InsnNode(DUP_X2));
                    adapter.loadIntAndTaint(list);
                    adapter.swapTaints(list);
                    adapter.storeIntAndTaint(list);
                    list.add(new InsnNode(DUP2_X1));
                    adapter.loadIntAndTaint(list);
                    adapter.swapTaints(list);
                    adapter.storeIntAndTaint(list);
                    adapter.swapTaintWithOther(list);
                    adapter.loadIntAndTaint(list);
                }
            }
        } else {
            // ?, ?, val
            if (adapter.isTaintOnTop(2)) {
                // ?, int, taint, val
                adapter.swapWithTaint(list);
                adapter.storeIntAndTaint(list);
                if (adapter.isTaintOnTop(4) || TypeHelper.isComputationalType2(adapter.getTop(4))) {
                    // int, taint, int, taint, val or val (type2), int, taint, val
                    list.add(new InsnNode(DUP_X2));
                } else {
                    // val, int, taint, val
                    list.add(new InsnNode(DUP_X1));
                }
                adapter.loadIntAndTaint(list);
                adapter.swapTaintWithOther(list);
            } else {
                // ?, val, val
                if (TypeHelper.isComputationalType2(adapter.getTop(2)) || !adapter.isTaintOnTop(3)) {
                    // val, val or val, val, val
                    list.add(insn.clone(null));
                } else {
                    // int, taint, val1, val2
                    list.add(new InsnNode(DUP2_X2));
                    list.add(new InsnNode(POP2));
                    adapter.storeIntAndTaint(list);
                    list.add(new InsnNode(DUP_X1));
                    adapter.loadIntAndTaint(list);
                    list.add(new InsnNode(DUP2_X2));
                    list.add(new InsnNode(POP));
                }
            }
        }
        return adapter.replaceInstruction(insn, list);
    }

    private static AbstractInsnNode handleSwap(AbstractInsnNode insn, ShadowMethodAdapter adapter) {
        InsnList list = new InsnList();
        if (adapter.isTaintOnTop()) {
            if (adapter.isTaintOnTop(3)) {
                adapter.swapTaints(list);
            } else {
                adapter.swapTaintWithOther(list);
            }
        } else {
            if (adapter.isTaintOnTop(2)) {
                adapter.swapTaintWithOther(list);
            } else {
                list.add(new InsnNode(SWAP));
            }
        }

        return adapter.replaceInstruction(insn, list);
    }


    private static AbstractInsnNode handlePop2(AbstractInsnNode insn, ShadowMethodAdapter adapter) {
        if (adapter.isTaintOnTop()) {
            if (adapter.isTaintOnTop(3))
                return adapter.insertInstruction(insn, new InsnNode(POP2)); // int, taint, int, taint
            return adapter.insertInstruction(insn, new InsnNode(POP)); // val, int, taint
        }
        if (!TypeHelper.isComputationalType2(adapter.getTop()) && adapter.isTaintOnTop(2))
            return adapter.insertInstruction(insn, new InsnNode(POP)); // int, taint, val
        return insn; // val, val
    }

    private static AbstractInsnNode handleDup2(AbstractInsnNode insn, ShadowMethodAdapter adapter) {
        if (TypeHelper.isComputationalType2(adapter.stack.getCurrent().getTop()))
            // val (type 2)
            return insn;

        InsnList list = new InsnList();
        if (adapter.isTaintOnTop()) {
            // ?, int, taint
            if (adapter.isTaintOnTop(3)) {
                // int, taint, int, taint
                list.add(new InsnNode(DUP2_X2));
                adapter.storeIntAndTaint(list);
                list.add(new InsnNode(DUP2_X2));
                adapter.loadIntAndTaint(list);
            } else {
                // val, int, taint
                adapter.storeIntAndTaint(list);
                list.add(new InsnNode(DUP));
                adapter.loadIntAndTaint(list);
                list.add(new InsnNode(DUP2_X1));
            }
        } else {
            if (adapter.isTaintOnTop(1)) {
                // int, taint, val
                list.add(new InsnNode(DUP_X2));
                adapter.swapWithTaint(list);
                // val, val, int, taint
                list.add(new InsnNode(DUP2_X2));
                adapter.swapTaintWithOther(list);
                // int, taint, val, int, taint, val
            } else {
                // val, val
                return insn;
            }
        }

        return adapter.replaceInstruction(insn, list);
    }

}
