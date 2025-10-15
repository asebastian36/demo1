package com.example.demo.controller;

// package com.example.demo.controller;

import com.example.demo.genetic.algorithm.ExecutionStatus;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class LoadingController {

    private final ExecutionStatus executionStatus;

    public LoadingController(ExecutionStatus executionStatus) {
        this.executionStatus = executionStatus;
    }

    @GetMapping("/loading")
    public String loadingPage(HttpSession session, Model model) {
        String sessionId = session.getId();
        model.addAttribute("sessionId", sessionId);
        return "loading";
    }

    @GetMapping("/api/execution-status")
    @ResponseBody
    public ExecutionProgress getExecutionStatus(@RequestParam String sessionId) {
        boolean completed = executionStatus.isCompleted(sessionId);
        int currentGen = executionStatus.getCurrentGeneration(sessionId);
        int totalGen = executionStatus.getTotalGenerations(sessionId);

        return new ExecutionProgress(completed, currentGen, totalGen);
    }

    public static class ExecutionProgress {
        private final boolean completed;
        private final int currentGeneration;
        private final int totalGenerations;

        public ExecutionProgress(boolean completed, int currentGeneration, int totalGenerations) {
            this.completed = completed;
            this.currentGeneration = currentGeneration;
            this.totalGenerations = totalGenerations;
        }

        // Getters
        public boolean isCompleted() { return completed; }
        public int getCurrentGeneration() { return currentGeneration; }
        public int getTotalGenerations() { return totalGenerations; }
    }
}
