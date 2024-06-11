package net.lisstem.taint.asm.insnhandler;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Map;

public class DelegateInsnByOpcode<T extends MethodNode> extends DelegateInsn<T> {
    public DelegateInsnByOpcode() {
        super();
    }

    public DelegateInsnByOpcode(Map<Integer, InsnHandler<T>> handlers) {
        super(handlers);
    }

    @Override
    public AbstractInsnNode handleInstruction(AbstractInsnNode insn, T MethodNode) {
        return super.handleInstruction(insn, MethodNode, insn.getOpcode());
    }
}
