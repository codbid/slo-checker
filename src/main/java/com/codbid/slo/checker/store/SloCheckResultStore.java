package com.codbid.slo.checker.store;

import com.codbid.slo.checker.model.SloCheckResult;

import java.util.List;

public interface SloCheckResultStore {

    void saveLatest(List<SloCheckResult> results);

    List<SloCheckResult> findLatest();

    List<SloCheckResult> findLatestViolations();
}
