package net.lisstem.taint.taint;

public class BooleanTaint implements Taintable {

    private boolean taint;

    public BooleanTaint() {
        taint = false;
    }

    public BooleanTaint(boolean taint) {
        this.taint = taint;
    }

    @Override
    public void setTaint(boolean taint) {
        this.taint = taint;
    }

    @Override
    public Taintable combine(Taintable taintable) {
        return new BooleanTaint(taint | ((BooleanTaint)taintable).taint);
    }

    @Override
    public Taintable copy() {
        return new BooleanTaint(taint);
    }

    @Override
    public String toString() {
        return "BooleanTaint{" +
                "taint=" + taint +
                '}';
    }
}
