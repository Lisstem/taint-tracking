package net.lisstem.taint.asm.shadowint;

import net.lisstem.taint.asm.MethodHelper;
import net.lisstem.taint.asm.MethodWrapper;
import net.lisstem.taint.asm.TypeHelper;
import net.lisstem.taint.taint.Taintable;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;

public abstract class ShadowMethodWrapper extends MethodWrapper {
    protected boolean returnsBox;

    protected boolean returnsInt;
    protected final Type shadowType;

    protected ShadowMethodWrapper(Type shadowType) {
        this.shadowType = shadowType;
    }

    public boolean isReturningBox() {
        return returnsBox;
    }

    public boolean isReturningInt() {
        return returnsInt;
    }


    protected List<Type> getArgs(Type[] args) {
        return new ArrayList<>(Arrays.asList(args));
    }

    private void loadArgsToStack(MethodVisitor mv, List<Type> args) {
        Type[] locals = Type.getMethodType(wrapper.desc).getArgumentTypes();
        for (int locale = 0, arg = 0, i = 0; arg < args.size(); locale++, i++) {
            Type type = locals[locale];
            switch (type.getSort()) {
                case Type.INT, Type.BYTE, Type.BOOLEAN, Type.SHORT, Type.CHAR -> {
                    mv.visitVarInsn(ILOAD, i);
                    arg++;
                }
                case Type.OBJECT -> {
                    if (type.equals(shadowType) == args.get(arg).equals(shadowType)) {
                        mv.visitVarInsn(ALOAD, i);
                        arg++;
                    }
                }
                case Type.DOUBLE -> {
                    mv.visitVarInsn(DLOAD, i);
                    arg++; i++;
                }
                case Type.FLOAT -> {
                    mv.visitVarInsn(FLOAD, i);
                    arg++;
                }
                case Type.LONG -> {
                    mv.visitVarInsn(LLOAD, i);
                    arg++; i++;
                }
                case Type.ARRAY -> {
                    if (TypeHelper.isArrayOf(type, shadowType) == TypeHelper.isArrayOf(args.get(arg), shadowType)) {
                        mv.visitVarInsn(ALOAD, i);
                        arg++;
                    }
                }
            }
        }
    }

    public void syncArrays(MethodVisitor mv) {
        Type[] args = Type.getMethodType(wrapper.desc).getArgumentTypes();
        for (int i = 0; i < args.length; i++) {
            Type arg = args[i];
            if (TypeHelper.isIntArray(arg) && arg.getDimensions() > 1) {
                mv.visitVarInsn(ALOAD, i);
                mv.visitVarInsn(ALOAD, i + 1);
                mv.visitTypeInsn(NEW, shadowType.getInternalName());
                mv.visitInsn(DUP);
                mv.visitMethodInsn(INVOKESPECIAL, shadowType.getInternalName(), MethodHelper.CONSTRUCTOR_NAME, MethodHelper.EMPTY_DESC, false);
                mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(TaintArray.class), "syncArrays", Type.getMethodDescriptor(Type.VOID_TYPE, args[i],
                        TypeHelper.createArrayType(args[i].getDimensions(), Type.getType(Taintable.class)), Type.getType(Taintable.class)), false);
            }
        }
    }

    @Override
    public void createWrappingMethod(MethodVisitor mv) {
        if (mv != null) {
            mv.visitCode();
            Type signature = getWrappedSignature();
            List<Type> args = getArgs(signature.getArgumentTypes());
            loadArgsToStack(mv, args);
            getWrappedInsn().accept(mv);
            syncArrays(mv);
            mv.visitInsn(TypeHelper.returnFor(signature.getReturnType()));
            mv.visitMaxs(args.size(), args.size());
            mv.visitEnd();
        }
    }
}
