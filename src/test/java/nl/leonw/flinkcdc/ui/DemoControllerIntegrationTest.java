package nl.leonw.flinkcdc.ui;

import nl.leonw.flinkcdc.orders.db.Order;
import nl.leonw.flinkcdc.orders.db.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DemoControllerIntegrationTest {
    @Autowired
    private OrderRepository orderRepository;

    @Container
    @ServiceConnection
    private static final PostgreSQLContainer<?> POSTGRES_CONTAINER =
            new PostgreSQLContainer<>("postgres:17.2-alpine") // same as docker-compose
                    .withDatabaseName("testdb")
                    .withUsername("testuser")
                    .withPassword("testpass")
                    // Default:  this.waitStrategy = (new LogMessageWaitStrategy()).withRegEx(".*database system is ready to accept connections.*\\s").withTimes(2).withStartupTimeout(Duration.of(60L, ChronoUnit.SECONDS));
                    // but that triggers before the listening port is available :(
                    .waitingFor(Wait.forListeningPorts());

    @LocalServerPort
    private int port;

    @Test
    @DisplayName("GET an individual order")
    public void testGetOrderEndpoint() {
        var customerId = "some customer id";
        var order = createOrderInDb(customerId);

        var html = retrieve("http://localhost:" + port + "/orders/" + order.getId()).getBody();
        assertThat(html).contains(customerId);
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

    Order createOrderInDb(String customerId) {
        var order = new Order();
        order.setCustomerId(customerId);
        return orderRepository.save(order);
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