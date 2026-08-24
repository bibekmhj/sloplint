/*
 * Copyright 2026 the sloplint authors
 * Licensed under the Apache License, Version 2.0.
 */
package io.github.bibekmhj.sloplint;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * The input to a {@link Sloplint#scan(ScanRequest)} call.
 *
 * <p>Immutable. Build with {@link #builder()}.
 */
public final class ScanRequest {

    private final List<Path> roots;
    private final Set<String> enabledRules;
    private final Set<String> disabledRules;
    private final List<String> excludeGlobs;

    private ScanRequest(Builder b) {
        this.roots = List.copyOf(b.roots);
        this.enabledRules = Set.copyOf(b.enabledRules);
        this.disabledRules = Set.copyOf(b.disabledRules);
        this.excludeGlobs = List.copyOf(b.excludeGlobs);
        if (roots.isEmpty()) {
            throw new IllegalArgumentException("at least one root path is required");
        }
    }

    public List<Path> roots() { return roots; }

    /** When non-empty, only these rule codes run. Empty means all enabled by default. */
    public Set<String> enabledRules() { return enabledRules; }

    /** These rule codes are always skipped, even if in {@link #enabledRules()}. */
    public Set<String> disabledRules() { return disabledRules; }

    /**
     * Glob patterns matched against the relative file path from each root.
     * Files matching any pattern are skipped.
     */
    public List<String> excludeGlobs() { return excludeGlobs; }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private final java.util.List<Path> roots = new java.util.ArrayList<>();
        private java.util.Set<String> enabledRules = Set.of();
        private java.util.Set<String> disabledRules = Set.of();
        private final java.util.List<String> excludeGlobs = new java.util.ArrayList<>();

        public Builder addRoot(Path p) { roots.add(Objects.requireNonNull(p)); return this; }
        public Builder enableRules(Set<String> codes) { this.enabledRules = codes; return this; }
        public Builder disableRules(Set<String> codes) { this.disabledRules = codes; return this; }
        public Builder addExcludeGlob(String g) { excludeGlobs.add(Objects.requireNonNull(g)); return this; }
        public ScanRequest build() { return new ScanRequest(this); }
    }
}
