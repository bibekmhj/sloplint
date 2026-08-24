/*
 * Copyright 2026 the sloplint authors
 * Licensed under the Apache License, Version 2.0.
 */
package io.github.bibekmhj.sloplint.cli;

import io.github.bibekmhj.sloplint.ScanRequest;
import io.github.bibekmhj.sloplint.ScanResult;
import io.github.bibekmhj.sloplint.Severity;
import io.github.bibekmhj.sloplint.Sloplint;
import io.github.bibekmhj.sloplint.output.JsonFormatter;
import io.github.bibekmhj.sloplint.output.SarifFormatter;
import io.github.bibekmhj.sloplint.output.TextFormatter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * The sloplint CLI. Packaged as an uber-jar; invoked with:
 *
 * <pre>{@code
 *   java -jar sloplint-0.1.0-cli.jar [options] <path>...
 * }</pre>
 *
 * <p>Exit codes:
 * <ul>
 *   <li>{@code 0} — no findings at or above the {@code --fail-on} threshold</li>
 *   <li>{@code 1} — at least one finding at or above the threshold</li>
 *   <li>{@code 2} — usage error</li>
 * </ul>
 */
public final class Main {

    private static final String USAGE = """
            sloplint 0.1 — catch AI-generated code smells in Java

            usage:
              sloplint [options] <path>...

            options:
              --format text|json|sarif    output format (default: text)
              --enable  SL001,SL002,...   run only these rules
              --disable SL001,SL002,...   never run these rules
              --exclude 'glob,glob,...'   skip files matching (globs relative to root)
              --fail-on error|warning     exit non-zero on findings at this level
                                          (default: error)
              --output PATH               write report to file instead of stdout
              --no-color                  disable ANSI colors in text output
              --list-rules                print the rule catalog and exit
              -h, --help                  this help
            """;

    public static void main(String[] args) {
        try {
            System.exit(run(args));
        } catch (RuntimeException e) {
            System.err.println("sloplint: " + e.getMessage());
            System.exit(2);
        }
    }

    static int run(String[] args) {
        Sloplint sloplint = new Sloplint();

        String format = "text";
        Set<String> enable = new LinkedHashSet<>();
        Set<String> disable = new LinkedHashSet<>();
        List<String> excludes = new ArrayList<>();
        List<Path> paths = new ArrayList<>();
        Severity failOn = Severity.ERROR;
        Path output = null;
        boolean color = true;

        for (int i = 0; i < args.length; i++) {
            String a = args[i];
            switch (a) {
                case "-h", "--help" -> { System.out.println(USAGE); return 0; }
                case "--list-rules" -> {
                    for (var r : sloplint.rules()) {
                        System.out.printf("%-8s %-25s %s%n", r.code(), r.name(), r.description());
                    }
                    return 0;
                }
                case "--format"    -> format   = requireValue(args, ++i, a);
                case "--enable"    -> enable.addAll(splitCsv(requireValue(args, ++i, a)));
                case "--disable"   -> disable.addAll(splitCsv(requireValue(args, ++i, a)));
                case "--exclude"   -> excludes.addAll(splitCsv(requireValue(args, ++i, a)));
                case "--fail-on"   -> failOn   = parseSeverity(requireValue(args, ++i, a));
                case "--output"    -> output   = Paths.get(requireValue(args, ++i, a));
                case "--no-color"  -> color    = false;
                default -> {
                    if (a.startsWith("--")) { System.err.println("unknown option: " + a); return 2; }
                    paths.add(Paths.get(a));
                }
            }
        }
        if (paths.isEmpty()) {
            System.err.println(USAGE);
            return 2;
        }

        ScanRequest.Builder b = ScanRequest.builder();
        for (Path p : paths) b.addRoot(p);
        if (!enable.isEmpty())  b.enableRules(enable);
        if (!disable.isEmpty()) b.disableRules(disable);
        for (String g : excludes) b.addExcludeGlob(g);
        ScanRequest req = b.build();

        ScanResult result = sloplint.scan(req);

        String rendered = switch (format) {
            case "text"  -> new TextFormatter(color && output == null).format(result);
            case "json"  -> new JsonFormatter().format(result);
            case "sarif" -> new SarifFormatter(sloplint.rules(), "0.1.0").format(result);
            default -> {
                System.err.println("unknown format: " + format);
                yield "";
            }
        };
        if (rendered.isEmpty()) return 2;

        if (output != null) {
            try {
                Files.writeString(output, rendered);
            } catch (IOException e) {
                System.err.println("failed to write " + output + ": " + e.getMessage());
                return 2;
            }
        } else {
            System.out.print(rendered);
        }

        boolean shouldFail = switch (failOn) {
            case ERROR   -> result.hasErrors();
            case WARNING -> result.hasWarningsOrWorse();
            case INFO    -> result.hasFindings();
        };
        return shouldFail ? 1 : 0;
    }

    private static String requireValue(String[] args, int i, String opt) {
        if (i >= args.length) throw new IllegalArgumentException(opt + " requires a value");
        return args[i];
    }

    private static Set<String> splitCsv(String s) {
        Set<String> out = new LinkedHashSet<>();
        for (String p : s.split(",")) {
            String t = p.trim();
            if (!t.isEmpty()) out.add(t);
        }
        return out;
    }

    private static Severity parseSeverity(String s) {
        return switch (s.toLowerCase()) {
            case "error"   -> Severity.ERROR;
            case "warning" -> Severity.WARNING;
            case "info"    -> Severity.INFO;
            default -> throw new IllegalArgumentException("unknown --fail-on: " + s);
        };
    }
}
