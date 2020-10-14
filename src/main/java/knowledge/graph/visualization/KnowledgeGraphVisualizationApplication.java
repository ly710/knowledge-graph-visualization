package knowledge.graph.visualization;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;

@SpringBootApplication(scanBasePackages = {"knowledge.graph.visualization"})
@EnableJpaRepositories(basePackages = "knowledge.graph.visualization")
@EntityScan(basePackages = "knowledge.graph.visualization.domain.model")
@MapperScan(
        basePackages = "knowledge.graph.visualization.*",
        sqlSessionTemplateRef = "sqlSessionTemplate",
        annotationClass = Repository.class
)

public class KnowledgeGraphVisualizationApplication {
    public static void main(String[] args) {
        SpringApplication.run(KnowledgeGraphVisualizationApplication.class, args);
    }
}
