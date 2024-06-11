package net.lisstem.taint.asm.shadowint.handlers;

import net.lisstem.taint.asm.insnhandler.DelegateInsnByOpcode;
import net.lisstem.taint.asm.shadowint.ShadowMethodAdapter;
import net.lisstem.taint.asm.shadowint.Utils;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnNode;

import static org.objectweb.asm.Opcodes.*;

public class TaintedFieldInstruction extends DelegateInsnByOpcode<ShadowMethodAdapter> {
        public TaintedFieldInstruction() {
            super();
            addHandler(GETFIELD, this::handleGetField);
            addHandler(GETSTATIC, this::handleGetStatic);
            addHandler(PUTSTATIC, this::handlePutStatic);
            addHandler(PUTFIELD, this::handlePutField);
        }

    private String getTaintedFieldDesc(FieldInsnNode insn, ShadowMethodAdapter adapter) {
        return Utils.shadowType(Type.getType(insn.desc), Type.getType(adapter.taintClass)).getDescriptor();
    }

    private AbstractInsnNode handleGetField(AbstractInsnNode insn, ShadowMethodAdapter adapter) {
        FieldInsnNode fieldInsn = (FieldInsnNode) insn;
        insn = adapter.insertInstructionBefore(insn, new InsnNode(DUP));
        insn = adapter.insertInstruction(insn, new InsnNode(SWAP));
        return adapter.insertInstruction(insn, new FieldInsnNode(GETFIELD, fieldInsn.owner,
                Utils.shadowVariableName(fieldInsn.name), getTaintedFieldDesc(fieldInsn, adapter)));
    }

    private AbstractInsnNode handlePutField(AbstractInsnNode insn, ShadowMethodAdapter adapter) {
        FieldInsnNode fieldInsn = (FieldInsnNode) insn;
        insn = adapter.insertInstructionBefore(insn, new InsnNode(DUP_X2));
        insn = adapter.insertInstructionBefore(insn, new InsnNode(POP));
        insn = adapter.insertInstructionBefore(insn, new InsnNode(DUP2));
        insn = adapter.insertInstruction(insn, new InsnNode(POP));
        insn = adapter.insertInstruction(insn, new InsnNode(SWAP));
        return adapter.insertInstruction(insn, new FieldInsnNode(PUTFIELD, fieldInsn.owner,
                Utils.shadowVariableName(fieldInsn.name), getTaintedFieldDesc(fieldInsn, adapter)));
    }

    private AbstractInsnNode handlePutStatic(AbstractInsnNode insn, ShadowMethodAdapter adapter) {
        FieldInsnNode fieldInsn = (FieldInsnNode) insn;
        return adapter.insertInstructionBefore(insn, new FieldInsnNode(PUTSTATIC, fieldInsn.owner,
                Utils.shadowVariableName(fieldInsn.name), getTaintedFieldDesc(fieldInsn, adapter)));
    }

    private AbstractInsnNode handleGetStatic(AbstractInsnNode insn, ShadowMethodAdapter adapter) {
        FieldInsnNode fieldInsn = (FieldInsnNode) insn;
        return adapter.insertInstruction(insn, new FieldInsnNode(GETSTATIC, fieldInsn.owner,
                Utils.shadowVariableName(fieldInsn.name), getTaintedFieldDesc(fieldInsn, adapter)));
    }
}
