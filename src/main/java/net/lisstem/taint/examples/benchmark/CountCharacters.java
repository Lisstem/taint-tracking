package net.lisstem.taint.examples.benchmark;

import net.lisstem.taint.asm.annotations.Taint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CountCharacters {

    public static int count(List<String> strings, char c) {
        int count = 0;
        for (int i = 0; i < strings.size(); i++) {
            String s = strings.get(i);
            for (int j = 0; j < s.length(); j++)
                if (s.charAt(j) == c) count++;
        }
        return count;
    }

    public static String count(List<String> strings, String c) {
        return Integer.toString(count(strings, c.charAt(0)));
    }

    public static void main(String[] args) {
        List<String> strings = Arrays.asList("foo", "bar", "aksd", "asdjkl", "azac");
        System.out.println(count(strings, 'o'));
        System.out.println(count(strings, 'a'));
        System.out.println(count(strings, "l"));
    }
}
