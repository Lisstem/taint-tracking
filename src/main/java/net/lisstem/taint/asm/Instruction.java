package net.lisstem.taint.asm;

import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.tree.AbstractInsnNode.*;
public class Instruction {
    
    static public String toString(AbstractInsnNode insn) {
        return switch (insn.getType()) {
            case INSN -> toString((InsnNode)insn);
            case INT_INSN -> toString((IntInsnNode)insn);
            case VAR_INSN -> toString((VarInsnNode)insn);
            case TYPE_INSN -> toString((TypeInsnNode)insn);
            case FIELD_INSN -> toString((FieldInsnNode)insn);
            case METHOD_INSN -> toString((MethodInsnNode)insn);
            case INVOKE_DYNAMIC_INSN -> toString((InvokeDynamicInsnNode)insn);
            case JUMP_INSN -> toString((JumpInsnNode)insn);
            case LABEL -> toString((LabelNode)insn);
            case LDC_INSN -> toString((LdcInsnNode)insn);
            case IINC_INSN -> toString((IincInsnNode)insn);
            case TABLESWITCH_INSN -> toString((TableSwitchInsnNode)insn);
            case LOOKUPSWITCH_INSN -> toString((LookupSwitchInsnNode)insn);
            case MULTIANEWARRAY_INSN -> toString((MultiANewArrayInsnNode)insn);
            case FRAME -> toString((FrameNode)insn);
            case LINE -> toString((LineNumberNode)insn);
            default -> "Invalid instruction: " + insn;
        };
    }
    static public String toString(InsnNode insn) {
        return opcodeName(insn.getOpcode());
    }

    static public String toString(IntInsnNode insn) {
        return opcodeName(insn.getOpcode()) + "{" + insn.operand + "}";
    }

    static public String toString(VarInsnNode insn) {
        return opcodeName(insn.getOpcode()) + "{" + insn.var + "}";
    }

    static public String toString(TypeInsnNode insn) {
        return opcodeName(insn.getOpcode()) + "{" + insn.desc + "}";
    }

    static public String toString(FieldInsnNode insn) {
        return opcodeName(insn.getOpcode()) + "{" + insn.owner + "." + insn.name +
                " " + insn.desc + "}";
    }
    static public String toString(MethodInsnNode insn) {
        return opcodeName(insn.getOpcode()) + "{" + insn.owner +
                "." + insn.name + insn.desc + (insn.itf ? " (itf)}" : "}");
    }
    static public String toString(InvokeDynamicInsnNode insn) {
        return opcodeName(insn.getOpcode()) + "{" + insn.name + insn.desc + "}";
    }
    static public String toString(JumpInsnNode insn) {
        return opcodeName(insn.getOpcode()) + "{" + insn.label.getLabel() + "}";
    }

    static public String toString(LabelNode insn) {
        return "Label{" + insn.getLabel() + "}";
    }

    static public String toString(LdcInsnNode insn) {
        return opcodeName(insn.getOpcode()) + "{" + insn.cst + "}";
    }
    static public String toString(IincInsnNode insn) {
        return opcodeName(insn.getOpcode()) + "{" + insn.var +
                ", incr=" + insn.incr + "}";
    }

    static public String toString(TableSwitchInsnNode insn) {
        return opcodeName(insn.getOpcode());
    }

    static public String toString(LookupSwitchInsnNode insn) {
        return opcodeName(insn.getOpcode());
    }

    static public String toString(MultiANewArrayInsnNode insn) {
        return opcodeName(insn.getOpcode()) + "{" + insn.desc +
                " (" + insn.dims + ")}";
    }

    static public String toString(FrameNode insn) {
        String type = switch (insn.type) {
            case F_NEW -> "new";
            case F_APPEND -> "append";
            case F_CHOP -> "chop";
            case F_FULL -> "full";
            case F_SAME -> "same";
            case F_SAME1 -> "same1";
            default -> "invalid";
        };

        return "Frame{" +
                "type=" + type +
                ", local=" + insn.local.toString() +
                ", stack=" + insn.stack.toString() +
                '}';
    }

    static public String toString(LineNumberNode insn) {
        return "Linenumber{" + insn.line + "}";
    }

    static public InsnNode loadConstInt(int value) {
        return new InsnNode(switch (value) {
            case -1 -> ICONST_M1;
            case 0 -> ICONST_0;
            case 1 -> ICONST_1;
            case 2 -> ICONST_2;
            case 3 -> ICONST_3;
            case 4 -> ICONST_4;
            case 5 -> ICONST_5;
            default -> throw new IllegalStateException("No opcode to load " + value + " as const");
        });
    }
    static public AbstractInsnNode loadInt(int value) {
        if (-1 <= value && value <= 5)
            return loadConstInt(value);
        if (Byte.MIN_VALUE <= value && value <= Byte.MAX_VALUE)
            return new IntInsnNode(BIPUSH, value);
        if (Short.MIN_VALUE <= value && value <= Short.MAX_VALUE)
            return new IntInsnNode(SIPUSH, value);

        return new LdcInsnNode(value);
    }
    
    static public String opcodeName(int opcode) {
        return switch (opcode) {
            case ICONST_M1 -> "ICONST_M1";
            case ICONST_0 -> "ICONST_0";
            case ICONST_1 -> "ICONST_1";
            case ICONST_2 -> "ICONST_2";
            case ICONST_3 -> "ICONST_3";
            case ICONST_4 -> "ICONST_4";
            case ICONST_5 -> "ICONST_5";
            case LCONST_0 -> "LCONST_0";
            case LCONST_1 -> "LCONST_1";
            case FCONST_0 -> "FCONST_0";
            case FCONST_1 -> "FCONST_1";
            case FCONST_2 -> "FCONST_2";
            case DCONST_0 -> "DCONST_0";
            case DCONST_1 -> "DCONST_1";
            case BIPUSH -> "BIPUSH";
            case SIPUSH -> "SIPUSH";
            case LDC -> "LDC";
            case ILOAD -> "ILOAD";
            case LLOAD -> "LLOAD";
            case FLOAD -> "FLOAD";
            case DLOAD -> "DLOAD";
            case ALOAD -> "ALOAD";
            case IALOAD -> "IALOAD";
            case LALOAD -> "LALOAD";
            case FALOAD -> "FALOAD";
            case DALOAD -> "DALOAD";
            case AALOAD -> "AALOAD";
            case BALOAD -> "BALOAD";
            case CALOAD -> "CALOAD";
            case SALOAD -> "SALOAD";
            case ISTORE -> "ISTORE";
            case LSTORE -> "LSTORE";
            case FSTORE -> "FSTORE";
            case DSTORE -> "DSTORE";
            case ASTORE -> "ASTORE";
            case IASTORE -> "IASTORE";
            case LASTORE -> "LASTORE";
            case FASTORE -> "FASTORE";
            case DASTORE -> "DASTORE";
            case AASTORE -> "AASTORE";
            case BASTORE -> "BASTORE";
            case CASTORE -> "CASTORE";
            case SASTORE -> "SASTORE";
            case POP -> "POP";
            case POP2 -> "POP2";
            case DUP -> "DUP";
            case DUP_X1 -> "DUP_X1";
            case DUP_X2 -> "DUP_X2";
            case DUP2 -> "DUP2";
            case DUP2_X1 -> "DUP2_X1";
            case DUP2_X2 -> "DUP2_X2";
            case SWAP -> "SWAP";
            case IADD -> "IADD";
            case LADD -> "LADD";
            case FADD -> "FADD";
            case DADD -> "DADD";
            case ISUB -> "ISUB";
            case LSUB -> "LSUB";
            case FSUB -> "FSUB";
            case DSUB -> "DSUB";
            case IMUL -> "IMUL";
            case LMUL -> "LMUL";
            case FMUL -> "FMUL";
            case DMUL -> "DMUL";
            case IDIV -> "IDIV";
            case LDIV -> "LDIV";
            case FDIV -> "FDIV";
            case DDIV -> "DDIV";
            case IREM -> "IREM";
            case LREM -> "LREM";
            case FREM -> "FREM";
            case DREM -> "DREM";
            case INEG -> "INEG";
            case LNEG -> "LNEG";
            case FNEG -> "FNEG";
            case DNEG -> "DNEG";
            case ISHL -> "ISHL";
            case LSHL -> "LSHL";
            case ISHR -> "ISHR";
            case LSHR -> "LSHR";
            case IUSHR -> "IUSHR";
            case LUSHR -> "LUSHR";
            case IAND -> "IAND";
            case LAND -> "LAND";
            case IOR -> "IOR";
            case LOR -> "LOR";
            case IXOR -> "IXOR";
            case LXOR -> "LXOR";
            case IINC -> "IINC";
            case I2L -> "I2L";
            case I2F -> "I2F";
            case I2D -> "I2D";
            case L2I -> "L2I";
            case L2F -> "L2F";
            case L2D -> "L2D";
            case F2I -> "F2I";
            case F2L -> "F2L";
            case F2D -> "F2D";
            case D2I -> "D2I";
            case D2L -> "D2L";
            case D2F -> "D2F";
            case I2B -> "I2B";
            case I2C -> "I2C";
            case I2S -> "I2S";
            case LCMP -> "LCMP";
            case FCMPL -> "FCMPL";
            case FCMPG -> "FCMPG";
            case DCMPL -> "DCMPL";
            case DCMPG -> "DCMPG";
            case IFEQ -> "IFEQ";
            case IFNE -> "IFNE";
            case IFLT -> "IFLT";
            case IFGE -> "IFGE";
            case IFGT -> "IFGT";
            case IFLE -> "IFLE";
            case IF_ICMPEQ -> "IF_ICMPEQ";
            case IF_ICMPNE -> "IF_ICMPNE";
            case IF_ICMPLT -> "IF_ICMPLT";
            case IF_ICMPGE -> "IF_ICMPGE";
            case IF_ICMPGT -> "IF_ICMPGT";
            case IF_ICMPLE -> "IF_ICMPLE";
            case IF_ACMPEQ -> "IF_ACMPEQ";
            case IF_ACMPNE -> "IF_ACMPNE";
            case GOTO -> "GOTO";
            case JSR -> "JSR";
            case RET -> "RET";
            case TABLESWITCH -> "TABLESWITCH";
            case LOOKUPSWITCH -> "LOOKUPSWITCH";
            case IRETURN -> "IRETURN";
            case LRETURN -> "LRETURN";
            case FRETURN -> "FRETURN";
            case DRETURN -> "DRETURN";
            case ARETURN -> "ARETURN";
            case RETURN -> "RETURN";
            case GETSTATIC -> "GETSTATIC";
            case PUTSTATIC -> "PUTSTATIC";
            case GETFIELD -> "GETFIELD";
            case PUTFIELD -> "PUTFIELD";
            case INVOKEVIRTUAL -> "INVOKEVIRTUAL";
            case INVOKESPECIAL -> "INVOKESPECIAL";
            case INVOKESTATIC -> "INVOKESTATIC";
            case INVOKEINTERFACE -> "INVOKEINTERFACE";
            case INVOKEDYNAMIC -> "INVOKEDYNAMIC";
            case NEW -> "NEW";
            case NEWARRAY -> "NEWARRAY";
            case ANEWARRAY -> "ANEWARRAY";
            case ARRAYLENGTH -> "ARRAYLENGTH";
            case ATHROW -> "ATHROW";
            case CHECKCAST -> "CHECKCAST";
            case INSTANCEOF -> "INSTANCEOF";
            case MONITORENTER -> "MONITORENTER";
            case MONITOREXIT -> "MONITOREXIT";
            case MULTIANEWARRAY -> "MULTIANEWARRAY";
            case IFNULL -> "IFNULL";
            case IFNONNULL -> "IFNONNULL";
            default -> "invalid";
        };
    }
}
