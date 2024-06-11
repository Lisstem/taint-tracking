package net.lisstem.taint;

import net.lisstem.taint.asm.annotations.PrintTaints;
import net.lisstem.taint.asm.annotations.Taint;

public class MyByte {
    private final byte a;
    private final byte b;
    private final boolean foo = true;

    public MyByte() {
        this((byte) 0, (byte) 0);
    }
    public MyByte(byte a, byte b) {
        this.a = a;
        this.b = b;
    }

    byte DoSomething(byte x) {
        return (byte) (a * x * x + b * x);
    }

    @PrintTaints @Taint
    public static void main(@Taint(false) String[] args) {
        MyByte foo = new MyByte((byte) 42, (byte) 3);
        MyByte bar = new MyByte();
        System.out.println(foo.DoSomething((byte) 10));
        System.out.println(bar.DoSomething((byte) 1));
    }
}
