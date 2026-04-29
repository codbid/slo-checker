package com.codbid.slo.checker.prometheus;

import java.util.Map;

public record PrometheusSample(
        Map<String, String> labels,
        double value
) {
}
