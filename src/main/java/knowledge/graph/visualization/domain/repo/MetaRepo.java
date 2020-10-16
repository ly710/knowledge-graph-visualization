package knowledge.graph.visualization.domain.repo;

import knowledge.graph.visualization.domain.model.Meta;
import org.springframework.stereotype.Repository;

@Repository
public interface MetaRepo {
    Meta getMeta(String dataset);
}
