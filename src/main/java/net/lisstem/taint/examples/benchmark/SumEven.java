package net.lisstem.taint.examples.benchmark;

public class SumEven {
    public static int sumEven(int[] numbers) {
        int count = 0;
        for (int i : numbers) {
            if (i % 2 == 0) count += i;
        }
        return count;
    }

    public static String sumEven(String max) {
        int[] numbers = new int[Integer.parseInt(max)];
        for (int i = 0; i < numbers.length; i++)
            numbers[i] = i;
        return Integer.toString(sumEven(numbers));
    }

    public static void main(String[] args) {
        for (int i = 0; i < 1000; i += 10)
            System.out.println(sumEven(Integer.toString(i)));
    }
}
