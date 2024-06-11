package net.lisstem.taint.examples;

public class FieldsWithValues {
    int instance = 5;
    static int clazz = 42;

    public static void main(String[] args) {
        System.out.println(clazz);
        System.out.println((new FieldsWithValues()).instance);
    }
}
