package knowledge.graph.visualization.controller;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import knowledge.graph.visualization.util.Edge;
import knowledge.graph.visualization.util.FruchtermanReingoldLayout;
import knowledge.graph.visualization.util.Node;
import knowledge.graph.visualization.util.XJson;
import okhttp3.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.RDFDataMgr;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@RestController
public class RdfFileReader {
    private List<Node> nodes = new ArrayList<>();

    private List<Edge> edges = new ArrayList<>();

    @GetMapping("/read-file")
    public GraphVO process() throws Exception {
        nodes = new ArrayList<>();
        edges = new ArrayList<>();
        String datasetName = "yago";
//        rdf2TsvFile(datasetName, "input/vc-db-1.rdf");

        String jobid = submitFlinkTask(datasetName);

        String applicationStatus = waitForTaskEnding(jobid);

        if(! applicationStatus.equals("SUCCEEDED")) {
            System.out.println("error");
            return null;
        }

        readIdGraph(datasetName);
        setNodes();

        FruchtermanReingoldLayout fruchtermanReingoldLayout = new FruchtermanReingoldLayout(3000, 3000, 500, 10, 0.55);
        fruchtermanReingoldLayout.run(nodes, edges);

        writeLayoutNodes(datasetName);

        return render();
    }

    @GetMapping("/graph/vo")
    public GraphVO getGraphVO(
            double leftTopX,
            double leftTopY,
            double rightBottomX,
            double rightBottomY
    ) throws Exception {
        System.out.println(leftTopX);
        System.out.println(leftTopY);
        System.out.println(rightBottomX);
        System.out.println(rightBottomY);
        System.out.println(getTitles(leftTopX, leftTopX, rightBottomX, rightBottomY));


        nodes = new ArrayList<>();
        edges = new ArrayList<>();
        List<String> nodeLines = new ArrayList<>();
        List<String> edgeLines = new ArrayList<>();

        for(String title : getTitles(leftTopX, leftTopY, rightBottomX, rightBottomY)) {
            try {
                nodeLines.addAll(readLines("/tmp/flink/output/yago-nodes-block/" + title));
                edgeLines.addAll(readLines("/tmp/flink/output/yago-edges-block/" + title));
            } catch (FileNotFoundException e) {
                continue;
            }
        }


        for(String str : nodeLines) {
            String[] split = str.split("\t");
            nodes.add(
                    new Node(
                            Long.valueOf(split[0]),
                            null,
                            Double.parseDouble(split[1]) - leftTopX,
                            Double.parseDouble(split[2]) - leftTopY
                    )
            );
        }

        for(String str : edgeLines) {
            String[] split = str.split("\t");
            edges.add(new Edge(Long.valueOf(split[0]), Long.valueOf(split[1]), 0L));
        }

        return render();
    }

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

    public String waitForTaskEnding(String jobid) throws IOException, InterruptedException, WaitForFlinkTaskEndingException {
        OkHttpClient client = new OkHttpClient();

        Request executeRequest;
        Call executeCall;
        Response executeResponse;
        ExecuteResult executeResult;

        do {
            Thread.sleep(1000);
            executeRequest = new Request.Builder()
                    .url("http://localhost:8081/jobs/" + jobid + "/execution-result")
                    .build();

            executeCall = client.newCall(executeRequest);
            executeResponse = executeCall.execute();

            if(executeResponse.body() == null) {
                throw new WaitForFlinkTaskEndingException();
            }

            executeResult = XJson.decodeJson(executeResponse.body().string(), ExecuteResult.class);

            if(executeResult == null || executeResult.status == null || executeResult.status.id == null) {
                throw new WaitForFlinkTaskEndingException();
            }

        } while(!executeResult.status.id.equals("COMPLETED"));

        if(executeResult.jobExecutionResult == null) {
            throw new WaitForFlinkTaskEndingException();
        }

        return executeResult.jobExecutionResult.applicationStatus;
    }

    public void readIdGraph(String datasetName) throws IOException {
        FileReader nodeFr = new FileReader("/tmp/flink/output/" +datasetName+ "-entities.tsv");
        FileReader edgeFr = new FileReader("/tmp/flink/output/" + datasetName + "-id-facts.tsv");
        BufferedReader nodeBf = new BufferedReader(nodeFr);
        BufferedReader edgeBf = new BufferedReader(edgeFr);

        String str;
        // 按行读取字符串
        Random random = new Random();
        while ((str = nodeBf.readLine()) != null) {
            String[] split = str.split("\t");
            nodes.add(
                    new Node(Long.valueOf(split[0]), null,null,null)
            );
        }

        while ((str = edgeBf.readLine()) != null) {
            String[] split = str.split("\t");
            edges.add(new Edge(Long.valueOf(split[0]), Long.valueOf(split[2]), Long.valueOf(split[1])));
        }

        nodeBf.close();
        nodeFr.close();
        edgeBf.close();
        edgeFr.close();
    }

    public void setNodes() {
        Random random = new Random();
        for(Node node : nodes) {
            node.setX(2000 * random.nextDouble());
            node.setY(2000 * random.nextDouble());
        }
    }

    public void writeLayoutNodes(String datasetName) throws FileNotFoundException, IOException {
        File fileOut = new File("/tmp/flink/output/" + datasetName + ".layout.nodes.tsv");

        FileOutputStream fileOutputStream = new FileOutputStream(fileOut);
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream));

        for (Node node : nodes) {
            String str = node.getId() + "\t" + node.getX() + "\t" + node.getY();
            bufferedWriter.write(str);
            bufferedWriter.newLine();
        }

        bufferedWriter.close();
    }

    public GraphVO render() {
        List<NodeVO> nodeVOS = new ArrayList<>();
        for(Node node : nodes) {
            NodeVO nodeVO = new NodeVO();
            nodeVO.id = node.getId().toString();
            nodeVO.x = node.getX();
            nodeVO.y = node.getY();
            nodeVOS.add(nodeVO);
        }

        List<EdgeVO> edgeVOS = new ArrayList<>();
        for(Edge edge : edges) {
            EdgeVO edgeVO = new EdgeVO();
            edgeVO.source = edge.getSourceId().toString();
            edgeVO.target = edge.getEndId().toString();
            edgeVOS.add(edgeVO);
        }

        GraphVO graphVO = new GraphVO();
        graphVO.nodes = nodeVOS;
        graphVO.edges = edgeVOS;

        return graphVO;
    }

    public static List<String> getTitles(double leftTopX, double leftTopY, double rightBottomX, double rightBottomY) {
        List<String> titles = new ArrayList<>();
        Block leftTopBlock = getBlock(leftTopX, leftTopY);
        Block rightBottomBlock = getBlock(rightBottomX, rightBottomY);

        for(int i = 0; i <= rightBottomBlock.y - leftTopBlock.y; i++) {
            for(int j = 0; j <= rightBottomBlock.x - leftTopBlock.x; j++) {
                titles.add(i + "-" + j + ".tsv");
            }
        }

        return titles;
    }

    public static Block getBlock(double x, double y) {
        int blockX = (int)(x / 500);
        int blockY = (int)(y / 500);

        return new Block(blockX, blockY);
    }

    public static List<String> readLines(String path) throws Exception {
        List<String> lines = new ArrayList<>();

        FileReader nodeFr = new FileReader(path);
        BufferedReader nodeBf = new BufferedReader(nodeFr);

        String str;
        // 按行读取字符串
        while ((str = nodeBf.readLine()) != null) {
            lines.add(str);
        }

        nodeBf.close();
        nodeFr.close();

        return lines;
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