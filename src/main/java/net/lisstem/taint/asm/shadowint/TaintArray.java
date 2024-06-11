package net.lisstem.taint.asm.shadowint;

import net.lisstem.taint.taint.BooleanTaint;
import net.lisstem.taint.taint.Taintable;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.Arrays;

public class TaintArray {
    public static final int MAX_DIM = 4;
    public static <T extends Taintable> T[] createTaintArray(int[] array, @NotNull T taint) {
        if (array == null)
            return null;
        T[] taints = (T[])Array.newInstance(taint.getClass(), array.length);
        Arrays.fill(taints, taint);
        return taints;
    }

    public static <T extends Taintable> T[][] createTaintArray(int[][] array, @NotNull T taint) {
        if (array == null)
            return null;
        T[][] taints = (T[][])Array.newInstance(taint.getClass(), array.length, 0);
        for (int i = 0; i < taints.length; i++)
            taints[i] = createTaintArray(array[i], taint);
        return taints;
    }

    public static <T extends Taintable> T[][][] createTaintArray(int[][][] array, @NotNull T taint) {
        if (array == null)
            return null;
        T[][][] taints = (T[][][])Array.newInstance(taint.getClass(), array.length, 0, 0);
        for (int i = 0; i < taints.length; i++)
            taints[i] = createTaintArray(array[i], taint);
        return taints;
    }

    public static <T extends Taintable> T[][][][] createTaintArray(int[][][][] array, @NotNull T taint) {
        if (array == null)
            return null;
        T[][][][] taints = (T[][][][])Array.newInstance(taint.getClass(), array.length, 0, 0, 0);
        for (int i = 0; i < taints.length; i++)
            taints[i] = createTaintArray(array[i], taint);
        return taints;
    }

    public static <T extends Taintable> void syncArrays(int[][] array, T[][] taints, T taint) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == null) {
                taints[i] = null;
            } else if (taints[i] == null || array[i].length != taints[i].length) {
                taints[i] = (T[]) Array.newInstance(taint.getClass(), array[i].length);
                Arrays.fill(taints[i], taint);
            }
        }
    }

    public static <T extends Taintable> void syncArrays(int[][][] array, T[][][] taints, T taint) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == null) {
                taints[i] = null;
            } else if (taints[i] == null || array[i].length != taints[i].length) {
                taints[i] = (T[][]) Array.newInstance(taints.getClass().componentType().componentType(), array[i].length);
                syncArrays(array[i], taints[i], taint);
            }
        }
    }

    public static <T extends Taintable> void syncArrays(int[][][][] array, T[][][][] taints, T taint) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == null) {
                taints[i] = null;
            } else if (taints[i] == null || array[i].length != taints[i].length) {
                taints[i] = (T[][][]) Array.newInstance(taints.getClass().componentType().componentType(), array[i].length);
                syncArrays(array[i], taints[i], taint);
            }
        }
    }
}
