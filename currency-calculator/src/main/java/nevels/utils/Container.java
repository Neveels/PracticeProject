package nevels.utils;


import nevels.utils.enums.LexemeType;

public class Container {
    private LexemeType key;
    private double value;

    public Container(LexemeType key, double value) {
        this.key = key;
        this.value = value;
    }

    public LexemeType getKey() {
        return key;
    }

    public void setKey(LexemeType key) {
        this.key = key;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Container{" +
                "key=" + key +
                ", value=" + value +
                '}';
    }
}
