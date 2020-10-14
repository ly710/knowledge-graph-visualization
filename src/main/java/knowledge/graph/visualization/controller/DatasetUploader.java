package knowledge.graph.visualization.controller;

import knowledge.graph.visualization.service.FileService;
import knowledge.graph.visualization.service.FlinkSyncClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

@RestController
public class DatasetUploader {
    private final FileService fileService;

    @Autowired
    public DatasetUploader(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    public Result<List<String>> rdfUploader(
            @RequestParam("name") String name,
            @RequestParam("tuples") MultipartFile tuplesFile,
            @Nullable  @RequestParam("schema") MultipartFile schemaFile
    ) {
        try {
            String tuplePrefix = name + "/" + "tuples.tsv";
            String tuplesLocation = fileService.storeFile(tuplesFile, tuplePrefix);

            String tuplesPath = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path(tuplesLocation)
                    .toUriString();

            List<String> list = new ArrayList<>();
            list.add(tuplesPath);

            if(!schemaFile.getOriginalFilename().isEmpty()) {
                String schemaPrefix = name + "/" + "schema";
                String schemaLocation = fileService.storeFile(schemaFile, schemaPrefix);

                String schemaPath = ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path(schemaLocation)
                        .toUriString();

                list.add(schemaPath);
            }

            FlinkSyncClient flinkSyncClient = new FlinkSyncClient(
                    "knowledge.graph.visualization.jobs.JobReceiver",
                    "ce81a9aa-fb9a-4432-bade-ce56fd298231_knowledge-graph-visualization-jobs-0.0.1.jar",
                    "localhost",
                    8081
            );

            flinkSyncClient.execute("to-graph", name);

            return new Result<>(0, "upload success", list);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result<>(1, "upload error", null);
        }
    }

    @GetMapping("/test1")
    public void test() throws Exception {
        FlinkSyncClient flinkSyncClient = new FlinkSyncClient(
                "knowledge.graph.visualization.jobs.JobReceiver",
                "948b0da7-6efa-45dc-ab11-6fe2aa391cf0_knowledge-graph-visualization-jobs-0.0.1.jar",
                "localhost",
                8081
        );

        flinkSyncClient.execute("layout", "yago-sample");
    }

    public Result<Void> schemaUploader() {
        return new Result<>();
    }
}
