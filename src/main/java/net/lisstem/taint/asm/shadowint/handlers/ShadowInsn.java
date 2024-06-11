package net.lisstem.taint.asm.shadowint.handlers;

import net.lisstem.taint.asm.TypeHelper;
import net.lisstem.taint.asm.insnhandler.DelegateInsnByOpcode;
import net.lisstem.taint.asm.insnhandler.DelegateInsnByType;
import net.lisstem.taint.asm.shadowint.ShadowMethodAdapter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.tree.AbstractInsnNode.*;
public class ShadowInsn extends DelegateInsnByType<ShadowMethodAdapter> {
    public ShadowInsn() {
        addHandler(INSN, new Instruction());
        addHandler(FIELD_INSN, new FieldInstruction());
        addHandler(LDC_INSN, (insn, adapter) -> ((LdcInsnNode)insn).cst instanceof Integer ? adapter.addTaint(insn) : insn);
        addHandler(FRAME, Frame::handle);
        addHandler(MULTIANEWARRAY_INSN, MultiANewArrayInstruction::handle);
        addHandler(IINC_INSN, (insn, adapter) -> {
            IincInsnNode iincInsn = (IincInsnNode)insn;
            adapter.map(iincInsn);
            return insn;
        });
        addHandler(INT_INSN, new IntInstruction());

        addHandler(JUMP_INSN, new DelegateInsnByOpcode<>() {
            {
                for (int opcode: new int[]{IF_ICMPEQ, IF_ICMPNE, IF_ICMPLT, IF_ICMPGE, IF_ICMPGT, IF_ICMPLE}) {
                    addHandler(opcode, (insn, adapter) -> adapter.pop2Taints(insn));
                }
                for (int opcode: new int[]{IFEQ, IFNE, IFLT, IFGE, IFGT, IFLE}) {
                    addHandler(opcode, (insn, adapter) -> adapter.popTaint(insn));
                }
                for (int opcode: new int[]{IFNULL, IFNONNULL}) {
                    addHandler(opcode, (insn, adapter) -> adapter.isTaintOnTop() ? adapter.popTaint() : insn);
                }
                for (int opcode: new int[]{IF_ACMPEQ, IF_ACMPNE}) {
                    addHandler(opcode, (insn, adapter) ->
                            {
                                if (adapter.isTaintOnTop())
                                    insn = adapter.popTaint(insn);
                                if (adapter.isTaintOnTop(2))
                                    insn = adapter.popSecondTaint(insn);
                                return insn;
                            }
                            );
                }
                /* GOTO, JSR (deprecated) does not do anything */
            }
        });
        addHandler(LOOKUPSWITCH_INSN, (insn, adapter) -> adapter.popTaint(insn));
        addHandler(TABLESWITCH_INSN, (insn, adapter) -> adapter.popTaint(insn));

        MethodInvocation methodInvocation = new MethodInvocation();
        addHandler(METHOD_INSN, methodInvocation::handleMethodInstruction);
        addHandler(INVOKE_DYNAMIC_INSN, methodInvocation::handleInvokeDynamicInstruction);

        addHandler(TYPE_INSN, new DelegateInsnByOpcode<>() {
            {
                addHandler(ANEWARRAY, this::handleANewArray);
                addHandler(INSTANCEOF, (insn, adapter) -> adapter.addTaint(handleCast(insn, adapter)));
                addHandler(CHECKCAST, this::handleCheckCast);
                /* NEW does nothing*/
            }

            private AbstractInsnNode handleCast(AbstractInsnNode insn, ShadowMethodAdapter adapter) {
                if (adapter.isTaintOnTop())
                    return adapter.popTaint(insn);

                return insn;
            }

            private AbstractInsnNode handleCheckCast(AbstractInsnNode insn, ShadowMethodAdapter adapter) {
                insn = handleCast(insn, adapter);
                Type type = Type.getObjectType(((TypeInsnNode) insn).desc);
                if (TypeHelper.isIntArray(type)) {
                    return adapter.createTaintArray(insn, type);
                }
                return insn;
            }

            private AbstractInsnNode handleANewArray(AbstractInsnNode insn, ShadowMethodAdapter adapter) {
                insn = adapter.popTaint(insn);
                Type type = Type.getObjectType(((TypeInsnNode) insn).desc);
                if (TypeHelper.isIntArray(type)) {
                    return adapter.createTaintArray(insn, TypeHelper.createArrayType(1, type));
                }
                return insn;
            }
        });

        addHandler(VAR_INSN, new VarInstruction());
        /* LABEL, LINE does nothing */
    }

}
