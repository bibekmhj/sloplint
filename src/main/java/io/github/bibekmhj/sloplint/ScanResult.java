/*
 * Copyright 2026 the sloplint authors
 * Licensed under the Apache License, Version 2.0.
 */
package io.github.bibekmhj.sloplint;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

/**
 * The output of a {@link Sloplint#scan(ScanRequest)} call.
 * Immutable.
 */
public final class ScanResult {

    private final List<Finding> findings;
    private final int filesScanned;
    private final Duration duration;

    public ScanResult(List<Finding> findings, int filesScanned, Duration duration) {
        this.findings = List.copyOf(Objects.requireNonNull(findings, "findings"));
        this.filesScanned = filesScanned;
        this.duration = Objects.requireNonNullElse(duration, Duration.ZERO);
    }

    public List<Finding> findings() { return findings; }
    public int filesScanned() { return filesScanned; }
    public Duration duration() { return duration; }

    public boolean hasFindings() { return !findings.isEmpty(); }
    public boolean hasErrors() {
        return findings.stream().anyMatch(f -> f.severity() == Severity.ERROR);
    }
    public boolean hasWarningsOrWorse() {
        return findings.stream().anyMatch(f ->
                f.severity() == Severity.WARNING || f.severity() == Severity.ERROR);
    }
    public long countAt(Severity s) {
        return findings.stream().filter(f -> f.severity() == s).count();
    }
}
