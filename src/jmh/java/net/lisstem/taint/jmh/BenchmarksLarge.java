package net.lisstem.taint.jmh;
import net.lisstem.taint.examples.benchmark.*;
import org.openjdk.jmh.annotations.*;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@BenchmarkMode({Mode.AverageTime})
@Warmup(iterations = 10, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Fork(1)
public class BenchmarksLarge {
    String fibonacciN = "24";
    String[] matrixDimensions = {"100", "300", "400" };
    String factorizeInt = "12345678";
    List<String> strings = Arrays.asList("Lorem", "ipsum", "dolor", "sit", "amet,", "consetetur", "sadipscing", "elitr,", "sed", "diam", "nonumy", "eirmod", "tempor", "invidunt", "ut", "labore", "et", "dolore", "magna", "aliquyam", "erat,", "sed", "diam", "voluptua.", "At", "vero", "eos", "et", "accusam", "et", "justo", "duo", "dolores", "et", "ea", "rebum.", "Stet", "clita", "kasd", "gubergren,", "no", "sea", "takimata", "sanctus", "est", "Lorem", "ipsum", "dolor", "sit", "amet.", "Lorem", "ipsum", "dolor", "sit", "amet,", "consetetur", "sadipscing", "elitr,", "sed", "diam", "nonumy", "eirmod", "tempor", "invidunt", "ut", "labore", "et", "dolore", "magna", "aliquyam", "erat,", "sed", "diam", "voluptua.", "At", "vero", "eos", "et", "accusam", "et", "justo", "duo", "dolores", "et", "ea", "rebum.", "Stet", "clita", "kasd", "gubergren,", "no", "sea", "takimata", "sanctus", "est", "Lorem", "ipsum", "dolor", "sit", "amet.");
    String character = "l";
    String sumEven = "123456";


    String[] arrayCopyDims = {"120", "330", "235"};
    String arrayCopyMod = "133";

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

    //@Benchmark
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
