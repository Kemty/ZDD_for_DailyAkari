import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class SharedList<T> {    
    
    public int n;
    public ArrayList<T> items;
    public HashMap<T, Integer> idx;
    public SharedListNode terminal;
    
    public RollingHash rollingHash = new RollingHash(1652572013); // 1652572013 は素数。
    public HashMap<SharedListNode, SharedListNode> node_table = new HashMap<>();
    public HashMap<Tuple, SharedListNode> memo_table = new HashMap<>();
    
    public SharedList(ArrayList<T> arrayList) {
        this.n = arrayList.size();
        this.items = (ArrayList<T>)arrayList.clone();
        this.idx = new HashMap<>();
        this.terminal = new SharedListNode(null, null);
        terminal.hash = rollingHash.xorshift(n+1);
        for(int i = 0; i < n; ++i) idx.put(items.get(i), i);
    }
    
    public SharedListNode get_node(T top, SharedListNode nxt) {
        SharedListNode node = new SharedListNode(top, nxt);
        if (node_table.containsKey(node)) {
            node = node_table.get(node);
        } else {
            node_table.put(node, node);
            node.length = node.nxt.length + 1;
        }
        return node;
    }
    
    public SharedListNode push(List<T> list) {
    
        List<T> sortedList = list.stream().sorted(new Comparator<>() {
            @Override
            public int compare(T e1, T e2) {
                return Integer.compare(idx.get(e1), idx.get(e2));
            }
        }).toList();
    
        SharedListNode node = terminal;
        for (T e : sortedList.reversed()) {
            node = get_node(e, node);
        }
        return node;
    }
    


    public class SharedListNode {
    
        public T top;
        public SharedListNode nxt;
        public int length = 0; // 集合のサイズ
        public int hash;

        public SharedListNode(T top, SharedListNode nxt) {
            this.top = top;
            this.nxt = nxt;
            if (top != null) {
                this.hash =  rollingHash.hash(Arrays.asList(idx.get(top), nxt));
            }
        }
    
        @Override
        public int hashCode() {
            return this.hash;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (other instanceof SharedList<?>.SharedListNode o) {
                if (this.top == null || o.top == null) return this == o;
                return this.top.equals(o.top)
                && this.nxt == o.nxt;
            } else {
                return false;
            }
        }

        public int size() {
            return this.length;
        }
    }
}
