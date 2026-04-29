package com.codbid.slo.checker.service;

import com.codbid.slo.checker.config.SloProperties;
import com.codbid.slo.checker.model.SloCheckResult;
import com.codbid.slo.checker.model.SloOperator;
import com.codbid.slo.checker.model.SloStatus;
import com.codbid.slo.checker.prometheus.PrometheusClient;
import com.codbid.slo.checker.prometheus.PrometheusSample;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class SloCheckerService {

    private final PrometheusClient client;
    private final PromQlBuilder promQlBuilder;

    public SloCheckerService(PrometheusClient client, PromQlBuilder promQlBuilder) {
        this.client = client;
        this.promQlBuilder = promQlBuilder;
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

    private SloCheckResult checkSample(SloProperties.RuleProperties rule, PrometheusSample sample, Instant checkedAt) {
        boolean ok = compare(sample.value(), rule.getThreshold(), rule.getOperator());

        return new SloCheckResult(
                rule.getName(),
                sample.labels(),
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
}
