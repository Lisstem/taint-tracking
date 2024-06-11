package net.lisstem.taint.asm.shadowint.handlers;

import net.lisstem.taint.asm.shadowint.ShadowMethodAdapter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FrameNode;

public class Frame {
    public static AbstractInsnNode handle(AbstractInsnNode insn, ShadowMethodAdapter adapter) {
        adapter.instructions.remove(insn);
        return null;
    }

    private static Object mapType(Type type) {
        return switch (type.getSort()) {
            case Type.ARRAY, Type.OBJECT -> type.getInternalName();
            case Type.BOOLEAN, Type.BYTE, Type.CHAR, Type.INT, Type.SHORT -> Opcodes.INTEGER;
            case Type.DOUBLE -> Opcodes.DOUBLE;
            case Type.FLOAT -> Opcodes.FLOAT;
            case Type.LONG -> Opcodes.LONG;
            default -> null;
        };
    }
}
