package net.lisstem.taint.examples;

public class IntArray {
    public static void main(String[] args) {
        int[] ints = {3, 1, 2, 3, 4};
        ints[3] = 42;
        int[] empty = new int[0];
        System.out.println(ints.length);
        System.out.println(ints[0]);
        System.out.println(ints[1]);
        System.out.println(ints[2]);
        System.out.println(ints[3]);
        System.out.println(ints[4]);
        System.out.println(empty.length);
    }
}
