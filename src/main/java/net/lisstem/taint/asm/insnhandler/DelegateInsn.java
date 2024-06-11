package net.lisstem.taint.asm.insnhandler;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.HashMap;
import java.util.Map;

public abstract class DelegateInsn<T extends MethodNode> implements InsnHandler<T> {
    private final Map<Integer, InsnHandler<T>> handlers;
    private InsnHandler<T> defaultHandler;

    public DelegateInsn() {
        this(new HashMap<>());
    }

    public DelegateInsn(Map<Integer, InsnHandler<T>> handlers) {
        this.handlers = handlers;
    }

    public void setDefault(InsnHandler<T> handler) {
        defaultHandler = handler;
    }

    public void addHandler(int key, InsnHandler<T> handler) {
        if (handler == this)
            return;
        if (handlers.containsKey(key))
            throw new IllegalArgumentException("Handler for " + key + " is already set");
        handlers.put(key, handler);
    }

    public AbstractInsnNode handleInstruction(AbstractInsnNode insn, T MethodNode, int key) {
        InsnHandler<T> handler = handlers.get(key);
        if (handler != null)
            return handler.handleInstruction(insn, MethodNode);

        return defaultHandler == null ? insn : defaultHandler.handleInstruction(insn, MethodNode);
    }
}
