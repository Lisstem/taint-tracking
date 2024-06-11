package net.lisstem.taint.examples.benchmark;

import net.lisstem.taint.asm.annotations.Taint;

import java.util.Arrays;

public class MatrixMultplication {
    public static int[][] createMatrix(int m, int n) {
        int[][] matrix = new int[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++)
                matrix[i][j] = i * n + j;
        }
        return matrix;
    }

    public static int[][] multiply(int[][] A, int[][] B) {
        int m = A.length;
        int n = B[0].length;
        int[][] result = new int[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                int sum = 0;
                for (int k = 0; k < m; k++) {
                    sum += A[i][k] * B[k][j];
                }
                result[i][j] = sum;
            }
        }
        return result;
    }

    public static String toString(int[][] A) {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < A.length; i++) {
            if (i != 0) sb.append('\n');
            sb.append('[');
            for (int j = 0; j < A[i].length; j++) {
                if (j != 0) sb.append(", ");
                sb.append(A[i][j]);
            }
            sb.append(']');
        }
        sb.append(']');
        return sb.toString();
    }

    public static String multiplication(String mString, String kString, String nString) {
        int m = Integer.parseInt(mString);
        int k = Integer.parseInt(kString);
        int n = Integer.parseInt(nString);
        int[][] A = createMatrix(m, k);
        int[][] B = createMatrix(k, n);
        return toString(multiply(A, B));
    }

    public static void main(String[] args) {
        System.out.println(multiplication("4", "4", "4"));
        System.out.println(multiplication("3", "4", "5"));
    }
}
