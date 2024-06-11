package net.lisstem.taint.asm;

import net.lisstem.taint.asm.insnhandler.InsnHandler;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;

public class FrameMethodNode extends MethodNode {

    public FrameList stack;

    public final String className;

    public FrameMethodNode(int access, String name, String desc, String signature, String[] exceptions,
                           String className) {
        super(ASM9, access, name, desc, signature, exceptions);
        this.className = className;
    }

    public AbstractInsnNode print(AbstractInsnNode insn, String message) {
        InsnList list = new InsnList();
        list.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(java.lang.System.class), "out", Type.getDescriptor(java.io.PrintStream.class)));
        list.add(new LdcInsnNode(message));
        list.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(java.io.PrintStream.class), "println", "(Ljava/lang/String;)V", false));
        return insertInstructionsBefore(insn, list);
    }

    public AbstractInsnNode replaceInstruction(AbstractInsnNode previous, AbstractInsnNode replacement) {
        if (previous == replacement)
            return previous;

        instructions.insert(previous, replacement);
        instructions.remove(previous);
        return replacement;
    }

    public AbstractInsnNode replaceInstruction(AbstractInsnNode previous, InsnList replacement) {
        AbstractInsnNode current = replacement.getFirst();
        while (current.getNext() != null) {
            stack.apply(current);
            current = current.getNext();
        }

        instructions.insert(previous, replacement);
        instructions.remove(previous);
        return current;
    }

    public AbstractInsnNode insertInstruction(AbstractInsnNode previous, AbstractInsnNode next) {
        instructions.insert(previous, next);
        stack.apply(previous);
        return next;
    }

    public AbstractInsnNode insertInstructions(AbstractInsnNode previous, InsnList next) {
        if (next.size() == 0)
            return previous;

        stack.apply(previous);
        AbstractInsnNode current = next.getFirst();
        while (current.getNext() != null) {
            stack.apply(current);
            current = current.getNext();
        }
        instructions.insert(previous, next);
        return current;
    }

    public AbstractInsnNode insertInstructionBefore(AbstractInsnNode next, AbstractInsnNode previous) {
        instructions.insertBefore(next, previous);
        stack.apply(previous);
        return next;
    }
    public AbstractInsnNode insertInstructionsBefore(AbstractInsnNode next, InsnList previous) {
        if (previous.size() == 0)
            return next;

        previous.forEach(insn -> stack.apply(insn));
        instructions.insertBefore(next, previous);
        return next;
    }

    public void swap2(InsnList list) {
        list.add(new InsnNode(DUP2_X2));
        list.add(new InsnNode(POP2));
    }

    protected boolean isMember() {
        return (access & ACC_STATIC) == 0;
    }

    protected List<Type> getArgs() {
        List<Type> args = new ArrayList<>(Arrays.asList(Type.getMethodType(desc).getArgumentTypes()));
        if (isMember())
            args.add(0, Type.getObjectType(className));
        return args;
    }

    public <T extends MethodNode> void visitEnd(InsnHandler<T> handler, T node) {
        stack = new FrameList(getArgs());
        Iterator<AbstractInsnNode> iter = instructions.iterator();
        while (iter.hasNext()) {
            AbstractInsnNode insn = handler.handleInstruction(iter.next(), node);
            if (insn != null)
                stack.apply(insn);
        }
        afterInstructions();
    }

    protected void afterInstructions() {

    }
}
