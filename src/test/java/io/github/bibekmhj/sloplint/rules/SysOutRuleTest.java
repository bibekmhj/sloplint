/*
 * Copyright 2026 the sloplint authors
 * Licensed under the Apache License, Version 2.0.
 */
package io.github.bibekmhj.sloplint.rules;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class SysOutRuleTest {

    private final SysOutRule rule = new SysOutRule();
    private final Path FAKE = Path.of("Test.java");

    @Test @DisplayName("flags System.out.println in a regular method")
    void outsideMain() {
        String src = """
            class T {
              void foo() {
                System.out.println("debug");
              }
            }
            """;
        assertThat(rule.check(FAKE, src)).hasSize(1);
    }

    @Test @DisplayName("does not flag inside public static void main(String[])")
    void insideMain() {
        String src = """
            class T {
              public static void main(String[] args) {
                System.out.println("hello");
              }
            }
            """;
        assertThat(rule.check(FAKE, src)).isEmpty();
    }

    @Test @DisplayName("flags System.err.println too")
    void err() {
        String src = """
            class T {
              void foo() {
                System.err.println("oh no");
              }
            }
            """;
        assertThat(rule.check(FAKE, src)).hasSize(1);
    }
}
