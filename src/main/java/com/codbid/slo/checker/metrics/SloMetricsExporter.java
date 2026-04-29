package com.codbid.slo.checker.metrics;

import com.codbid.slo.checker.config.SloProperties;
import com.codbid.slo.checker.model.SloCheckResult;
import com.codbid.slo.checker.store.SloCheckResultStore;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.MultiGauge;
import io.micrometer.core.instrument.Tags;
import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.ToDoubleFunction;

@Component
public class SloMetricsExporter {

    private final SloCheckResultStore store;

    private final MultiGauge ruleStatus;
    private final MultiGauge actualValue;
    private final MultiGauge thresholdValue;

    public SloMetricsExporter(SloProperties properties, SloCheckResultStore store, MeterRegistry meterRegistry) {
        this.store = store;

        Gauge.builder("slo_rules_total", properties, p -> p.getRules().size())
                .description("Total configured SLO rules")
                .register(meterRegistry);

        Gauge.builder("slo_results_total", store, s  -> s.findLatest().size())
                .description("Total latest SLO check results")
                .register(meterRegistry);

        Gauge.builder("slo_violations_total", store, s  -> s.findLatestViolations().size())
                .description("Total latest SLO violations")
                .register(meterRegistry);

        this.ruleStatus = MultiGauge.builder("slo_rule_status")
                .description("Current SLO rule status. 0=OK, 1=VIOLATED, 2=NO_DATA, 3=ERROR")
                .register(meterRegistry);

        this.actualValue = MultiGauge.builder("slo_rule_actual_value")
                .description("Latest actual value checked by SLO rule")
                .register(meterRegistry);

        this.thresholdValue = MultiGauge.builder("slo_rule_threshold")
                .description("Configured threshold value for SLO rule")
                .register(meterRegistry);
    }

    @PostConstruct
    public void init() {
        refresh();
    }

    @Scheduled(fixedDelayString = "${slo.metrics.refresh-interval:5s}")
    public void refresh() {
        ruleStatus.register(rows(this::statusValue), true);
        actualValue.register(rows(result -> result.actualValue() == null ? Double.NaN : result.actualValue()), true);
        thresholdValue.register(rows(SloCheckResult::threshold), true);
    }

    private List<MultiGauge.Row<Number>> rows(ToDoubleFunction<SloCheckResult> valueExtractor) {
        return store.findLatest().stream()
                .map(result -> MultiGauge.Row.of(tags(result), valueExtractor.applyAsDouble(result)))
                .toList();
    }

    private double statusValue(SloCheckResult result) {
        return switch (result.status()) {
            case OK -> 0.0;
            case VIOLATED -> 1.0;
            case NO_DATA -> 2.0;
            case ERROR -> 3.0;
        };
    }

    private Tags tags(SloCheckResult result) {
        Map<String, String> labels = result.labels();

        return Tags.of(
                "rule", safe(result.ruleName()),
                "metric", result.metric() == null ? "unknown" : result.metric().name(),
                "status", result.status() == null ? "unknown" : result.status().name(),
                "service", safe(labels.get("service")),
                "operation", safe(labels.get("operation")),
                "kind", safe(labels.get("kind")),
                "environment", safe(labels.get("environment")),
                "window", safe(labels.get("window"))
        );
    }

    private String safe(String value) {
        if (value == null || value.isBlank()) {
            return "unknown";
        }

        return value;
    }
}
