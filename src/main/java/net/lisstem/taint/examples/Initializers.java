package net.lisstem.taint.examples;

import java.util.ArrayList;
import java.util.List;

public class Initializers {
    public static List<String> classVariable = new ArrayList<>();
    public List<String> variable = new ArrayList<>();
    static {
        for (int i = 0; i < 20; i++)
            classVariable.add(Integer.toHexString(i));
    }
    {
        for (int i = 0; i < 20; i++)
            variable.add(Integer.toBinaryString(i));
    }

    public static void main(String[] args) {
        classVariable.forEach(System.out::println);
        Initializers instance = new Initializers();
        instance.variable.forEach(System.out::println);
    }
}
