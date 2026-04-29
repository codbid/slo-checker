package com.codbid.slo.checker.prometheus;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PrometheusQueryResponse(
        String status,
        Data data
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Data(
            String resultType,
            List<Result> result
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Result(
            Map<String, String> metric,
            List<Object> value
    ) {
    }
}
