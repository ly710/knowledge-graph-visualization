package knowledge.graph.visualization;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"knowledge.graph.visualization"})
public class KnowledgeGraphVisualizationApplication {
    public static void main(String[] args) {
        SpringApplication.run(KnowledgeGraphVisualizationApplication.class, args);
    }
}
