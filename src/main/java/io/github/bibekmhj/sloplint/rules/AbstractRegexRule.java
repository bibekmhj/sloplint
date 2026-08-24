/*
 * Copyright 2026 the sloplint authors
 * Licensed under the Apache License, Version 2.0.
 */
package io.github.bibekmhj.sloplint.rules;

import io.github.bibekmhj.sloplint.Finding;
import io.github.bibekmhj.sloplint.Rule;
import io.github.bibekmhj.sloplint.Severity;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Base class for rules that are cleanly expressible as one or more regexes over
 * source text. Subclasses provide the patterns and a message factory.
 *
 * <p>Line comments and block comments are still scanned — an API key
 * <em>commented out</em> is still an API key you shouldn't commit.
 */
public abstract class AbstractRegexRule implements Rule {

    private final String code;
    private final String name;
    private final String description;
    private final Severity severity;
    private final List<Pattern> patterns;

    protected AbstractRegexRule(String code, String name, String description,
                                Severity severity, List<Pattern> patterns) {
        this.code = Objects.requireNonNull(code);
        this.name = Objects.requireNonNull(name);
        this.description = Objects.requireNonNull(description);
        this.severity = Objects.requireNonNull(severity);
        this.patterns = List.copyOf(patterns);
    }

    @Override public String code() { return code; }
    @Override public String name() { return name; }
    @Override public String description() { return description; }
    @Override public Severity defaultSeverity() { return severity; }

    /** Message shown for a match. Subclasses may override to include the matched text. */
    protected String messageFor(Matcher m) { return description; }

    @Override
    public List<Finding> check(Path file, String source) {
        if (patterns.isEmpty()) return Collections.emptyList();
        List<Finding> out = new ArrayList<>();
        for (Pattern p : patterns) {
            Matcher m = p.matcher(source);
            while (m.find()) {
                int start = m.start();
                int line = lineOfOffset(source, start);
                int col = columnOfOffset(source, start);
                String snippet = snippetAt(source, line).trim();
                out.add(Finding.builder()
                        .ruleCode(code)
                        .ruleName(name)
                        .severity(severity)
                        .file(file)
                        .line(line)
                        .column(col)
                        .message(messageFor(m))
                        .snippet(snippet)
                        .build());
            }
        }
        return out;
    }

    // --- helpers ---

    static int lineOfOffset(String source, int offset) {
        int line = 1;
        for (int i = 0; i < offset && i < source.length(); i++) {
            if (source.charAt(i) == '\n') line++;
        }
        return line;
    }

    static int columnOfOffset(String source, int offset) {
        int col = 1;
        for (int i = 0; i < offset && i < source.length(); i++) {
            if (source.charAt(i) == '\n') col = 1;
            else col++;
        }
        return col;
    }

    static String snippetAt(String source, int line) {
        int cur = 1;
        int start = 0;
        for (int i = 0; i < source.length() && cur < line; i++) {
            if (source.charAt(i) == '\n') {
                cur++;
                start = i + 1;
            }
        }
        int end = source.indexOf('\n', start);
        if (end < 0) end = source.length();
        return source.substring(start, end);
    }
}
