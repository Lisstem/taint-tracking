package net.lisstem.taint.asm.wrapint.handlers;

import net.lisstem.taint.asm.insnhandler.DelegateInsnByOpcode;
import net.lisstem.taint.asm.wrapint.TaintMethodAdapter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;

import static org.objectweb.asm.Opcodes.*;

public class IntInstruction extends DelegateInsnByOpcode<TaintMethodAdapter> {
    public IntInstruction() {
        for (int opcode: new int[]{ BIPUSH, SIPUSH }) {
            addHandler(opcode, (insn, adapter) -> adapter.box(insn));
        }
        addHandler(NEWARRAY, IntInstruction::handleNewArray);
    }
    private static AbstractInsnNode handleNewArray(AbstractInsnNode insn, TaintMethodAdapter adapter) {
        IntInsnNode intInsn = (IntInsnNode)insn;
        if (intInsn.operand == T_INT)
            insn = adapter.replaceInstruction(insn, new TypeInsnNode(ANEWARRAY, adapter.targetType.getInternalName()));
        return adapter.unbox(insn);
    }

}
