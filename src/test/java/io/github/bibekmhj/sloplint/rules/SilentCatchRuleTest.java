/*
 * Copyright 2026 the sloplint authors
 * Licensed under the Apache License, Version 2.0.
 *
 * NOTE: These tests exercise the real JavaParser. They run under `mvn test` and
 * will produce findings on the malformed-example code below. In-sandbox stubs
 * do not parse Java, so a stub run returns zero findings for AST rules.
 */
package io.github.bibekmhj.sloplint.rules;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class SilentCatchRuleTest {

    private final SilentCatchRule rule = new SilentCatchRule();
    private final Path FAKE = Path.of("Test.java");

    @Test @DisplayName("flags empty catch block")
    void emptyCatch() {
        String src = """
            class T {
              void m() {
                try { work(); }
                catch (Exception e) { }
              }
              void work() {}
            }
            """;
        assertThat(rule.check(FAKE, src))
                .anyMatch(f -> f.message().contains("empty"));
    }

    @Test @DisplayName("flags catch with only e.printStackTrace()")
    void printStackOnly() {
        String src = """
            class T {
              void m() {
                try { work(); }
                catch (Exception e) { e.printStackTrace(); }
              }
              void work() {}
            }
            """;
        assertThat(rule.check(FAKE, src))
                .anyMatch(f -> f.message().contains("printStackTrace"));
    }

    @Test @DisplayName("does not flag catch with real handling")
    void realHandling() {
        String src = """
            class T {
              void m() {
                try { work(); }
                catch (Exception e) {
                  logger.error("failed", e);
                  throw new RuntimeException(e);
                }
              }
              void work() {}
              org.slf4j.Logger logger;
            }
            """;
        assertThat(rule.check(FAKE, src)).isEmpty();
    }
}
