package nl.leonw.flinkcdc.streamtransform.flink;

import nl.leonw.flinkcdc.streamtransform.CreateTargetTopicConfig;
import nl.leonw.flinkcdc.streamtransform.KafkaConfiguration;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class OrderCombiner {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderCombiner.class);

    private final StreamTableEnvironment tableEnvironment;
    private final KafkaConfiguration kafkaConfiguration;

    public OrderCombiner(
            StreamTableEnvironment tableEnvironment,
            KafkaConfiguration kafkaConfiguration,
            CreateTargetTopicConfig.PublicOrderTopicExistsConfirmation requirement
    ) {
        this.tableEnvironment = tableEnvironment;
        this.kafkaConfiguration = kafkaConfiguration;

        createSourceTables();
        createSinkTable();
        createAndExecuteJoinQuery();
    }

    private void createSourceTables() {
        // Create orders source table
        LOGGER.info("Creating source tables");
        tableEnvironment.executeSql(String.format("""
                        CREATE TABLE orders (
                            id STRING,
                            customerId STRING,
                            deliveryAddressId STRING,
                            totalPriceExVatCents BIGINT,
                            totalVatCents BIGINT,
                            createdDate TIMESTAMP(3),
                            lastModifiedDate TIMESTAMP(3),
                            PRIMARY KEY (id) NOT ENFORCED
                        ) WITH (
                            'scan.startup.mode' = '%s',
                            'connector' = 'kafka',
                            'topic' = '%s',
                            'properties.bootstrap.servers' = '%s',
                            'properties.group.id' = 'order-combiner-group',
                            'format' = 'debezium-json',
                            'debezium-json.schema-include' = 'true'
                        )
                        """,
                kafkaConfiguration.getOffset(),
                kafkaConfiguration.getOrdersTopicName(),
                kafkaConfiguration.getBootstrapServers())
        );

        // Create order items source table
        tableEnvironment.executeSql(String.format("""
                        CREATE TABLE orderitems (
                            id STRING,
                            productId STRING,
                            quantity INT,
                            pricePerItemExVatCents BIGINT,
                            vatPerItemCents BIGINT,
                            createdDate TIMESTAMP(3),
                            lastModifiedDate TIMESTAMP(3),
                            order_id STRING,
                            PRIMARY KEY (id) NOT ENFORCED
                        ) WITH (
                            'scan.startup.mode' = '%s',
                            'connector' = 'kafka',
                            'topic' = '%s',
                            'properties.bootstrap.servers' = '%s',
                            'properties.group.id' = 'order-combiner-group',
                            'format' = 'debezium-json',
                            'debezium-json.schema-include' = 'true'
                        )
                        """,
                kafkaConfiguration.getOffset(),
                kafkaConfiguration.getOrderItemsTopicName(),
                kafkaConfiguration.getBootstrapServers())
        );
        LOGGER.info("Source tables created");
    }

    private void createSinkTable() {
        LOGGER.info("Creating sink table");

        tableEnvironment.executeSql(String.format("""
                        CREATE TABLE public_orders (
                            order_id STRING NOT NULL,
                            customer_id STRING,
                            delivery_address_id STRING,
                            total_price_ex_vat_cents BIGINT,
                            total_vat_cents BIGINT,
                            items ARRAY<ROW(
                                id STRING,
                                product_id STRING,
                                quantity INT,
                                price_per_item_ex_vat_cents BIGINT,
                                vat_per_item_cents BIGINT
                            )>,
                            created_date TIMESTAMP(3),
                            last_modified_date TIMESTAMP(3),
                            PRIMARY KEY (order_id) NOT ENFORCED
                        ) WITH (
                            'connector' = 'upsert-kafka',
                            'topic' = '%s',
                            'properties.bootstrap.servers' = '%s',
                            'key.format' = 'json',
                            'value.format' = 'json'
                        )
                        """,
                kafkaConfiguration.getPublicOrdersTopicName(),
                kafkaConfiguration.getBootstrapServers())
        );

        LOGGER.info("Sink table created");
    }

    private void createAndExecuteJoinQuery() {
        LOGGER.info("Configuring join query");
        tableEnvironment.executeSql(String.format("""
                        INSERT INTO public_orders
                        SELECT
                            o.id as order_id,
                            o.customerId as customer_id,
                            o.deliveryAddressId as delivery_address_id,
                            o.totalPriceExVatCents as total_price_ex_vat_cents,
                            o.totalVatCents as total_vat_cents,
                            ARRAY_AGG(
                                ROW(
                                    i.id,
                                    i.productId,
                                    i.quantity,
                                    i.pricePerItemExVatCents,
                                    i.vatPerItemCents
                                )
                            ) as items,
                            o.createdDate as created_date,
                            o.lastModifiedDate as last_modified_date
                        FROM orders o
                        LEFT JOIN orderitems i ON o.id = i.order_id
                        GROUP BY
                            o.id,
                            o.customerId,
                            o.deliveryAddressId,
                            o.totalPriceExVatCents,
                            o.totalVatCents,
                            o.createdDate,
                            o.lastModifiedDate
                        """
                )
        );
    }
}
