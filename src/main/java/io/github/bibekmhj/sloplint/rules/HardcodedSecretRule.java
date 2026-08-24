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
 * SL001 — Hardcoded API secrets / cloud credentials.
 *
 * <p>Detects real-looking API keys committed as string literals or comments:
 * AWS access keys, OpenAI keys, Anthropic keys, GitHub tokens, Google API keys,
 * Stripe live keys, and PEM private-key blocks.
 *
 * <p>Not detected: passwords stored as bcrypt hashes, JWT tokens (too many
 * false positives — random base64 chunks), or {@code YOUR_..._HERE} placeholders
 * (those are {@link PlaceholderMarkerRule}).
 */
public final class HardcodedSecretRule extends AbstractRegexRule {

    private static final List<Pattern> PATTERNS = List.of(
            // AWS access key ID
            Pattern.compile("\\bAKIA[0-9A-Z]{16}\\b"),
            Pattern.compile("\\bASIA[0-9A-Z]{16}\\b"),
            // OpenAI (sk-... but require a realistic length to avoid matching sk-test-01)
            Pattern.compile("\\bsk-(?:proj-)?[A-Za-z0-9_-]{40,}\\b"),
            // Anthropic
            Pattern.compile("\\bsk-ant-[A-Za-z0-9_-]{40,}\\b"),
            // GitHub personal access tokens
            Pattern.compile("\\bghp_[A-Za-z0-9]{36}\\b"),
            Pattern.compile("\\bgithub_pat_[A-Za-z0-9_]{80,}\\b"),
            // Google API
            Pattern.compile("\\bAIza[0-9A-Za-z_-]{35}\\b"),
            // Stripe live keys
            Pattern.compile("\\bsk_live_[0-9a-zA-Z]{24,}\\b"),
            Pattern.compile("\\bpk_live_[0-9a-zA-Z]{24,}\\b"),
            // PEM private keys
            Pattern.compile("-----BEGIN (?:RSA |EC |OPENSSH |DSA |PGP )?PRIVATE KEY-----")
    );

    public HardcodedSecretRule() {
        super("SL001", "hardcoded-api-secret",
                "Hardcoded API secret or private key committed to source",
                Severity.ERROR, PATTERNS);
    }

    @Override
    protected String messageFor(Matcher m) {
        String matched = m.group();
        // Redact — never echo a real secret in tool output.
        String redacted = matched.length() < 12
                ? matched.charAt(0) + "…"
                : matched.substring(0, 6) + "…" + matched.substring(matched.length() - 4);
        return "hardcoded secret matching a known provider format: " + redacted;
    }
}
