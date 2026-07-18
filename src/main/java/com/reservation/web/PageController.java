package com.reservation.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping({"/restaurant", "/restaurant/"})
    public String restaurant() {
        return "forward:/restaurant/index.html";
    }

    @GetMapping({"/booking", "/booking/"})
    public String booking() {
        return "forward:/booking/index.html";
    }
}
