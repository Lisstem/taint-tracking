package net.lisstem.taint.examples.methodinvocation;

import net.lisstem.taint.jdk.Array;

public class MyJDK {
    public static void main(String[] args) {
        int[] dim1 = Array.createDim1();
        Array.print(dim1);
        dim1[0] = 42;
        Array.print(dim1);
        int[][] dim2 = Array.createDim2();
        Array.print(dim2);
        dim2[0][0] = 1337;
        dim2[1] = dim1;
        Array.print(dim2);
    }
}
