package net.lisstem.taint.examples;

public class IntArithmetic {
    public static void main(String[] args) {
        int first = 131;
        int second = 3;
        System.out.println(first * second);
        System.out.println(first / second);
        System.out.println(first + second);
        System.out.println(first - second);
        System.out.println(first % second);
        System.out.println(first & second);
        System.out.println(first | second);
        System.out.println(first ^ second);
        System.out.println(~first);
        System.out.println(-first);
        System.out.println(first << second);
        System.out.println(first >> second);
        System.out.println(first >>> second);
        System.out.println(-first >>> second);
        int large = 0x12345678;
        System.out.println((byte) large);
        System.out.println((char) large);
        System.out.println((short) large);
    }
}
