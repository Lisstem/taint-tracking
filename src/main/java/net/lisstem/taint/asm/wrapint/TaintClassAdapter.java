package net.lisstem.taint.asm.wrapint;

import net.lisstem.taint.asm.AccessHelper;
import net.lisstem.taint.asm.InsnHelper;
import net.lisstem.taint.asm.MethodHelper;
import net.lisstem.taint.asm.TypeHelper;
import net.lisstem.taint.taint.Taintable;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.objectweb.asm.Opcodes.*;

public class TaintClassAdapter extends ClassNode {
    private final Type targetType;
    private final List<BoxingMethodWrapper> requiredFunctions;
    private final boolean noPrinting;


    public TaintClassAdapter(ClassVisitor cv, Class<?> TaintedClass, boolean noPrinting) {
        super(ASM9);
        this.cv = cv;
        targetType = Type.getType(TaintedClass);
        this.noPrinting = noPrinting;
        requiredFunctions = new ArrayList<>();
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        descriptor = TypeHelper.replaceIntegerMethod(Type.getMethodType(descriptor), targetType).getDescriptor();
        MethodNode method = new TaintMethodAdapter(access, name, descriptor, signature, exceptions, targetType, this.name,
                requiredFunctions, noPrinting);
        methods.add(method);
        return method;
    }

    private void AddMethod(BoxingMethodWrapper wrapper) {
        if (!wrapper.isWrapperRequired())
            return;

        int access = ACC_PRIVATE | ACC_FINAL | ACC_STATIC;
        MethodInsnNode wrapperInsn = wrapper.getWrapperInsn();
        MethodNode method = new MethodNode(access, wrapperInsn.name, wrapperInsn.desc, null, null);
        wrapper.createWrappingMethod(method);
        methods.add(method);
    }

    private void transformField(FieldNode field) {
        field.desc = TypeHelper.replaceIntVar(Type.getType(field.desc), targetType).toString();
    }

    private InsnList initializeField(FieldNode field) {
        InsnList insns = new InsnList();
        boolean isMember = (field.access & ACC_STATIC) == 0;
        if (isMember)
            insns.add(new VarInsnNode(ALOAD, 0));

        insns.add(new TypeInsnNode(NEW, targetType.getInternalName()));
        insns.add(new InsnNode(DUP));
        insns.add(new InsnNode(ICONST_0));
        insns.add(new MethodInsnNode(INVOKESPECIAL, targetType.getInternalName(), MethodHelper.CONSTRUCTOR_NAME, "(I)V"));
        insns.add(new FieldInsnNode(isMember ? PUTFIELD : PUTSTATIC, name, field.name, field.desc));
        field.value = null;

        return insns;
    }

    private void initializeTaintedFields(List<FieldNode> taintedFields, MethodNode initializer) {
        InsnList insns = taintedFields.stream().map(this::initializeField)
                .reduce(new InsnList(), InsnHelper::mergeInsnList);
        initializer.instructions.insert(insns);
    }

    private void initializeTaintedFields(List<FieldNode> taintedFields) {
        List<FieldNode> taintedInstanceFields = taintedFields.stream().filter(AccessHelper::isNotStatic).toList();
        List<FieldNode> taintedClassFields = taintedFields.stream().filter(AccessHelper::isStatic).toList();

        if (!taintedClassFields.isEmpty()) {
            Optional<MethodNode> classInitializer = methods.stream().filter(MethodHelper::isClassInitializer).findFirst();
            if (classInitializer.isEmpty()) {
                MethodNode newInitializer = MethodHelper.classInitializer();
                methods.add(newInitializer);
                initializeTaintedFields(taintedClassFields, newInitializer);
            } else {
                initializeTaintedFields(taintedClassFields, classInitializer.get());
            }
        }

        methods.stream().filter(MethodHelper::isConstructor)
                .forEach(constructor -> initializeTaintedFields(taintedInstanceFields, constructor));
    }

    private boolean isTaintedField(FieldNode field) {
        return TypeHelper.isTypeOrArrayOf(Type.getType(field.desc), targetType);
    }

    @Override
    public void visitEnd() {
        fields.forEach(this::transformField);
        initializeTaintedFields(fields.stream().filter(this::isTaintedField).toList());
        Stream<BoxingMethodWrapper> distinct = requiredFunctions.stream().distinct();
        distinct.forEach(this::AddMethod);
        accept(cv);
    }
}
