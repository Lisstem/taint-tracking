package net.lisstem.taint.jdk;

import java.util.Arrays;

public class Array {
    public static int[] createDim1() {
        return new int[]{1,2,3,4,5};
    }

    public static int[][] createDim2() {
        int[][] dim2 = new int[3][];
        for (int i = 0; i < dim2.length; i++)
            dim2[i] = createDim1();
        return dim2;
    }

    public static void print(int[] array) {
        Arrays.stream(array).mapToObj(i -> i + ", ").forEach(System.out::println);
    }

    public static void print(int[][] array) {
        Arrays.stream(array).forEach(a -> {
            print(a);
            System.out.println();
        });
    }
}
