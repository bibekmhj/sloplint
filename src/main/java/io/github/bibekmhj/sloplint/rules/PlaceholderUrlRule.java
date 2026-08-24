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
 * SL003 — Placeholder URLs. Domains reserved by IANA for documentation
 * ({@code example.com/net/org}) plus common invented placeholder domains
 * LLMs reach for by default.
 *
 * <p>Skips matches inside comments that look like {@code // e.g. https://example.com/foo}
 * to reduce false positives on documentation examples.
 */
public final class PlaceholderUrlRule extends AbstractRegexRule {

    private static final List<Pattern> PATTERNS = List.of(
            // IANA reserved documentation domains
            Pattern.compile("\\bhttps?://(?:[a-zA-Z0-9-]+\\.)*example\\.(?:com|net|org)\\b"),
            // Common invented placeholders
            Pattern.compile("\\bhttps?://(?:[a-zA-Z0-9-]+\\.)*your-domain\\.(?:com|net|org)\\b"),
            Pattern.compile("\\bhttps?://(?:[a-zA-Z0-9-]+\\.)*mydomain\\.(?:com|net|org)\\b"),
            Pattern.compile("\\bhttps?://(?:[a-zA-Z0-9-]+\\.)*mysite\\.(?:com|net|org)\\b"),
            Pattern.compile("\\bhttps?://(?:[a-zA-Z0-9-]+\\.)*yoursite\\.(?:com|net|org)\\b"),
            Pattern.compile("\\bhttps?://foo\\.bar\\b")
    );

    public PlaceholderUrlRule() {
        super("SL003", "placeholder-url",
                "Placeholder URL (example.com, your-domain.com, etc.) — should be a real endpoint",
                Severity.WARNING, PATTERNS);
    }

    @Override
    protected String messageFor(Matcher m) {
        return "placeholder URL: " + m.group();
    }

    @Override
    public java.util.List<io.github.bibekmhj.sloplint.Finding> check(
            java.nio.file.Path file, String source) {
        // Skip matches inside a line that begins with "// e.g." to reduce
        // false positives on doc examples.
        java.util.List<io.github.bibekmhj.sloplint.Finding> all = super.check(file, source);
        java.util.List<io.github.bibekmhj.sloplint.Finding> out = new java.util.ArrayList<>(all.size());
        for (io.github.bibekmhj.sloplint.Finding f : all) {
            String snippet = f.snippet().orElse("");
            String trimmed = snippet.stripLeading();
            if (trimmed.startsWith("// e.g.") || trimmed.startsWith("* e.g.")) continue;
            out.add(f);
        }
        return out;
    }
}
