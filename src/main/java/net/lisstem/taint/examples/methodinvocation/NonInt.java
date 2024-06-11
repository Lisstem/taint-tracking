package net.lisstem.taint.examples.methodinvocation;

import java.util.Arrays;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class NonInt {
    public static String staticFoo(String s) {
        return s + "_static";
    }

    public static String staticFoo(String[] s) {
        return String.join(", ", s) + "_static";
    }

    public String foo(String s) {
        return s + "_instance";
    }

    public String foo(String[] s) {
        return String.join(", ", s) + "_instance";
    }

    public static void main(String[] args) {
        NonInt instance = new NonInt();
        String s = "test";
        String[] strings = {"a", "b", "c"};
        System.out.println(staticFoo(s));
        System.out.println(instance.foo(s));
        System.out.println(staticFoo(strings));
        System.out.println(instance.foo(strings));
    }
}
