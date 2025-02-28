package nl.leonw.flinkcdc.streamtransform.flink;

import jakarta.annotation.PostConstruct;
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
    private boolean debugOrderTable = true;

    public OrderCombiner(
            StreamTableEnvironment tableEnvironment,
            KafkaConfiguration kafkaConfiguration,
            CreateTargetTopicConfig.PublicOrderTopicExistsConfirmation requirement
    ) {
        this.tableEnvironment = tableEnvironment;
        this.kafkaConfiguration = kafkaConfiguration;
    }

    @PostConstruct
    public void needsToBeDoneWhenTheDebeziumTopicsExist() throws InterruptedException {
        // You need to have data in the database (startup with add-initial-data: true) and then wait
        // for debezium to sync. Easiest is to just create data and then restart this app.
        Thread.sleep(3_000);
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
                            customer_id STRING,
                            delivery_address_id STRING,
                            total_price_ex_vat_cents BIGINT,
                            total_vat_cents BIGINT,
                            created_date STRING,
                            last_modified_date STRING,
                            PRIMARY KEY (id) NOT ENFORCED
                        ) WITH (
                            'scan.startup.mode' = '%s',
                            'connector' = 'kafka',
                            'topic' = '%s',
                            'properties.bootstrap.servers' = '%s',
                            'properties.group.id' = 'order-combiner-group',
                            'format' = 'debezium-json',
                            'debezium-json.schema-include' = 'true',
                            'debezium-json.ignore-parse-errors' = 'true'
                        );
                        """,
                kafkaConfiguration.getOffset(),
                kafkaConfiguration.getOrdersTopicName(),
                kafkaConfiguration.getBootstrapServers())
        );

        debugOrderTableIfRequired();

        // Create order items source table
        tableEnvironment.executeSql(String.format("""
                        CREATE TABLE orderitems (
                            id STRING,
                            product_id STRING,
                            quantity INT,
                            price_per_item_ex_vat_cents BIGINT,
                            vat_per_item_cents BIGINT,
                            created_date STRING,
                            last_modified_date STRING,
                            order_id STRING,
                            PRIMARY KEY (id) NOT ENFORCED
                        ) WITH (
                            'scan.startup.mode' = '%s',
                            'connector' = 'kafka',
                            'topic' = '%s',
                            'properties.bootstrap.servers' = '%s',
                            'properties.group.id' = 'order-combiner-group',
                            'format' = 'debezium-json',
                            'debezium-json.schema-include' = 'true',
                            'debezium-json.ignore-parse-errors' = 'true'
                        )
                        """,
                kafkaConfiguration.getOffset(),
                kafkaConfiguration.getOrderItemsTopicName(),
                kafkaConfiguration.getBootstrapServers())
        );
        LOGGER.info("Source tables created");
    }

    private void debugOrderTableIfRequired() {
        if (debugOrderTable) {
            tableEnvironment.executeSql("""
                    CREATE TABLE log_sink (
                        id STRING,
                        customer_id STRING,
                        delivery_address_id STRING,
                        total_price_ex_vat_cents BIGINT,
                        total_vat_cents BIGINT,
                        created_date STRING,
                        last_modified_date STRING
                    ) WITH (
                        'connector' = 'print',
                        'print-identifier' = 'PARSED ORDER: '
                    );
                    """);
            tableEnvironment.executeSql("""
                    INSERT INTO log_sink
                    SELECT * FROM orders;
                    """);
        }
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
                            created_date STRING,
                            last_modified_date STRING,
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
                            o.customer_id as customer_id,
                            o.delivery_address_id as delivery_address_id,
                            o.total_price_ex_vat_cents as total_price_ex_vat_cents,
                            o.total_vat_cents as total_vat_cents,
                            ARRAY_AGG(
                                ROW(
                                    i.id,
                                    i.product_id,
                                    i.quantity,
                                    i.price_per_item_ex_vat_cents,
                                    i.vat_per_item_cents
                                )
                            ) as items,
                            o.created_date as created_date,
                            o.last_modified_date as last_modified_date
                        FROM orders o
                        LEFT JOIN orderitems i ON o.id = i.order_id
                        GROUP BY
                            o.id,
                            o.customer_id,
                            o.delivery_address_id,
                            o.total_price_ex_vat_cents,
                            o.total_vat_cents,
                            o.created_date,
                            o.last_modified_date
                        """
                )
        );
    }
}
