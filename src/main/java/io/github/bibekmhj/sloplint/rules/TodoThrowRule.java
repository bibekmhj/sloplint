/*
 * Copyright 2026 the sloplint authors
 * Licensed under the Apache License, Version 2.0.
 */
package io.github.bibekmhj.sloplint.rules;

import io.github.bibekmhj.sloplint.Severity;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SL004 — Stub methods that throw an "unimplemented" exception.
 *
 * <p>Catches the classic pattern where an LLM generates the signatures of a
 * class it doesn't know how to implement and fills each method body with
 * {@code throw new UnsupportedOperationException("TODO")}. Also flags equivalent
 * patterns with {@code NotImplementedException} and {@code RuntimeException("TODO")}.
 */
public final class TodoThrowRule extends AbstractRegexRule {

    private static final List<Pattern> PATTERNS = List.of(
            Pattern.compile(
                    "throw\\s+new\\s+UnsupportedOperationException\\s*\\([^)]*(?:TODO|not\\s+implemented|stub|unimplemented)[^)]*\\)",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile(
                    "throw\\s+new\\s+NotImplementedException\\s*\\([^)]*\\)",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile(
                    "throw\\s+new\\s+RuntimeException\\s*\\(\\s*\"(?:TODO|not\\s+implemented|unimplemented)[^\"]*\"\\s*\\)",
                    Pattern.CASE_INSENSITIVE)
    );

    public TodoThrowRule() {
        super("SL004", "todo-throw",
                "Stub method that throws \"not implemented\" — real code is missing",
                Severity.WARNING, PATTERNS);
    }

    @Override
    protected String messageFor(Matcher m) {
        return "method body is a not-implemented stub: " + m.group().replaceAll("\\s+", " ");
    }
}
