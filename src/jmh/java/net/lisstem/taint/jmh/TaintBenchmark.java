package net.lisstem.taint.jmh;

import net.lisstem.taint.taint.BooleanTaint;
import net.lisstem.taint.taint.Taintable;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@BenchmarkMode({Mode.Throughput})
@Warmup(iterations = 100, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 100, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(1)
public class TaintBenchmark {
    int a = 235789;
    int b = 1343;

    int shift = 3;

    Taintable taint1 = new BooleanTaint(true);
    Taintable taint2 = new BooleanTaint(false);

    @Benchmark
    public int baseline() {
        return a;
    }

    @Benchmark
    public int add() {
        return a + b;
    }

    @Benchmark
    public int sub() {
        return a - b;
    }

    @Benchmark
    public int mul() {
        return a * b;
    }

    @Benchmark
    public int div() {
        return a / b;
    }

    @Benchmark
    public int rem() {
        return a % b;
    }

    @Benchmark
    public int and() {
        return a & b;
    }

    @Benchmark
    public int or() {
        return a | b;
    }

    @Benchmark
    public int xor() {
        return a ^ b;
    }

    @Benchmark
    public int shr() {
        return a >> shift;
    }

    @Benchmark
    public int ushr() {
        return a >>> shift;
    }

    @Benchmark
    public int shl() {
        return a << shift;
    }

    @Benchmark
    public Taintable propagateTaint() {
        return taint1.combine(taint2);
    }

}
