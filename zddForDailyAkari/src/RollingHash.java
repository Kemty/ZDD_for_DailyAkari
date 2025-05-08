import java.util.List;
import java.util.Random;

public class RollingHash {

    public int m; // 法
    private int r; // 基数

    public RollingHash(int m) {
        this.m = m;
        Random random = new Random();
        this.r = random.nextInt(2, m);
    }

    public int hash(List<Object> list) {
        long l = r;
        long h = 0;
        for (Object e : list) {
            h += xorshift(e.hashCode() + 1) * l;
            h %= m;
            l = l * r % m;
        }
        return (int) h;
    }

    public int xorshift(int x) {
        x ^= x << 13;
        x ^= x >> 17;
        x ^= x << 15;
        return Integer.remainderUnsigned(x, m);
    }
}