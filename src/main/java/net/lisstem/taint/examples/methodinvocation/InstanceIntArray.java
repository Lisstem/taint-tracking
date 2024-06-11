package net.lisstem.taint.examples.methodinvocation;

public class InstanceIntArray {
    public void printArray(int[] array) {
        for (int i: array)
            System.out.print(Integer.toString(i) + ", ");
        System.out.println();
    }
    public int[] map(int[] array) {
        int[] mapped = new int[array.length];
        for (int i = 0; i < array.length; i++)
            mapped[i] = array[i] * 42;
        return mapped;
    }

    public void transform(int[] array) {
        for (int i = 0; i < array.length; i++)
            array[i] *= 1337;
    }

    public static void main(String[] args) {
        InstanceIntArray instance = new InstanceIntArray();
        instance.printArray(instance.map(new int[]{1, 2}));

        int[] array = new int[]{0, 1, 2, 3};
        instance.transform(array);
        instance.printArray(array);
    }
}
