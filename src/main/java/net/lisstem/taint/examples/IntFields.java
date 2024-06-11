package net.lisstem.taint.examples;

import net.lisstem.taint.asm.annotations.PrintTaints;
import net.lisstem.taint.asm.annotations.Taint;

public class IntFields {
    private int Integer;
    private static int classInt;
    private byte Byte;
    private static byte classByte;
    private short Short;
    private static short classShort;
    private boolean Bool;
    private static boolean classBool;
    private char Char;
    private static char classChar;

    @PrintTaints
    public static @Taint int foo(@Taint int first, int second, @Taint int third) {
        return second;
    }


    @PrintTaints
    public static void main(@Taint String[] args) {
        System.out.println(classInt);
        System.out.println(foo(1, 2, 3));
        IntFields instance = new IntFields();
        instance.Integer = 0x01234567;
        classInt = instance.Integer;
        instance.Byte = (byte) classInt;
        classByte = instance.Byte;
        instance.Short = (short) classInt;
        classShort = instance.Short;
        instance.Integer = classShort;
        instance.Char = (char)instance.Integer;
        classChar = instance.Char;
        instance.Bool = true;
        classBool = instance.Bool;
        instance.Bool = classBool;

        System.out.println(instance.Integer);
        System.out.println(classInt);
        System.out.println(instance.Byte);
        System.out.println(classByte);
        System.out.println(instance.Short);
        System.out.println(classShort);
        System.out.println(instance.Char);
        System.out.println(classChar);
        System.out.println(instance.Bool);
        System.out.println(classBool);
    }
}
