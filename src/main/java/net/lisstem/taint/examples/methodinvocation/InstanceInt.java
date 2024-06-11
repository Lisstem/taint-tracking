package net.lisstem.taint.examples.methodinvocation;

public class InstanceInt {
    public int foo(int a, int b) {
        return a | b;
    }

    public byte foo(byte a, byte b) {
        return (byte)(a | b);
    }

    public char foo(char a, char b) {
        return (char)(a | b);
    }

    public short foo(short a, short b) {
        return (short)(a | b);
    }

    public int mixed(int i, byte b, char c, short s) {
        return i << b + c * s;
    }

    public static void main(String[] args) {
        InstanceInt instance = new InstanceInt();
        int i1 = 1;
        int i2 = 1 << 1;
        byte b1 = 1 << 2;
        byte b2 = 1 << 3;
        char c1 = 1 << 4;
        char c2 = 1 << 5;
        short s1 = 1 << 6;
        short s2 = 1 << 7;
        System.out.println(instance.foo(i1, i2));
        System.out.println(instance.foo(b1, b2));
        System.out.println(instance.foo(c1, c2));
        System.out.println(instance.foo(s1, s2));
        System.out.println(instance.mixed(i1, b1, c1, s1));
    }
}
