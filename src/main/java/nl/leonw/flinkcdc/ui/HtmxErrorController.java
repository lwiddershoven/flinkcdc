package nl.leonw.flinkcdc.ui;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class HtmxErrorController {

    private final BasicErrorController basicErrorController;

    public HtmxErrorController(final BasicErrorController basicErrorController) {
        this.basicErrorController = basicErrorController;
    }

    @RequestMapping(value = "${server.error.path:${error.path:/error}}", headers = "HX-Request=true")
    @ResponseStatus(HttpStatus.OK)
    public ModelAndView errorHtmx(final HttpServletRequest request,
                                  final HttpServletResponse response) {
        return basicErrorController.errorHtml(request, response);
    }

}