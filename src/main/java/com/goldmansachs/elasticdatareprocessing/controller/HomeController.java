package com.goldmansachs.elasticdatareprocessing.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for serving the main application UI.
 */
@Controller
public final class HomeController {

    /**
     * Serves the main application page.
     *
     * @return the name of the view template to render
     */
    @GetMapping("/")
    public String home() {
        return "index";
    }
}
