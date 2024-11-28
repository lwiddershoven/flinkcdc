package nl.leonw.flinkcdc;

import nl.leonw.flinkcdc.orders.db.Order;
import nl.leonw.flinkcdc.orders.db.OrderItem;
import nl.leonw.flinkcdc.orders.db.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

@EnableJpaAuditing
@SpringBootApplication
public class FlinkcdcApplication {
    static final Random RANDOM = new Random();
    static final AtomicInteger GENERATOR = new AtomicInteger(1000);

    @Autowired
    private OrderRepository orderRepository;

    public static void main(String[] args) {
        SpringApplication.run(FlinkcdcApplication.class, args);
    }

    @Bean
    CommandLineRunner someInitialData() {
        return args -> {
            orderRepository.deleteAll();
            for (int i = 0; i < 3; i++) {
                orderRepository.save(createOrder());
            }
        };
    }

    static Order createOrder() {
        var nrOfItems = RANDOM.nextInt(4) + 1; // at least 1 item
        var items = new HashSet<OrderItem>();
        for (int i = 0; i < nrOfItems; i++) {
            items.add(createItem(RANDOM.nextInt(100_000)));
        }

        var totalPrice = items.stream()
                .mapToLong(item -> item.getQuantity() * item.getPricePerItemExVatCents())
                .sum();
        var totalVat = items.stream()
                .mapToLong(item -> item.getQuantity() * item.getVatPerItemCents())
                .sum();

        var order = new Order();
        order.setTotalPriceExVatCents(totalPrice);
        order.setTotalVatCents(totalVat);
        order.setDeliveryAddressId(String.valueOf(GENERATOR.incrementAndGet()));
        order.setCustomerId(String.valueOf(GENERATOR.incrementAndGet()));
        order.setItems(items);
        return order;
    }

    static OrderItem createItem(int priceExVat) {
        var item = new OrderItem();
        item.setProductId("product" + GENERATOR.incrementAndGet());
        item.setQuantity(2);
        item.setPricePerItemExVatCents(priceExVat);
        item.setVatPerItemCents((long) (item.getVatPerItemCents() * 0.2));
        return item;
    }
}
