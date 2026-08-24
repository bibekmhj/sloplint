/*
 * Copyright 2026 the sloplint authors
 * Licensed under the Apache License, Version 2.0.
 */
package io.github.bibekmhj.sloplint.output;

import io.github.bibekmhj.sloplint.Finding;
import io.github.bibekmhj.sloplint.Rule;
import io.github.bibekmhj.sloplint.ScanResult;
import io.github.bibekmhj.sloplint.Severity;

import java.util.List;

/**
 * SARIF 2.1.0 output — the format GitHub Code Scanning consumes. Emitting
 * SARIF from CI lets findings appear inline on the "Files changed" tab.
 *
 * <p>Minimal-but-valid shape. Not every optional SARIF property is populated;
 * everything GitHub actually renders is.
 */
public final class SarifFormatter {

    private static final String SCHEMA =
            "https://raw.githubusercontent.com/oasis-tcs/sarif-spec/master/Schemata/sarif-schema-2.1.0.json";
    private static final String VERSION = "2.1.0";
    private static final String TOOL_URI = "https://github.com/bibekmhj/sloplint";

    private final List<Rule> rules;
    private final String toolVersion;

    public SarifFormatter(List<Rule> rules, String toolVersion) {
        this.rules = List.copyOf(rules);
        this.toolVersion = toolVersion == null ? "0.0.0" : toolVersion;
    }

    public String format(ScanResult result) {
        StringBuilder sb = new StringBuilder(1024);
        sb.append('{');
        kv(sb, "$schema", SCHEMA); sb.append(',');
        kv(sb, "version", VERSION); sb.append(',');
        sb.append("\"runs\":[{");
        // tool.driver
        sb.append("\"tool\":{\"driver\":{");
        kv(sb, "name", "sloplint"); sb.append(',');
        kv(sb, "version", toolVersion); sb.append(',');
        kv(sb, "informationUri", TOOL_URI); sb.append(',');
        sb.append("\"rules\":[");
        boolean first = true;
        for (Rule r : rules) {
            if (!first) sb.append(',');
            first = false;
            sb.append('{');
            kv(sb, "id", r.code()); sb.append(',');
            kv(sb, "name", r.name()); sb.append(',');
            sb.append("\"shortDescription\":{"); kv(sb, "text", r.description()); sb.append('}').append(',');
            sb.append("\"defaultConfiguration\":{"); kv(sb, "level", sarifLevel(r.defaultSeverity())); sb.append('}');
            sb.append('}');
        }
        sb.append("]}}"); // rules, driver, tool
        sb.append(',');
        // results
        sb.append("\"results\":[");
        first = true;
        for (Finding f : result.findings()) {
            if (!first) sb.append(',');
            first = false;
            writeResult(sb, f);
        }
        sb.append("]");
        sb.append("}]}"); // runs
        return sb.toString();
    }

    private void writeResult(StringBuilder sb, Finding f) {
        sb.append('{');
        kv(sb, "ruleId", f.ruleCode()); sb.append(',');
        kv(sb, "level", sarifLevel(f.severity())); sb.append(',');
        sb.append("\"message\":{"); kv(sb, "text", f.message()); sb.append('}').append(',');
        sb.append("\"locations\":[{");
        sb.append("\"physicalLocation\":{");
        sb.append("\"artifactLocation\":{"); kv(sb, "uri", f.file().toString()); sb.append('}').append(',');
        sb.append("\"region\":{");
        sb.append("\"startLine\":").append(f.line()).append(',');
        sb.append("\"startColumn\":").append(f.column());
        sb.append('}');
        sb.append('}');
        sb.append('}');
        sb.append(']');
        sb.append('}');
    }

    private static String sarifLevel(Severity s) {
        return switch (s) {
            case ERROR -> "error";
            case WARNING -> "warning";
            case INFO -> "note";
        };
    }

    private static void kv(StringBuilder sb, String k, String v) {
        sb.append('"').append(k).append("\":\"").append(escape(v)).append('"');
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
