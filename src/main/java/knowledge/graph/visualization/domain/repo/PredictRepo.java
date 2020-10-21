package knowledge.graph.visualization.domain.repo;

import knowledge.graph.visualization.domain.model.Predict;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PredictRepo {
    List<Predict> getPredicts(String dataset);

    void operatePredicts(long id, int keep);
}
