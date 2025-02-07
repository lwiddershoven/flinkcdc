package nl.leonw.flinkcdc.streamtransform;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.Collections;
import java.util.concurrent.ExecutionException;

@Configuration
public class CreateTargetTopicConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(CreateTargetTopicConfig.class);

    private static final int NUM_PARTITIONS = 1;
    private static final short REPLICATION_FACTOR = 1;

    @Bean
    public KafkaAdmin kafkaAdmin(KafkaConfiguration config) {
        return new KafkaAdmin(Collections.singletonMap("bootstrap.servers", config.getBootstrapServers()));
    }

    @Bean
    public NewTopic newTopic(KafkaConfiguration config) {
        return new NewTopic(config.getPublicOrdersTopicName(), NUM_PARTITIONS, REPLICATION_FACTOR);
    }

    @Bean
    public PublicOrderTopicExistsConfirmation createTopicIfNotExists(KafkaAdmin kafkaAdmin, NewTopic newTopic) {
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            if (!adminClient.listTopics().names().get().contains(newTopic.name())) {
                adminClient.createTopics(Collections.singleton(newTopic)).all().get();
                LOGGER.info("Topic created: {}", newTopic.name());
            } else {
                LOGGER.info("Topic already exists: {}", newTopic.name());
            }
            return new PublicOrderTopicExistsConfirmation();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to create Kafka topic", e);
        }
    }

    /**
     * Flows that require the public order topic to be there can
     * depend on this.
     */
    public record PublicOrderTopicExistsConfirmation() {}
}
