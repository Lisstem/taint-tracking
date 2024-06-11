package net.lisstem.taint.asm.wrapint;

import net.lisstem.taint.asm.FrameMethodNode;
import net.lisstem.taint.asm.MethodHelper;
import net.lisstem.taint.asm.TypeHelper;
import net.lisstem.taint.asm.annotations.AnnotationHelper;
import net.lisstem.taint.asm.shadowint.Utils;
import net.lisstem.taint.asm.wrapint.handlers.WrapInsn;
import net.lisstem.taint.taint.Taintable;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.objectweb.asm.Opcodes.*;

public class TaintMethodAdapter extends FrameMethodNode {
    public final Type targetType;
    public final boolean returnsTarget;

    public boolean taintsResult = false;
    public boolean printTaints = false;

    public final boolean noPrinting;
    public final List<BoxingMethodWrapper> requiredFunctions;

    public TaintMethodAdapter(int access, String name, String desc, String signature, String[] exceptions,
                              Type targetType, String className,
                              List<BoxingMethodWrapper> requiredFunctions, boolean noPrinting) {
        super(access, name, desc, signature, exceptions, className);
        this.targetType = targetType;
        returnsTarget = Type.getReturnType(desc).equals(targetType);
        this.requiredFunctions = requiredFunctions;
        this.noPrinting = noPrinting;
    }

    private LocalVariableNode transform(LocalVariableNode variable) {
        Type type = Type.getType(variable.desc);
        Type transformed = TypeHelper.replaceIntVar(type, targetType);
        if (type.equals(transformed))
            return variable;

        return new LocalVariableNode(variable.name, transformed.getDescriptor(),
                variable.signature, variable.start, variable.end, variable.index);
    }

    public void unboxSecond(InsnList list) {
        list.add(new InsnNode(SWAP));
        list.add(unbox());
        list.add(new InsnNode(SWAP));
    }

    public AbstractInsnNode unboxSecond(AbstractInsnNode insn) {
        InsnList list = new InsnList();
        unboxSecond(list);
        return insertInstructionsBefore(insn, list);
    }

    public void unbox2(InsnList list) {
        unboxSecond(list);
        list.add(unbox());
    }

    public AbstractInsnNode unbox2(AbstractInsnNode insn) {
        InsnList list = new InsnList();
        unbox2(list);
        return insertInstructionsBefore(insn, list);
    }

    public AbstractInsnNode unbox() {
        return new MethodInsnNode(INVOKEVIRTUAL, targetType.getInternalName(), "getData", "()I");
    }

    public AbstractInsnNode unbox(AbstractInsnNode insn) {
        return insertInstructionBefore(insn, unbox());
    }

    public void box(InsnList list) {
        list.add(new TypeInsnNode(NEW, targetType.getInternalName()));
        list.add(new InsnNode(DUP_X1));
        list.add(new InsnNode(SWAP));
        list.add(new MethodInsnNode(INVOKESPECIAL, targetType.getInternalName(), MethodHelper.CONSTRUCTOR_NAME, "(I)V"));
    }

    public AbstractInsnNode box(AbstractInsnNode insn) {
        InsnList list = new InsnList();
        box(list);
        return insertInstructions(insn, list);
    }

    public Object replaceInts(Object obj) {
        if (obj instanceof Integer && obj.equals(INTEGER))
            return targetType.getDescriptor();
        if (obj instanceof String) {
            Type type = Type.getObjectType((String) obj);
            if (type.getSort() == Type.ARRAY && type.getElementType().equals(targetType))
                return TypeHelper.createArrayType(type.getDimensions(), targetType).getDescriptor();
        }
        return obj;
    }

    private void taintParameter() {
        if (visibleParameterAnnotations == null)
            return;
        InsnList list = new InsnList();
        Type[] args = Type.getMethodType(desc).getArgumentTypes();
        for (int i = 0; i < args.length; i++) {
            if (targetType.equals(args[i])) {
                AnnotationNode annotation = AnnotationHelper.getTaintAnnotation(visibleParameterAnnotations[i]);
                if (annotation != null) {
                    list.add(new VarInsnNode(ALOAD, i));
                    list.add(new MethodInsnNode(INVOKEVIRTUAL, targetType.getInternalName(), "copy", "()" + targetType.getDescriptor(), false));
                    list.add(new InsnNode(DUP));
                    list.add(new InsnNode(AnnotationHelper.getTaint(annotation) ? ICONST_1 : ICONST_0));
                    list.add(new MethodInsnNode(INVOKEVIRTUAL, targetType.getInternalName(), "setTaint", "(Z)V", false));
                    list.add(new VarInsnNode(ASTORE, i));
                }
            }
        }
        instructions.insert(list);
    }

    public AbstractInsnNode taintResult(AbstractInsnNode insn) {
        InsnList list = new InsnList();
        list.add(new MethodInsnNode(INVOKEVIRTUAL, targetType.getInternalName(), "copy", "()" + targetType.getDescriptor(), false));
        list.add(new InsnNode(DUP));
        list.add(new InsnNode(AnnotationHelper.getTaint(AnnotationHelper.getTaintAnnotation(visibleAnnotations)) ? ICONST_1 : ICONST_0));
        list.add(new MethodInsnNode(INVOKEVIRTUAL, targetType.getInternalName(), "setTaint", "(Z)V", false));

        return insertInstructionsBefore(insn, list);
    }

    private void printParameterTaints() {
        InsnList list = new InsnList();
        AbstractInsnNode println = new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(java.io.PrintStream.class), "println", "(Ljava/lang/String;)V", false);
        AbstractInsnNode concat = new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(String.class), "concat", "(Ljava/lang/String;)Ljava/lang/String;", false);
        AbstractInsnNode toString = new MethodInsnNode(INVOKEVIRTUAL, targetType.getInternalName(), "toString", "()Ljava/lang/String;", false);
        AbstractInsnNode arrayToString = new MethodInsnNode(INVOKESTATIC, Type.getInternalName(java.util.Arrays.class), "deepToString", "([Ljava/lang/Object;)Ljava/lang/String;", false);
        list.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(java.lang.System.class), "out", Type.getDescriptor(java.io.PrintStream.class)));
        list.add(new InsnNode(DUP));
        list.add(new LdcInsnNode("Taints in " + className + "." + name + " " + desc));
        list.add(println.clone(null));
        list.add(new InsnNode(DUP));
        Type[] args = Type.getMethodType(desc).getArgumentTypes();
        for (int i = 0; i < args.length; i++) {
            if (TypeHelper.isTypeOrArrayOf(args[i], targetType)) {
                list.add(new LdcInsnNode("taint of arg " + i + ": "));
                list.add(new VarInsnNode(ALOAD, i));
                if (TypeHelper.isArrayOf(args[i], targetType)) {
                    list.add(arrayToString.clone(null));
                } else {
                    list.add(toString.clone(null));
                }
                list.add(concat.clone(null));
                list.add(println.clone(null));
                list.add(new InsnNode(DUP));
            }
        }
        list.remove(list.getLast());
        instructions.insert(list);
    }

    public AbstractInsnNode printTaint(AbstractInsnNode insn, String message, boolean isArray) {
        InsnList list = new InsnList();
        list.add(new InsnNode(DUP));
        list.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(java.lang.System.class), "out", Type.getDescriptor(java.io.PrintStream.class)));
        list.add(new InsnNode(SWAP));
        if (message != null) {
            list.add(new LdcInsnNode(message));
            list.add(new InsnNode(SWAP));
        }
        if (isArray) {
            list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(java.util.Arrays.class), "deepToString", "([Ljava/lang/Object;)Ljava/lang/String;", false));
        } else {
            list.add(new MethodInsnNode(INVOKEVIRTUAL, targetType.getInternalName(), "toString", "()Ljava/lang/String;", false));
        }
        if (message != null) {
            list.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(String.class), "concat", "(Ljava/lang/String;)Ljava/lang/String;", false));
        }
        list.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(java.io.PrintStream.class), "println", "(Ljava/lang/String;)V", false));
        return insertInstructionsBefore(insn, list);
    }

    @Override
    public void visitEnd() {
        localVariables = localVariables.stream().map(this::transform).collect(Collectors.toList());
        taintsResult = AnnotationHelper.getTaint(AnnotationHelper.getTaintAnnotation(visibleAnnotations));
        printTaints = !noPrinting && AnnotationHelper.printTaints(visibleAnnotations);
        visitEnd(new WrapInsn(), this);
    }

    @Override
    protected void afterInstructions() {
        taintParameter();
        if (printTaints) {
            printParameterTaints();
        }
    }
}
