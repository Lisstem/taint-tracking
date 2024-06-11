package net.lisstem.taint.taint;

public class TaintBox<T extends Taintable> {
    private final Object data;
    private final Object taint;

    public TaintBox(int data, T taint) {
        this.data = data;
        this.taint = taint;
    }

    public int getInt() {
        return (int)data;
    }

    public Object getArray() {
        return data;
    }

    public T getTaint() {
        return (T) taint;
    }

    public Object getTaintArray() {
        return taint;
    }

    public TaintBox(Object data, Object taint) {
        this.data = data;
        this.taint = taint;
    }


}
