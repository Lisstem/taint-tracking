package net.lisstem.taint.examples.benchmark;

import java.util.ArrayList;
import java.util.List;

public class PrimeFactorization {
    public static boolean isPrime(int n) {
        int sqrt = (int) Math.sqrt(n);
        for (int i = 2; i < sqrt; i++) {
            if (n % i == 0) return false;
        }
        return true;
    }

    public static boolean isPrime(int n, List<Integer> foundPrimes) {
        for (int i: foundPrimes) {
            if (n % i == 0) return false;
        }
        foundPrimes.add(n);
        return true;
    }

    public static String factorize(String n) {
        int number = Integer.parseInt(n);
        if (number <= 0) return null;

        List<Integer> factors = new ArrayList<>();

        for (int i = 2; i <= number; i++) {
            if (isPrime(i)) {
                while (number % i == 0) {
                    number /= i;
                    factors.add(i);
                }
            }
        }
        return factors.toString();
    }

    public static String factorizeWithList(String n) {
        int number = Integer.parseInt(n);
        if (number <= 0) return null;
        List<Integer> primes = new ArrayList<>();
        List<Integer> factors = new ArrayList<>();
        for (int i = 2; i <= number; i++) {
            if (isPrime(i, primes)) {
                while (number % i == 0) {
                    number /= i;
                    factors.add(i);
                }
            }
        }
        return factors.toString();
    }

    public static void main(String[] args) {
        for (int i = 1; i < 24; i++) {
            System.out.println(factorize("" + i));
        }
        System.out.println(factorize("120"));
        System.out.println(factorize("12345678"));

        for (int i = 1; i < 24; i++) {
            System.out.println(factorizeWithList("" + i));
        }
        System.out.println(factorizeWithList("120"));
        System.out.println(factorizeWithList("12345678"));
    }
}
