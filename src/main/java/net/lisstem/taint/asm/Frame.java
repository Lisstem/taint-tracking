package net.lisstem.taint.asm;

import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.List;

import static org.objectweb.asm.tree.FieldInsnNode.*;
import static org.objectweb.asm.Opcodes.*;

public class Frame {

    public Frame previous = null;
    public Frame next = null;

    private AbstractInsnNode insn = null;
    private List<Type> locals = null;
    private List<Type> stack = null;

    private Frame(AbstractInsnNode insn) {
        this.insn = insn;
    }

    public Frame(List<Type> locals) {
        insn = null;
        stack = new ArrayList<>(0);
        this.locals = locals;
    }

    private void setLocalVariable(int index, Type type) {
        while (index >= locals.size())
            locals.add(null);
        locals.set(index, type);
    }

    private void insertLocalVariable(int index, Type type) {
        setLocalVariable(index, type);
        if (TypeHelper.isComputationalType2(type))
            setLocalVariable(index + 1, type);
    }

    private void insertLocalVariable(LocalVariableNode var) {
        insertLocalVariable(var.index, Type.getType(var.desc));
    }

    public AbstractInsnNode getInsn() {
        return insn;
    }

    public List<Type> getLocals() {
        return locals;
    }

    public List<Type> getStack() {
        return stack;
    }

    public Type getTop(int i) {
        return stack.size() < i ? null : stack.get(stack.size() - i);
    }

    public Type getTop() {
        return getTop(1);
    }

    public Frame apply(AbstractInsnNode insn) {
        Frame frame = new Frame(insn);
        next = frame;
        frame.previous = this;
        frame.locals = locals;
        switch (insn.getType()) {
            case FIELD_INSN -> apply((FieldInsnNode) insn, frame);
            case FRAME -> apply((FrameNode) insn, frame);
            case INSN -> apply((InsnNode) insn, frame);
            case INT_INSN -> apply((IntInsnNode) insn, frame);
            case INVOKE_DYNAMIC_INSN -> apply((InvokeDynamicInsnNode) insn, frame);
            case JUMP_INSN -> apply((JumpInsnNode) insn, frame);
            case LABEL -> apply((LabelNode) insn, frame);
            case LDC_INSN -> apply((LdcInsnNode) insn, frame);
            case LOOKUPSWITCH_INSN, TABLESWITCH_INSN -> pop(frame);
            case METHOD_INSN -> apply((MethodInsnNode) insn, frame);
            case MULTIANEWARRAY_INSN -> apply((MultiANewArrayInsnNode) insn, frame);
            case TYPE_INSN -> apply((TypeInsnNode) insn, frame);
            case VAR_INSN -> apply((VarInsnNode) insn, frame);
            case LINE -> frame.stack = stack;
            case IINC_INSN -> apply((IincInsnNode) insn, frame);
            default -> throw new IllegalStateException(""+ insn.getType());
        }
        if (frame.stack == null)
            throw new IllegalStateException(""+ insn.getType());
        return frame;
    }

    private void apply(IincInsnNode insn, Frame frame) {
        frame.stack = stack;
        if (!TypeHelper.isIntegerType(getLocal(insn.var)))
        {
            frame.locals = new ArrayList<>(locals);
            frame.setLocalVariable(insn.var, Type.INT_TYPE);
        }
    }

    public Type getLocal(int i) {
        if (i >= locals.size()) {
            return null;
        }
        return locals.get(i);
    }

    private void apply(FieldInsnNode insn, Frame frame) {
        Type fieldType = Type.getType(insn.desc);
        switch (insn.getOpcode()) {
            case GETSTATIC -> { frame.stack = new ArrayList<>(stack.size() + 1);
                frame.stack.addAll(stack);
                frame.stack.add(fieldType);
            }
            case PUTSTATIC -> frame.stack = stack.subList(0, stack.size() - 1);
            case GETFIELD -> {
                frame.stack = new ArrayList<>(stack);
                frame.stack.set(stack.size() - 1, fieldType);
            }
            case PUTFIELD -> frame.stack = stack.subList(0, stack.size() - 2);
        }
    }

    private void apply(FrameNode insn, Frame frame) {
        frame.stack = stack;
        // FrameNodes should not change the stack
    }

    private Type pushType(int opcode) {
        return switch (opcode) {
            case ACONST_NULL -> null;
            case ICONST_M1, ICONST_0, ICONST_1, ICONST_2, ICONST_3, ICONST_4, ICONST_5, IALOAD, BALOAD, CALOAD, SALOAD,
                    L2I, F2I, D2I, DCMPL, DCMPG, FCMPL, FCMPG, LCMP, ARRAYLENGTH, ILOAD -> Type.INT_TYPE;
            case LCONST_0, LCONST_1, LALOAD, I2L, F2L, D2L, LLOAD -> Type.LONG_TYPE;
            case FCONST_0, FCONST_1, FCONST_2, FALOAD, I2F, L2F, D2F, FLOAD -> Type.FLOAT_TYPE;
            case DCONST_0, DCONST_1, DALOAD, I2D, L2D, F2D, DLOAD -> Type.DOUBLE_TYPE;
            case AALOAD -> TypeHelper.reduceArrayType(getTop(2));
            default -> Type.VOID_TYPE;
        };
    }

    private void push(Frame frame, Type type) {
        frame.stack = new ArrayList<>(stack.size() + 1);
        frame.stack.addAll(stack);
        frame.stack.add(type);
    }

    private void replaceTop(Frame frame, Type type) {
        frame.stack = new ArrayList<>(stack);
        frame.stack.set(frame.stack.size() - 1, type);
    }

    private void pop(Frame frame, int count) {
        frame.stack = stack.subList(0, stack.size() - count);
    }

    private void pop(Frame frame) {
        pop(frame, 1);
    }

    private void dup(Frame frame, int depth) {
        frame.stack = new ArrayList<>(stack.size() + 1);
        frame.stack.addAll(stack);
        frame.stack.add(stack.size() - (depth + 1), getTop());
    }

    private void dupType2(int opcode, Frame frame) {
        switch (opcode) {
            case DUP2 -> dup(frame, 0);
            case DUP2_X1 -> {
                if (TypeHelper.isComputationalType2(getTop(2)))
                    dup(frame, 1);
                else
                    throw new IllegalStateException("Invalid computational type by DUP2_X1");
            }
            case DUP2_X2 -> dup(frame, TypeHelper.isComputationalType2(getTop(2)) ? 1 : 2);
        }
    }

    private void apply(InsnNode insn, Frame frame) {
        switch (insn.getOpcode()) {
            case ACONST_NULL, ICONST_M1, ICONST_0, ICONST_1, ICONST_2, ICONST_3, ICONST_4, ICONST_5, LCONST_0, LCONST_1,
                    FCONST_0, FCONST_1, FCONST_2, DCONST_0, DCONST_1 -> push(frame, pushType(insn.getOpcode()));
            case IALOAD, LALOAD, FALOAD, DALOAD, BALOAD, CALOAD, SALOAD, AALOAD, DCMPL, DCMPG, FCMPL, FCMPG, LCMP -> {
                frame.stack = new ArrayList<>(stack.subList(0, stack.size() - 1));
                Type type = pushType(insn.getOpcode());
                frame.stack.set(frame.stack.size() - 1, type);
                if (type == null)
                    throw new IllegalStateException("Cannot load from type: " + getTop(2).getDescriptor());
            }
            case IASTORE, LASTORE, FASTORE, DASTORE, AASTORE, BASTORE, CASTORE, SASTORE
                    -> frame.stack = stack.subList(0, stack.size() - 3);
            case POP, MONITORENTER, MONITOREXIT, IADD, LADD, FADD, DADD, ISUB, LSUB, FSUB, DSUB, IMUL, LMUL, FMUL, DMUL,
                    IDIV, LDIV, FDIV, DDIV, IREM, LREM, FREM, DREM, ISHL, LSHL, ISHR, LSHR, IUSHR, LUSHR, IAND, LAND,
                    IOR, LOR, IXOR, LXOR
                    -> pop(frame); // result has same Type as second from top
            case POP2 -> pop(frame, 2);
            case DUP, DUP_X1, DUP_X2 -> {
                if (TypeHelper.isComputationalType2(getTop()))
                    throw new IllegalStateException("DUP on type 2 computational type");
                dup(frame, switch (insn.getOpcode()) {
                    case DUP -> 0;
                    case DUP_X1 -> 1;
                    case DUP_X2 -> 2;
                    default -> -1;
                });
                }
            case DUP2, DUP2_X1, DUP2_X2  -> {
                if (TypeHelper.isComputationalType2(getTop())) {
                    dupType2(insn.getOpcode(), frame);
                } else {
                    frame.stack = new ArrayList<>(stack.size() + 2);
                    frame.stack.addAll(stack);
                    int offset = switch (insn.getOpcode()) {
                        case DUP2 -> 2;
                        case DUP2_X1 -> 3;
                        case DUP2_X2 -> TypeHelper.isComputationalType2(getTop(3)) ? 3 : 4;
                        default -> -2;
                    };
                    frame.stack.add(stack.size() - offset, getTop());
                    frame.stack.add(stack.size() - offset, getTop(2));
                }
            }
            case SWAP -> {
                frame.stack = new ArrayList<>(stack);
                frame.stack.set(frame.stack.size() - 1, getTop(2));
                frame.stack.set(frame.stack.size() - 2, getTop());
            }
            case I2L, I2F, I2D, L2I, L2F, L2D, F2I, F2L, F2D, D2I, D2L, D2F, ARRAYLENGTH
                    -> replaceTop(frame, pushType(insn.getOpcode()));
            case IRETURN, LRETURN, FRETURN, DRETURN, ARETURN, RETURN -> frame.stack = new ArrayList<>(0);
            case ATHROW -> frame.stack = new ArrayList<>(stack.subList(stack.size() - 1, stack.size()));
            case INEG, LNEG, FNEG, DNEG, I2B, I2C, I2S -> frame.stack = stack;
        }
    }
    private void apply(IntInsnNode insn, Frame frame) {
        switch (insn.getOpcode()) {
            case BIPUSH, SIPUSH -> push(frame, Type.INT_TYPE);
            case NEWARRAY -> replaceTop(frame, TypeHelper.createArrayType(1, switch (insn.operand) {
                default -> Type.VOID_TYPE;
                case T_BOOLEAN -> Type.BOOLEAN_TYPE;
                case T_CHAR -> Type.CHAR_TYPE;
                case T_FLOAT -> Type.FLOAT_TYPE;
                case T_DOUBLE -> Type.DOUBLE_TYPE;
                case T_BYTE -> Type.BYTE_TYPE;
                case T_SHORT -> Type.SHORT_TYPE;
                case T_INT -> Type.INT_TYPE;
                case T_LONG -> Type.LONG_TYPE;
            }));
        }
    }
    private void apply(InvokeDynamicInsnNode insn, Frame frame) {
        Type type = Type.getMethodType(insn.desc);
        int popCount = type.getArgumentTypes().length;
        boolean returnsValue = !type.getReturnType().equals(Type.VOID_TYPE);
        if (returnsValue)
            popCount--;
        frame.stack = new ArrayList<>(stack.size() - popCount);
        frame.stack.addAll(stack.subList(0, stack.size() - popCount));
        if (returnsValue)
            frame.stack.set(frame.stack.size() - 1, type.getReturnType());
    }

    private void apply(JumpInsnNode insn, Frame frame) {
        switch (insn.getOpcode()) {
            case IF_ICMPEQ, IF_ICMPNE, IF_ICMPLT, IF_ICMPGE, IF_ICMPGT, IF_ICMPLE, IF_ACMPEQ, IF_ACMPNE -> pop(frame, 2);
            case IFEQ, IFNE, IFLT, IFGE, IFGT, IFLE, IFNULL, IFNONNULL -> pop(frame);
            case GOTO -> frame.stack = stack; // Does not change the stack
            case JSR -> throw new IllegalStateException("JSR opcodes not supported use the JSRInlinerAdapter to remove them prior to using this adapter");
        }
    }
    private void apply(LabelNode insn, Frame frame) {
        frame.stack = stack;
        frame.locals = locals;
    }
    private void apply(LdcInsnNode insn, Frame frame) {
        if (insn.cst instanceof Type) {
            push(frame, (Type) insn.cst);
        } else if (insn.cst instanceof Integer) {
            push(frame, Type.INT_TYPE);
        } else if (insn.cst instanceof Float) {
            push(frame, Type.FLOAT_TYPE);
        } else if (insn.cst instanceof Long) {
            push(frame, Type.LONG_TYPE);
        } else if (insn.cst instanceof Double) {
            push(frame, Type.DOUBLE_TYPE);
        } else if (insn.cst instanceof String) {
            push(frame, Type.getType(String.class));
        } else if (insn.cst instanceof Handle) {
            frame.stack = stack;
            // TODO
        } else if (insn.cst instanceof ConstantDynamic) {
            frame.stack = stack;
            // TODO
        }
    }

    private void apply(MethodInsnNode insn, Frame frame) {
        Type type = Type.getMethodType(insn.desc);
        int popCount = type.getArgumentTypes().length;
        if (MethodHelper.invokesOnObject(insn))
            popCount++;
        boolean returnsValue = !type.getReturnType().equals(Type.VOID_TYPE);
        frame.stack = new ArrayList<>(stack.size() - popCount + (returnsValue ? 1 : 0));
        if (stack.size() >= popCount)
            frame.stack.addAll(stack.subList(0, stack.size() - popCount));
        if (stack.size() < popCount)
            throw new IllegalStateException("Methodinvocation requires more arguments then present");
        if (returnsValue)
            frame.stack.add(type.getReturnType());
    }

    private void apply(MultiANewArrayInsnNode insn, Frame frame) {
        frame.stack = new ArrayList<>(stack.size() - insn.dims + 1);
        frame.stack.addAll(stack.subList(0, stack.size() - insn.dims));
        frame.stack.add(Type.getType(insn.desc));
    }

    private void apply(TypeInsnNode insn, Frame frame) {
        switch (insn.getOpcode()) {
            case NEW -> push(frame, Type.getObjectType(insn.desc));
            case ANEWARRAY -> replaceTop(frame, TypeHelper.createArrayType(1, Type.getObjectType(insn.desc)));
            case INSTANCEOF -> replaceTop(frame, Type.INT_TYPE);
            case CHECKCAST -> replaceTop(frame, Type.getObjectType(insn.desc)); // might throw exception ...
        }
    }

    private void apply(VarInsnNode insn, Frame frame) {
        switch (insn.getOpcode()) {
            case ILOAD, LLOAD, FLOAD, DLOAD -> push(frame, pushType(insn.getOpcode()));
            case ALOAD -> push(frame, locals.get(insn.var));
            case ISTORE, LSTORE, FSTORE, DSTORE, ASTORE -> {
                if (!getTop().equals(getLocal(insn.var))) {
                    frame.locals = new ArrayList<>(locals);
                    frame.setLocalVariable(insn.var, getTop());
                    if (TypeHelper.isComputationalType2(getTop()))
                        frame.setLocalVariable(insn.var + 1, getTop());
                }
                pop(frame);
            }
            case RET -> frame.stack = stack; // Does not change stack and also is deprecated
        }
    }

    @Override
    public String toString() {
        return "Frame{" +
                "insn=" + (insn == null ? "null" : Instruction.toString(insn)) +
                ", locals=" + locals +
                ", stack=" + stack +
                '}';
    }
}
