package net.lisstem.taint.examples.benchmark;

import net.lisstem.taint.asm.annotations.Taint;

public class Fibonacci {
    public static int fibonacci(int n) {
        if (n <= 0) return 0;
        if (n == 1) return 1;

        return fibonacci(n - 1) + fibonacci(n - 2);
    }

    public static String fibonacci(String n) {
        return Integer.toString(fibonacci(Integer.parseInt(n)));
    }

    public static void main(String[] args) {
        for (int i = -1; i < 10; i++)
            System.out.println("Fibonacchi(" + i + ") = " + fibonacci(i));

        System.out.println(fibonacci("10"));
    }
}
