package net.lisstem.taint.asm.shadowint;

import net.lisstem.taint.asm.FrameMethodNode;
import net.lisstem.taint.asm.InsnHelper;
import net.lisstem.taint.asm.TypeHelper;
import net.lisstem.taint.asm.annotations.AnnotationHelper;
import net.lisstem.taint.asm.shadowint.handlers.ShadowInsn;
import net.lisstem.taint.taint.TaintBox;
import net.lisstem.taint.taint.Taintable;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.*;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.tree.AbstractInsnNode.*;
import static net.lisstem.taint.asm.MethodHelper.*;

public class ShadowMethodAdapter extends FrameMethodNode {
    public final Class<? extends Taintable> taintClass;
    public final boolean returnsBox;

    public boolean taintsResult = false;
    public boolean printTaints = false;

    public final boolean noPrinting;
    public final List<ShadowMethodWrapper> requiredFunctions;

    public static final int TEMP_COUNT = 2;
    public LocalVariableNode[] tempInt = new LocalVariableNode[TEMP_COUNT];
    public LocalVariableNode[] tempTaint  = new LocalVariableNode[TEMP_COUNT];

    public final Map<Integer, Integer> variableIndexMap = new HashMap<>();

    public ShadowMethodAdapter(int access, String name, String descriptor, String signature, String[] exceptions,
                               Class<? extends Taintable> taintClass, String className,
                               List<ShadowMethodWrapper> requiredFunctions, boolean noPrinting) {
        super(access, name, descriptor, signature, exceptions, className);
        this.taintClass = taintClass;
        this.noPrinting = noPrinting;
        returnsBox = Type.getReturnType(descriptor).equals(Type.getType(TaintBox.class));
        this.requiredFunctions = requiredFunctions;
    }

    public AbstractInsnNode storeInt(int i) {
        return new VarInsnNode(ISTORE, tempInt[i].index);
    }

    public AbstractInsnNode storeTaint(int i) {
        return new VarInsnNode(ASTORE, tempTaint[i].index);
    }


    public AbstractInsnNode loadInt(int i) {
        return new VarInsnNode(ILOAD, tempInt[i].index);
    }

    public AbstractInsnNode loadTaint(int i) {
        return new VarInsnNode(ALOAD, tempTaint[i].index);
    }

    public AbstractInsnNode storeInt() {
        return storeInt(0);
    }

    public AbstractInsnNode storeTaint() {
        return storeTaint(0);
    }


    public AbstractInsnNode loadInt() {
        return loadInt(0);
    }

    public AbstractInsnNode loadTaint() {
        return loadTaint(0);
    }

    public void storeIntAndTaint(InsnList list, int i) {
        list.add(storeTaint(i));
        list.add(storeInt(i));
    }

    public void loadIntAndTaint(InsnList list, int i) {
        list.add(loadInt(i));
        list.add(loadTaint(i));
    }

    public void storeIntAndTaint(InsnList list) {
        storeIntAndTaint(list, 0);
    }

    public void loadIntAndTaint(InsnList list) {
        loadIntAndTaint(list, 0);
    }

    public void swapTaints(InsnList list) {
         swap2(list);
    }

    public void swapWithTaint(InsnList list) {
        list.add(new InsnNode(DUP_X2));
        list.add(new InsnNode(POP));
    }

    public void swapTaintWithOther(InsnList list) {
        list.add(new InsnNode(DUP2_X1));
        list.add(new InsnNode(POP2));
    }

    public AbstractInsnNode popTaint() {
        return new InsnNode(POP);
    }

    public AbstractInsnNode popTaint(AbstractInsnNode insn) {
        return insertInstructionBefore(insn, popTaint());
    }

    public AbstractInsnNode pop2Taints(AbstractInsnNode insn) {
        insn = popTaint(insn);
        insn = insertInstructionBefore(insn, new InsnNode(SWAP));
        return popTaint(insn);
    }

    public AbstractInsnNode popSecondTaint(AbstractInsnNode insn) {
        return popTaint(insertInstructionBefore(insn, new InsnNode(SWAP)));
    }

    public AbstractInsnNode popSecondTaintW(AbstractInsnNode insn) {
        insn = insertInstruction(insn, new InsnNode(DUP2_X1));
        insn = insertInstruction(insn, new InsnNode(POP2));
        return popTaint(insn);
    }

    public void beforeCombineTaint(InsnList list) {
        list.add(new InsnNode(SWAP));
        list.add(new InsnNode(DUP_X2));
        list.add(new InsnNode(POP));
        list.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(taintClass), "combine",
                Type.getMethodDescriptor(Type.getType(Taintable.class), Type.getType(Taintable.class)), false));
        list.add(new TypeInsnNode(CHECKCAST, Type.getInternalName(taintClass)));
        list.add(new InsnNode(DUP_X2));
        list.add(new InsnNode(POP));
    }

    public AbstractInsnNode setTaint(AbstractInsnNode insn, boolean value) {
        String internalName = Type.getInternalName(taintClass);
        InsnList list = new InsnList();
        list.add(new MethodInsnNode(INVOKEVIRTUAL, internalName, "copy", "()" + Type.getDescriptor(Taintable.class), false));
        list.add(new TypeInsnNode(CHECKCAST, internalName));
        list.add(new InsnNode(DUP));
        list.add(new InsnNode(value ? ICONST_1 : ICONST_0));
        list.add(new MethodInsnNode(INVOKEVIRTUAL, internalName, "setTaint", "(Z)V", false));
        return insertInstructionsBefore(insn, list);
    }

    public AbstractInsnNode combineTaint(AbstractInsnNode insn) {
        InsnList list = new InsnList();
        beforeCombineTaint(list);
        insertInstructionsBefore(insn, list);
        return insertInstruction(insn, new InsnNode(SWAP));
    }

    public AbstractInsnNode addTaint(AbstractInsnNode insn) {
        InsnList list = new InsnList();
        addTaint(list);
        return insertInstructions(insn, list);
    }

    public void addTaint(InsnList list) {
        list.add(new TypeInsnNode(NEW, Type.getInternalName(taintClass)));
        list.add(new InsnNode(DUP));
        list.add(new MethodInsnNode(INVOKESPECIAL, Type.getInternalName(taintClass), CONSTRUCTOR_NAME, EMPTY_DESC, false));
    }

    public void createTaintArray(Type array, InsnList list) {
        if (array.getDimensions() > TaintArray.MAX_DIM)
            throw new IllegalArgumentException("Taint creation for arrays with " + array.getDimensions() + " dimensions is not support.\nMaximal dimensions are " + TaintArray.MAX_DIM + ".");
        Type taint = Type.getType(Taintable.class);
        Type taintArray = TypeHelper.createArrayType(array.getDimensions(), taint);
        list.add(new InsnNode(DUP));
        addTaint(list);
        list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(TaintArray.class), "createTaintArray",
                Type.getMethodDescriptor(taintArray, array, taint), false));
        list.add(new TypeInsnNode(CHECKCAST, TypeHelper.createArrayType(array.getDimensions(), Type.getType(taintClass)).getDescriptor()));
    }

    public AbstractInsnNode createTaintArray(AbstractInsnNode insn, Type array) {
        InsnList list = new InsnList();
        createTaintArray(array, list);
        return insertInstructions(insn, list);
    }


    private void mapParamIndexes() {
        Type method = Type.getMethodType(desc);
        List<Type> args = new ArrayList<>(Arrays.asList(method.getArgumentTypes()));
        if ((access & ACC_STATIC) == 0)
            args.add(0, Type.getObjectType(className));
        int param = 0;
        int type2s = 0;
        for (int i = 0; i < args.size(); i++) {
            Type arg = args.get(i);
            if (isTaint(arg)) {
                variableIndexMap.put(i + type2s, i + 1 + type2s);
            } else {
                variableIndexMap.put(param + type2s, i + type2s);
                if (TypeHelper.isComputationalType2(arg)) {
                    variableIndexMap.put(param + type2s + 1, i + type2s + 1);
                    type2s++;
                }
                param++;
            }
        }
    }


    public AbstractInsnNode taintLoad(VarInsnNode insn) {
        if (variableIndexMap.containsValue(insn.var + 1))
            variableIndexMap.replaceAll((key, value) -> value > insn.var ? value + 1 : value);
        return new VarInsnNode(ALOAD, insn.var + 1);
    }

    public AbstractInsnNode taintStore(VarInsnNode insn) {
        if (variableIndexMap.containsValue(insn.var + 1))
            variableIndexMap.replaceAll((key, value) -> value > insn.var ? value + 1 : value);
        return new VarInsnNode(ASTORE, insn.var + 1);
    }

    private void shadowLocalVariables() {
        int count = localVariables.size();
        for (int i = 0; i < count; i++) {
            LocalVariableNode var = localVariables.get(i);
            if (var.name.startsWith("$temp"))
                continue;
            var.index = variableIndexMap.get(var.index);
            if (TypeHelper.isIntegerOrIntArray(var)) {
                localVariables.add( new LocalVariableNode(Utils.shadowVariableName(var.name),
                        Utils.shadowType(var, Type.getType(taintClass)).getDescriptor(),
                        null, var.start, var.end, var.index + 1));
            }
        }
    }

    public List<Type> getTopElements(int i) {
        List<Type> top = new ArrayList<>(i > 0 ? i*2 : 0);
        for (int count = 0, j = 1; count < i; j++) {
            Type type = getTop(j);
            if (!isTaint(type))
                count++;
            top.add(0, type);
        }
        return top;
    }

    public int stackConsumption(@NotNull Type methodType, boolean invokesOnInstance) {
        int count = 0;
        for (Type type: methodType.getArgumentTypes()) {
            if (!isTaint(type)) count++;
        }
        return count + (invokesOnInstance ? 1 : 0);
    }


    public boolean fitsStackToMethod(@NotNull Type methodType, Type instanceType) {
        List<Type> args = new ArrayList<>(Arrays.asList(methodType.getArgumentTypes()));
        if (instanceType != null)
            args.add(0, instanceType);
        List<Type> stack = getTopElements(stackConsumption(methodType, instanceType != null));
        if (args.size() != stack.size())
            return false;
        for (int i = 0; i < args.size(); i++) {
            if (isTaint(args.get(i)) != isTaint(stack.get(i)))
                return false;
        }
        return true;
    }

    public Type getTop(int i) {
        return stack.getCurrent().getTop(i);
    }

    public Type getTop() {
        return getTop(1);
    }

    public boolean isTaint(Type type) {
        return TypeHelper.isTypeOrArrayOf(type, Type.getType(taintClass));
    }

    public boolean isTaintOnTop(int i) {
        return isTaint(getTop(i));
    }

    public boolean isTaintOnTop() {
        return isTaintOnTop(1);
    }

    public void map(IincInsnNode insn) {
        int newIndex;
        if (variableIndexMap.containsKey(insn.var))
            newIndex = variableIndexMap.get(insn.var);
        else {
            newIndex = maxIndex() + 1;
            variableIndexMap.put(insn.var, newIndex);
        }
        checkTaintMap(insn.var, newIndex);
        insn.var = newIndex;
    }

    private void checkTaintMap(int index, int newIndex) {
        if (!variableIndexMap.containsKey(index + 1))
            variableIndexMap.put(index + 1, newIndex + 2);
        else if (variableIndexMap.containsValue(newIndex + 1))
            variableIndexMap.replaceAll((key, value) -> value > newIndex ? value + 1 : value);
    }

    public void map(VarInsnNode insn) {
        int newIndex;
        if (variableIndexMap.containsKey(insn.var)) {
            newIndex = variableIndexMap.get(insn.var);
        } else {
            newIndex = maxIndex() + 1;
            variableIndexMap.put(insn.var, newIndex);
            if (TypeHelper.isComputationalType2(InsnHelper.operatesOn(insn))) {
                variableIndexMap.put(insn.var + 1, newIndex + 1);
            }
        }
        if (InsnHelper.operatesOn(insn) == Type.INT ||
                (InsnHelper.doesStore(insn) && isTaintOnTop()))
            checkTaintMap(insn.var, newIndex);
        insn.var = newIndex;
    }

    private int maxIndex() {
        return variableIndexMap.values().stream().max(Integer::compareTo).orElse(0);
    }

    private LabelNode firstLabel() {
        AbstractInsnNode insn = instructions.getFirst();
        while (insn != null && insn.getType() != LABEL)
            insn = insn.getNext();

        if (insn == null) {
            insn = new LabelNode();
            instructions.insert(insn);
        }

        return (LabelNode) insn ;
    }

    private LabelNode lastLabel() {
        AbstractInsnNode insn = instructions.getLast();
        while (insn != null && insn.getType() != LABEL)
            insn = insn.getPrevious();

        if (insn == null) {
            insn = new LabelNode();
            instructions.add(insn);
        }

        return (LabelNode) insn;
    }


    private int argSize() {
        int size = 0;
        if ((access & ACC_STATIC) == 0)
            size++;

        for (Type type: Type.getMethodType(desc).getArgumentTypes()) {
            if (TypeHelper.isComputationalType2(type)) {
                size += 2;
            } else {
                size++;
            }
        }

        return size;
    }

    private int taintedArgs() {
        return (int) Arrays.stream(Type.getMethodType(desc).getArgumentTypes()).filter(this::isTaint).count();
    }


    private void createTempTaintVars() {
        LabelNode first = firstLabel();
        LabelNode last = lastLabel();
        int index = argSize();
        int taints = taintedArgs();
        for (int i = 0; i < TEMP_COUNT; i++) {
            variableIndexMap.put(index - taints + 2*i, index + 2 * TEMP_COUNT + 2*i);
            variableIndexMap.put(index - taints + 2*i + 1, index + 2 * TEMP_COUNT + 2*i + 1);
            tempInt[i] = new LocalVariableNode("$tempInt" + i, "I", null, first, last, index + 2*i);
            tempTaint[i] = new LocalVariableNode("$tempTaint" + i, Type.getDescriptor(taintClass), null, first, last, index + 2*i + 1);
            localVariables.add(tempInt[i]);
            localVariables.add(tempTaint[i]);
        }
    }

    private void taintParameters() {
        if (visibleParameterAnnotations == null)
            return;

        Type[] args = Type.getMethodType(desc).getArgumentTypes();
        InsnList list = new InsnList();

        for (int i = 0; i < visibleParameterAnnotations.length; i++) {
            List<AnnotationNode> annotations = visibleParameterAnnotations[i];
            if (annotations == null)
                continue;
            int index = getUntaintedAt(args, i);
            if (index < 0 || !Type.INT_TYPE.equals(args[index]))
                continue;
            AnnotationNode taint = AnnotationHelper.getTaintAnnotation(annotations);
            if (taint != null) {
                String internalName = Type.getInternalName(taintClass);
                list.add(new VarInsnNode(ALOAD, index + 1));
                list.add(new MethodInsnNode(INVOKEVIRTUAL, internalName, "copy", "()" + Type.getDescriptor(Taintable.class), false));
                list.add(new TypeInsnNode(CHECKCAST, internalName));
                list.add(new InsnNode(DUP));
                list.add(new InsnNode(AnnotationHelper.getTaint(taint) ? ICONST_1 : ICONST_0));
                list.add(new MethodInsnNode(INVOKEVIRTUAL, internalName, "setTaint", "(Z)V", false));
                list.add(new VarInsnNode(ASTORE, index + 1));
            }
        }
        instructions.insert(list);
    }

    private int getUntaintedAt(Type[] types, int index) {
        int current = -1;
        for (int i = 0; i < types.length; i++) {
            if (Utils.isShadowType(types[i], Type.getType(taintClass)))
                continue;
            current++;
            if (current == index)
                return i;
        }
        return -1;
    }

    public AbstractInsnNode printTaint(AbstractInsnNode insn, String message) {
        InsnList list = new InsnList();
        list.add(new InsnNode(DUP));
        list.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(java.lang.System.class), "out", Type.getDescriptor(java.io.PrintStream.class)));
        list.add(new InsnNode(SWAP));
        if (message != null) {
            list.add(new LdcInsnNode(message));
            list.add(new InsnNode(SWAP));
        }
        if (TypeHelper.isArrayOf(getTop(), Type.getType(taintClass))) {
            list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(java.util.Arrays.class), "deepToString", "([Ljava/lang/Object;)Ljava/lang/String;", false));
        } else {
            list.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(taintClass), "toString", "()Ljava/lang/String;", false));
        }
        if (message != null) {
            list.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(String.class), "concat", "(Ljava/lang/String;)Ljava/lang/String;", false));
        }
        list.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(java.io.PrintStream.class), "println", "(Ljava/lang/String;)V", false));
        return insertInstructionsBefore(insn, list);
    }

    private void printParameterTaints() {
        InsnList list = new InsnList();
        AbstractInsnNode println = new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(java.io.PrintStream.class), "println", "(Ljava/lang/String;)V", false);
        AbstractInsnNode concat = new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(String.class), "concat", "(Ljava/lang/String;)Ljava/lang/String;", false);
        AbstractInsnNode toString = new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(taintClass), "toString", "()Ljava/lang/String;", false);
        AbstractInsnNode arrayToString = new MethodInsnNode(INVOKESTATIC, Type.getInternalName(java.util.Arrays.class), "deepToString", "([Ljava/lang/Object;)Ljava/lang/String;", false);
        list.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(java.lang.System.class), "out", Type.getDescriptor(java.io.PrintStream.class)));
        list.add(new InsnNode(DUP));
        list.add(new LdcInsnNode("Taints in " + className + "." + name + " " + desc));
        list.add(println.clone(null));
        list.add(new InsnNode(DUP));
        Type[] args = Type.getMethodType(desc).getArgumentTypes();
        Type shadowed = Type.getType(taintClass);
        for (int i = 0; i < args.length; i++) {
            if (Utils.isShadowType(args[i], shadowed)) {
                list.add(new LdcInsnNode("taint of arg " + i + ": "));
                list.add(new VarInsnNode(ALOAD, i));
                if (TypeHelper.isArrayOf(args[i], shadowed)) {
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

    @Override
    public void visitEnd() {
        taintsResult = AnnotationHelper.getTaint(AnnotationHelper.getTaintAnnotation(visibleAnnotations));
        printTaints = !noPrinting && AnnotationHelper.printTaints(visibleAnnotations);
        mapParamIndexes();
        if (instructions.size() > 0) {
            createTempTaintVars();
        }
        visitEnd(new ShadowInsn(), this);
        /*
        System.out.println("METHOD: " + className + ":"+ name);
        System.out.println(stack);
        */
    }

    @Override
    protected void afterInstructions() {
        shadowLocalVariables();
        // printParameterTaints and taintParameters both insert instructions at the beginning of the function
        // thus we need to call printParameterTaints first to first taint and then print
        if (printTaints)
            printParameterTaints();
        taintParameters();
    }
}
