package net.lisstem.taint.taint;

public class TaintedInt {
    private final int data;
    private final Taintable taint;

    public TaintedInt(int value) {
        this(value, new BooleanTaint());
    }

    public TaintedInt(int value, Taintable taint) {
        this.taint = taint;
        data = value;
    }

    public TaintedInt copy() {
        return new TaintedInt(data, taint.copy());
    }

    public int getData() {
        return data;
    }

    public void setTaint(boolean tainted) {
        taint.setTaint(tainted);
    }

    public TaintedInt add(TaintedInt other) {
        return new TaintedInt(data + other.data, taint.combine(other.taint));
    }

    public TaintedInt sub(TaintedInt other) {
        return new TaintedInt(data - other.data, taint.combine(other.taint));
    }

    public TaintedInt mul(TaintedInt other) {
        return new TaintedInt(data * other.data, taint.combine(other.taint));
    }

    public TaintedInt div(TaintedInt other) {
        return new TaintedInt(data / other.data, taint.combine(other.taint));
    }
    
    public TaintedInt rem(TaintedInt other) {
        return new TaintedInt(data % other.data, taint.combine(other.taint));
    }

    public TaintedInt shl(TaintedInt other) {
        return new TaintedInt(data << other.data, taint.combine(other.taint));
    }
    
    public TaintedInt shr(TaintedInt other) {
        return new TaintedInt(data >> other.data, taint.combine(other.taint));
    }

    public TaintedInt ushr(TaintedInt other) {
        return new TaintedInt(data >>> other.data, taint.combine(other.taint));
    }

    public TaintedInt or(TaintedInt other) {
        return new TaintedInt(data | other.data, taint.combine(other.taint));
    }

    public TaintedInt and(TaintedInt other) {
        return new TaintedInt(data & other.data, taint.combine(other.taint));
    }

    public TaintedInt xor(TaintedInt other) {
        return new TaintedInt(data ^ other.data, taint.combine(other.taint));
    }

    public TaintedInt neg() {
        return new TaintedInt(-data, taint);
    }

    public TaintedInt i2b() {
        return new TaintedInt((byte) data, taint);
    }

    public TaintedInt i2c() {
        return new TaintedInt((char) data, taint);
    }

    public TaintedInt i2s() {
        return new TaintedInt((short) data, taint);
    }

    public TaintedInt iinc(int increment) {
        return new TaintedInt(data + increment, taint);
    }

    @Override
    public String toString() {
        return "TaintedInt@"+ Integer.toHexString(hashCode()) + '{' +
                "data=" + data +
                ",taint=" + taint.toString() +
                '}';
    }
}
