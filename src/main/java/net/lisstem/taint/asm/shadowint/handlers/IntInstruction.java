package net.lisstem.taint.asm.shadowint.handlers;

import net.lisstem.taint.asm.insnhandler.DelegateInsnByOpcode;
import net.lisstem.taint.asm.shadowint.ShadowMethodAdapter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

public class IntInstruction extends DelegateInsnByOpcode<ShadowMethodAdapter> {
    public IntInstruction()
    {
        addHandler(BIPUSH, (insn, adapter) -> adapter.addTaint(insn));
        addHandler(SIPUSH, (insn, adapter) -> adapter.addTaint(insn));
        // TODO: Propagate Taint to TaintedArray?
        addHandler(NEWARRAY, IntInstruction::handleNewArray);
        /* NEW does nothing*/
    }

    private static AbstractInsnNode handleNewArray(AbstractInsnNode insn, ShadowMethodAdapter adapter) {
        IntInsnNode newArray = (IntInsnNode) insn;
        if (newArray.operand == T_INT) {
            InsnList list = new InsnList();
            list.add(adapter.popTaint());
            list.add(new InsnNode(DUP));
            list.add(insn.clone(null));
            list.add(new InsnNode(SWAP));
            list.add(new TypeInsnNode(ANEWARRAY, Type.getInternalName(adapter.taintClass)));
            return adapter.replaceInstruction(insn, list);
        } else {
            return adapter.popTaint(insn);
        }
    }
}
