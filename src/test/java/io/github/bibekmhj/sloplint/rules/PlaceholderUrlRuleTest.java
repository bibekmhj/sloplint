/*
 * Copyright 2026 the sloplint authors
 * Licensed under the Apache License, Version 2.0.
 */
package io.github.bibekmhj.sloplint.rules;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class PlaceholderUrlRuleTest {

    private final PlaceholderUrlRule rule = new PlaceholderUrlRule();
    private final Path FAKE = Path.of("Test.java");

    @Test @DisplayName("detects example.com URL")
    void exampleCom() {
        String src = "String u = \"https://api.example.com/v1/foo\";";
        assertThat(rule.check(FAKE, src)).hasSize(1);
    }

    @Test @DisplayName("detects your-domain.com URL")
    void yourDomain() {
        String src = "String u = \"https://your-domain.com/x\";";
        assertThat(rule.check(FAKE, src)).hasSize(1);
    }

    @Test @DisplayName("skips example.com inside a // e.g. comment")
    void skipsEgComment() {
        String src = "// e.g. https://example.com/foo";
        assertThat(rule.check(FAKE, src)).isEmpty();
    }

    @Test @DisplayName("does not flag real URLs")
    void clean() {
        String src = "String u = \"https://api.stripe.com/v1/charges\";";
        assertThat(rule.check(FAKE, src)).isEmpty();
    }
}
