package knowledge.graph.visualization.controller;

import knowledge.graph.visualization.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
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
            String tuplePrefix = name + "_" + "tuple";
            String tuplesLocation = fileService.storeFile(tuplesFile, tuplePrefix);

            String tuplesPath = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path(tuplesLocation)
                    .toUriString();

            List<String> list = new ArrayList<>();
            list.add(tuplesPath);

            if(schemaFile != null) {
                String schemaPrefix = name + "_" + "schema";
                String schemaLocation = fileService.storeFile(schemaFile, schemaPrefix);

                String schemaPath = ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path(schemaLocation)
                        .toUriString();

                list.add(schemaPath);
            }

            return new Result<>(0, "upload success", list);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result<>(1, "upload error", null);
        }
    }

    public Result<Void> schemaUploader() {
        return new Result<>();
    }
}
