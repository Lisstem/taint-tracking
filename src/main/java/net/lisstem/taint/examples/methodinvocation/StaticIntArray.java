package net.lisstem.taint.examples.methodinvocation;

import net.lisstem.taint.asm.annotations.PrintTaints;
import net.lisstem.taint.asm.annotations.Taint;

public class StaticIntArray {

    @PrintTaints
    public static void printArray(int[] array) {
        for (int i: array)
            System.out.print(i + ", ");
        System.out.println();
    }
    @PrintTaints
    public static int[] map(int[] array) {
        int[] mapped = new int[array.length];
        for (int i = 0; i < array.length; i++)
            mapped[i] = array[i] * 42;
        return mapped;
    }


    @PrintTaints
    public static void transform(int[] array) {
        for (int i = 0; i < array.length; i++)
            array[i] *= 1337;
    }

    private static int counter = initCounter();


    public static @Taint int initCounter() {
        return 0;
    }

    @PrintTaints
    public static int nextInt() {
        return counter++;
    }

    @PrintTaints
    public static void main(String[] args) {
        printArray(map(new int[]{nextInt(), nextInt()}));
        int[] array = new int[]{0, 1, nextInt(), 3};
        transform(array);
        printArray(array);
    }
}
