/*
 * Copyright 2026 the sloplint authors
 * Licensed under the Apache License, Version 2.0.
 */
package io.github.bibekmhj.sloplint.rules;

import io.github.bibekmhj.sloplint.Finding;
import io.github.bibekmhj.sloplint.Severity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HardcodedSecretRuleTest {

    private final HardcodedSecretRule rule = new HardcodedSecretRule();
    private final Path FAKE = Path.of("Test.java");

    @Test @DisplayName("detects AWS access key ID")
    void detectsAws() {
        String src = "String key = \"AKIAIOSFODNN7EXAMPLE\";";
        List<Finding> f = rule.check(FAKE, src);
        assertThat(f).hasSize(1);
        assertThat(f.get(0).ruleCode()).isEqualTo("SL001");
        assertThat(f.get(0).severity()).isEqualTo(Severity.ERROR);
        assertThat(f.get(0).message()).contains("AKIAIO"); // redacted prefix
    }

    @Test @DisplayName("detects OpenAI-style sk- key of realistic length")
    void detectsOpenAi() {
        String src = "String k = \"sk-abcdefghijklmnopqrstuvwxyzABCDEFGHIJ0123456789\";";
        List<Finding> f = rule.check(FAKE, src);
        assertThat(f).hasSize(1);
    }

    @Test @DisplayName("does NOT flag short sk- placeholders like sk-test-01")
    void skipsShortStubs() {
        String src = "String k = \"sk-test-01\";";
        assertThat(rule.check(FAKE, src)).isEmpty();
    }

    @Test @DisplayName("detects GitHub personal access token")
    void detectsGithubPat() {
        String src = "String tok = \"ghp_1234567890abcdef1234567890abcdef1234\";";
        assertThat(rule.check(FAKE, src)).hasSize(1);
    }

    @Test @DisplayName("detects PEM private key block header")
    void detectsPemHeader() {
        String src = "String key = \"-----BEGIN RSA PRIVATE KEY-----\";";
        assertThat(rule.check(FAKE, src)).hasSize(1);
    }

    @Test @DisplayName("redacts the matched secret in the message")
    void redacts() {
        String src = "String k = \"ghp_1234567890abcdef1234567890abcdef1234\";";
        Finding f = rule.check(FAKE, src).get(0);
        assertThat(f.message()).contains("…").doesNotContain("ghp_1234567890abcdef1234567890abcdef1234");
    }
}
