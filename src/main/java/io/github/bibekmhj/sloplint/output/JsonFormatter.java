/*
 * Copyright 2026 the sloplint authors
 * Licensed under the Apache License, Version 2.0.
 */
package io.github.bibekmhj.sloplint.output;

import io.github.bibekmhj.sloplint.Finding;
import io.github.bibekmhj.sloplint.ScanResult;

/**
 * Minimal JSON output. Zero external dependencies so sloplint stays a small jar.
 */
public final class JsonFormatter {

    public String format(ScanResult result) {
        StringBuilder sb = new StringBuilder(512);
        sb.append('{');
        kv(sb, "filesScanned", result.filesScanned());
        sb.append(',');
        kv(sb, "durationMillis", result.duration().toMillis());
        sb.append(',');
        sb.append("\"findings\":[");
        boolean first = true;
        for (Finding f : result.findings()) {
            if (!first) sb.append(',');
            first = false;
            writeFinding(sb, f);
        }
        sb.append(']');
        sb.append('}');
        return sb.toString();
    }

    private void writeFinding(StringBuilder sb, Finding f) {
        sb.append('{');
        kvStr(sb, "ruleCode", f.ruleCode()); sb.append(',');
        kvStr(sb, "ruleName", f.ruleName()); sb.append(',');
        kvStr(sb, "severity", f.severity().name()); sb.append(',');
        kvStr(sb, "file", f.file().toString()); sb.append(',');
        kv(sb, "line", f.line()); sb.append(',');
        kv(sb, "column", f.column()); sb.append(',');
        kvStr(sb, "message", f.message());
        f.snippet().ifPresent(s -> { sb.append(','); kvStr(sb, "snippet", s); });
        sb.append('}');
    }

    private void kvStr(StringBuilder sb, String k, String v) {
        sb.append('"').append(k).append("\":\"").append(escape(v)).append('"');
    }

    private void kv(StringBuilder sb, String k, long v) {
        sb.append('"').append(k).append("\":").append(v);
    }

    private static String escape(String s) {
        if (s == null) return "";
        StringBuilder out = new StringBuilder(s.length() + 8);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\': out.append("\\\\"); break;
                case '"':  out.append("\\\""); break;
                case '\n': out.append("\\n");  break;
                case '\r': out.append("\\r");  break;
                case '\t': out.append("\\t");  break;
                default:
                    if (c < 0x20) out.append(String.format("\\u%04x", (int) c));
                    else out.append(c);
            }
        }
        return out.toString();
    }
}
