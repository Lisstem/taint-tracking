package net.lisstem.taint.asm.shadowint.handlers;

import net.lisstem.taint.asm.TypeHelper;
import net.lisstem.taint.asm.shadowint.ShadowMethodAdapter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;

import static org.objectweb.asm.Opcodes.*;

public class Dup2X1 {
    private static void Dup2X1_TTT(InsnList list, ShadowMethodAdapter adapter) {
        // int, taint, int, taint, int, taint
        adapter.storeIntAndTaint(list);
        list.add(new InsnNode(DUP2_X2));
        adapter.loadIntAndTaint(list);
        adapter.swapTaints(list);
        adapter.storeIntAndTaint(list);
        list.add(new InsnNode(DUP2_X2));
        adapter.loadIntAndTaint(list);
        adapter.swapTaints(list);
    }

    private static void Dup2X1_VTT(InsnList list, ShadowMethodAdapter adapter) {
        // val, int, taint, int, taint
        adapter.storeIntAndTaint(list);
        list.add(new InsnNode(DUP2_X1));
        adapter.loadIntAndTaint(list);
        adapter.swapTaints(list);
        adapter.storeIntAndTaint(list);
        list.add(new InsnNode(DUP2_X1));
        adapter.loadIntAndTaint(list);
        adapter.swapTaints(list);
    }

    private static void Dup2X1_TVT(InsnList list, ShadowMethodAdapter adapter) {
        // int, taint, val, int, taint
        adapter.storeIntAndTaint(list);
        list.add(new InsnNode(DUP_X2));
        adapter.swapWithTaint(list);
        adapter.loadIntAndTaint(list);
        adapter.swapTaints(list);
        adapter.storeIntAndTaint(list);
        list.add(new InsnNode(DUP2_X2));
        adapter.loadIntAndTaint(list);
        adapter.swapTaints(list);
        adapter.storeIntAndTaint(list);
        adapter.swapTaintWithOther(list);
        adapter.loadIntAndTaint(list);

    }
    private static void Dup2X1_VVT(InsnList list, ShadowMethodAdapter adapter) {
        // val, val, int, taint
        adapter.storeIntAndTaint(list);
        list.add(new InsnNode(DUP_X1));
        adapter.loadIntAndTaint(list);
        list.add(new InsnNode(DUP2_X2));
    }

    private static void Dup2X1_TTV(InsnList list, ShadowMethodAdapter adapter) {
        // int1, taint1, int2, taint2, val
        adapter.swapWithTaint(list);
        adapter.storeIntAndTaint(list);
        list.add(new InsnNode(DUP_X2));
        adapter.swapWithTaint(list);
        // val, val, int1, taint1, cache: int2, taint2
        adapter.loadIntAndTaint(list);
        adapter.swapTaints(list);
        adapter.storeIntAndTaint(list);
        // val, val, int2, taint2, cache: int1, taint1
        list.add(new InsnNode(DUP2_X1));
        adapter.loadIntAndTaint(list);
        adapter.swapTaints(list);
        adapter.storeIntAndTaint(list);
        // val, int2, taint2, val, int1, taint1, cache: int2, taint2
        adapter.swapTaintWithOther(list);
        adapter.loadIntAndTaint(list);
        // val, int2, taint2, int1, taint1, val, int2, taint2
    }

    private static void Dup2X1_VTV(InsnList list, ShadowMethodAdapter adapter) {
        // val1, int, taint, val2
        adapter.swapWithTaint(list);
        list.add(new InsnNode(DUP2_X2));
        adapter.storeIntAndTaint(list);
        // int, taint, val1, val2, cache: int, taint
        list.add(new InsnNode(DUP_X1));
        adapter.loadIntAndTaint(list);
        // int, taint, val2, val1, val2, int, taint
        adapter.swapTaintWithOther(list);
        // int, taint, val2, val1, int, taint, val2
    }

    public static AbstractInsnNode handle(AbstractInsnNode insn, ShadowMethodAdapter adapter) {
        // ??, ?, ?
        InsnList list = new InsnList();
        if (adapter.isTaintOnTop()) {
            // ?, ?, int, taint
            if (adapter.isTaintOnTop(3)) {
                // ?, int, taint, int, taint
                if (adapter.isTaintOnTop(5)) {
                    // int, taint, int, taint, int, taint
                    Dup2X1_TTT(list, adapter);
                } else {
                    // val, int, taint, int, taint
                    Dup2X1_VTT(list, adapter);
                }
            } else {
                // ?, val, int, taint
                if (adapter.isTaintOnTop(4)) {
                    // int, taint, val, int, taint
                    Dup2X1_TVT(list, adapter);
                } else {
                    // val, val, int, taint
                    Dup2X1_VVT(list, adapter);
                }
            }
        } else {
            // ??, ?, val
            if (TypeHelper.isComputationalType2(adapter.getTop())) {
                // ?, val (type 2)
                if (adapter.isTaintOnTop(2)) {
                    // int, taint, val (type 2)
                    return adapter.replaceInstruction(insn, new InsnNode(DUP2_X2));
                } else {
                    // val, val (type 2)
                    return insn;
                }
            } else {
                // ?, ?, val
                if (adapter.isTaintOnTop(2)) {
                    // ?, int, taint, val
                    if (adapter.isTaintOnTop(4)) {
                        // int, taint, int, taint, val
                        Dup2X1_TTV(list, adapter);
                    } else {
                        // val, int, taint, val
                        Dup2X1_VTV(list, adapter);
                    }
                } else {
                    // ?, val, val
                    if (adapter.isTaintOnTop(3)) {
                        // int, taint, val, val
                        return adapter.replaceInstruction(insn,new InsnNode(DUP2_X2));
                    } else {
                        // val, val, val
                        return insn;
                    }
                }
            }
        }

        return adapter.replaceInstruction(insn, list);
    }
}
