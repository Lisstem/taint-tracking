package net.lisstem.taint.asm.shadowint;

import net.lisstem.taint.taint.BooleanTaint;
import net.lisstem.taint.taint.Taintable;

public class ShadowedArray<T extends Taintable> {
    public static <T extends Taintable> void iastore(int[] ia, T[] ta, int index, T tIndex, int value, T tValue) {
        ia[index] = value;
        ta[index] = tValue;
    }
}
