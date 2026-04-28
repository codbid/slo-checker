package com.codbid.slo.checker.config;

import com.codbid.slo.checker.model.SloMetricType;
import com.codbid.slo.checker.model.SloOperator;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "slo")
public class SloProperties {

    private Duration checkInterval = Duration.ofSeconds(10);

    private PrometheusProperties properties = new PrometheusProperties();

    private List<RuleProperties> rules = new ArrayList<>();

    public Duration getCheckInterval() {
        return checkInterval;
    }

    public void setCheckInterval(Duration checkInterval) {
        this.checkInterval = checkInterval;
    }

    public PrometheusProperties getPrometheusProperties() {
        return properties;
    }

    public void setPrometheusProperties(PrometheusProperties properties) {
        this.properties = properties;
    }

    public List<RuleProperties> getRules() {
        return rules;
    }

    public void setRules(List<RuleProperties> rules) {
        this.rules = rules;
    }

    public static class PrometheusProperties {
        private String url;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    public static class RuleProperties {

        private String name;
        private String service;
        private String operation;
        private String kind;
        private String window;

        private SloMetricType metric;
        private SloOperator operator;
        private double threshold;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getService() {
            return service;
        }

        public void setService(String service) {
            this.service = service;
        }

        public String getOperation() {
            return operation;
        }

        public void setOperation(String operation) {
            this.operation = operation;
        }

        public String getKind() {
            return kind;
        }

        public void setKind(String kind) {
            this.kind = kind;
        }

        public String getWindow() {
            return window;
        }

        public void setWindow(String window) {
            this.window = window;
        }

        public SloMetricType getMetric() {
            return metric;
        }

        public void setMetric(SloMetricType metric) {
            this.metric = metric;
        }

        public SloOperator getOperator() {
            return operator;
        }

        public void setOperator(SloOperator operator) {
            this.operator = operator;
        }

        public double getThreshold() {
            return threshold;
        }

        public void setThreshold(double threshold) {
            this.threshold = threshold;
        }
    }
}
