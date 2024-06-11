package net.lisstem.taint.asm.insnhandler;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

public interface InsnHandler<T extends MethodNode> {
    AbstractInsnNode handleInstruction(AbstractInsnNode insn, T MethodNode);
}
