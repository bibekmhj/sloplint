/*
 * Copyright 2026 the sloplint authors
 * Licensed under the Apache License, Version 2.0.
 */
package io.github.bibekmhj.sloplint;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SloplintIntegrationTest {

    @Test @DisplayName("scans a directory and finds regex-based violations end-to-end")
    void endToEndRegex(@TempDir Path root) throws IOException {
        Files.writeString(root.resolve("Dirty.java"),
                "class Dirty {\n" +
                "    String key = \"ghp_1234567890abcdef1234567890abcdef1234\";\n" +
                "    String placeholder = \"YOUR_API_KEY_HERE\";\n" +
                "    String url = \"https://example.com/foo\";\n" +
                "}\n");

        Sloplint s = new Sloplint();
        ScanResult r = s.scan(ScanRequest.builder().addRoot(root).build());

        assertThat(r.filesScanned()).isEqualTo(1);
        assertThat(r.findings()).hasSizeGreaterThanOrEqualTo(3);
        assertThat(r.findings().stream().map(Finding::ruleCode).toList())
                .contains("SL001", "SL002", "SL003");
    }

    @Test @DisplayName("--disable removes a rule from the scan")
    void disableRule(@TempDir Path root) throws IOException {
        Files.writeString(root.resolve("A.java"), "String x = \"YOUR_KEY_HERE\";");
        ScanResult withRule = new Sloplint().scan(
                ScanRequest.builder().addRoot(root).build());
        ScanResult without = new Sloplint().scan(
                ScanRequest.builder().addRoot(root).disableRules(Set.of("SL002")).build());
        assertThat(withRule.findings()).isNotEmpty();
        assertThat(without.findings()).noneMatch(f -> f.ruleCode().equals("SL002"));
    }

    @Test @DisplayName("--enable runs only listed rules")
    void enableOnly(@TempDir Path root) throws IOException {
        Files.writeString(root.resolve("A.java"),
                "String s1 = \"ghp_1234567890abcdef1234567890abcdef1234\";\n" +
                "String s2 = \"YOUR_TOKEN_HERE\";\n");
        ScanResult r = new Sloplint().scan(
                ScanRequest.builder().addRoot(root).enableRules(Set.of("SL001")).build());
        assertThat(r.findings()).allMatch(f -> f.ruleCode().equals("SL001"));
    }

    @Test @DisplayName("--exclude skips matching files")
    void excludeGlob(@TempDir Path root) throws IOException {
        Files.createDirectories(root.resolve("gen"));
        Files.writeString(root.resolve("gen").resolve("Ignored.java"),
                "String x = \"YOUR_KEY_HERE\";");
        Files.writeString(root.resolve("Real.java"),
                "String x = \"YOUR_KEY_HERE\";");
        ScanResult r = new Sloplint().scan(
                ScanRequest.builder().addRoot(root).addExcludeGlob("gen/**").build());
        assertThat(r.findings()).allMatch(f -> !f.file().toString().contains("Ignored.java"));
    }
}
