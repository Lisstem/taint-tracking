package net.lisstem.taint.examples;

public class IntIfs {
    
    public static void ifs(int a, int b) {
        if (a < b) {
            System.out.println("lt");
        }
        if (a <= b) {
            System.out.println("le");
        }
        if (a > b) {
            System.out.println("gt");
        }
        if (a >= b) {
            System.out.println("ge");
        }
        if (a == b) {
            System.out.println("eq");
        }
        if (a != b) {
            System.out.println("ne");
        }
        if (a < 0) {
            System.out.println("lt0");
        }
        if (a <= 0) {
            System.out.println("le0");
        }
        if (a > 0) {
            System.out.println("gt0");
        }
        if (a >= 0) {
            System.out.println("ge0");
        }
        if (a == 0) {
            System.out.println("eq0");
        }
        if (a != 0) {
            System.out.println("ne0");
        }
    }
    
    public static void main(String[] args) {
        ifs(-1, 2);
        ifs(2, 1);
        ifs(0, 0);
    }
}
