/*
 * Copyright 2026 the sloplint authors
 * Licensed under the Apache License, Version 2.0.
 */
package io.github.bibekmhj.sloplint;

import io.github.bibekmhj.sloplint.rules.HardcodedSecretRule;
import io.github.bibekmhj.sloplint.rules.PlaceholderMarkerRule;
import io.github.bibekmhj.sloplint.rules.PlaceholderUrlRule;
import io.github.bibekmhj.sloplint.rules.SilentCatchRule;
import io.github.bibekmhj.sloplint.rules.SysOutRule;
import io.github.bibekmhj.sloplint.rules.TodoThrowRule;
import io.github.bibekmhj.sloplint.rules.TrivialTestRule;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

/**
 * The library entry point. Assemble a {@link ScanRequest}, call {@link #scan(ScanRequest)},
 * inspect the {@link ScanResult}.
 *
 * <p>Thread-safety: all built-in rules are stateless; a single {@code Sloplint}
 * instance can be shared across threads.
 */
public final class Sloplint {

    private final List<Rule> allRules;

    public Sloplint() {
        this(defaultRules());
    }

    Sloplint(List<Rule> rules) {
        this.allRules = List.copyOf(Objects.requireNonNull(rules, "rules"));
    }

    /** The built-in rules shipped with sloplint 0.1. */
    public static List<Rule> defaultRules() {
        return List.of(
                new HardcodedSecretRule(),
                new PlaceholderMarkerRule(),
                new PlaceholderUrlRule(),
                new TodoThrowRule(),
                new SilentCatchRule(),
                new SysOutRule(),
                new TrivialTestRule()
        );
    }

    public List<Rule> rules() { return allRules; }

    /**
     * Scan every {@code .java} file under each root recursively.
     * Returns immutable {@link ScanResult} with the collected findings.
     */
    public ScanResult scan(ScanRequest req) {
        Objects.requireNonNull(req, "req");
        Instant t0 = Instant.now();

        List<Rule> active = filterActiveRules(req);
        List<PathMatcher> excludes = compileExcludes(req.excludeGlobs());

        List<Finding> findings = new ArrayList<>();
        int filesScanned = 0;

        for (Path root : req.roots()) {
            if (!Files.exists(root)) continue;
            try (Stream<Path> stream = Files.walk(root)) {
                List<Path> javaFiles = stream
                        .filter(Files::isRegularFile)
                        .filter(p -> p.getFileName().toString().endsWith(".java"))
                        .filter(p -> !isExcluded(root, p, excludes))
                        .toList();
                for (Path file : javaFiles) {
                    filesScanned++;
                    String source;
                    try {
                        source = Files.readString(file);
                    } catch (IOException e) {
                        continue;
                    }
                    for (Rule r : active) {
                        try {
                            findings.addAll(r.check(file, source));
                        } catch (RuntimeException e) {
                            // A misbehaving rule must never break the whole scan.
                            // In a future release we'd surface these as diagnostics.
                        }
                    }
                }
            } catch (IOException e) {
                // Skip roots we can't walk.
            }
        }

        return new ScanResult(findings, filesScanned, Duration.between(t0, Instant.now()));
    }

    private List<Rule> filterActiveRules(ScanRequest req) {
        Set<String> enabled = req.enabledRules();
        Set<String> disabled = req.disabledRules();
        List<Rule> out = new ArrayList<>();
        for (Rule r : allRules) {
            if (disabled.contains(r.code())) continue;
            if (!enabled.isEmpty() && !enabled.contains(r.code())) continue;
            out.add(r);
        }
        return out;
    }

    private List<PathMatcher> compileExcludes(List<String> globs) {
        List<PathMatcher> out = new ArrayList<>(globs.size());
        for (String g : globs) {
            out.add(FileSystems.getDefault().getPathMatcher("glob:" + g));
        }
        return out;
    }

    private boolean isExcluded(Path root, Path file, List<PathMatcher> excludes) {
        if (excludes.isEmpty()) return false;
        Path rel = root.relativize(file);
        for (PathMatcher pm : excludes) {
            if (pm.matches(rel) || pm.matches(file.getFileName())) return true;
        }
        return false;
    }
}
