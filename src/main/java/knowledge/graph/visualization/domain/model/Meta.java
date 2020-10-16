package knowledge.graph.visualization.domain.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class Meta extends AbstractModel {
    private String dataset;

    private Long width;

    private Long height;
}
