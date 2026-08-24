/*
 * Copyright 2026 the sloplint authors
 * Licensed under the Apache License, Version 2.0.
 */
package io.github.bibekmhj.sloplint;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

/**
 * A single lint finding — one place in one file where a rule matched.
 *
 * <p>Immutable value type. Compare by all fields.
 */
public final class Finding {

    private final String ruleCode;
    private final String ruleName;
    private final Severity severity;
    private final Path file;
    private final int line;
    private final int column;
    private final String message;
    private final String snippet;

    private Finding(Builder b) {
        this.ruleCode = Objects.requireNonNull(b.ruleCode, "ruleCode");
        this.ruleName = Objects.requireNonNull(b.ruleName, "ruleName");
        this.severity = Objects.requireNonNullElse(b.severity, Severity.WARNING);
        this.file = Objects.requireNonNull(b.file, "file");
        this.line = b.line;
        this.column = b.column;
        this.message = Objects.requireNonNull(b.message, "message");
        this.snippet = b.snippet;
    }

    public String ruleCode() { return ruleCode; }
    public String ruleName() { return ruleName; }
    public Severity severity() { return severity; }
    public Path file() { return file; }
    public int line() { return line; }
    public int column() { return column; }
    public String message() { return message; }
    public Optional<String> snippet() { return Optional.ofNullable(snippet); }

    public static Builder builder() { return new Builder(); }

    @Override public String toString() {
        return String.format("%s:%d:%d [%s %s] %s", file, line, column, ruleCode, severity, message);
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Finding f)) return false;
        return line == f.line
                && column == f.column
                && ruleCode.equals(f.ruleCode)
                && ruleName.equals(f.ruleName)
                && severity == f.severity
                && file.equals(f.file)
                && message.equals(f.message)
                && Objects.equals(snippet, f.snippet);
    }

    @Override public int hashCode() {
        return Objects.hash(ruleCode, ruleName, severity, file, line, column, message, snippet);
    }

    public static final class Builder {
        private String ruleCode;
        private String ruleName;
        private Severity severity;
        private Path file;
        private int line;
        private int column;
        private String message;
        private String snippet;

        public Builder ruleCode(String v) { this.ruleCode = v; return this; }
        public Builder ruleName(String v) { this.ruleName = v; return this; }
        public Builder severity(Severity v) { this.severity = v; return this; }
        public Builder file(Path v) { this.file = v; return this; }
        public Builder line(int v) { this.line = v; return this; }
        public Builder column(int v) { this.column = v; return this; }
        public Builder message(String v) { this.message = v; return this; }
        public Builder snippet(String v) { this.snippet = v; return this; }
        public Finding build() { return new Finding(this); }
    }
}
