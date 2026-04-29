package com.codbid.slo.checker.model;

import java.time.Instant;
import java.util.Map;

public record SloCheckResult(
        String ruleName,
        Map<String, String> labels,
        SloMetricType metric,
        SloOperator operator,
        double threshold,
        Double actualValue,
        SloStatus status,
        String message,
        Instant checkedAt
) {
}
