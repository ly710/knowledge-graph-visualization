package knowledge.graph.visualization.config;

import com.nextbreakpoint.flinkclient.api.FlinkApi;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "flink.cluster.api")
@Data
public class FlinkClusterApiConfig {
    private String schema;

    private String host;

    private String port;

    private Long connectionTimeout;

    private Long writeTimeout;

    private Long readTimeout;

    @Bean
    public FlinkApi getFlinkApi() {
        FlinkApi flinkApi = new FlinkApi();
        flinkApi.getApiClient().setBasePath(getSchema() + "://" + getHost() + ":" + getPort());
        flinkApi.getApiClient().getHttpClient().setConnectTimeout(getConnectionTimeout(), TimeUnit.MILLISECONDS);
        flinkApi.getApiClient().getHttpClient().setWriteTimeout(getWriteTimeout(), TimeUnit.MILLISECONDS);
        flinkApi.getApiClient().getHttpClient().setReadTimeout(getReadTimeout(), TimeUnit.MILLISECONDS);

        return flinkApi;
    }
}
