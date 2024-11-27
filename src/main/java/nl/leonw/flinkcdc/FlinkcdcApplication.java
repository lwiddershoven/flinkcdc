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

@EnableJpaAuditing
@SpringBootApplication
public class FlinkcdcApplication {

    @Autowired
    private OrderRepository orderRepository;

    public static void main(String[] args) {
        SpringApplication.run(FlinkcdcApplication.class, args);
    }

    @Bean
    CommandLineRunner someInitialData() {
        return args -> {
            orderRepository.deleteAll();
            var order = new Order();
            order.setTotalPriceExVatCents(101);
            var item = new OrderItem();
            item.setProductId("product1");
            var items = new HashSet<OrderItem>();
            items.add(item);
            order.setItems(items);
            orderRepository.save(order);
            Thread.sleep(1000);
            var someOrder = orderRepository.findAll().iterator().next();
            someOrder.setTotalPriceExVatCents(102);
            orderRepository.save(someOrder);
        };
    }
}
