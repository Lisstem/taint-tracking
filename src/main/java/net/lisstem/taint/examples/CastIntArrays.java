package net.lisstem.taint.examples;

public class CastIntArrays {
    public static void instanceOf(Object obj) {
        if (obj instanceof int[] array) {
            for (int i: array) {
                System.out.println(i);
            }
        } else {
            System.out.println("Not a int array");
        }
    }

    public static void checkCast(Object obj) {
        int[] array = (int[]) obj;
        for (int i: array) {
            System.out.println(i);
        }
    }

    public static void main(String[] args) {
        instanceOf(new int[]{ 1, 2, 3});
        instanceOf(null);
        instanceOf(new Object());
        checkCast(new int[]{ 4, 5, 6, 7});
    }
}
