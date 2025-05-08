import java.util.ArrayList;
import java.util.Arrays;

public class Tuple {

    public Object[] elements;
    public int hash;
    public static RollingHash rollingHash = new RollingHash(1571755261); // 1571755261 は素数。
    
    public Tuple(Object... elements) {
        this.elements = elements;
        this.hash = rollingHash.hash(new ArrayList<>(Arrays.asList(elements)));
    }
    
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other instanceof Tuple o) {
            if (this.length() != o.length()) return false;
            for (int i = 0; i < elements.length; i++) {
                if (!this.get(i).equals(o.get(i))) return false;
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return this.hash;
    }
    
    public int length() {
        return this.elements.length;
    }

    public Object get(int i) {
        return this.elements[i];
    }
}
