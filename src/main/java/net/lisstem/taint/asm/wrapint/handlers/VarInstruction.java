package net.lisstem.taint.asm.wrapint.handlers;

import net.lisstem.taint.asm.insnhandler.DelegateInsnByOpcode;
import net.lisstem.taint.asm.wrapint.TaintMethodAdapter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Opcodes.ASTORE;

public class VarInstruction extends DelegateInsnByOpcode<TaintMethodAdapter> {
    public VarInstruction() {
        addHandler(ILOAD, (insn, adapter) ->
                adapter.replaceInstruction(insn, new VarInsnNode(ALOAD, ((VarInsnNode)insn).var)));
        addHandler(ISTORE, (insn, adapter) ->
                adapter.replaceInstruction(insn, new VarInsnNode(ASTORE, ((VarInsnNode)insn).var)));
    }
}
