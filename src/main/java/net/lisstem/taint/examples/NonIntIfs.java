package net.lisstem.taint.examples;

public class NonIntIfs {
    public static void ifs(Object first, Object second) {
        if (first == null) {
            System.out.println("null");
        }
        if (first != null) {
            System.out.println("non null");
        }
        if (first == second) {
            System.out.println("eq");
        }
        if (first != second) {
            System.out.println("ne");
        }
    }

    public static void main(String[] args) {
        Object first = new Object();
        Object second = new Object();
        ifs(first, second);
        ifs(null, second);
        ifs(first, first);
    }
}
