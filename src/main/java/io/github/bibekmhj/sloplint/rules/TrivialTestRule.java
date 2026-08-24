/*
 * Copyright 2026 the sloplint authors
 * Licensed under the Apache License, Version 2.0.
 */
package io.github.bibekmhj.sloplint.rules;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.Statement;
import io.github.bibekmhj.sloplint.Finding;
import io.github.bibekmhj.sloplint.Severity;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * SL007 — Test methods that don't actually test anything.
 *
 * <p>Matches:
 * <ul>
 *   <li>{@code @Test} method with an empty body.</li>
 *   <li>{@code @Test} method whose only statement is {@code assertTrue(true)}
 *       or {@code assertFalse(false)}.</li>
 *   <li>{@code @Test} method whose only statement is {@code assertEquals(x, x)}
 *       with two literally-identical literals.</li>
 * </ul>
 *
 * <p>These are placeholder tests LLMs generate to hit coverage thresholds — the
 * kind that pass forever but assert nothing.
 */
public final class TrivialTestRule extends AbstractAstRule {

    public TrivialTestRule() {
        super("SL007", "trivial-test",
                "Test method has no meaningful assertion",
                Severity.WARNING);
    }

    @Override
    protected List<Finding> checkAst(Path file, CompilationUnit cu) {
        List<Finding> out = new ArrayList<>();
        cu.findAll(MethodDeclaration.class).forEach(md -> {
            if (!isJUnitTest(md)) return;
            Optional<BlockStmt> body = md.getBody();
            if (body.isEmpty()) return;
            List<Statement> stmts = body.get().getStatements();

            String reason = triviality(stmts);
            if (reason == null) return;

            int line = md.getRange().map(r -> r.begin.line).orElse(1);
            int col = md.getRange().map(r -> r.begin.column).orElse(1);
            out.add(findingBuilder()
                    .file(file)
                    .line(line)
                    .column(col)
                    .message("test " + md.getNameAsString() + "() " + reason)
                    .snippet(md.getDeclarationAsString(false, false, false))
                    .build());
        });
        return out;
    }

    /** Return a human-readable reason if the body is trivial, else null. */
    private static String triviality(List<Statement> stmts) {
        if (stmts.isEmpty()) return "has an empty body";
        if (stmts.size() != 1) return null;
        Statement only = stmts.get(0);
        if (!(only instanceof ExpressionStmt es)) return null;
        if (!(es.getExpression() instanceof MethodCallExpr mce)) return null;

        String name = mce.getNameAsString();
        List<Expression> args = mce.getArguments();

        if ("assertTrue".equals(name) && args.size() == 1
                && args.get(0) instanceof BooleanLiteralExpr b && b.getValue()) {
            return "asserts only assertTrue(true)";
        }
        if ("assertFalse".equals(name) && args.size() == 1
                && args.get(0) instanceof BooleanLiteralExpr b && !b.getValue()) {
            return "asserts only assertFalse(false)";
        }
        if ("assertEquals".equals(name) && args.size() == 2 && sameLiteral(args.get(0), args.get(1))) {
            return "asserts only assertEquals(x, x) with identical literals";
        }
        if ("assertNotNull".equals(name) && args.size() == 1
                && args.get(0) instanceof com.github.javaparser.ast.expr.ObjectCreationExpr) {
            return "asserts assertNotNull(new ...) — the value can never be null";
        }
        return null;
    }

    private static boolean sameLiteral(Expression a, Expression b) {
        if (a instanceof IntegerLiteralExpr ia && b instanceof IntegerLiteralExpr ib) {
            return ia.getValue().equals(ib.getValue());
        }
        if (a instanceof StringLiteralExpr sa && b instanceof StringLiteralExpr sb) {
            return sa.getValue().equals(sb.getValue());
        }
        if (a instanceof BooleanLiteralExpr ba && b instanceof BooleanLiteralExpr bb) {
            return ba.getValue() == bb.getValue();
        }
        return false;
    }

    private static boolean isJUnitTest(MethodDeclaration md) {
        return md.getAnnotations().stream().anyMatch(a -> {
            String n = a.getNameAsString();
            return "Test".equals(n) || "org.junit.Test".equals(n) || "org.junit.jupiter.api.Test".equals(n);
        });
    }
}
