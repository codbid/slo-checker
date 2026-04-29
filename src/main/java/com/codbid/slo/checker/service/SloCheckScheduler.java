package com.codbid.slo.checker.service;

import com.codbid.slo.checker.model.SloCheckResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SloCheckScheduler {

    private static final Logger logger = LoggerFactory.getLogger(SloCheckScheduler.class);

    private final SloCheckerService checkerService;

    public SloCheckScheduler(SloCheckerService checkerService) {
        this.checkerService = checkerService;
    }

    @Scheduled(fixedRateString = "${slo.check-interval}")
    public void check() {
        List<SloCheckResult> results = checkerService.checkAll();
        List<SloCheckResult> violations = checkerService.getLatestViolations();

        logger.info(
                "SLO check completed. resultsTotal={}, violationsTotal={}",
                results.size(),
                violations.size()
        );
    }
}
