package com.tomasburgaleta.exampleia.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Web controller for serving Thymeleaf templates
 * This controller handles the user interface for audio transcription
 */
@Controller
public class WebController {
    
    @GetMapping("/")
    public String index(Model model) {
        // Add any model attributes if needed for the template
        model.addAttribute("appName", "ExampleIA - Transcriptor de Audio");
        return "index";
    }
    
    @GetMapping("/transcription")
    public String transcription() {
        return "index";
    }
}