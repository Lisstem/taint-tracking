package net.lisstem.taint.examples.methodinvocation;

public class StaticInt {
    public static void main(String[] args) {
        int i1 = 1;
        int i2 = 1 << 1;
        byte b1 = 1 << 2;
        byte b2 = 1 << 3;
        char c1 = 1 << 4;
        char c2 = 1 << 5;
        short s1 = 1 << 6;
        short s2 = 1 << 7;
        System.out.println(foo(i1, i2));
        System.out.println(foo(b1, b2));
        System.out.println(foo(c1, c2));
        System.out.println(foo(s1, s2));
        System.out.println(mixed(i1, b1, c1, s1));
    }

    public static int foo(int a, int b) {
        return a | b;
    }

    public static byte foo(byte a, byte b) {
        return (byte)(a | b);
    }

    public static char foo(char a, char b) {
        return (char)(a | b);
    }

    public static short foo(short a, short b) {
        return (short)(a | b);
    }

    public static int mixed(int i, byte b, char c, short s) {
        return i << b + c * s;
    }
}
