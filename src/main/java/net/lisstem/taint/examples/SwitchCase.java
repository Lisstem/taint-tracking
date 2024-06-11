package net.lisstem.taint.examples;

public class SwitchCase {
    public static String chooseNear(int i) {
        return switch (i) {
            case 0 -> "zero";
            case 1 -> "one";
            case 2 -> "two";
            default -> "default";
        };
    }

    public static String chooseFar(int i) {
        return switch (i) {
            case -100 -> "negative";
            case 0 -> "zero";
            case 100 -> "positive";
            default -> "default";
        };
    }

    public static void main(String[] args) {
        System.out.println(chooseNear(0));
        System.out.println(chooseNear(1));
        System.out.println(chooseNear(2));
        System.out.println(chooseNear(3));

        System.out.println(chooseFar(-100));
        System.out.println(chooseFar(0));
        System.out.println(chooseFar(100));
        System.out.println(chooseFar(42));
    }
}
