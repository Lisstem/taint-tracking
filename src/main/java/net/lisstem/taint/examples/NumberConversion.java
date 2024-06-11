package net.lisstem.taint.examples;

public class NumberConversion {
    public static void main(String[] args) {
        int i = 5;
        System.out.println(i);
        double d = i;
        System.out.println(d);
        float f = (float)d;
        System.out.println(f);
        i = (int)(f*2);
        System.out.println(i);
        f = i;
        System.out.println(f);
        i = (int)(2.0 * d);
        System.out.println(i);
        long l = i;
        System.out.println(l);
        i = (int)(l/2);
        System.out.println(i);
        l = (long)f;
        System.out.println(l);
        d = l;
        System.out.println(d);
        l = (long)(d*3.4);
        System.out.println(l);
        f = l;
        System.out.println(f);
    }
}
