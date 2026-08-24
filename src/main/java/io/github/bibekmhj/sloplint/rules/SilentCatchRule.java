/*
 * Copyright 2026 the sloplint authors
 * Licensed under the Apache License, Version 2.0.
 */
package io.github.bibekmhj.sloplint.rules;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.Statement;
import io.github.bibekmhj.sloplint.Finding;
import io.github.bibekmhj.sloplint.Severity;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * SL005 — Silent exception swallowing.
 *
 * <p>Matches three common LLM-produced patterns:
 * <ul>
 *   <li>Empty catch block: {@code catch (Exception e) { }}</li>
 *   <li>Catch that only calls {@code e.printStackTrace()} (and nothing else).</li>
 *   <li>Catch that only contains a comment (JavaParser's block sees no statements).</li>
 * </ul>
 *
 * <p>Does not flag: catch blocks that log with a real logger, rethrow, wrap, or
 * do any recovery work.
 */
public final class SilentCatchRule extends AbstractAstRule {

    public SilentCatchRule() {
        super("SL005", "silent-catch",
                "Catch block silently swallows the exception",
                Severity.WARNING);
    }

    @Override
    protected List<Finding> checkAst(Path file, CompilationUnit cu) {
        List<Finding> out = new ArrayList<>();
        cu.findAll(CatchClause.class).forEach(cc -> {
            BlockStmt body = cc.getBody();
            List<Statement> stmts = body.getStatements();

            boolean silent = false;
            String reason = null;

            if (stmts.isEmpty()) {
                silent = true;
                reason = "empty catch block";
            } else if (stmts.size() == 1 && isPrintStackTraceOnly(stmts.get(0))) {
                silent = true;
                reason = "catch calls only e.printStackTrace()";
            }

            if (silent) {
                Optional<com.github.javaparser.Range> range = cc.getRange();
                int line = range.map(r -> r.begin.line).orElse(1);
                int col = range.map(r -> r.begin.column).orElse(1);
                out.add(findingBuilder()
                        .file(file)
                        .line(line)
                        .column(col)
                        .message(reason)
                        .snippet(oneLine(cc.toString()))
                        .build());
            }
        });
        return out;
    }

    private static boolean isPrintStackTraceOnly(Statement s) {
        if (!(s instanceof ExpressionStmt es)) return false;
        if (!(es.getExpression() instanceof MethodCallExpr mce)) return false;
        return "printStackTrace".equals(mce.getNameAsString());
    }

    private static String oneLine(String s) {
        return s.replace('\n', ' ').replaceAll("\\s+", " ");
    }

    // Kept for future — unused now.
    @SuppressWarnings("unused")
    private static int lineOf(Node n) {
        return n.getRange().map(r -> r.begin.line).orElse(1);
    }
}
