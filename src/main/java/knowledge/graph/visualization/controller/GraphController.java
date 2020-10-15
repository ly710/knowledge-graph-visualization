package knowledge.graph.visualization.controller;

import knowledge.graph.visualization.domain.repo.EdgeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.ArrayList;
import java.util.List;

@RestController
public class GraphController {
    private final EdgeRepo edgeRepo;

    @Autowired
    public GraphController(EdgeRepo edgeRepo) {
        this.edgeRepo = edgeRepo;
    }

    @GetMapping("/graph")
    public GraphVO getGraph(
            String datasetName,
            Integer minimap,
            String leftTopX,
            String leftTopY,
            String rightBottomX,
            String rightBottomY
    ) throws Exception {
        List<knowledge.graph.visualization.domain.model.Edge> edges = edgeRepo.getEdges(minimap, leftTopX, leftTopY, rightBottomX, rightBottomY);

        List<NodeVO> nodeVOS = new ArrayList<>();
        List<EdgeVO> edgeVOS = new ArrayList<>();
        for(knowledge.graph.visualization.domain.model.Edge edge : edges) {
            nodeVOS.add(new NodeVO(
                    String.valueOf(edge.getSourceId()),
                    edge.getSourceX() - Double.parseDouble(leftTopX),
                    edge.getSourceY() - Double.parseDouble(leftTopY),
                    edge.getSourceLabel(),
                    edge.getSourceSize()
            ));

            nodeVOS.add(new NodeVO(
                    String.valueOf(edge.getTargetId()),
                    edge.getTargetX() - Double.parseDouble(leftTopX),
                    edge.getTargetY() - Double.parseDouble(leftTopY),
                    edge.getTargetLabel(),
                    edge.getTargetSize()
            ));

            edgeVOS.add(new EdgeVO(
                    String.valueOf(edge.getSourceId()),
                    String.valueOf(edge.getTargetId()),
                    edge.getEdgeLabel()
            ));
        }

        return new GraphVO(nodeVOS, edgeVOS);
    }

    public static class GraphVO {
        public List<NodeVO> nodes;

        public List<EdgeVO> edges;

        public GraphVO(List<NodeVO> nodeVOS, List<EdgeVO> edgeVOS) {
            this.nodes = nodeVOS;
            this.edges = edgeVOS;
        }
    }

    public static class NodeVO {
        public String id;

        public Double x;

        public Double y;

        public String label;

        public Double size;

        public NodeVO(String id, double x, double y, String label, double size) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.label = label;
            this.size = size;
        }
    }

    public static class EdgeVO {
        public String source;

        public String target;

        public String label;

        public EdgeVO(String source, String target, String label) {
            this.source = source;
            this.target =target;
            this.label = label;
        }
    }
}
