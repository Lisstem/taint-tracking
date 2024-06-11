package net.lisstem.taint.examples.benchmark;

public class Factorial {
    public static int factorial(int n) {
        int factorial = 1;
        for (int i = 2; i <= n; i++) factorial *= i;
        return factorial;
    }

    public static String factorial(String n) {
        return Integer.toString(factorial(Integer.parseInt(n)));
    }

    public static void main(String[] args) {
        for (int i = 9; i < 100; i += 10)
            System.out.println(factorial("" + i));

        System.out.println(factorial("5"));
        System.out.println(factorial("23"));
    }
}
