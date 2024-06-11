package net.lisstem.taint.asm.wrapint.handlers;

import net.lisstem.taint.asm.wrapint.TaintMethodAdapter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;

import static org.objectweb.asm.Opcodes.*;

public class FieldInstruction {

    public static AbstractInsnNode handle(AbstractInsnNode insn, TaintMethodAdapter adapter) {
        FieldInsnNode fieldInsn = (FieldInsnNode) insn;
        return switch (Type.getType(fieldInsn.desc).getSort()) {
            case Type.INT -> adapter.replaceInstruction(fieldInsn, replacedInstruction(fieldInsn, adapter.targetType));
            case Type.BYTE, Type.CHAR, Type.BOOLEAN, Type.SHORT ->
                    switch (insn.getOpcode()) {
                        case PUTSTATIC, PUTFIELD -> adapter.unbox(insn);
                        case GETSTATIC, GETFIELD -> adapter.box(insn);
                        default -> insn;
                    };
            default -> insn;
        };
    }

    private static AbstractInsnNode replacedInstruction(FieldInsnNode insn, Type targetType) {
        return new FieldInsnNode(insn.getOpcode(), insn.owner, insn.name, targetType.getDescriptor());
    }
}
