/*
 * Copyright 2026 the sloplint authors
 * Licensed under the Apache License, Version 2.0.
 */
package io.github.bibekmhj.sloplint.output;

import io.github.bibekmhj.sloplint.Finding;
import io.github.bibekmhj.sloplint.ScanResult;
import io.github.bibekmhj.sloplint.Severity;

/**
 * Human-readable text report — the default CLI output. Similar shape to
 * {@code ruff}/{@code eslint} for grep-ability.
 */
public final class TextFormatter {

    private final boolean color;

    public TextFormatter(boolean color) { this.color = color; }

    public String format(ScanResult result) {
        StringBuilder sb = new StringBuilder(256);
        for (Finding f : result.findings()) {
            String sev = sevBadge(f.severity());
            sb.append(f.file()).append(':')
                    .append(f.line()).append(':')
                    .append(f.column()).append(':').append(' ')
                    .append(sev).append(' ')
                    .append('[').append(f.ruleCode()).append(' ').append(f.ruleName()).append(']').append(' ')
                    .append(f.message()).append('\n');
            f.snippet().ifPresent(s -> sb.append("    | ").append(s).append('\n'));
        }
        sb.append('\n');
        sb.append("scanned ").append(result.filesScanned())
                .append(" file").append(result.filesScanned() == 1 ? "" : "s")
                .append(" in ").append(result.duration().toMillis()).append("ms — ");
        long errors = result.countAt(Severity.ERROR);
        long warnings = result.countAt(Severity.WARNING);
        long info = result.countAt(Severity.INFO);
        sb.append(errors).append(" error").append(errors == 1 ? "" : "s").append(", ")
          .append(warnings).append(" warning").append(warnings == 1 ? "" : "s");
        if (info > 0) sb.append(", ").append(info).append(" info");
        sb.append('\n');
        return sb.toString();
    }

    private String sevBadge(Severity s) {
        if (!color) return s.name();
        return switch (s) {
            case ERROR   -> "[31m" + s.name() + "[0m";
            case WARNING -> "[33m" + s.name() + "[0m";
            case INFO    -> "[36m" + s.name() + "[0m";
        };
    }
}
