package net.lisstem.taint.examples.methodinvocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JDKIntArray {
    public static int[] dim1(int dim) {
        int[] array = new int[dim];
        Arrays.fill(array, -31);
        return array;
    }
    public static int[][] dim2(int dim) {
        int[][] array = new int[dim][];
        Arrays.fill(array, dim1(2));
        return array;
    }

    public static int[][][] dim3(int dim) {
        int[][][] array = new int[dim][][];
        Arrays.fill(array, dim2(3));
        return array;
    }

    public static int[][][][] dim4(int dim) {
        int[][][][] array = new int[dim][][][];
        Arrays.fill(array, dim3(4));
        return array;
    }

    public static void print(int[] array) {
        for (int i: array)
            System.out.print(Integer.toString(i) + ", ");
        System.out.println();
    }

    public static void print(int[][] array) {
        for (int[] i: array)
            print(i);
        System.out.println();
    }


    public static void print(int[][][] array) {
        for (int[][] i: array)
            print(i);
        System.out.println();
    }
    public static void print(int[][][][] array) {
        for (int[][][] i: array)
            print(i);
        System.out.println();
    }

    public static void main(String[] args) {
        print(dim1(42));
        List<int[][][]> list = new ArrayList<>(Arrays.asList(dim4(2)));
        int[][][][] array =  list.toArray(new int[][][][]{});
        print(array);
        print(Arrays.copyOf(array, 1));
    }
}
