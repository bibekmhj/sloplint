/*
 * Copyright 2026 the sloplint authors
 * Licensed under the Apache License, Version 2.0.
 */
package io.github.bibekmhj.sloplint.rules;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class TodoThrowRuleTest {

    private final TodoThrowRule rule = new TodoThrowRule();
    private final Path FAKE = Path.of("Test.java");

    @Test @DisplayName("detects UnsupportedOperationException(\"TODO\")")
    void unsupportedTodo() {
        String src = "throw new UnsupportedOperationException(\"TODO: implement me\");";
        assertThat(rule.check(FAKE, src)).hasSize(1);
    }

    @Test @DisplayName("detects UnsupportedOperationException(\"Not implemented\")")
    void unsupportedNotImplemented() {
        String src = "throw new UnsupportedOperationException(\"Not implemented yet\");";
        assertThat(rule.check(FAKE, src)).hasSize(1);
    }

    @Test @DisplayName("detects RuntimeException(\"TODO ...\")")
    void runtimeTodo() {
        String src = "throw new RuntimeException(\"TODO refactor\");";
        assertThat(rule.check(FAKE, src)).hasSize(1);
    }

    @Test @DisplayName("does NOT flag UnsupportedOperationException with a legit message")
    void legitUnsupported() {
        String src = "throw new UnsupportedOperationException(\"immutable\");";
        assertThat(rule.check(FAKE, src)).isEmpty();
    }
}
