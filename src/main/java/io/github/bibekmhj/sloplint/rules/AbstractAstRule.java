/*
 * Copyright 2026 the sloplint authors
 * Licensed under the Apache License, Version 2.0.
 */
package io.github.bibekmhj.sloplint.rules;

import com.github.javaparser.ParseResult;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import io.github.bibekmhj.sloplint.Finding;
import io.github.bibekmhj.sloplint.Rule;
import io.github.bibekmhj.sloplint.Severity;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Base class for rules that need a parsed AST. Subclasses receive a
 * {@link CompilationUnit} and return findings.
 *
 * <p>Files with parse errors are silently skipped by AST rules — those errors
 * belong to {@code javac}, not sloplint.
 */
public abstract class AbstractAstRule implements Rule {

    private final String code;
    private final String name;
    private final String description;
    private final Severity severity;

    protected AbstractAstRule(String code, String name, String description, Severity severity) {
        this.code = Objects.requireNonNull(code);
        this.name = Objects.requireNonNull(name);
        this.description = Objects.requireNonNull(description);
        this.severity = Objects.requireNonNull(severity);
    }

    @Override public String code() { return code; }
    @Override public String name() { return name; }
    @Override public String description() { return description; }
    @Override public Severity defaultSeverity() { return severity; }

    protected abstract List<Finding> checkAst(Path file, CompilationUnit cu);

    @Override
    public final List<Finding> check(Path file, String source) {
        CompilationUnit cu;
        try {
            ParseResult<CompilationUnit> pr = new com.github.javaparser.JavaParser().parse(source);
            if (!pr.getResult().isPresent()) return Collections.emptyList();
            cu = pr.getResult().get();
        } catch (RuntimeException e) {
            return Collections.emptyList();
        }
        return checkAst(file, cu);
    }

    protected Finding.Builder findingBuilder() {
        return Finding.builder()
                .ruleCode(code)
                .ruleName(name)
                .severity(severity);
    }
}
