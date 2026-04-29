package com.codbid.slo.checker.prometheus;

import com.codbid.slo.checker.config.SloProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class PrometheusClient {

    private static final String QUERY_ENDPOINT = "/api/v1/query";

    private final RestClient restClient;
    private final String prometheusUrl;

    public PrometheusClient(SloProperties sloProperties) {
        this.prometheusUrl = normalizeUrl(sloProperties.getPrometheus().getUrl());
        this.restClient = RestClient.builder().build();
    }

    public List<PrometheusSample> queryVector(String promQl) {
        PrometheusQueryResponse response = executeQuery(promQl);

        if (response == null || response.data() == null || response.data().result() == null) {
            return List.of();
        }

        return response.data().result()
                .stream()
                .map(this::toSample)
                .toList();
    }

    private PrometheusQueryResponse executeQuery(String promQl) {
        return restClient.get()
                .uri(buildQueryUri(promQl))
                .retrieve()
                .body(PrometheusQueryResponse.class);
    }

    private URI buildQueryUri(String promQl) {
        String encodedQuery = URLEncoder.encode(promQl, StandardCharsets.UTF_8)
                .replace("+", "%20");

        return URI.create(prometheusUrl + QUERY_ENDPOINT + "?query=" + encodedQuery);
    }

    private PrometheusSample toSample(PrometheusQueryResponse.Result result) {
        if (result.value() == null || result.value().size() < 2 || result.value().get(1) == null) {
            throw new IllegalStateException("Invalid prometheus sample value");
        }

        return new PrometheusSample(
                result.metric(),
                Double.parseDouble(result.value().get(1).toString())
        );
    }

    private String normalizeUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalStateException("Prometheus url is not configured. Set slo.prometheus.url");
        }

        String normalized = url.trim();

        if (!normalized.startsWith("http://") && !normalized.startsWith("https://")) {
            throw new IllegalStateException(
                    "Prometheus url must start with http:// or https://. Current value: " + normalized
            );
        }

        if (normalized.endsWith("/")) {
            return normalized.substring(0, normalized.length() - 1);
        }

        return normalized;
    }
}
