package net.lisstem.taint.asm;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LocalVariableNode;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FrameList {
    private final Map<AbstractInsnNode, Frame> frames = new HashMap<>();
    private Frame current;

    public FrameList(List<Type> localVariables) {
        current = new Frame(localVariables);
        frames.put(null, current);
    }

    public Frame getCurrent() {
        return current;
    }

    public void apply(AbstractInsnNode instruction) {
        try {
            current = current.apply(instruction);
            frames.put(instruction, current);
        } catch (RuntimeException exception) {
            exception.printStackTrace();
            System.err.println(this);
            //throw exception;
        }
    }

    public Frame get(AbstractInsnNode instruction) {
        return frames.get(instruction);
    }

    public Frame next(Frame frame) {
        return frame == null ? null : frame.next;
    }

    public Frame previous(Frame frame) {
        if (frame == null)
            return null;
        return frame.previous;
    }

    @Override
    public String toString() {
        List<Frame> frames = new LinkedList<>();
        Frame iter = current;
        while (iter != null) {
            frames.add(0, iter);
            iter = iter.previous;
        }

        return frames.stream().map(Frame::toString).collect(Collectors.joining("\n"));
    }
}
