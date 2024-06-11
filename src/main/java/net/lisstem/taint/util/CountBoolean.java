package net.lisstem.taint.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class CountBoolean implements Collector<Boolean, int[], int[]> {
    public static final int COUNT = 0;
    public static final int TRUE = 1;
    public static final int FALSE = 2;
    public static final Set<Characteristics> characteristics = new HashSet<>(Arrays.asList(Characteristics.CONCURRENT, Characteristics.IDENTITY_FINISH, Characteristics.IDENTITY_FINISH));
    public static final Supplier<int[]> supplier = () -> new int[]{0,0,0};
    public static final BiConsumer<int[], Boolean> accumulator = (values, bool) -> {
        values[COUNT]++;
        values[bool ? TRUE : FALSE]++;
    };
    private static final BinaryOperator<int[]> combiner = (a, b) -> {
        a[COUNT] += b[COUNT];
        a[TRUE] += b[TRUE];
        a[FALSE] += b[FALSE];
        return a;
    };

    private static final CountBoolean instance = new CountBoolean();

    @Override
    public Supplier<int[]> supplier() { return supplier; }
    @Override
    public BiConsumer<int[], Boolean> accumulator() { return accumulator; }
    @Override
    public BinaryOperator<int[]> combiner() { return combiner; }
    @Override
    public Function<int[], int[]> finisher() { return Function.identity(); }
    @Override
    public Set<Characteristics> characteristics() { return characteristics; }

    public static CountBoolean get() { return instance; }
}
