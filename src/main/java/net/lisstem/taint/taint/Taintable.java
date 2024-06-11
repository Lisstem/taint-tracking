package net.lisstem.taint.taint;

public interface Taintable {
    void setTaint(boolean taint);

    // Assumption a.combine(b) = b.combine(a)
    Taintable combine(Taintable taintable);

    Taintable copy();
}
