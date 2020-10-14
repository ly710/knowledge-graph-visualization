package knowledge.graph.visualization.controller;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import knowledge.graph.visualization.util.XJson;
import okhttp3.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.RDFDataMgr;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.util.List;
@RestController
public class RdfFileReader {
    public void rdf2TsvFile(String datasetName, String rdfFilePath) throws FileNotFoundException, IOException {
        InputStream in = RDFDataMgr.open(rdfFilePath);
        Model model = ModelFactory.createDefaultModel();

        model.read(in, null);

        File fileOut = new File("/tmp/flink/input/" + datasetName + ".tsv");

        FileOutputStream fileOutputStream = new FileOutputStream(fileOut);
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream));

        for (Statement s : model.listStatements().toList()) {
            String str = s.getSubject().toString() + "\t" + s.getPredicate().getLocalName() + "\t" + s.getObject().toString();
            bufferedWriter.write(str);
            bufferedWriter.newLine();
        }

        bufferedWriter.close();
    }

    public String submitFlinkTask(String datasetName) throws IOException, SubmitFlinkTaskException {
        OkHttpClient client = new OkHttpClient();

        SubmitRequestParams submitRequestParams = new SubmitRequestParams();
        submitRequestParams.entryClass = "knowledge.graph.visualization.jobs.JobReceiver";
        submitRequestParams.programArgs = datasetName;
        RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8"), XJson.encodeJson(submitRequestParams));


        Request submitRequest = new Request.Builder()
                .url("http://localhost:8081/jars/021c66c1-bc4c-4621-a079-dcb357eb3b12_knowledge-graph-visualization-jobs-0.0.1.jar/run?entry-class=knowledge.graph.visualization.jobs.JobReceiver")
                .post(requestBody)
                .build();

        Call submitCall = client.newCall(submitRequest);
        Response submitResponse = submitCall.execute();

        if(submitResponse.body() == null) {
            throw new SubmitFlinkTaskException();
        }

        SubmitResult submitResult = XJson.decodeJson(submitResponse.body().string(), SubmitResult.class);

        if(submitResult == null || submitResult.jobid == null) {
            throw new SubmitFlinkTaskException();
        }

        return submitResult.jobid;
    }

    public static int range(int from, int to) {
        return to - from;
    }

    public static class SubmitRequestParams {
        public String entryClass;

        public Integer parallelism;

        public String programArgs;

        public String savepointPath;

        public Boolean allowNonRestoredState;
    }

    public static class SubmitResult {
        public String jobid;
    }

    @JsonNaming(PropertyNamingStrategy.KebabCaseStrategy.class)
    public static class ExecuteResult {
        public Status status;

        public JobExecutionResult jobExecutionResult;

        @JsonNaming(PropertyNamingStrategy.KebabCaseStrategy.class)
        public static class JobExecutionResult {
             public String applicationStatus;
        }

        public static class Status {
            public String id;
        }
    }

    public static class SubmitFlinkTaskException extends Exception {}

    public static class WaitForFlinkTaskEndingException extends  Exception {}

    public static class GraphVO {
        public List<NodeVO> nodes;

        public List<EdgeVO> edges;
    }

    public static class NodeVO {
        public String id;

        public Double x;

        public Double y;

        public String label;

        public Integer size = 12;
    }

    public static class EdgeVO {
        public String source;

        public String target;

        public String label;
    }

    public static class Block {
        public Integer x;

        public Integer y;

        public Block(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public Integer getX() {
            return x;
        }

        public void setX(Integer x) {
            this.x = x;
        }

        public Integer getY() {
            return y;
        }

        public void setY(Integer y) {
            this.y = y;
        }
    }
}