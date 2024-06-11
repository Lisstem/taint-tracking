package net.lisstem.taint.asm.wrapint;

import net.lisstem.taint.taint.TaintedInt;

public class TaintedIntArray {
    public static final int MAX_DIM = 4;

    public static TaintedInt[] box(int[] ints) {
        if (ints == null)
            return null;
        TaintedInt[] tainted = new TaintedInt[ints.length];
        for (int i = 0; i < ints.length; i++) {
            tainted[i] = new TaintedInt(ints[i]);
        }
        return tainted;
    }

    public static TaintedInt[][] box(int[][] ints) {
        if (ints == null)
            return null;
        TaintedInt[][] tainted = new TaintedInt[ints.length][];
        for (int i = 0; i < ints.length; i++) {
            tainted[i] = box(ints[i]);
        }
        return tainted;
    }


    public static TaintedInt[][][] box(int[][][] ints) {
        if (ints == null)
            return null;
        TaintedInt[][][] tainted = new TaintedInt[ints.length][][];
        for (int i = 0; i < ints.length; i++) {
            tainted[i] = box(ints[i]);
        }
        return tainted;
    }


    public static TaintedInt[][][][] box(int[][][][] ints) {
        if (ints == null)
            return null;
        TaintedInt[][][][] tainted = new TaintedInt[ints.length][][][];
        for (int i = 0; i < ints.length; i++) {
            tainted[i] = box(ints[i]);
        }
        return tainted;
    }

    public static TaintedInt[] sync(TaintedInt[] taintedInts, int[] ints) {
        if (ints == null)
            return null;
        if (ints.length != taintedInts.length)
            taintedInts = new TaintedInt[ints.length];
        for (int i = 0; i < ints.length; i++) {
            taintedInts[i] = new TaintedInt(ints[i]);
        }
        return taintedInts;
    }

    public static TaintedInt[][] sync(TaintedInt[][] taintedInts, int[][] ints) {
        if (ints == null)
            return null;
        if (ints.length != taintedInts.length)
            taintedInts = new TaintedInt[ints.length][];
        for (int i = 0; i < ints.length; i++) {
            taintedInts[i] = sync(taintedInts[i], ints[i]);
        }
        return taintedInts;
    }


    public static TaintedInt[][][] sync(TaintedInt[][][] taintedInts, int[][][] ints) {
        if (ints == null)
            return null;
        if (ints.length != taintedInts.length)
            taintedInts = new TaintedInt[ints.length][][];
        for (int i = 0; i < ints.length; i++) {
            taintedInts[i] = sync(taintedInts[i], ints[i]);
        }
        return taintedInts;
    }


    public static TaintedInt[][][][] sync(TaintedInt[][][][] taintedInts, int[][][][] ints) {
        if (ints == null)
            return null;
        if (ints.length != taintedInts.length)
            taintedInts = new TaintedInt[ints.length][][][];
        for (int i = 0; i < ints.length; i++) {
            taintedInts[i] = sync(taintedInts[i], ints[i]);
        }
        return taintedInts;
    }


    public static int[] unbox(TaintedInt[] array) {
        if (array == null)
            return null;
        int[] unboxed = new int[array.length];
        for (int i = 0; i < array.length; i++)
            unboxed[i] = array[i] == null ? 0 : array[i].getData();
        return unboxed;
    }

    public static int[][] unbox(TaintedInt[][] array) {
        if (array == null)
            return null;
        int[][] unboxed = new int[array.length][];
        for (int i = 0; i < array.length;  i++) {
            unboxed[i] = unbox(array[i]);
        }
        return unboxed;
    }

    public static int[][][] unbox(TaintedInt[][][] array) {
        if (array == null)
            return null;
        int[][][] unboxed = new int[array.length][][];
        for (int i = 0; i < array.length;  i++) {
            unboxed[i] = unbox(array[i]);
        }
        return unboxed;
    }

    public static int[][][][] unbox(TaintedInt[][][][] array) {
        if (array == null)
            return null;
        int[][][][] unboxed = new int[array.length][][][];
        for (int i = 0; i < array.length;  i++) {
            unboxed[i] = unbox(array[i]);
        }
        return unboxed;
    }
}
