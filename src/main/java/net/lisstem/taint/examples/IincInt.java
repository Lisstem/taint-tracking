package net.lisstem.taint.examples;

public class IincInt {
    public static void main(String[] args) {
        int i = 5;
        System.out.println(i);
        i++;
        System.out.println(i);
        // Two byte/wide version
        i += 0xFFF;
        System.out.println(i);
    }
}
