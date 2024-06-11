package net.lisstem.taint.examples;

import net.lisstem.taint.asm.annotations.PrintTaints;
import net.lisstem.taint.asm.annotations.Taint;

public class Presentation {
    private static int integer = 5;
    private static String string = "Hello World";

    public static int sum(int a, int b) {
        return a + b;
    }

    public static void main(String[] args) {
        System.out.println(sum(3, integer));
        System.out.println(string);
    }
}
