// src/main/java/com/example/demo/controller/ElitistStatusController.java
package com.example.demo.controller;

import com.example.demo.execution.elitist.model.ElitistExecutionContext;
import com.example.demo.execution.elitist.service.ElitistExecutionStatusService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ElitistStatusController {

    private final ElitistExecutionStatusService statusService;

    public ElitistStatusController(ElitistExecutionStatusService statusService) {
        this.statusService = statusService;
    }

    @GetMapping("/api/elitist-status")
    @ResponseBody
    public ElitistProgress getExecutionStatus(@RequestParam String sessionId) {
        ElitistExecutionContext context = statusService.getContext(sessionId);
        if (context == null) {
            return new ElitistProgress(0, 0, true);
        }
        return new ElitistProgress(
                context.getCompletedCombinations(),
                context.getTotalCombinations(),
                context.isCompleted()
        );
    }

    public static class ElitistProgress {
        private final int completed;
        private final int total;
        private final boolean completedAll;

        public ElitistProgress(int completed, int total, boolean completedAll) {
            this.completed = completed;
            this.total = total;
            this.completedAll = completedAll;
        }

        public int getCompleted() { return completed; }
        public int getTotal() { return total; }
        public boolean isCompletedAll() { return completedAll; }
    }
}