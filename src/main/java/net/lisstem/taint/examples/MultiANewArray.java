package net.lisstem.taint.examples;

import java.util.Arrays;

public class MultiANewArray {
    public static void dim1() {
        String[] dim1 = new String[3];
        Arrays.fill(dim1, "dim1");
        for (String s: dim1) {
            System.out.println(s);
        }
    }

    public static void dim2() {
        String[][] dim2 = new String[3][2];
        for (String[] dim1: dim2) Arrays.fill(dim1, "dim2");
        for (String[] dim1: dim2) System.out.println(String.join(", ", dim1));
    }

    public static void dim3() {
        String[][][] dim3 = new String[4][3][2];
        for (String[][] dim2: dim3)
            for (String[] dim1: dim2)
                Arrays.fill(dim1, "dim3");
        for (String[][] dim2: dim3)
            for (String[] dim1: dim2)
                System.out.println(String.join(", ", dim1));
    }

    public static void dim4() {
        String[][][][] dim4 = new String[5][4][3][2];
        for (String[][][] dim3: dim4)
            for (String[][] dim2: dim3)
                for (String[] dim1: dim2)
                    Arrays.fill(dim1, "dim4");
        for (String[][][] dim3: dim4)
            for (String[][] dim2: dim3)
                for (String[] dim1: dim2)
                    System.out.println(String.join(", ", dim1));
    }

    public static void main(String[] args) {
        dim1();
        dim2();
        dim3();
        dim4();
    }
}
