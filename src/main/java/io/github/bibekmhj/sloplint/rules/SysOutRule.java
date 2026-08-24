/*
 * Copyright 2026 the sloplint authors
 * Licensed under the Apache License, Version 2.0.
 */
package io.github.bibekmhj.sloplint.rules;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import io.github.bibekmhj.sloplint.Finding;
import io.github.bibekmhj.sloplint.Severity;
import com.github.javaparser.ast.Node;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * SL006 — Stray {@code System.out.println} / {@code System.err.println} calls.
 *
 * <p>Skipped inside {@code public static void main(String[] args)} — CLI main
 * methods are the one place these belong. Everywhere else, they are debug
 * output the LLM added and forgot to remove.
 */
public final class SysOutRule extends AbstractAstRule {

    public SysOutRule() {
        super("SL006", "stray-println",
                "Stray System.out.println/System.err.println outside main()",
                Severity.WARNING);
    }

    @Override
    protected List<Finding> checkAst(Path file, CompilationUnit cu) {
        List<Finding> out = new ArrayList<>();
        cu.findAll(MethodCallExpr.class).forEach(mce -> {
            if (!isPrintlnCall(mce) && !isPrintCall(mce)) return;
            if (insideMainMethod(mce)) return;

            Optional<com.github.javaparser.Range> range = mce.getRange();
            int line = range.map(r -> r.begin.line).orElse(1);
            int col = range.map(r -> r.begin.column).orElse(1);
            out.add(findingBuilder()
                    .file(file)
                    .line(line)
                    .column(col)
                    .message("stray " + describe(mce) + " outside main() — use a logger")
                    .snippet(mce.toString())
                    .build());
        });
        return out;
    }

    private static boolean isPrintlnCall(MethodCallExpr mce) {
        return isSystemStream(mce) && "println".equals(mce.getNameAsString());
    }

    private static boolean isPrintCall(MethodCallExpr mce) {
        return isSystemStream(mce) && "print".equals(mce.getNameAsString());
    }

    private static boolean isSystemStream(MethodCallExpr mce) {
        if (mce.getScope().isEmpty()) return false;
        var scope = mce.getScope().get();
        if (!(scope instanceof FieldAccessExpr fae)) return false;
        String field = fae.getNameAsString();
        if (!"out".equals(field) && !"err".equals(field)) return false;
        var inner = fae.getScope();
        return inner instanceof NameExpr ne && "System".equals(ne.getNameAsString());
    }

    private static String describe(MethodCallExpr mce) {
        FieldAccessExpr fae = (FieldAccessExpr) mce.getScope().orElseThrow();
        return "System." + fae.getNameAsString() + "." + mce.getNameAsString() + "()";
    }

    private static boolean insideMainMethod(MethodCallExpr mce) {
    Optional<Node> current = mce.getParentNode();

    while (current.isPresent()) {
        Node node = current.get();

        if (node instanceof MethodDeclaration m) {
            return m.isStatic()
                    && m.isPublic()
                    && "main".equals(m.getNameAsString())
                    && m.getParameters().size() == 1
                    && m.getParameter(0).getType().toString().contains("String");
        }

        current = node.getParentNode();
    }

    return false;
    }
}
