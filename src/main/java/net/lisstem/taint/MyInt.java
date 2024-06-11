package net.lisstem.taint;

public class MyInt {
    private final int a;
    private final int b;
    private boolean foo;

    public MyInt() {
        this(0, 0);
    }
    public MyInt(int a, int b) {
        this.a = a;
        this.b = b;
        foo = true;
    }

    void SetFoo(boolean foo) {
        this.foo = foo;
    }

    int DoSomething(int x) {
        return a * x * x + b * x;
    }

    public static void main(String[] args) {
        MyInt foo = new MyInt(42, 3);
        foo.SetFoo(false);
        MyInt bar = new MyInt();
        System.out.println(foo.a);
        int[] array = new int[3];
        array[0] = 10;
        int fooSomething = foo.DoSomething(array[0]);
        array[1] = fooSomething;
        array[2] = bar.DoSomething(array[0]);
        System.out.println(array[1]);
        System.out.println(array[2]);
    }
}