package nl.leonw.flinkcdc.ui;

import lombok.AllArgsConstructor;
import nl.leonw.flinkcdc.orders.db.Order;
import nl.leonw.flinkcdc.orders.db.OrderRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.UUID;

/**
 * Allows the user to do CRUD operations on the postgres database and
 * provides insight in what is on the various Kafka topics.
 * <p>
 * This is a demo tool.
 */
@Controller
@AllArgsConstructor
public class DemoController {

    private OrderRepository orderRepository;

    @GetMapping("/")
    public String home(Model model) {
        var orders = new HashSet<Order>();
        orderRepository.findAll().forEach(orders::add); // yuck. Mutability in forEach. Also, JPA entity to UI.
        model.addAttribute("orders", orders);
        return "index";
    }

    @GetMapping("/orders/{order-id}/edit")
    public String editOrder(Model model, @PathVariable("order-id") UUID orderId) {
        orderRepository.findById(orderId);
        model.addAttribute("now", LocalDateTime.now().toString());
        return "/fragments/dbitems :: order-row";
    }

    @GetMapping("/orders/{order-id}/items/{item-id}/edit")
    public String editItem(Model model, @PathVariable("order-id") UUID orderId, @PathVariable("item-id") UUID itemId) {
        model.addAttribute("now", LocalDateTime.now().toString());
        return "/fragments/dbitems :: item-row";
    }

    @PostMapping("/clicked")
    public String clicked(Model model) {
        model.addAttribute("now", LocalDateTime.now().toString());
        return "clicked :: result";
    }
}
