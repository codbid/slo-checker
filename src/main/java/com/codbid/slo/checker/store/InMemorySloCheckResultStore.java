package com.codbid.slo.checker.store;

import com.codbid.slo.checker.model.SloCheckResult;
import com.codbid.slo.checker.model.SloStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class InMemorySloCheckResultStore implements SloCheckResultStore {

    private final AtomicReference<List<SloCheckResult>> latestResults = new AtomicReference<>(List.of());

    @Override
    public void saveLatest(List<SloCheckResult> results) {
        latestResults.set(List.copyOf(results));
    }

    @Override
    public List<SloCheckResult> findLatest() {
        return latestResults.get();
    }

    @Override
    public List<SloCheckResult> findLatestViolations() {
        return latestResults.get().stream()
                .filter(result -> result.status() == SloStatus.VIOLATED)
                .toList();
    }
}
