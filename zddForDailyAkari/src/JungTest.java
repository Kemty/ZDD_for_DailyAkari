import java.util.ArrayList;
import java.util.Arrays;

public class JungTest {
    public static void main(String[] args) {
        String[] items = {"a", "b", "c"};
        ArrayList<String> arrayList = new ArrayList<>(Arrays.asList(items));
        ZDD<String> zdd = new ZDD<>(arrayList);
        ZDD_Visualizer<String> visualizer = new ZDD_Visualizer<>(zdd);

        ZDD<String>.ZDD_Node n1 = zdd.get_node("c", zdd.zero_terminal, zdd.one_terminal);
        ZDD<String>.ZDD_Node n2 = zdd.get_node("b", n1, zdd.one_terminal);
        ZDD<String>.ZDD_Node n3 = zdd.get_node("a", n1, n2);
        visualizer.visualize(n3);

        // ZDD<String>.ZDD_Node n4 = n3.hint_range(Arrays.asList("a", "b"), 2, 2);
        // visualizer.visualize(n4);
    }
}
