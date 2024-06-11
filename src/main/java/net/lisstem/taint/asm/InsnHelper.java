package net.lisstem.taint.asm;


import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.VarInsnNode;
import static org.objectweb.asm.Opcodes.*;

public class InsnHelper {
    public static boolean doesStore(VarInsnNode insn) {
        return switch (insn.getOpcode()) {
            case ISTORE, ASTORE, DSTORE, LSTORE, FSTORE -> true;
            default -> false;
        };
    }

    public static int operatesOn(VarInsnNode insn) {
        return switch (insn.getOpcode()) {
            case ILOAD, ISTORE -> Type.INT;
            case DLOAD, DSTORE -> Type.DOUBLE;
            case ALOAD, ASTORE -> Type.OBJECT;
            case FLOAD, FSTORE -> Type.FLOAT;
            case LLOAD, LSTORE -> Type.LONG;
            default -> -1;
        };
    }



    public static InsnList mergeInsnList(InsnList a, InsnList b) {
        a.add(b);
        return a;
    }
}
