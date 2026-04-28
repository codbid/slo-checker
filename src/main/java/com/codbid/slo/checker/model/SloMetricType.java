package com.codbid.slo.checker.model;

public enum SloMetricType {

    ERROR_RATE("telemetry_window_error_rate"),
    SUCCESS_RATE("telemetry_window_success_rate"),
    P95_LATENCY_MS("telemetry_window_latency_p95_ms"),
    P99_LATENCY_MS("telemetry_window_latency_p99_ms"),
    RPS("telemetry_window_rps");

    private final String prometheusMetricName;

    SloMetricType(String prometheusMetricName) {
        this.prometheusMetricName = prometheusMetricName;
    }

    public String getPrometheusMetricName() {
        return prometheusMetricName;
    }
}
