package net.lisstem.taint.asm.shadowint;

import net.lisstem.taint.asm.AccessHelper;
import net.lisstem.taint.asm.InsnHelper;
import net.lisstem.taint.asm.MethodHelper;
import net.lisstem.taint.asm.TypeHelper;
import net.lisstem.taint.taint.Taintable;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.lisstem.taint.asm.MethodHelper.CONSTRUCTOR_NAME;
import static net.lisstem.taint.asm.MethodHelper.EMPTY_DESC;
import static org.objectweb.asm.Opcodes.*;

public class TaintClassAdapter extends ClassNode {
    private final Class<? extends Taintable> taintClass;
    private final boolean noPrinting;
    private final List<ShadowMethodWrapper> requiredFunctions = Collections.synchronizedList(new ArrayList<>());

    public TaintClassAdapter(ClassVisitor cv, Class<? extends Taintable> taintClass, boolean noPrinting) {
        super(ASM9);
        this.cv = cv;
        this.taintClass = taintClass;
        this.noPrinting = noPrinting;
    }


    private FieldNode shadow(FieldNode field) {
        FieldNode shadowed = new FieldNode(ASM9, field.access, Utils.shadowVariableName(field.name), Type.getDescriptor(taintClass),
                field.signature, null);
        shadowed.visibleAnnotations = field.visibleAnnotations;
        shadowed.visibleTypeAnnotations = field.visibleTypeAnnotations;
        shadowed.invisibleAnnotations = field.invisibleAnnotations;
        shadowed.invisibleTypeAnnotations = field.invisibleTypeAnnotations;
        return shadowed;
    }

    private void addMethod(ShadowMethodWrapper wrapper) {
        if (!wrapper.isWrapperRequired())
            return;

        int access = ACC_PRIVATE | ACC_FINAL | ACC_STATIC;
        MethodInsnNode wrapperInsn = wrapper.getWrapperInsn();
        MethodNode method = new MethodNode(access, wrapperInsn.name, wrapperInsn.desc, null, null);
        wrapper.createWrappingMethod(method);
        methods.add(method);
    }

    private List<FieldNode> shadowFields() {
        return fields.stream().filter(TypeHelper::isInt).map(this::shadow).toList();
    }


    private InsnList initializeField(FieldNode field) {
        InsnList insns = new InsnList();
        boolean isMember = (field.access & ACC_STATIC) == 0;
        if (isMember)
            insns.add(new VarInsnNode(ALOAD, 0));
        insns.add(new TypeInsnNode(NEW, Type.getInternalName(taintClass)));
        insns.add(new InsnNode(DUP));
        insns.add(new MethodInsnNode(INVOKESPECIAL, Type.getInternalName(taintClass), CONSTRUCTOR_NAME, EMPTY_DESC));
        insns.add(new FieldInsnNode(isMember ? PUTFIELD : PUTSTATIC, name, field.name, field.desc));

        return insns;
    }

    private void initializeShadowFields(List<FieldNode> shadowFields, MethodNode initializer) {
        InsnList insns = shadowFields.stream().map(this::initializeField)
                                              .reduce(new InsnList(), InsnHelper::mergeInsnList);
        initializer.instructions.insert(insns);
    }

    private void initializeShadowFields(List<FieldNode> shadowFields) {
        List<FieldNode> shadowInstanceFields = shadowFields.stream().filter(AccessHelper::isNotStatic).toList();
        List<FieldNode> shadowClassFields = shadowFields.stream().filter(AccessHelper::isStatic).toList();

        if (!shadowClassFields.isEmpty()) {
            Optional<MethodNode> classInitializer = methods.stream().filter(MethodHelper::isClassInitializer).findFirst();
            if (classInitializer.isEmpty()) {
                MethodNode newInitializer = MethodHelper.classInitializer();
                methods.add(newInitializer);
                initializeShadowFields(shadowClassFields, newInitializer);

            } else {
                initializeShadowFields(shadowClassFields, classInitializer.get());
            }
        }

        methods.stream().filter(MethodHelper::isConstructor)
                        .forEach(constructor -> initializeShadowFields(shadowInstanceFields, constructor));
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        descriptor = Utils.shadowedMethodDesc(Type.getMethodType(descriptor), Type.getType(taintClass));
        MethodNode method = new ShadowMethodAdapter(access, name, descriptor, signature, exceptions,
                taintClass, this.name, requiredFunctions, noPrinting);
        this.methods.add(method);

        return method;
    }

    @Override
    public void visitEnd() {
        List<FieldNode> shadowFields = shadowFields();
        fields.addAll(shadowFields);
        initializeShadowFields(shadowFields);

        Stream<ShadowMethodWrapper> distinct = requiredFunctions.stream().distinct();
        distinct.forEach(this::addMethod);

        accept(cv);
    }
}
