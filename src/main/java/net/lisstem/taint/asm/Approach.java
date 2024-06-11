package net.lisstem.taint.asm;

import net.lisstem.taint.taint.BooleanTaint;
import net.lisstem.taint.taint.TaintedInt;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassVisitor;

public enum Approach {
    BOX, SHADOW, NONE;

    public static Approach get(@NotNull String name) {
        return switch (name.toLowerCase())
        {
            case "wrap", "box" -> BOX;
            case "shadow" -> SHADOW;
            default -> NONE;
        };
    }

    public ClassVisitor getAdapter(ClassVisitor delegate) {
        return getAdapter(delegate, false);
    }

    public ClassVisitor getAdapter(ClassVisitor delegate, boolean noPrinting) {
        return switch (this) {
            case NONE -> delegate;
            case BOX -> new net.lisstem.taint.asm.wrapint.TaintClassAdapter(delegate, TaintedInt.class, noPrinting);
            case SHADOW -> new net.lisstem.taint.asm.shadowint.TaintClassAdapter(delegate, BooleanTaint.class, noPrinting);
        };
    }
}