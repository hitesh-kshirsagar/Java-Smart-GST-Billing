package com.gst.billing.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Serves Thymeleaf HTML pages for the frontend SPA-style navigation.
 */
@Controller
public class PageController {

    @GetMapping("/")
    public String index() { return "redirect:/login"; }

    @GetMapping("/login")
    public String login() { return "login"; }

    @GetMapping("/register")
    public String register() { return "register"; }

    @GetMapping("/dashboard")
    public String dashboard() { return "dashboard"; }

    @GetMapping("/products")
    public String products() { return "products"; }

    @GetMapping("/customers")
    public String customers() { return "customers"; }

    @GetMapping("/invoices")
    public String invoices() { return "invoices"; }

    @GetMapping("/reports")
    public String reports() { return "reports"; }
}
