/*
 * Copyright 2026 the sloplint authors
 * Licensed under the Apache License, Version 2.0.
 */
package io.github.bibekmhj.sloplint.rules;

import io.github.bibekmhj.sloplint.Finding;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PlaceholderMarkerRuleTest {

    private final PlaceholderMarkerRule rule = new PlaceholderMarkerRule();
    private final Path FAKE = Path.of("Test.java");

    @Test @DisplayName("detects YOUR_API_KEY_HERE style")
    void detectsYourApiKey() {
        String src = "String k = \"YOUR_API_KEY_HERE\";";
        assertThat(rule.check(FAKE, src)).hasSize(1);
    }

    @Test @DisplayName("detects <REPLACE_ME> style")
    void detectsReplaceMe() {
        String src = "String u = \"<REPLACE_WITH_YOUR_URL>\";";
        assertThat(rule.check(FAKE, src)).hasSize(1);
    }

    @Test @DisplayName("detects CHANGE_ME")
    void detectsChangeMe() {
        String src = "String p = \"CHANGE_ME\";";
        assertThat(rule.check(FAKE, src)).hasSize(1);
    }

    @Test @DisplayName("detects password123 default")
    void detectsWeakDefault() {
        String src = "if (pw.equals(\"password123\")) { /* auth */ }";
        assertThat(rule.check(FAKE, src)).hasSize(1);
    }

    @Test @DisplayName("detects xxxxxx scaffold")
    void detectsXxxxxx() {
        String src = "String t = \"xxxxxxxx-abcd-1234\";";
        assertThat(rule.check(FAKE, src)).hasSize(1);
    }

    @Test @DisplayName("detects TODO: add ... comment")
    void detectsTodoComment() {
        String src = "// TODO: implement retry logic";
        assertThat(rule.check(FAKE, src)).hasSize(1);
    }

    @Test @DisplayName("does not flag normal code")
    void clean() {
        String src = "String url = \"https://api.stripe.com/v1/charges\";";
        assertThat(rule.check(FAKE, src)).isEmpty();
    }
}
