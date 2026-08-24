/*
 * Copyright 2026 the sloplint authors
 * Licensed under the Apache License, Version 2.0.
 */
package io.github.bibekmhj.sloplint;

import java.nio.file.Path;
import java.util.List;

/**
 * A lint rule. Given the raw source of a single {@code .java} file and its path,
 * return zero or more {@link Finding}s.
 *
 * <p>Rules must be safe to reuse across many files and threads. State should live
 * on the stack, not on the rule instance.
 */
public interface Rule {

    /** Short stable code such as {@code "SL001"}. Used in output and config filtering. */
    String code();

    /** Human name of the rule, e.g. {@code "hardcoded-api-secret"}. */
    String name();

    /** One-sentence description shown in {@code --list-rules} and reports. */
    String description();

    /** Default severity if the user has not remapped this rule. */
    Severity defaultSeverity();

    /**
     * Apply the rule to a single source file.
     *
     * @param file    the file path (used only for reporting)
     * @param source  the raw text of the source file
     * @return zero or more findings — never {@code null}
     */
    List<Finding> check(Path file, String source);
}
