package knowledge.graph.visualization.service;

import com.nextbreakpoint.flinkclient.api.ApiException;
import com.nextbreakpoint.flinkclient.api.FlinkApi;
import com.nextbreakpoint.flinkclient.model.JarRunResponseBody;
import com.nextbreakpoint.flinkclient.model.JobDetailsInfo;
import knowledge.graph.visualization.config.DBConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KGVisualizationJobService {
    private final FlinkApi flinkApi;

    @Value("${kg.visualization.jar-id}")
    private String jarId;

    private final DBConfig dbConfig;

    public KGVisualizationJobService(
            FlinkApi flinkApi,
            DBConfig dbConfig
    ) {
        this.flinkApi = flinkApi;
        this.dbConfig = dbConfig;
    }

    public boolean layout(String datasetName, String path) {
        String programArgs = datasetName + "," + path + "," + dbConfig.getUrl() + "," + dbConfig.getUsername() + "," + dbConfig.getPassword();

        try {
            JarRunResponseBody response = flinkApi.runJar(
                    jarId,
                    true,
                    null,
                    null,
                    programArgs,
                    "knowledge.graph.visualization.jobs.LayoutJob",
                    null
            );

            return checkJobSuccess(response.getJobid());
        } catch (ApiException e) {
            log.error("run jar faild", e);
            return false;
        }
    }

    private boolean checkJobSuccess(String jobId) {
        try {
            JobDetailsInfo details = flinkApi.getJobDetails(jobId);

            while (
                    details.getState().equals(JobDetailsInfo.StateEnum.CREATED) ||
                            details.getState().equals(JobDetailsInfo.StateEnum.RUNNING)

            ) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    log.error("interrupted while sleep", e);
                }
                details = flinkApi.getJobDetails(jobId);
            }

            return details.getState().equals(JobDetailsInfo.StateEnum.FINISHED);
        } catch (ApiException e) {
            log.error("get job detail failed", e);
            return false;
        }
    }
}
