package nl.leonw.flinkcdc.streamtransform.flink;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.springframework.context.annotation.Bean;

@org.springframework.context.annotation.Configuration
public class FlinkConfiguration {

    @Bean
    public StreamExecutionEnvironment streamExecutionEnvironment() {
        Configuration configuration = new Configuration();
        return StreamExecutionEnvironment.getExecutionEnvironment(configuration);
    }

    @Bean
    public StreamTableEnvironment streamTableEnvironment(StreamExecutionEnvironment env) {
        return StreamTableEnvironment.create(
                env,
                EnvironmentSettings.newInstance().build()
        );
    }
}
