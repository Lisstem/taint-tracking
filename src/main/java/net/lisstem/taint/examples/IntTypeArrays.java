package net.lisstem.taint.examples;

public class IntTypeArrays {
    public static void main(String[] args) {
        byte[] bytes = {5, 6};
        char[] chars = { 'a', 'b', 'c', 'd'};
        short[] shorts = {11, 12, 13};
        boolean[] booleans = {true, false, true};
        System.out.println(bytes.length);
        System.out.println(bytes[0]);
        System.out.println(bytes[1]);
        System.out.println(chars.length);
        System.out.println(chars[0]);
        System.out.println(chars[1]);
        System.out.println(chars[2]);
        System.out.println(chars[3]);
        System.out.println(shorts.length);
        System.out.println(shorts[0]);
        System.out.println(shorts[1]);
        System.out.println(shorts[2]);
        System.out.println(booleans.length);
        System.out.println(booleans[0]);
        System.out.println(booleans[1]);
        System.out.println(booleans[2]);
    }
}
