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

    public String getDataset() {
        return dataset;
    }

    public void setDataset(String dataset) {
        this.dataset = dataset;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public Integer getKeep() {
        return keep;
    }

    public void setKeep(Integer keep) {
        this.keep = keep;
    }
}
