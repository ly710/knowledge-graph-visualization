package knowledge.graph.visualization.domain.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class Predict extends AbstractModel {
    private String dataset;

    private String name;

    private Long count;

    private Integer keep;
}
