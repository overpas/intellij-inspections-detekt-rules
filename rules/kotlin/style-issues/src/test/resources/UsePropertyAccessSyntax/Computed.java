public class Computed {
    private int x;
    public int getX() {
        int result = x * 2;
        return result;
    }
    public int getValue() {
        return x;
    }
    public void setValue(int value) {
        int doubled = value * 2;
        x = doubled;
    }
    public int canGetX() {
        return x;
    }
    public void willSetX(int x) {
        this.x = x;
    }
}
