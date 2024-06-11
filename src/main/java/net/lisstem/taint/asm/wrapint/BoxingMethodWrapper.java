package net.lisstem.taint.asm.wrapint;

import net.lisstem.taint.asm.MethodWrapper;
import net.lisstem.taint.asm.TypeHelper;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Integer.max;
import static org.objectweb.asm.Opcodes.*;

public abstract class BoxingMethodWrapper extends MethodWrapper {

    protected final Type targetType;
    protected boolean boxingRequired;

    protected BoxingMethodWrapper(Type targetType) {
        this.targetType = targetType;;
        boxingRequired = false;
    }

    public boolean isBoxingRequired() {
        return boxingRequired;
    }

    protected List<Type> GetParams(Type[] params) {
        return new ArrayList<>(Arrays.asList(params));
    }
    @Override
    public void createWrappingMethod(MethodVisitor mv) {
        if (mv != null) {
            Type signature = getWrappedSignature();
            List<Type> params = GetParams(signature.getArgumentTypes());
            Label start = new Label();
            Label end = new Label();

            mv.visitCode();

            mv.visitLabel(start);
            int intArrays = unboxIntArrays(mv, params, start, end);
            unboxParams(mv, params);
            getWrappedInsn().accept(mv);
            syncArrays(mv, params);
            mv.visitLabel(end);

            Type ret = signature.getReturnType();
            returnValue(mv, ret);

            mv.visitMaxs(params.size() + intArrays, max(max(
                            params.size(),
                            intArrays > 0 ? 2 + (TypeHelper.isIntArray(ret) ?  1 : 0) : 0),
                    ret.equals(Type.VOID_TYPE) ? 0 : 1));
            mv.visitEnd();
        }
    }


    private void unboxIntArray(@NotNull MethodVisitor mv, int var, int dim, int unboxVar) {
        if (dim > TaintedIntArray.MAX_DIM)
            throw new IllegalArgumentException("Unboxing tainted int arrays not implemented for dimension greater than " + TaintedIntArray.MAX_DIM);
        mv.visitVarInsn(ALOAD, var);
        mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(TaintedIntArray.class), "unbox",
                Type.getMethodDescriptor(TypeHelper.createArrayType(dim, Type.INT_TYPE), TypeHelper.createArrayType(dim, targetType)),
                false);
        mv.visitVarInsn(ASTORE, unboxVar);
    }

    private int unboxIntArrays(@NotNull MethodVisitor mv, List<Type> params, Label start, Label end) {
        int count = 0;
        for (int i = 0; i < params.size(); i++) {
            Type param = params.get(i);
            if (TypeHelper.isIntArray(param)) {
                int index = params.size() + count++;
                mv.visitLocalVariable("$intArray" + count,
                        TypeHelper.createArrayType(param.getDimensions(), Type.INT_TYPE).getDescriptor(),
                        null, start, end, index);
                unboxIntArray(mv, i, param.getDimensions(), index);
            }
        }
        return count;
    }

    private void unboxParams(@NotNull MethodVisitor mv, @NotNull List<Type> params) {
        int intArrays = 0;
        for (int i = 0; i < params.size(); i++) {
            Type param = params.get(i);
            switch (param.getSort()) {
                //VOID, BOOLEAN, CHAR, BYTE, SHORT, INT, FLOAT, LONG, DOUBLE, ARRAY, OBJECT or METHOD.
                case Type.BOOLEAN, Type.CHAR, Type.BYTE, Type.SHORT, Type.INT -> {
                    mv.visitVarInsn(ALOAD, i);
                    mv.visitMethodInsn(INVOKEVIRTUAL, targetType.getInternalName(), "getData", "()I", false);
                }
                case Type.FLOAT -> mv.visitVarInsn(FLOAD, i);
                case Type.LONG -> mv.visitVarInsn(LLOAD, i++);
                case Type.DOUBLE -> mv.visitVarInsn(DLOAD, i++);
                case Type.ARRAY -> mv.visitVarInsn(ALOAD,
                        TypeHelper.isIntArray(param) ? params.size() + intArrays++ : i);
                case Type.OBJECT -> mv.visitVarInsn(ALOAD, i);
            }
        }
    }

    private void syncArray(@NotNull MethodVisitor mv, int var, int dim, int unboxVar) {
        if (dim > TaintedIntArray.MAX_DIM)
            throw new IllegalArgumentException("Syncing tainted int arrays not implemented for dimension greater than " + TaintedIntArray.MAX_DIM);
        mv.visitVarInsn(ALOAD, var);
        mv.visitVarInsn(ALOAD, unboxVar);
        mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(TaintedIntArray.class), "sync",
                Type.getMethodDescriptor(TypeHelper.createArrayType(dim, targetType), TypeHelper.createArrayType(dim, targetType), TypeHelper.createArrayType(dim, Type.INT_TYPE)), false);
        mv.visitInsn(POP);
    }

    private void syncArrays(@NotNull MethodVisitor mv, @NotNull List<Type> params) {
        int count = 0;
        for (int i = 0; i < params.size(); i++) {
            Type param = params.get(i);
            if (TypeHelper.isIntArray(param)) {
                int index = params.size() + count++;
                syncArray(mv, i, param.getDimensions(), index);
            }
        }
    }

    private void returnValue(@NotNull MethodVisitor mv, @NotNull Type returnType) {
        if (TypeHelper.isIntArray(returnType))
            mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(TaintedIntArray.class), "box",
                    Type.getMethodDescriptor(TypeHelper.createArrayType(returnType.getDimensions(), targetType), returnType), false);
        mv.visitInsn(TypeHelper.returnFor(returnType));
    }
}
