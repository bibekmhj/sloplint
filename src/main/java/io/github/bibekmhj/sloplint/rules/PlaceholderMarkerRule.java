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
 * SL002 — Placeholder text an LLM left in that a human should have replaced.
 *
 * <p>Matches {@code YOUR_API_KEY_HERE}, {@code <REPLACE_ME>}, {@code CHANGE_ME},
 * classic weak defaults like {@code password123}, and long runs of {@code x} or
 * {@code X} used as scaffold placeholders. These almost never survive a real
 * code review, but they slip past when an AI-generated PR merges quickly.
 */
public final class PlaceholderMarkerRule extends AbstractRegexRule {

    private static final List<Pattern> PATTERNS = List.of(
            // YOUR_FOO_HERE / YOUR_FOO_KEY / YOUR-FOO-BAR
            Pattern.compile("\\bYOUR[_-][A-Z0-9_-]{2,}[_-](?:HERE|KEY|TOKEN|SECRET|PASSWORD|VALUE)\\b"),
            // <REPLACE...> <YOUR ... > <CHANGE ME>
            Pattern.compile("<\\s*(?:REPLACE|CHANGE|YOUR|INSERT|FILL)[_ \\t\\w-]*>", Pattern.CASE_INSENSITIVE),
            // INSERT_..._HERE
            Pattern.compile("\\bINSERT[_-][A-Z0-9_-]{2,}[_-](?:HERE|KEY|VALUE)\\b"),
            // CHANGE_ME / CHANGEME
            Pattern.compile("\\bCHANGE[_-]?ME\\b", Pattern.CASE_INSENSITIVE),
            // "password123", "admin123" — the weak defaults LLMs love
            Pattern.compile("\"(?:password|admin|pass|secret)(?:123|1234|12345|123456)?\"",
                    Pattern.CASE_INSENSITIVE),
            // 6+ x's or X's inside a string (scaffold placeholders)
            Pattern.compile("\"[^\"\\n]*[xX]{6,}[^\"\\n]*\""),
            // FIXME/TODO with an obvious "add", "implement", "fill" instruction
            Pattern.compile("//\\s*(?:TODO|FIXME)[:\\s]+(?:add|implement|fill|replace|change|set)\\b",
                    Pattern.CASE_INSENSITIVE)
    );

    public PlaceholderMarkerRule() {
        super("SL002", "placeholder-marker",
                "Placeholder text left in code that a human should have replaced",
                Severity.ERROR, PATTERNS);
    }

    @Override
    protected String messageFor(Matcher m) {
        return "placeholder marker not replaced: " + m.group();
    }
}
