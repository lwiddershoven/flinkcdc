package nl.leonw.flinkcdc.ui;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDateTime;

/**
 * Allows the user to do CRUD operations on the postgres database and
 * provides insight in what is on the various Kafka topics.
 * <p>
 * This is a demo tool.
 */
@Controller
public class DemoController {
    @GetMapping("/")
    public String home() {
        return "index";
    }

    @PostMapping("/clicked")
    public String clicked(Model model) {
        model.addAttribute("now", LocalDateTime.now().toString());
        return "clicked :: result";
    }
}
