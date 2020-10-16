package knowledge.graph.visualization.domain.repo;

import knowledge.graph.visualization.domain.model.Edge;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EdgeRepo {
    List<Edge> getEdges(String dataset, String leftTopX, String leftTopY, String rightBottomX, String rightBottomY);
}
