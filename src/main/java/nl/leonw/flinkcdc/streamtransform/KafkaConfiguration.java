package nl.leonw.flinkcdc.streamtransform;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

// TODO : record

@Configuration
@ConfigurationProperties(prefix = "kafka")
public class KafkaConfiguration {
    private String bootstrapServers;
    private String ordersTopicName = "orders";
    private String orderItemsTopicName = "orderitems";
    private String publicOrdersTopicName = "public_orders";
    private String offset = "latest-offset";

    // Getters and setters
    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    public String getOrdersTopicName() {
        return ordersTopicName;
    }

    public void setOrdersTopicName(String ordersTopicName) {
        this.ordersTopicName = ordersTopicName;
    }

    public String getOrderItemsTopicName() {
        return orderItemsTopicName;
    }

    public void setOrderItemsTopicName(String orderItemsTopicName) {
        this.orderItemsTopicName = orderItemsTopicName;
    }

    public String getPublicOrdersTopicName() {
        return publicOrdersTopicName;
    }

    public void setPublicOrdersTopicName(String publicOrdersTopicName) {
        this.publicOrdersTopicName = publicOrdersTopicName;
    }

    public String getOffset() {
        return offset;
    }

    public void setOffset(String offset) {
        this.offset = offset;
    }

}
