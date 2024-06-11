package net.lisstem.taint.asm.annotations;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.FieldNode;

import java.lang.annotation.Annotation;
import java.util.List;

public class AnnotationHelper {
    public static AnnotationNode getTaintAnnotation(FieldNode field) {
        return getTaintAnnotation(field.visibleAnnotations);
    }

    public static AnnotationNode getTaintAnnotation(List<AnnotationNode> annotations) {
        return findAnnotation(annotations, Taint.class);
    }

    public static AnnotationNode findAnnotation(List<AnnotationNode> annotations, Class<? extends Annotation> clazz) {
        if (annotations == null)
            return null;
        Type type = Type.getType(clazz);
        for (AnnotationNode node : annotations) {
            if (Type.getType(node.desc).equals(type)) {
                return node;
            }
        }
        return null;
    }

    public static boolean printTaints(List<AnnotationNode> annotations) {
        return findAnnotation(annotations, PrintTaints.class) != null;
    }

    public static boolean getTaint(AnnotationNode taint) {
        if (taint == null)
            return false;
        if (taint.values != null) {
            for (int i = 0; i < taint.values.size(); i += 2) {
                if ("value".equals(taint.values.get(i))) {
                    return (Boolean) taint.values.get(i + 1);
                }
            }
        }
        try {
            return (Boolean) Taint.class.getDeclaredMethod("value").getDefaultValue();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
