package com.codbid.slo.checker.service;

import com.codbid.slo.checker.config.SloProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PromQlBuilder {

    private static final String ALL = "ALL";

    public String build(SloProperties.RuleProperties rule) {
        String metricName = rule.getMetric().getPrometheusMetricName();

        List<String> matchers = new ArrayList<>();

        addMatcher(matchers, "service", rule.getService());
        addMatcher(matchers, "operation", rule.getOperation());
        addMatcher(matchers, "kind", rule.getKind());
        addMatcher(matchers, "window", rule.getWindow());

        return metricName + "{" + String.join(",", matchers) + "}";
    }

    private void addMatcher(List<String> matchers, String label, String value) {
        if (value == null || value.isBlank() || ALL.equalsIgnoreCase(value)) {
            return;
        }

        matchers.add(label + "=\"" + escape(value) + "\"");
    }

    private String escape(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}
