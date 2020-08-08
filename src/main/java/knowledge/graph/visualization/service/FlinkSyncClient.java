package knowledge.graph.visualization.service;

import knowledge.graph.visualization.controller.RdfFileReader;
import knowledge.graph.visualization.util.XJson;
import okhttp3.*;

import java.io.IOException;

public class FlinkSyncClient {
    private String entryClass;

    private String jarId;

    private String host;

    private Integer port;

    public FlinkSyncClient(
            String entryClass,
            String jarId,
            String host,
            Integer port
    ) {
        this.entryClass = entryClass;
        this.jarId = jarId;
        this.host = host;
        this.port = port;
    }

    public void execute(String task, String dataset)
            throws IOException, InterruptedException, SubmitFlinkTaskException, WaitForFlinkTaskEndingException {
        String jobId = submitJob(task + " " + dataset);
        waitForTaskDone(jobId);
    }

    public String submitJob(String programArgs) throws IOException, SubmitFlinkTaskException {
        OkHttpClient client = new OkHttpClient();

        RdfFileReader.SubmitRequestParams submitRequestParams = new RdfFileReader.SubmitRequestParams();
        submitRequestParams.entryClass = entryClass;
        submitRequestParams.programArgs = programArgs;
        RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8"), XJson.encodeJson(submitRequestParams));


        Request submitRequest = new Request.Builder()
                .url("http://" + host + ":" + port + "/jars/" + jarId + "/run?entry-class=" + entryClass)
                .post(requestBody)
                .build();

        Call submitCall = client.newCall(submitRequest);
        Response submitResponse = submitCall.execute();

        if(submitResponse.body() == null) {
            throw new SubmitFlinkTaskException();
        }

        RdfFileReader.SubmitResult submitResult = XJson.decodeJson(submitResponse.body().string(), RdfFileReader.SubmitResult.class);

        if(submitResult == null || submitResult.jobid == null) {
            throw new SubmitFlinkTaskException();
        }

        return submitResult.jobid;
    }

    public void waitForTaskDone(String jobid) throws IOException, InterruptedException, WaitForFlinkTaskEndingException {
        OkHttpClient client = new OkHttpClient();

        Request executeRequest;
        Call executeCall;
        Response executeResponse;
        RdfFileReader.ExecuteResult executeResult;

        do {
            Thread.sleep(1000);
            executeRequest = new Request.Builder()
                    .url("http://" + host + ":" + port + "/jobs/" + jobid + "/execution-result")
                    .build();

            executeCall = client.newCall(executeRequest);
            executeResponse = executeCall.execute();

            if(executeResponse.body() == null) {
                throw new WaitForFlinkTaskEndingException();
            }

            executeResult = XJson.decodeJson(executeResponse.body().string(), RdfFileReader.ExecuteResult.class);

            if(executeResult == null || executeResult.status == null || executeResult.status.id == null) {
                throw new WaitForFlinkTaskEndingException();
            }

        } while(!executeResult.status.id.equals("COMPLETED"));

        if(executeResult.jobExecutionResult == null) {
            throw new WaitForFlinkTaskEndingException();
        }
    }

    public static class SubmitFlinkTaskException extends Exception {}

    public static class WaitForFlinkTaskEndingException extends Exception {}
}
