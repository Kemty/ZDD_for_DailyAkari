final class TestClass {
    
    public int n;

    public TestClass(int n) {
        this.n = n;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof TestClass o) {
            return this.n == o.n;
        } else {
            return false;
        }
    }
}

public class codeTest {
    public static void main(String[] args) {
        TestClass t1 = new TestClass(1);
        TestClass t2 = new TestClass(1);
        Object[] tt = new Object[2];
        tt[0] = t1;
        tt[1] = t2;
        System.out.println(t1 == t2);
        System.out.println(t1.equals(t2));
        System.out.println(tt[0] == tt[1]);
        System.out.println(tt[0].equals(tt[1]));
    }
}

