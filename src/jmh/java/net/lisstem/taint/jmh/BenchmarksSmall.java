package net.lisstem.taint.jmh;
import net.lisstem.taint.examples.benchmark.*;
import org.openjdk.jmh.annotations.*;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@BenchmarkMode({Mode.Throughput})
@Warmup(iterations = 10, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Fork(1)
public class BenchmarksSmall {
    String fibonacciN = "5";
    String[] matrixDimensions = {"3", "4", "3"};
    String factorizeInt = "123";
    List<String> strings = Arrays.asList("Lorem", "ipsum", "dolor", "sit", "amet,", "consetetur");
    String character = "l";
    String sumEven = "123";
    String[] arrayCopyDims = {"10", "30", "5"};
    String arrayCopyMod = "13";

    @Benchmark
    public String fibonacci() {
        return Fibonacci.fibonacci(fibonacciN);
    }

    @Benchmark
    public String matrixMultiplication() {
        return MatrixMultplication.multiplication(matrixDimensions[0], matrixDimensions[1], matrixDimensions[2]);
    }

    @Benchmark
    public String IntegerFactorization() {
        return PrimeFactorization.factorize(factorizeInt);
    }

    @Benchmark
    public String IntegerFactorizationWithList() {
        return PrimeFactorization.factorizeWithList(factorizeInt);
    }

    @Benchmark
    public String CountCharacters() {
        return CountCharacters.count(strings, character);
    }

    @Benchmark
    public String Factorial() {
        return Factorial.factorial(factorizeInt);
    }

    @Benchmark
    public String SumEven() {
        return SumEven.sumEven(sumEven);
    }

    @Benchmark
    public String ArrayCopy() {
        return ArrayCopy.arrayCopy(arrayCopyDims[0], arrayCopyDims[1], arrayCopyDims[2], arrayCopyMod);
    }
}
