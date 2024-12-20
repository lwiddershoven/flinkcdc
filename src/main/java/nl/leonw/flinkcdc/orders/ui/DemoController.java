package nl.leonw.flinkcdc.orders.ui;

import lombok.AllArgsConstructor;
import nl.leonw.flinkcdc.orders.db.Order;
import nl.leonw.flinkcdc.orders.db.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Set;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(DemoController.class);

    private OrderRepository orderRepository;

    @GetMapping("/")
    public String home(Model model) {
        var orders = orderRepository.findAllByOrderByIdAsc();
        model.addAttribute("orders", orders);
        return "index";
    }

    @GetMapping("/orders/{order-id}/edit")
    public String editOrder(Model model, @PathVariable("order-id") UUID orderId) {
        var order = orderRepository.findById(orderId).orElseThrow();
        model.addAttribute("order", order);
        return "fragments/dbitems :: edit_order_details";
    }

    @GetMapping("/orders/{order-id}")
    public String getOrder(Model model, @PathVariable("order-id") UUID orderId) {
        var order = orderRepository.findById(orderId).orElseThrow();
        model.addAttribute("order", order);
        return "fragments/dbitems :: order_details";
    }

    @PostMapping("/orders/{order-id}")
    public String saveOrder(
            @ModelAttribute("order")  final Order orderDTO,
            Model model,
            @PathVariable("order-id") UUID orderId
    ) {
        LOGGER.info("Saving order {}, model {}, dto {}", orderId, model, orderDTO);
        var order = orderRepository.findById(orderId).orElseThrow();
        model.addAttribute("order", order);
        return "fragments/dbitems :: order_details";
    }

    @DeleteMapping("/orders/{order-id}")
    @ResponseBody
    public String deleteOrder(@PathVariable("order-id") UUID orderId) {
        orderRepository.deleteById(orderId);
        return ""; // replacement html for deleted order
    }

    @GetMapping("/orders/{order-id}/items/{item-id}/edit")
    public String editItem(Model model, @PathVariable("order-id") UUID orderId, @PathVariable("item-id") UUID itemId) {
        var order = orderRepository.findById(orderId).orElseThrow();
        var item = order.getItems().stream()
                .filter(x -> x.getId().equals(itemId))
                .findFirst()
                .orElseThrow();
        order.getItems().retainAll(Set.of(item)); // TODO make UI objects instead of manipulating DB objects.
        model.addAttribute("order", order);
        model.addAttribute("item", item);

        return "fragments/dbitems :: orderitem_rows";
    }



    @DeleteMapping("/orders/{order-id}/items/{item-id}")
    @ResponseBody
    public String deleteItem(@PathVariable("order-id") UUID orderId, @PathVariable("item-id") UUID itemId) {
        orderRepository.findById(orderId)
                .ifPresent(order -> {
                    order.getItems().removeIf(x -> x.getId().equals(itemId));
                    orderRepository.save(order);
                });
        return "";
    }


    @PostMapping("/clicked")
    public String clicked(Model model) {
        model.addAttribute("now", LocalDateTime.now().toString());
        return "clicked :: result";
    }
}
