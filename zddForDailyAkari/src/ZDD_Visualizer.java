import javax.swing.JFrame;

import org.apache.commons.collections15.Transformer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Arc2D;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;

import java.util.ArrayDeque;
import java.util.HashSet;

public class ZDD_Visualizer<T> {

    public class Vertex{
        
        ZDD<T>.ZDD_Node node;
        String label;


        public Vertex(ZDD<T>.ZDD_Node node) {
            this.node = node;
            this.label = setLabel();
        }

        @Override
        public String toString() {
            return label;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (other instanceof ZDD_Visualizer<?>.Vertex o) {
                return this.node == o.node;
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return this.node.hashCode();
        }
        
        private String setLabel() {
            if (this.node == zdd.zero_terminal) return "⊥";
            if (this.node == zdd.one_terminal) return "T";
            return this.node.top.toString();
        }
    }

    public class Edge {

        Vertex vFrom;
        Vertex vTo;
        int label; // 0 or 1

        public Edge(Vertex vFrom, Vertex vTo, int label) {
            this.vFrom = vFrom;
            this.vTo = vTo;
            this.label = label;
        }
    }

    ZDD<T> zdd;

    public ZDD_Visualizer(ZDD<T> zdd) {
        this.zdd = zdd;
    }

    public void visualize(ZDD<T>.ZDD_Node root) {

        DirectedSparseMultigraph<Vertex, Edge> graph = new DirectedSparseMultigraph<>();
        Vertex vRoot = new Vertex(root);
        graph.addVertex(vRoot);

        HashSet<Vertex> foundNodes = new HashSet<>();
        ArrayDeque<Vertex> queue = new ArrayDeque<>();
        queue.add(vRoot);
        foundNodes.add(vRoot);

        while (!queue.isEmpty()) {
            Vertex vNode = queue.poll();
            if (vNode.node == zdd.zero_terminal || vNode.node == zdd.one_terminal) continue;

            Vertex vZero = new Vertex(vNode.node.zero);
            Vertex vOne = new Vertex(vNode.node.one);
            if (!foundNodes.contains(vZero)) {
                foundNodes.add(vZero);
                graph.addVertex(vZero);
                queue.add(vZero);
            }
            if (!foundNodes.contains(vOne)) {
                foundNodes.add(vOne);
                graph.addVertex(vOne);
                queue.add(vOne);
            }
            if (!graph.addEdge(new Edge(vNode, vZero, 0), vNode, vZero)) System.err.println("can't create");;
            if (!graph.addEdge(new Edge(vNode, vOne, 1), vNode, vOne)) System.err.println("can't create");;
        }

        Layout layout = new FRLayout(graph);
        BasicVisualizationServer visualizationServer = new BasicVisualizationServer(layout, new Dimension(800, 600));

        visualizationServer.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<>()); // toString() で得られる文字列を節点ラベルとして表示する
        visualizationServer.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR); // 節点ラベルを節点内に表示する

        // 節点ラベルのフォントの設定
        Transformer<Vertex, Font> vertexLabelFontTransformer = new Transformer<>() {
            public Font transform(Vertex vertex) {
                return new Font(visualizationServer.getFont().getFontName(), Font.BOLD, 24);
            }
        };
        visualizationServer.getRenderContext().setVertexFontTransformer(vertexLabelFontTransformer);

        // 節点の色の設定
        Transformer<Vertex, Paint> vertexColorTransformer = new Transformer<>() {
            public Paint transform(Vertex vertex) {
                if (vertex.node == zdd.zero_terminal) return new Color(0xff6666);
                if (vertex.node == zdd.one_terminal) return new Color(0x6666ff);
                return Color.WHITE;
            }
        };
        visualizationServer.getRenderContext().setVertexFillPaintTransformer(vertexColorTransformer);
        
        // 節点の形状の設定
        Transformer<Vertex, Shape> vertexShapeTransformer = new Transformer<>() {
            public Shape transform(Vertex vertex) {
                if (vertex.node == zdd.zero_terminal || vertex.node == zdd.one_terminal) return new Rectangle(-5,-5,30,40);
                return new Arc2D.Double(-7, -7, 30, 30, 0, 360, Arc2D.OPEN);
            }
        };
        visualizationServer.getRenderContext().setVertexShapeTransformer(vertexShapeTransformer);

        // 枝・矢頭の色の設定
        Transformer<Edge, Paint> edgeColorTransformer = new Transformer<>() {
            public Paint transform(Edge edge) {
                return edge.label == 0 ? Color.RED : Color.BLUE;
            }
        };
        visualizationServer.getRenderContext().setEdgeDrawPaintTransformer(edgeColorTransformer); // 枝の色
        visualizationServer.getRenderContext().setArrowDrawPaintTransformer(edgeColorTransformer); // 矢頭の枠の色
        visualizationServer.getRenderContext().setArrowFillPaintTransformer(edgeColorTransformer); // 矢頭の塗りつぶし色

        JFrame frame = new JFrame("ZDD_visualizer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(visualizationServer);
        frame.pack();
        frame.setVisible(true);
    }
}