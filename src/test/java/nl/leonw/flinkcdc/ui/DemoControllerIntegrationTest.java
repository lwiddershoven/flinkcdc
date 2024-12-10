package nl.leonw.flinkcdc.ui;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.util.UUID;

import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DemoControllerIntegrationTest {
    static final UUID ORDER_ID = UUID.fromString("2cf5181c-5185-4da1-ba2c-f0d8f334679c");

    @Container
    private static final PostgreSQLContainer<?> POSTGRES_CONTAINER =
            new PostgreSQLContainer<>("postgres:17.2-alpine") // same as docker-compose
                    .withDatabaseName("testdb")
                    .withUsername("testuser")
                    .withPassword("testpass");

    @LocalServerPort
    private int port;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public DataSource dataSource() {
            try {
                // While postgres is running it's not yet really really ready - maybe the port forward is not set up?
                // TODO poll in a more robust way, probably the log message followed by host port wait strategy. No prio though
                sleep(1000L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            return DataSourceBuilder.create()
                    .url(POSTGRES_CONTAINER.getJdbcUrl())
                    .username(POSTGRES_CONTAINER.getUsername())
                    .password(POSTGRES_CONTAINER.getPassword())
                    .build();
        }
    }

    @Test
    @DisplayName("Cannot GET an individual order")
    public void testGetOrderEndpoint() {
        assertThrows(
                HttpClientErrorException.MethodNotAllowed.class,
                () -> retrieve("http://localhost:" + port + "/orders/" + ORDER_ID)
        );
    }

    @Test
    @DisplayName("Get the main page with all orders")
    public void getMainPage() {
        var response = retrieve("http://localhost:" + port + "/");

        assertEquals(HttpStatus.OK, response.getStatusCode());

        var body = response.getBody();
        assertNotNull(body);
        assertTrue(body.contains("<title>Demo: Order Service</title>"));
    }


    private ResponseEntity<String> retrieve(String url) {
        var restClient = RestClient.create();
        return restClient
                .get()
                .uri(url)
                .retrieve()
                // If you don't like exceptions or are migrating RestTemplate you can do the following and
                // check status codes instead of catch exceptions
                // .onStatus(HttpStatusCode::isError, (request, response) -> response.getBody())
                .toEntity(String.class);
    }
}