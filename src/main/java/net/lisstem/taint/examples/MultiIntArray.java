package net.lisstem.taint.examples;

public class MultiIntArray {
    public static void dim1() {
        int[] dim1 = new int[3];
        for (int i = 0; i < dim1.length; i++)
            dim1[i] = 1;
        for (int i: dim1) {
            System.out.println(i);
        }
    }

    public static void dim2() {
        int[][] dim2 = new int[3][2];
        for (int[] dim1: dim2)
            for (int i = 0; i < dim1.length; i++)
                dim1[i] = 2;
        for (int[] dim1: dim2)
            for (int i: dim1)
                System.out.println(i);
    }

    public static void dim3() {
        int[][][] dim3 = new int[4][3][2];
        for (int[][] dim2: dim3)
            for (int[] dim1: dim2)
                for (int i = 0; i < dim1.length; i++)
                    dim1[i] = 3;
        for (int[][] dim2: dim3)
            for (int[] dim1: dim2)
                for (int i: dim1)
                    System.out.println(i);
    }

    public static void dim4() {
        int[][][][] dim4 = new int[5][4][3][2];
        for (int[][][] dim3: dim4)
            for (int[][] dim2: dim3)
                for (int[] dim1: dim2)
                    for (int i = 0; i < dim1.length; i++)
                        dim1[i] = 4;
        for (int[][][] dim3: dim4)
            for (int[][] dim2: dim3)
                for (int[] dim1: dim2)
                    for (int i: dim1)
                        System.out.println(i);
    }

    public static void main(String[] args) {
        dim1();
        dim2();
        dim3();
        dim4();
    }
}
