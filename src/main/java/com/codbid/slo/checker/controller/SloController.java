package com.codbid.slo.checker.controller;

import com.codbid.slo.checker.config.SloProperties;
import com.codbid.slo.checker.model.SloCheckResult;
import com.codbid.slo.checker.service.SloCheckerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/slo")
public class SloController {

    private final SloProperties properties;
    private final SloCheckerService checkerService;

    public SloController(SloProperties properties, SloCheckerService checkerService) {
        this.properties = properties;
        this.checkerService = checkerService;
    }

    @GetMapping("/rules")
    public List<SloProperties.RuleProperties> getRules() {
        return properties.getRules();
    }

    @GetMapping("/results")
    public List<SloCheckResult> results() {
        return checkerService.getLatestResults();
    }

    @GetMapping("/violations")
    public List<SloCheckResult> violations() {
        return checkerService.getLatestViolations();
    }

    @PostMapping("/check")
    public List<SloCheckResult> checkForce() {
        return checkerService.checkAll();
    }

    @GetMapping("/status")
    public Map<String, Object> status() {
        List<SloCheckResult> latestResults = checkerService.getLatestResults();
        List<SloCheckResult> latestViolations = checkerService.getLatestViolations();

        return Map.of(
                "service", "slo-checker",
                "status", latestViolations.isEmpty() ? "OK" : "VIOLATED",
                "rulesTotal", properties.getRules().size(),
                "resultsTotal", latestResults.size(),
                "violationsTotal", latestViolations.size(),
                "timestamp", Instant.now().toString()
        );
    }
}
