public class Direction {

    public int di;
    public int dj;

    public Direction(int di, int dj) {
        this.di = di;
        this.dj = dj;
    }

    public static Direction UP = new Direction(-1, 0);
    public static Direction DOWN = new Direction(1, 0);
    public static Direction LEFT = new Direction(0, -1);
    public static Direction RIGHT = new Direction(0, 1);
}