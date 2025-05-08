import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class ZDD<T> {
    
    public int n;
    public ArrayList<T> items;
    public HashMap<T, Integer> idx;
    public ZDD_Node zero_terminal;
    public ZDD_Node one_terminal;
    public SharedList sl;
    
    public RollingHash rollingHash = new RollingHash(1401055423); // 1401055423 は素数。
    public HashMap<ZDD_Node, ZDD_Node> node_table = new HashMap<>();
    public HashMap<Tuple, ZDD_Node> memo_table = new HashMap<>();
    public HashMap<ZDD_Node, Long> sizeMap = new HashMap<>();

    public final ArrayList<ArrayList<T>> emptySet = new ArrayList<>();
    public final ArrayList<ArrayList<T>> unitSet = new ArrayList<>(Arrays.asList(new ArrayList<>()));
    
    public ZDD(ArrayList<T> arrayList) {
        this.n = arrayList.size();
        this.items = (ArrayList<T>)arrayList.clone();
        this.idx = new HashMap<>();
        this.zero_terminal = new ZDD_Node(null, null, null);
        this.one_terminal = new ZDD_Node(null, null, null);
        zero_terminal.hash = rollingHash.xorshift(n+1);
        one_terminal.hash = rollingHash.xorshift(n+2);
        for(int i = 0; i < n; ++i) idx.put(items.get(i), i);
        this.sl = new SharedList(arrayList);
    }
    
    public ZDD_Node get_node(T top, ZDD_Node zero, ZDD_Node one) {
        if (one == zero_terminal) return zero;
        ZDD_Node node = new ZDD_Node(top, zero, one);
        if (node_table.containsKey(node)) {
            node = node_table.get(node);
        } else {
            node_table.put(node, node);
        }
        return node;
    }

    public ZDD_Node powerSet() {
        return powerSet(items);
    }

    public ZDD_Node build(ArrayList<ArrayList<T>> setFamily) {
        if (setFamily.equals(emptySet)) return zero_terminal;
        if (setFamily.equals(unitSet)) return one_terminal;
        T top;
        ZDD_Node zero = build()
        return get_node(, zero_terminal, one_terminal)
    }
    
    public ZDD_Node powerSet(List<T> list) {

        List<T> sortedList = list.stream().sorted(new Comparator<>() {
            @Override
            public int compare(T e1, T e2) {
                return Integer.compare(idx.get(e1), idx.get(e2));
            }
        }).toList();

        ZDD_Node node = one_terminal;
        for (T e : sortedList.reversed()) {
            node = get_node(e, node, node);
        }
        return node;
    }



    public class ZDD_Node {
        
        public T top;
        public ZDD_Node zero;
        public ZDD_Node one;
        public int hash;
        
        private final static int OFFSET_ID       = 1;
        private final static int OFFSET_S_ID     = 2;
        private final static int ONSET_ID        = 3;
        private final static int ONSET_S_ID      = 4;
        private final static int ONSET_HOLD_ID   = 5;
        private final static int ONSET_HOLD_S_ID = 6;
        private final static int HINT_ID         = 7;
        private final static int HINT_GE_ID      = 8;
        private final static int HINT_LE_ID      = 9;
        private final static int HINT_RANGE_ID   = 10;

        public ZDD_Node(T top, ZDD_Node zero, ZDD_Node one) {
            this.top = top;
            this.zero = zero;
            this.one = one;
            if (top != null) {
                this.hash = rollingHash.hash(Arrays.asList(idx.get(top), zero, one));
            }
        }
    
        @Override
        public int hashCode() {
            return this.hash;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (other instanceof ZDD<?>.ZDD_Node o) {
                if (this.top == null || o.top == null) return this == o;
                return this.top.equals(o.top)
                && this.zero == o.zero
                && this.one == o.one;
            } else {
                return false;
            }
        }

        public long size() {
            if (this == zero_terminal) return 0;
            if (this == one_terminal) return 1;
            if (!sizeMap.containsKey(this)) {
                sizeMap.put(this, this.zero.size() + this.one.size());
            }
            return sizeMap.get(this);
        }

        public ZDD_Node offset(T e) {
            if (this == zero_terminal) return zero_terminal;
            if (this == one_terminal) return one_terminal;
            Tuple keyTuple = new Tuple(OFFSET_ID, this, e);
            ZDD_Node valueNode;
            if (!memo_table.containsKey(keyTuple)) {
                if (idx.get(this.top) < idx.get(e)) {
                    valueNode = get_node(this.top, this.zero.offset(e), this.one.offset(e));
                } else if (idx.get(this.top) > idx.get(e)) {
                    return this;
                } else {
                    return this.zero;
                }
                memo_table.put(keyTuple, valueNode);
            }
            return memo_table.get(keyTuple);
        }

        public ZDD_Node offset_s(T[] eArray) {return offset_s(Arrays.asList(eArray));}

        public ZDD_Node offset_s(List<T> eList) {return offset_s(sl.push(eList));}

        public ZDD_Node offset_s(SharedList<T>.SharedListNode eListNode) {
            if (eListNode == sl.terminal) return this;
            if (this == zero_terminal) return zero_terminal;
            if (this == one_terminal) return one_terminal;
            Tuple keyTuple = new Tuple(OFFSET_S_ID, this, eListNode);
            ZDD_Node valueNode;
            if (!memo_table.containsKey(keyTuple)) {
                if (idx.get(this.top) < idx.get(eListNode.top)) {
                    valueNode = get_node(this.top, this.zero.offset_s(eListNode), this.one.offset_s(eListNode));
                } else if (idx.get(this.top) > idx.get(eListNode.top)) {
                    valueNode = this.offset_s(eListNode.nxt);
                } else {
                    valueNode = this.zero.offset_s(eListNode.nxt);
                }
                memo_table.put(keyTuple, valueNode);
            }
            return memo_table.get(keyTuple);
        }
        
        public ZDD_Node onset(T e) {
            if (this == zero_terminal) return zero_terminal;
            if (this == one_terminal) return zero_terminal;
            Tuple keyTuple = new Tuple(ONSET_ID, this, e);
            ZDD_Node valueNode;
            if (!memo_table.containsKey(keyTuple)) {
                if (idx.get(this.top) < idx.get(e)) {
                    valueNode = get_node(this.top, this.zero.onset(e), this.one.onset(e));
                } else if (idx.get(this.top) > idx.get(e)) {
                    return zero_terminal;
                } else {
                    return this.one;
                }
                memo_table.put(keyTuple, valueNode);
            }
            return memo_table.get(keyTuple);
        }
        
        public ZDD_Node onset_s(T[] eArray) {return onset_s(Arrays.asList(eArray));}
        
        public ZDD_Node onset_s(List<T> eList) {return onset_s(sl.push(eList));}

        public ZDD_Node onset_s(SharedList<T>.SharedListNode eListNode) {
            if (eListNode == sl.terminal) return this;
            if (this == zero_terminal) return zero_terminal;
            if (this == one_terminal) return zero_terminal;
            Tuple keyTuple = new Tuple(ONSET_S_ID, this, eListNode);
            ZDD_Node valueNode;
            if (!memo_table.containsKey(keyTuple)) {
                if (idx.get(this.top) < idx.get(eListNode.top)) {
                    valueNode = get_node(this.top, this.zero.onset_s(eListNode), this.one.onset_s(eListNode));
                } else if (idx.get(this.top) > idx.get(eListNode.top)) {
                    return zero_terminal;
                } else {
                    valueNode = this.one.onset_s(eListNode.nxt);
                }
                memo_table.put(keyTuple, valueNode);
            }
            return memo_table.get(keyTuple);
        }

        public ZDD_Node onset_hold(T e) {
            if (this == zero_terminal) return zero_terminal;
            if (this == one_terminal) return zero_terminal;
            Tuple keyTuple = new Tuple(ONSET_HOLD_ID, this, e);
            ZDD_Node valueNode;
            if (!memo_table.containsKey(keyTuple)) {
                if (idx.get(this.top) < idx.get(e)) {
                    valueNode = get_node(this.top, this.zero.onset_hold(e), this.one.onset_hold(e));
                } else if (idx.get(this.top) > idx.get(e)) {
                    return zero_terminal;
                } else {
                    return get_node(this.top, zero_terminal, this.one);
                }
                memo_table.put(keyTuple, valueNode);
            }
            return memo_table.get(keyTuple);
        }

        public ZDD_Node onset_hold_s(T[] eArray) {return onset_hold_s(Arrays.asList(eArray));}

        public ZDD_Node onset_hold_s(List<T> eList) {return onset_hold_s(sl.push(eList));}
        
        public ZDD_Node onset_hold_s(SharedList<T>.SharedListNode eListNode) {
            if (eListNode == sl.terminal) return this;
            if (this == zero_terminal) return zero_terminal;
            if (this == one_terminal) return zero_terminal;
            Tuple keyTuple = new Tuple(ONSET_HOLD_S_ID, this, eListNode);
            ZDD_Node valueNode;
            if (!memo_table.containsKey(keyTuple)) {
                if (idx.get(this.top) < idx.get(eListNode.top)) {
                    valueNode = get_node(this.top, this.zero.onset_hold_s(eListNode), this.one.onset_hold_s(eListNode));
                } else if (idx.get(this.top) > idx.get(eListNode.top)) {
                    return zero_terminal;
                } else {
                    valueNode = get_node(this.top, zero_terminal, this.one.onset_hold_s(eListNode.nxt));
                }
                memo_table.put(keyTuple, valueNode);
            }
            return memo_table.get(keyTuple);
        }
        
        public ZDD_Node hint(T[] eArray, int h) {return hint(Arrays.asList(eArray), h);}
        
        public ZDD_Node hint(List<T> eList, int h) {return hint(sl.push(eList), h);}
        
        public ZDD_Node hint(SharedList<T>.SharedListNode eListNode, int h) {
            if (h == 0) return this.offset_s(eListNode);
            if (h > eListNode.size()) return zero_terminal; // この行より下では、eListNode != sl.terminal が保証される。 
            if (h == eListNode.size()) return this.onset_hold_s(eListNode);
            if (this == zero_terminal) return zero_terminal;
            if (this == one_terminal) return zero_terminal;
            Tuple keyTuple = new Tuple(HINT_ID, this, eListNode, h);
            ZDD_Node valueNode;
            if (!memo_table.containsKey(keyTuple)) {
                if (idx.get(this.top) < idx.get(eListNode.top)) {
                    valueNode = get_node(this.top, this.zero.hint(eListNode, h), this.one.hint(eListNode, h));
                } else if (idx.get(this.top) > idx.get(eListNode.top)) {
                    valueNode = this.hint(eListNode.nxt, h);
                } else {
                    valueNode = get_node(this.top, this.zero.hint(eListNode.nxt, h), this.one.hint(eListNode.nxt, h-1));
                }
                memo_table.put(keyTuple, valueNode);
            }
            return memo_table.get(keyTuple);
        }
        
        public ZDD_Node hint_ge(T[] eArray, int hl) {return hint_ge(Arrays.asList(eArray), hl);}
        
        public ZDD_Node hint_ge(List<T> eList, int hl) {return hint_ge(sl.push(eList), hl);}
        
        public ZDD_Node hint_ge(SharedList<T>.SharedListNode eListNode, int hl) {
            if (hl == 0) return this;
            if (hl > eListNode.size()) return zero_terminal; // この行より下では、eListNode != sl.terminal が保証される。 
            if (hl == eListNode.size()) return this.onset_hold_s(eListNode);
            if (this == zero_terminal) return zero_terminal;
            if (this == one_terminal) return zero_terminal;
            Tuple keyTuple = new Tuple(HINT_GE_ID, this, eListNode, hl);
            ZDD_Node valueNode;
            if (!memo_table.containsKey(keyTuple)) {
                if (idx.get(this.top) < idx.get(eListNode.top)) {
                    valueNode = get_node(this.top, this.zero.hint_ge(eListNode, hl), this.one.hint_ge(eListNode, hl));
                } else if (idx.get(this.top) > idx.get(eListNode.top)) {
                    valueNode = this.hint_ge(eListNode.nxt, hl);
                } else {
                    valueNode = get_node(this.top, this.zero.hint_ge(eListNode.nxt, hl), this.one.hint_ge(eListNode.nxt, hl-1));
                }
                memo_table.put(keyTuple, valueNode);
            }
            return memo_table.get(keyTuple);
        }
        
        public ZDD_Node hint_le(T[] eArray, int hr) {return hint_le(Arrays.asList(eArray), hr);}
        
        public ZDD_Node hint_le(List<T> eList, int hr) {return hint_le(sl.push(eList), hr);}
        
        public ZDD_Node hint_le(SharedList<T>.SharedListNode eListNode, int hr) {
            if (hr == 0) return this.offset_s(eListNode); 
            if (hr >= eListNode.size()) return this; // この行より下では、eListNode != sl.terminal が保証される。 
            if (this == zero_terminal) return zero_terminal;
            if (this == one_terminal) return one_terminal;
            Tuple keyTuple = new Tuple(HINT_LE_ID, this, eListNode, hr);
            ZDD_Node valueNode;
            if (!memo_table.containsKey(keyTuple)) {
                if (idx.get(this.top) < idx.get(eListNode.top)) {
                    valueNode = get_node(this.top, this.zero.hint_le(eListNode, hr), this.one.hint_le(eListNode, hr));
                } else if (idx.get(this.top) > idx.get(eListNode.top)) {
                    valueNode = this.hint_le(eListNode.nxt, hr);
                } else {
                    valueNode = get_node(this.top, this.zero.hint_le(eListNode.nxt, hr), this.one.hint_le(eListNode.nxt, hr-1));
                }
                memo_table.put(keyTuple, valueNode);
            }
            return memo_table.get(keyTuple);
        }
        
        public ZDD_Node hint_range(T[] eArray, int hl, int hr) {return hint_range(Arrays.asList(eArray), hl, hr);}
        
        public ZDD_Node hint_range(List<T> eList, int hl, int hr) {return hint_range(sl.push(eList), hl, hr);}
    
        public ZDD_Node hint_range(SharedList<T>.SharedListNode eListNode, int hl, int hr) {
            if (hl == hr) return this.hint(eListNode, hr);
            if (hl == 0) return this.hint_le(eListNode, hr);
            if (hl > eListNode.size()) return zero_terminal; // この行より下では、eListNode != sl.terminal が保証される。 
            if (hl == eListNode.size()) return this.onset_hold_s(eListNode);
            if (hr >= eListNode.size()) return this.hint_ge(eListNode, hl);
            if (this == zero_terminal) return zero_terminal;
            if (this == one_terminal) return zero_terminal;
            Tuple keyTuple = new Tuple(HINT_RANGE_ID, this, eListNode, hl, hr);
            ZDD_Node valueNode;
            if (!memo_table.containsKey(keyTuple)) {
                if (idx.get(this.top) < idx.get(eListNode.top)) {
                    valueNode = get_node(this.top, this.zero.hint_range(eListNode, hl, hr), this.one.hint_range(eListNode, hl, hr));
                } else if (idx.get(this.top) > idx.get(eListNode.top)) {
                    valueNode = this.hint_range(eListNode.nxt, hl, hr);
                } else {
                    valueNode = get_node(this.top, this.zero.hint_range(eListNode.nxt, hl, hr), this.one.hint_range(eListNode.nxt, hl-1, hr-1));
                }
                memo_table.put(keyTuple, valueNode);
            }
            return memo_table.get(keyTuple);
        }
    }
}