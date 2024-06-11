package net.lisstem.taint.asm.wrapint.handlers;

import net.lisstem.taint.asm.TypeHelper;
import net.lisstem.taint.asm.wrapint.TaintMethodAdapter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;

import static org.objectweb.asm.Opcodes.ANEWARRAY;
import static org.objectweb.asm.Opcodes.INSTANCEOF;

public class TypeInstruction {
    public static AbstractInsnNode handle(AbstractInsnNode insn, TaintMethodAdapter adapter) {
        TypeInsnNode typeInsn = (TypeInsnNode)insn;
        // TODO: Check if top is really tainted before instanceof and checkcast
        Type type = Type.getObjectType(typeInsn.desc);
        if (TypeHelper.isIntArray(type))
            typeInsn.desc = TypeHelper.createArrayType(type.getDimensions(), adapter.targetType).getInternalName();

        return switch (insn.getOpcode()) {
            case ANEWARRAY -> adapter.unbox(insn);
            case INSTANCEOF -> adapter.box(insn);
            default /* NEW, CHECKCAST */ -> insn;
        };
    }
}
