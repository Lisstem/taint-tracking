package net.lisstem.taint.asm.wrapint.handlers;

import net.lisstem.taint.asm.insnhandler.DelegateInsnByType;
import net.lisstem.taint.asm.wrapint.TaintMethodAdapter;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.tree.AbstractInsnNode.*;
public class WrapInsn extends DelegateInsnByType<TaintMethodAdapter> {
    public WrapInsn() {
        addHandler(INSN, new Instruction());
        addHandler(FIELD_INSN, FieldInstruction::handle);
        addHandler(FRAME, WrapInsn::handleFrame);
        addHandler(IINC_INSN, WrapInsn::handleIincInstruction);
        addHandler(INT_INSN, new IntInstruction());
        addHandler(JUMP_INSN, new JumpInstruction());
        addHandler(LDC_INSN, (insn, adapter) -> ((LdcInsnNode)insn).cst instanceof Integer ? adapter.box(insn) : insn);
        addHandler(TYPE_INSN, TypeInstruction::handle);
        addHandler(LOOKUPSWITCH_INSN, (insn, adapter) -> adapter.unbox(insn));
        addHandler(TABLESWITCH_INSN, (insn, adapter) -> adapter.unbox(insn));
        addHandler(VAR_INSN, new VarInstruction());
        addHandler(METHOD_INSN, MethodInvocation::handleMethodInstruction);
        addHandler(INVOKE_DYNAMIC_INSN, MethodInvocation::handleInvokeDynamicInstruction);
        addHandler(MULTIANEWARRAY_INSN, MultiANewArray::handleFrame);
        /* LABEL, LINE */
    }


    public static AbstractInsnNode handleFrame(AbstractInsnNode insn, TaintMethodAdapter adapter) {
        FrameNode frame = (FrameNode) insn;
        if (frame.local != null)
            frame.local.replaceAll(adapter::replaceInts);
        if (frame.stack != null)
            frame.stack.replaceAll(adapter::replaceInts);

        return frame;
    }

    private static AbstractInsnNode handleIincInstruction(AbstractInsnNode insn, TaintMethodAdapter adapter) {
        IincInsnNode iincInsn = (IincInsnNode)insn;
        InsnList list = new InsnList();
        list.add(new VarInsnNode(ALOAD, iincInsn.var));
        list.add(net.lisstem.taint.asm.Instruction.loadInt(iincInsn.incr));
        list.add(new MethodInsnNode(INVOKEVIRTUAL, adapter.targetType.getInternalName(), "iinc", "(I)" + adapter.targetType.getDescriptor(), false));
        list.add(new VarInsnNode(ASTORE, iincInsn.var));
        return adapter.replaceInstruction(insn, list);
    }

}
