package ru.izebit.mobile.booking.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class NotFoundController implements ErrorController {
    @RequestMapping("/error")
    public String handleError(HttpServletResponse response) {
        return "redirect:/booking/phones";
    }
}
