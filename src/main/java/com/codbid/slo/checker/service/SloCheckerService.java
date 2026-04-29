package com.codbid.slo.checker.service;

import com.codbid.slo.checker.config.SloProperties;
import com.codbid.slo.checker.model.SloCheckResult;
import com.codbid.slo.checker.model.SloOperator;
import com.codbid.slo.checker.model.SloStatus;
import com.codbid.slo.checker.prometheus.PrometheusClient;
import com.codbid.slo.checker.prometheus.PrometheusSample;
import com.codbid.slo.checker.store.SloCheckResultStore;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SloCheckerService {

    private final PrometheusClient client;
    private final PromQlBuilder promQlBuilder;
    private final SloCheckResultStore resultStore;
    private final SloProperties properties;

    public SloCheckerService(
            SloProperties properties,
            PrometheusClient client,
            PromQlBuilder promQlBuilder,
            SloCheckResultStore resultStore
    ) {
        this.properties = properties;
        this.client = client;
        this.promQlBuilder = promQlBuilder;
        this.resultStore = resultStore;
    }

    public List<SloCheckResult> checkRule(SloProperties.RuleProperties rule) {
        Instant checkedAt = Instant.now();
        String promQl = promQlBuilder.build(rule);

        try {
            List<PrometheusSample> samples = client.queryVector(promQl);

            if (samples.isEmpty()) {
                return List.of(noDataResult(rule, promQl, checkedAt));
            }

            return samples.stream()
                    .map(sample -> checkSample(rule, sample, checkedAt))
                    .toList();
        } catch (Exception e) {
            return List.of(errorResult(rule, promQl, e, checkedAt));
        }
    }

    public List<SloCheckResult> checkAll() {
        List<SloCheckResult> results = new ArrayList<>();

        for (SloProperties.RuleProperties rule : properties.getRules()) {
            results.addAll(checkRule(rule));
        }

        resultStore.saveLatest(results);

        return results;
    }

    public List<SloCheckResult> getLatestResults() {
        return resultStore.findLatest();
    }

    public List<SloCheckResult> getLatestViolations() {
        return resultStore.findLatestViolations();
    }

    private SloCheckResult checkSample(SloProperties.RuleProperties rule, PrometheusSample sample, Instant checkedAt) {
        boolean ok = compare(sample.value(), rule.getThreshold(), rule.getOperator());

        return new SloCheckResult(
                rule.getName(),
                businessLabels(sample.labels()),
                rule.getMetric(),
                rule.getOperator(),
                rule.getThreshold(),
                sample.value(),
                ok ? SloStatus.OK : SloStatus.VIOLATED,
                ok ? "SLO is satisfied" : "SLO violation detected",
                checkedAt
        );
    }

    private SloCheckResult noDataResult(SloProperties.RuleProperties rule, String promQl, Instant checkedAt) {
        return new SloCheckResult(
                rule.getName(),
                Map.of(),
                rule.getMetric(),
                rule.getOperator(),
                rule.getThreshold(),
                null,
                SloStatus.NO_DATA,
                "No data available for query: " + promQl,
                checkedAt
        );
    }

    private SloCheckResult errorResult(
            SloProperties.RuleProperties rule,
            String promQl,
            Exception exception,
            Instant checkedAt
    ) {
        return new SloCheckResult(
                rule.getName(),
                Map.of(),
                rule.getMetric(),
                rule.getOperator(),
                rule.getThreshold(),
                null,
                SloStatus.ERROR,
                "Failed to check SLO. Query: " + promQl + ". Error: " + exception.getMessage(),
                checkedAt
        );
    }

    private boolean compare(double actual, double threshold, SloOperator operator) {
        return switch (operator) {
            case LESS_THAN -> actual < threshold;
            case LESS_THAN_OR_EQUAL -> actual <= threshold;
            case GREATER_THAN -> actual > threshold;
            case GREATER_THAN_OR_EQUAL -> actual >= threshold;
        };
    }

    private Map<String, String> businessLabels(Map<String, String> labels) {
        return Map.of(
                "service", labels.getOrDefault("service", "unknown"),
                "operation", labels.getOrDefault("operation", "unknown"),
                "kind", labels.getOrDefault("kind", "unknown"),
                "environment", labels.getOrDefault("environment", "unknown"),
                "window", labels.getOrDefault("window", "unknown")
        );
    }
}
