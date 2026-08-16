package org.example.aisprinboot.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RequestMapping("/api")
@RestController
public class Text {
    @GetMapping("/text")
    public String getText() {
        return "Hello, World!";
    }

}
