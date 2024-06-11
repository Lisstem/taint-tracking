package net.lisstem.taint.examples.methodinvocation;

public class SpecialInt extends InstanceInt {
    @Override
    public int foo(int a, int b) {
        System.out.println("child");
        return super.foo(a, b);
    }

    @Override
    public byte foo(byte a, byte b) {
        System.out.println("child");
        return super.foo(a, b);
    }

    @Override
    public char foo(char a, char b) {
        System.out.println("child");
        return super.foo(a, b);
    }

    @Override
    public short foo(short a, short b) {
        System.out.println("child");
        return super.foo(a, b);
    }

    @Override
    public int mixed(int i, byte b, char c, short s) {
        System.out.println("child");
        return super.mixed(i, b, c, s);
    }

    public static void main(String[] args) {
        SpecialInt instance = new SpecialInt();
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
