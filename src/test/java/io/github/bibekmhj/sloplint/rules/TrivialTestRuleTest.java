/*
 * Copyright 2026 the sloplint authors
 * Licensed under the Apache License, Version 2.0.
 */
package io.github.bibekmhj.sloplint.rules;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class TrivialTestRuleTest {

    private final TrivialTestRule rule = new TrivialTestRule();
    private final Path FAKE = Path.of("Test.java");

    @Test @DisplayName("flags @Test with empty body")
    void emptyBody() {
        String src = """
            import org.junit.jupiter.api.Test;
            class T {
              @Test void nothing() { }
            }
            """;
        assertThat(rule.check(FAKE, src)).anyMatch(f -> f.message().contains("empty"));
    }

    @Test @DisplayName("flags assertTrue(true)")
    void assertTrueTrue() {
        String src = """
            import org.junit.jupiter.api.Test;
            import static org.junit.jupiter.api.Assertions.assertTrue;
            class T {
              @Test void t() { assertTrue(true); }
            }
            """;
        assertThat(rule.check(FAKE, src)).anyMatch(f -> f.message().contains("assertTrue(true)"));
    }

    @Test @DisplayName("flags assertEquals(1, 1)")
    void assertEqualsSame() {
        String src = """
            import org.junit.jupiter.api.Test;
            import static org.junit.jupiter.api.Assertions.assertEquals;
            class T {
              @Test void t() { assertEquals(1, 1); }
            }
            """;
        assertThat(rule.check(FAKE, src)).anyMatch(f -> f.message().contains("identical"));
    }

    @Test @DisplayName("does not flag a real test")
    void realTest() {
        String src = """
            import org.junit.jupiter.api.Test;
            import static org.junit.jupiter.api.Assertions.assertEquals;
            class T {
              @Test void addsTwoAndTwo() {
                assertEquals(4, 2 + 2);
              }
            }
            """;
        assertThat(rule.check(FAKE, src)).isEmpty();
    }
}
