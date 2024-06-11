package net.lisstem.taint.asm.wrapint.handlers;

import net.lisstem.taint.asm.insnhandler.DelegateInsnByOpcode;
import net.lisstem.taint.asm.wrapint.TaintMethodAdapter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Opcodes.IFLE;

public class JumpInstruction extends DelegateInsnByOpcode<TaintMethodAdapter> {
    public JumpInstruction() {
        for (int opcode: new int[] { IF_ICMPEQ, IF_ICMPNE, IF_ICMPLT, IF_ICMPGE, IF_ICMPGT, IF_ICMPLE }) {
            addHandler(opcode, (insn, adapter) -> adapter.unbox2(insn));
        }
        for (int opcode: new int[] { IFEQ, IFNE, IFLT, IFGE, IFGT, IFLE }) {
            addHandler(opcode, (insn, adapter) -> adapter.unbox(insn));
        }
        /* GOTO, IF_ACMPEQ, IF_ACMPNE, IFNULL, IFNONNULL, JSR (depracted) */
    }
}
