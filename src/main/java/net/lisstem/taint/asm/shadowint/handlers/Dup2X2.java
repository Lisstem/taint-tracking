package net.lisstem.taint.asm.shadowint.handlers;

import net.lisstem.taint.asm.TypeHelper;
import net.lisstem.taint.asm.shadowint.ShadowMethodAdapter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;

import static org.objectweb.asm.Opcodes.*;

public class Dup2X2 {
    private static void dup2X2_TTV2(InsnList list, ShadowMethodAdapter adapter) {
        // int1, taint1, int2, taint2, val (type 2)
        adapter.swap2(list);
        adapter.storeIntAndTaint(list);
        // int1, taint1, val (type 2)
        list.add(new InsnNode(DUP2_X2));
        // val (type 2), int1, taint1, val (type 2)
        adapter.loadIntAndTaint(list);
        adapter.swap2(list);
        // val (type 2), int1, taint1, int2, taint2, val (type 2)
    }

    private static void dup2X2_VTV2(InsnList list, ShadowMethodAdapter adapter) {
        // val1, int, taint, val2 (type 2)
        adapter.swap2(list);
        adapter.storeIntAndTaint(list);
        // val1, val2 (type 2)
        list.add(new InsnNode(DUP2_X1));
        adapter.loadIntAndTaint(list);
        adapter.swap2(list);
        // val2 (type 2), val1, int, taint, val2 (type 2)
    }


    private static void dup2X2_TVV2(InsnList list, ShadowMethodAdapter adapter) {
        // int, taint, val1, val2 (type 2)
        adapter.swap2(list);
        list.add(new InsnNode(SWAP));
        list.add(adapter.storeTaint());
        // int, val2 (type 2), val1
        list.add(new InsnNode(DUP_X2));
        list.add(new InsnNode(POP));
        adapter.swap2(list);
        // val2 (type 2), int, val1
        list.add(new InsnNode(SWAP));
        list.add(adapter.storeInt());
        // val2 (type 2), val1
        list.add(new InsnNode(DUP_X2));
        list.add(new InsnNode(POP));
        // val1, val2 (type 2)
        list.add(new InsnNode(DUP2_X1));
        list.add(new InsnNode(DUP2_X1));
        list.add(new InsnNode(POP2));
        // val2 (type 2), val2 (type 2), val1
        list.add(adapter.loadInt());
        list.add(new InsnNode(SWAP));
        adapter.swap2(list);
        // val2 (type 2), int, val1, val2 (type 2)
        list.add(new InsnNode(DUP2_X1));
        list.add(new InsnNode(POP2));
        // val2 (type 2), int, val2 (type 2), val1
        list.add(adapter.loadTaint());
        list.add(new InsnNode(SWAP));
        adapter.swap2(list);
        // val2 (type 2), int, taint, val1, val2 (type 2)
    }

    private static void dup2X2_V2TT(InsnList list, ShadowMethodAdapter adapter) {
        // val1 (type 2), int1, taint1, int2, taint2
        adapter.storeIntAndTaint(list, 0);
        list.add(new InsnNode(DUP2_X2));
        // int1, taint1, val1 (type 2), int1, taint1
        adapter.storeIntAndTaint(list, 1);
        adapter.loadIntAndTaint(list, 0);
        list.add(new InsnNode(DUP2_X2));
        // int1, taint1, int2, taint2, val1 (type 2), int2, taint2
        adapter.loadIntAndTaint(list, 1);
        adapter.swapTaints(list);
        // int1, taint1, int2, taint2, val1 (type 2), int1, taint1, int2, taint2
    }

    private static void dup2X2_V2VT(InsnList list, ShadowMethodAdapter adapter) {
        // val1 (type 2), val2, int, taint
        adapter.storeIntAndTaint(list);
        list.add(adapter.loadInt());
        list.add(new InsnNode(DUP2_X2));
        // val2, int, val1 (type 2), val2, int
        list.add(new InsnNode(POP));
        list.add(adapter.loadTaint());
        list.add(new InsnNode(SWAP));
        // val2, int, val1 (type 2), taint, val2
        adapter.swap2(list);
        // val2, int, taint, val2, val1 (type 2)
        list.add(new InsnNode(DUP2_X1));
        list.add(new InsnNode(POP2));
        // val2, int, taint, val1 (type 2), val2
        adapter.loadIntAndTaint(list);
        // val2, int, taint, val1 (type 2), val2, int, taint
    }

    private static void dup2X2_V2TV(InsnList list, ShadowMethodAdapter adapter) {
        // val1 (type 2), int, taint, val2
        adapter.swapWithTaint(list);
        adapter.storeIntAndTaint(list);
        list.add(adapter.loadInt());
        adapter.swap2(list);
        // int, val2, val1 (type 2)
        list.add(new InsnNode(DUP2_X1));
        list.add(new InsnNode(POP2));
        // int, val1 (type 2), val2
        list.add(adapter.loadTaint());
        list.add(new InsnNode(SWAP));
        list.add(new InsnNode(DUP2_X2));
        // int, taint, val2, val1 (type 2), taint, val2
        list.add(adapter.loadInt());
        list.add(new InsnNode(DUP_X2));
        list.add(new InsnNode(POP));
        // int, taint, val2, val1 (type 2), int, taint, val2
    }

    private static void dup2X2_TTTT(InsnList list, ShadowMethodAdapter adapter) {
        // int1, taint1, int2, taint2, int3, taint3, int4, taint4
        adapter.storeIntAndTaint(list, 0); // int4, taint4
        adapter.swapTaints(list);
        adapter.storeIntAndTaint(list, 1); // int2, taint2
        // int1, taint1, int3, taint3
        list.add(new InsnNode(DUP2_X2));
        // int3, taint3, int1, taint1, int3, taint3
        adapter.loadIntAndTaint(list, 0);
        adapter.swapTaints(list);
        adapter.storeIntAndTaint(list, 0); // int3, taint3
        // int3, taint3, int1, taint1, int4, taint4
        list.add(new InsnNode(DUP2_X2));
        // int3, taint3, int4, taint4, int1, taint1, int4, taint4
        adapter.loadIntAndTaint(list, 1);
        adapter.swapTaints(list);
        // int3, taint3, int4, taint4, int1, taint1, int2, taint2, int4, taint4
        adapter.loadIntAndTaint(list, 0);
        adapter.swapTaints(list);
        // int3, taint3, int4, taint4, int1, taint1, int2, taint2, int3, taint3, int4, taint4
    }
    private static void dup2X2_VTTT(InsnList list, ShadowMethodAdapter adapter) {
        // val1, int1, taint1, int2, taint2, int3, taint3
        adapter.storeIntAndTaint(list, 0); // int3, taint3
        adapter.swapTaints(list);
        adapter.storeIntAndTaint(list, 1); // int1, taint1
        // val, int2, taint2
        list.add(new InsnNode(DUP2_X1));
        // int2, taint2, val, int2, taint2
        adapter.loadIntAndTaint(list, 0);
        adapter.swapTaints(list);
        adapter.storeIntAndTaint(list, 0); // int2, taint2
        // int2, taint2, val, int3, taint3
        list.add(new InsnNode(DUP2_X1));
        // int2, taint2, int3, taint3, val, int3, taint3
        adapter.loadIntAndTaint(list, 1);
        adapter.swapTaints(list);
        // int2, taint2, int3, taint3, val, int1, taint1, int3, taint3
        adapter.loadIntAndTaint(list, 0);
        adapter.swapTaints(list);
        // int2, taint2, int3, taint3, val1, int1, taint1, int2, taint2, int3, taint3
    }

    private static void dup2X2_TVTT(InsnList list, ShadowMethodAdapter adapter) {
        // int1, taint1, val, int2, taint2, int3, taint3
        adapter.storeIntAndTaint(list, 0);
        adapter.storeIntAndTaint(list, 1);
        list.add(adapter.loadInt(1));
        list.add(new InsnNode(SWAP));
        adapter.swap2(list);
        // int2, val, int1, taint1
        list.add(adapter.loadTaint(1));
        list.add(new InsnNode(SWAP));
        adapter.swap2(list);
        // int2, taint2, taint1, int1, val
        list.add(adapter.loadInt(0));
        list.add(new InsnNode(SWAP));
        adapter.swap2(list);
        // int2, taint2, int3, val, taint1, int1
        list.add(adapter.loadTaint(0));
        list.add(new InsnNode(SWAP));
        adapter.swap2(list);
        // int2, taint2, int3, taint3, int1, val, taint1
        list.add(new InsnNode(SWAP));
        // int2, taint2, int3, taint3, int1, taint1, val
        adapter.loadIntAndTaint(list, 1);
        adapter.loadIntAndTaint(list, 0);
        // int2, taint2, int3, taint3, int1, taint1, val, int2, taint2, int3, taint3

    }

    private static void dup2X2_VVTT(InsnList list, ShadowMethodAdapter adapter) {
        // val1, val2, int1, taint1, int2, taint2
        adapter.storeIntAndTaint(list, 0);
        list.add(new InsnNode(DUP2_X2));
        adapter.storeIntAndTaint(list, 1);
        // int1, taint1, val1, val2
        adapter.loadIntAndTaint(list, 0);
        list.add(new InsnNode(DUP2_X2));
        // int1, taint1, int2, taint2, val1, val2, int2, taint2
        adapter.loadIntAndTaint(list, 1);
        adapter.swap2(list);
        // int1, taint1, int2, taint2, val1, val2, int1, taint1, int2, taint2
    }

    private static void dup2X2_TTVT(InsnList list, ShadowMethodAdapter adapter) {
        // int1, taint1, int2, taint2, val, int3, taint3
        adapter.storeIntAndTaint(list, 0);
        adapter.swapWithTaint(list);
        adapter.storeIntAndTaint(list, 1);
        // int1, taint1, val
        list.add(adapter.loadInt(0));
        list.add(new InsnNode(DUP2_X2));
        // val, int3, int1, taint1, val, int3
        list.add(new InsnNode(POP));
        list.add(adapter.loadTaint(0));
        list.add(new InsnNode(SWAP));
        adapter.swap2(list);
        // val, int3, taint3, val, int1, taint1
        adapter.swapTaintWithOther(list);
        // val, int3, taint3, int1, taint1, val
        adapter.loadIntAndTaint(list, 1);
        adapter.swapTaintWithOther(list);
        // val, int3, taint3, int1, taint1, int2, taint2, val
        adapter.loadIntAndTaint(list, 0);
        // val, int3, taint3, int1, taint1, int2, taint2, val, int3, taint3
    }

    private static void dup2X2_VTVT(InsnList list, ShadowMethodAdapter adapter) {
        // val1, int1, taint1, val2, int2, taint2
        adapter.storeIntAndTaint(list, 0);
        adapter.swapWithTaint(list);
        adapter.storeIntAndTaint(list, 1);
        // val1, val2
        list.add(new InsnNode(DUP_X1));
        adapter.loadIntAndTaint(list, 0);
        adapter.swap2(list);
        // val2, int2, taint2, val1, val2
        adapter.loadIntAndTaint(list, 1);
        adapter.swapTaintWithOther(list);
        // val2, int2, taint2, val1, int1, taint1, val2
        adapter.loadIntAndTaint(list, 0);
        // val2, int2, taint2, val1, int1, taint1, val2, int2, taint2
    }

    private static void dup2X2_TVVT(InsnList list, ShadowMethodAdapter adapter) {
        // int1, taint1, val1, val2, int2, taint2
        adapter.storeIntAndTaint(list, 0);
        adapter.swap2(list);
        adapter.storeIntAndTaint(list, 1);
        // val1, val2
        list.add(new InsnNode(DUP_X1));
        // val2, val1, val2
        adapter.loadIntAndTaint(list, 0);
        adapter.swap2(list);
        // val2, int2, taint2, val1, val2
        adapter.loadIntAndTaint(list, 1);
        adapter.swap2(list);
        // val2, int2, taint2, int1, taint1, val1, val2
        adapter.loadIntAndTaint(list, 2);
        // val2, int2, taint2, int1, taint1, val1, val2, int2, taint2
    }

    private static void dup2X2_VVVT(InsnList list, ShadowMethodAdapter adapter) {
        // val1, val2, val3, int, taint
        adapter.storeIntAndTaint(list);
        list.add(adapter.loadInt());
        list.add(new InsnNode(DUP2_X2));
        // val3, int, val1, val2, val3, int
        list.add(new InsnNode(POP));
        list.add(adapter.loadTaint());
        list.add(new InsnNode(SWAP));
        adapter.swap2(list);
        // val3, int, taint, val3, val1, val2
        list.add(new InsnNode(DUP2_X1));
        list.add(new InsnNode(POP2));
        // val3, int, taint, val1, val2, val3
        adapter.loadIntAndTaint(list);
        // val3, int, taint, val1, val2, val3, int, taint

    }

    private static void dup2X2_TTTV(InsnList list, ShadowMethodAdapter adapter) {
        // int1, taint1, int2, taint2, int3, taint3, val
        adapter.swapWithTaint(list);
        adapter.storeIntAndTaint(list, 0);
        adapter.swapWithTaint(list);
        adapter.storeIntAndTaint(list, 1);
        // int1, taint1, val
        list.add(adapter.loadInt(0));
        list.add(new InsnNode(SWAP));
        adapter.swap2(list);
        // int3, val, int1, taint1, int3, val;
        list.add(new InsnNode(POP2));
        list.add(adapter.loadTaint(0));
        list.add(new InsnNode(SWAP));
        // int3, val, int1, taint3, taint1
        adapter.swap2(list);
        // int3, taint3, taint1, val, int1
        list.add(new InsnNode(DUP2_X1));
        list.add(new InsnNode(POP));
        // int3, taint3, val, int1, taint1, val
        adapter.loadIntAndTaint(list, 1);
        adapter.swapTaintWithOther(list);
        // int3, taint3, val, int1, taint1, int2, taint2, val
        adapter.loadIntAndTaint(list, 0);
        adapter.swapTaintWithOther(list);
        // int3, taint3, val, int1, taint1, int2, taint2, int3, taint3, val
    }

    private static void dup2X2_VTTV(InsnList list, ShadowMethodAdapter adapter) {
        // val1, int1, taint1, int2, taint2, val2
        adapter.swapWithTaint(list);
        adapter.storeIntAndTaint(list, 0);
        adapter.swapWithTaint(list);
        adapter.storeIntAndTaint(list, 1);
        // val1, val2
        adapter.loadIntAndTaint(list, 0);
        adapter.swap2(list);
        list.add(new InsnNode(DUP_X1));
        // int2, taint2, val2, val1, val2
        adapter.loadIntAndTaint(list, 1);
        adapter.swapTaintWithOther(list);
        // int2, taint2, val2, val1, int1, taint1, val2
        adapter.loadIntAndTaint(list, 0);
        adapter.swapTaintWithOther(list);
        // int2, taint2, val2, val1, int1, taint1, int2, taint2, val2
    }

    private static void dup2X2_TVTV(InsnList list, ShadowMethodAdapter adapter) {
        // int1, taint1, val1, int2, taint2, val2
        adapter.swapWithTaint(list);
        adapter.storeIntAndTaint(list, 0);
        // int1, taint1, val1, val2
        adapter.swap2(list);
        // val1, val2, int1, taint1
        adapter.storeIntAndTaint(list, 1);
        adapter.loadIntAndTaint(list, 0);
        adapter.swap2(list);
        // int2, taint2, val1, val2
        list.add(new InsnNode(DUP_X1));
        adapter.loadIntAndTaint(list, 1);
        // int2, taint2, val2, val1, val2, int1, taint1
        adapter.swap2(list);
        // int2, taint2, val2, int1, taint1, val1, val2
        adapter.loadIntAndTaint(list, 0);
        adapter.swapTaintWithOther(list);
        // int2, taint2, val2, int1, taint1, val1, int2, taint2, val2
    }

    private static void dup2X2_VVTV(InsnList list, ShadowMethodAdapter adapter) {
        // val1, val2, int, taint, val3
        adapter.swapWithTaint(list);
        adapter.storeIntAndTaint(list);
        list.add(adapter.loadInt());
        // val1, val2, val3, int
        list.add(new InsnNode(SWAP));
        adapter.swap2(list);
        // int, val3, val1, val2
        list.add(adapter.loadTaint());
        list.add(new InsnNode(SWAP));
        // int, val3, val1, taint, val2
        adapter.swap2(list);
        // int, taint, val2, val3, val1
        list.add(new InsnNode(DUP2_X1));
        // int, taint, val3, val1, val2, val3, val1
        list.add(new InsnNode(POP));
        adapter.loadIntAndTaint(list);
        // int, taint, val3, val1, val2, val3, int, taint
        adapter.swapTaintWithOther(list);
        // int, taint, val3, val1, val2, int, taint, val3
    }

    private static void dup2X2_TTVV(InsnList list, ShadowMethodAdapter adapter) {
        // int1, taint1, int2, taint2, val1, val2
        adapter.swap2(list);
        adapter.storeIntAndTaint(list);
        // int1, taint1, val1, val2
        list.add(new InsnNode(DUP2_X2));
        adapter.loadIntAndTaint(list);
        // val1, val2, int1, taint1, val1, val2, int2, taint2
        adapter.swap2(list);
        // val1, val2, int1, taint1, int2, taint2, val1, val2
    }

    private static void dup2X2_VTVV(InsnList list, ShadowMethodAdapter adapter) {
        // val1, int, taint, val2, val3
        adapter.swap2(list);
        adapter.storeIntAndTaint(list);
        // val1, val2, val3
        list.add(new InsnNode(DUP2_X1));
        adapter.loadIntAndTaint(list);
        // val2, val3, val1, val2, val3, int, taint
        adapter.swap2(list);
        // val2, val3, val1, int, taint, val2, val3
    }

    private static void dup2X2_TVVV(InsnList list, ShadowMethodAdapter adapter) {
        // int, taint, val1, val2, val3
        adapter.swap2(list);
        list.add(new InsnNode(SWAP));
        // int, val2, val3, val1, taint
        list.add(adapter.storeTaint());
        // int, val2, val3, val1
        list.add(new InsnNode(SWAP));
        // int, val2, val1, val3
        adapter.swap2(list);
        // val1, val3, int, val2,
        list.add(new InsnNode(SWAP));
        // val1, val3, val2, int
        list.add(adapter.storeInt());
        list.add(new InsnNode(SWAP));
        list.add(new InsnNode(DUP2_X2));
        // val2, val3, val1, val2, val3
        list.add(adapter.loadInt());
        list.add(new InsnNode(SWAP));
        // val2, val3, val1, val2, int, val3
        adapter.swap2(list);
        // val2, val3, int, val3, val1, val2
        list.add(adapter.loadTaint());
        list.add(new InsnNode(SWAP));
        // val2, val3, int, val3, val1, taint, val2
        adapter.swap2(list);
        // val2, val3, int, taint, val2, val3, val1
        list.add(new InsnNode(DUP_X2));
        list.add(new InsnNode(POP));
        // val2, val3, int, taint, val1, val2, val3
    }

    public static AbstractInsnNode handle(AbstractInsnNode insn, ShadowMethodAdapter adapter) {
        // ??, ??, ?, ?
        InsnList list = new InsnList();
        if (TypeHelper.isComputationalType2(adapter.getTop())) {
            // ??, ?, val (type2)
            if (TypeHelper.isComputationalType2(adapter.getTop(2))) {
                // val (type2), val (type2)
                return insn;
            } else if (adapter.isTaintOnTop(2)) {
                // ?, int, taint, val (type2)
                if (adapter.isTaintOnTop(4)) {
                    // int, taint, int, taint, val (type2)
                    dup2X2_TTV2(list, adapter);
                } else {
                    // val, int, taint, val (type2)
                    dup2X2_VTV2(list, adapter);
                }
            } else {
                // ?, val, val (type2)
                if (adapter.isTaintOnTop(3)) {
                    // int, taint, val, val (type2)
                    dup2X2_TVV2(list, adapter);
                } else {
                    // val, val, val (type2)
                    return insn;
                }
            }
        } else if (adapter.isTaintOnTop()) {
            // ??, ?, ?, int, taint
            if (adapter.isTaintOnTop(3)) {
                // ??, ?, int, taint, int, taint
                if (TypeHelper.isComputationalType2(adapter.getTop(5))) {
                    // val (type2), int, taint, int, taint
                    dup2X2_V2TT(list, adapter);
                } else if (adapter.isTaintOnTop(5)) {
                    // ?, int, taint, int, taint, int, taint
                    if (adapter.isTaintOnTop(7)) {
                        // int, taint, int, taint, int, taint, int, taint
                        dup2X2_TTTT(list, adapter);
                    } else {
                        // val, int, taint, int, taint, int, taint
                        dup2X2_VTTT(list, adapter);
                    }
                } else {
                    // ?, val, int, taint, int, taint
                    if (adapter.isTaintOnTop(6)) {
                        // int, taint, val, int, taint, int, taint
                        dup2X2_TVTT(list, adapter);
                    } else {
                        // val, val, int, taint, int, taint
                        dup2X2_VVTT(list, adapter);
                    }
                }
            } else {
                // ??, ?, val, int, taint
                if (TypeHelper.isComputationalType2(adapter.getTop(4))) {
                    // val (type2), val, int, taint
                    dup2X2_V2VT(list, adapter);
                } else if (adapter.isTaintOnTop(4)) {
                    // ?, int, taint, val, int, taint
                    if (adapter.isTaintOnTop(6)) {
                        // int, taint, int, taint, val, int, taint
                        dup2X2_TTVT(list, adapter);
                    } else {
                        // val, int, taint, val, int, taint
                        dup2X2_VTVT(list, adapter);
                    }
                } else {
                    // ?, val, val, int, taint
                    if (adapter.isTaintOnTop(5)) {
                        // int, taint, val, val, int, taint
                        dup2X2_TVVT(list, adapter);
                    } else {
                        // val, val, val, int, taint
                        dup2X2_VVVT(list, adapter);
                    }
                }
            }
        } else {
            // ??, ?, ?, val
            if (adapter.isTaintOnTop(2)) {
                // ??, ?, int, taint, val
                if (TypeHelper.isComputationalType2(adapter.getTop(4))) {
                    // val (type2), int, taint, val
                    dup2X2_V2TV(list, adapter);
                } else if (adapter.isTaintOnTop(4)) {
                    // ?, int, taint, int, taint, val
                    if (adapter.isTaintOnTop(6)) {
                        // int, taint, int, taint, int, taint, val
                        dup2X2_TTTV(list, adapter);
                    } else {
                        // val, int, taint, int, taint, val
                        dup2X2_VTTV(list, adapter);
                    }
                } else {
                    // ?, val, int, taint, val
                    if (adapter.isTaintOnTop(5)) {
                        // int, taint, val, int, taint, val
                        dup2X2_TVTV(list, adapter);
                    } else {
                        // val, val, int, taint, val
                        dup2X2_VVTV(list, adapter);
                    }
                }
            } else {
                // ??, ?, val, val
                if (TypeHelper.isComputationalType2(adapter.getTop(3))) {
                    // val (type2), val, val
                    return insn;
                } else if (adapter.isTaintOnTop(3)) {
                    // ?, int, taint, val, val
                    if (adapter.isTaintOnTop(5)) {
                        // int, taint, int, taint, val, val
                        dup2X2_TTVV(list, adapter);
                    } else {
                        // val, int, taint, val, val
                        dup2X2_VTVV(list, adapter);
                    }
                } else {
                    // ?, val, val, val
                    if (adapter.isTaintOnTop(4)) {
                        // int, taint, val, val, val
                        dup2X2_TVVV(list, adapter);
                    } else {
                        // val, val, val, val, val
                        return insn;
                    }
                }
            }
        }
        return adapter.replaceInstruction(insn, list);
    }
}
