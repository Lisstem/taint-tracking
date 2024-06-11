package net.lisstem.taint.examples.benchmark;

import java.util.Arrays;

public class ArrayCopy {
    public static void arrayCopy(int[][][] source, int[][][] target) {
        for (int i = 0; i < source.length; i++) {
            for (int j = 0; j < source[i].length; j++) {
                for (int k = 0; k < source[i][j].length; k++) {
                    target[i][j][k] = source[i][j][k];
                }
            }
        }
    }

    public static void fillArray(int[][][] array, int value) {
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[i].length; j++) {
                for (int k = 0; k < array[i][j].length; k++) {
                    array[i][j][k] = value;
                }
            }
        }
    }

    public static String toString(int[][][] A) {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < A.length; i++) {
            if (i != 0) sb.append(", ");
            sb.append('[');
            for (int j = 0; j < A[i].length; j++) {
                if (j != 0) sb.append(", ");
                sb.append('[');
                for (int k = 0; k < A[i][j].length; k++) {
                    if (k != 0) sb.append(", ");
                    sb.append(A[i][j][k]);
                }
                sb.append(']');
            }
            sb.append(']');
        }
        sb.append(']');
        return sb.toString();
    }

    public static String arrayCopy(String dim1, String dim2, String dim3, String value) {
        int d1 = Integer.parseInt(dim1);
        int d2 = Integer.parseInt(dim2);
        int d3 = Integer.parseInt(dim3);
        int[][][] source = new int[d1][d2][d3];
        fillArray(source, Integer.parseInt(value));
        int[][][] target = new int[d1][d2][d3];
        arrayCopy(source, target);
        return toString(target);
    }

    public static void main(String[] args) {
        System.out.println(arrayCopy("4", "3", "2", "5"));
        System.out.println(arrayCopy("3", "5", "2", "5"));
    }
}
