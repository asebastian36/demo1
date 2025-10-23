package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ElitistLoadingController {

    @GetMapping("/loading-elitist")
    public String loadingPage(@RequestParam String sessionId, Model model) {
        model.addAttribute("sessionId", sessionId);
        return "loading-elitist";
    }
}