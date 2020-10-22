package knowledge.graph.visualization.controller;

import com.zeaho.util.jsonModel.JsonCode;
import com.zeaho.util.jsonModel.JsonResult;
import knowledge.graph.visualization.domain.model.Meta;
import knowledge.graph.visualization.domain.model.Predict;
import knowledge.graph.visualization.domain.repo.EdgeRepo;
import knowledge.graph.visualization.domain.repo.MetaRepo;
import knowledge.graph.visualization.domain.repo.PredictRepo;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.ArrayList;
import java.util.List;

@RestController
public class GraphController {
    private final EdgeRepo edgeRepo;

    private final MetaRepo metaRepo;

    private final PredictRepo predictRepo;

    @Autowired
    public GraphController(
            EdgeRepo edgeRepo,
            MetaRepo metaRepo,
            PredictRepo predictRepo
    ) {
        this.edgeRepo = edgeRepo;
        this.metaRepo = metaRepo;
        this.predictRepo = predictRepo;
    }

    @GetMapping("/graph")
    public GraphVO getGraph(
            String datasetName,
            String leftTopX,
            String leftTopY,
            String rightBottomX,
            String rightBottomY
    ) {
        List<knowledge.graph.visualization.domain.model.Edge> edges = edgeRepo.getEdges(datasetName, leftTopX, leftTopY, rightBottomX, rightBottomY);

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

    @GetMapping("/meta-info")
    public JsonResult getMetaInfo(@RequestParam String datasetName) {
        Meta meta = metaRepo.getMeta(datasetName);
        return JsonResult.successResult(meta);
    }

    @GetMapping("/predict")
    public JsonResult getPredicts(@RequestParam String datasetName) {
        List<Predict> predicts = predictRepo.getPredicts(datasetName);
        for(Predict predict : predicts) {
            predict.setName(StringEscapeUtils.escapeHtml4(predict.getName()));
        }
        return JsonResult.successResult(predicts);
    }

    @GetMapping("/predict/{id}/hide")
    public JsonResult hidePredicts(@PathVariable Long id, @RequestParam Integer keep) {
        try {
            predictRepo.operatePredicts(id, keep);
            return JsonResult.successResult();
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.newJsonResult(JsonCode.OPERATE_FAILED);
        }
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

    /**
     * CREATE TABLE `meta` (
     *   `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
     *   `dataset` char(64) DEFAULT NULL,
     *   `width` int(10) unsigned NOT NULL,
     *   `height` int(10) unsigned NOT NULL,
     *   `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
     *   `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
     *   PRIMARY KEY (`id`),
     *   KEY `dataset` (`dataset`)
     * ) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC
     */

    /**
     * CREATE TABLE `edge` (
     *   `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
     *   `dataset` char(64) DEFAULT NULL,
     *   `source_id` bigint(20) unsigned NOT NULL,
     *   `target_id` bigint(20) unsigned NOT NULL,
     *   `source_label` char(64) DEFAULT NULL,
     *   `target_label` char(64) DEFAULT NULL,
     *   `source_size` double NOT NULL,
     *   `target_size` double NOT NULL,
     *   `edge_label` char(64) DEFAULT NULL,
     *   `position` linestring NOT NULL,
     *   `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
     *   `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
     *   PRIMARY KEY (`id`),
     *   KEY `dataset` (`dataset`),
     *   SPATIAL KEY `position` (`position`)
     * ) ENGINE=InnoDB AUTO_INCREMENT=23158 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC
     */

    /**
     * CREATE TABLE `predict` (
     *   `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
     *   `dataset` char(64) DEFAULT NULL,
     *   `name` char(64) DEFAULT NULL,
     *   `count` int(10) NOT NULL DEFAULT '0',
     *   `keep` tinyint(3) DEFAULT '1',
     *   `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
     *   `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
     *   PRIMARY KEY (`id`),
     *   KEY `dataset` (`dataset`)
     * ) ENGINE=InnoDB AUTO_INCREMENT=265 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC
     */
}
