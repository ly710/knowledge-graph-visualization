package knowledge.graph.visualization.controller;

import com.zeaho.util.jsonModel.JsonCode;
import com.zeaho.util.jsonModel.JsonResult;
import knowledge.graph.visualization.service.FileService;
import knowledge.graph.visualization.service.KGVisualizationJobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class DatasetUploader {
    private final KGVisualizationJobService kgVisualizationJobService;

    private final FileService fileService;

    @Autowired
    public DatasetUploader(KGVisualizationJobService kgVisualizationJobService, FileService fileService) {
        this.kgVisualizationJobService = kgVisualizationJobService;
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    public JsonResult rdfUploader(
            @RequestParam("name") String datasetName,
            @RequestParam("tuples") MultipartFile tuplesFile
    ) {
        String tupleFilename = datasetName + "." + "tuples.tsv";
        String tuplesLocation = fileService.storeFile(tuplesFile, tupleFilename);

        if(kgVisualizationJobService.layout(datasetName, tuplesLocation)) {
            return JsonResult.successResult();
        }

        return JsonResult.newJsonResult(JsonCode.OPERATE_FAILED);
    }
}
