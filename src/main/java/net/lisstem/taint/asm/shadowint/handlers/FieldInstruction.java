package net.lisstem.taint.asm.shadowint.handlers;

import net.lisstem.taint.asm.TypeHelper;
import net.lisstem.taint.asm.insnhandler.DelegateInsn;
import net.lisstem.taint.asm.insnhandler.DelegateInsnByOpcode;
import net.lisstem.taint.asm.insnhandler.InsnHandler;
import net.lisstem.taint.asm.shadowint.ShadowMethodAdapter;
import net.lisstem.taint.asm.shadowint.Utils;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnNode;


import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Opcodes.GETSTATIC;

public class FieldInstruction extends DelegateInsn<ShadowMethodAdapter> {

    private final InsnHandler<ShadowMethodAdapter> taintedFieldInstruction;

    public FieldInstruction() {
        super();

        taintedFieldInstruction = new TaintedFieldInstruction();

        InsnHandler<ShadowMethodAdapter> nonIntInteger = new DelegateInsnByOpcode<>() {
            {
                addHandler(PUTFIELD, ((insn, adapter) -> adapter.popTaint(insn)));
                addHandler(PUTSTATIC, ((insn, adapter) -> adapter.popTaint(insn)));
                addHandler(GETFIELD, ((insn, adapter) -> adapter.addTaint(insn)));
                addHandler(GETSTATIC, ((insn, adapter) -> adapter.addTaint(insn)));
            }
        };
        for (int sort: new int[]{Type.BYTE, Type.CHAR, Type.BOOLEAN, Type.SHORT}) {
            addHandler(sort, nonIntInteger);
        }
        addHandler(Type.INT, this::handleIntOrArray);
        addHandler(Type.ARRAY, this::handleIntOrArray);
    }

    private AbstractInsnNode handleIntOrArray(AbstractInsnNode insn, ShadowMethodAdapter adapter) {
        if (TypeHelper.isIntTypeOrArray(Type.getType(((FieldInsnNode)insn).desc))) {
            return taintedFieldInstruction.handleInstruction(insn, adapter);
        }
        return insn;
    }

    @Override
    public AbstractInsnNode handleInstruction(AbstractInsnNode insn, ShadowMethodAdapter MethodNode) {
        Type type = Type.getType(((FieldInsnNode)insn).desc);
        return super.handleInstruction(insn, MethodNode, type.getSort());
    }
}
