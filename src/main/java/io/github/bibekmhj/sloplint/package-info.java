/**
 * sloplint — catch AI-generated code smells in Java before they ship.
 *
 * <p>A linter for JVM projects, specifically tuned for the patterns that show up
 * when an LLM writes the code: placeholder secrets, hallucinated URLs, silent
 * exception swallowing, trivial tests, {@code TODO}-throwing stubs, stray
 * {@code System.out.println}s.
 *
 * <p>Two entry points:
 * <ul>
 *   <li>{@link io.github.bibekmhj.sloplint.Sloplint} — the library API, for
 *       embedding into your own tools or CI scripts.</li>
 *   <li>{@link io.github.bibekmhj.sloplint.cli.Main} — the standalone CLI, packaged
 *       as an uber-jar for {@code java -jar sloplint-cli.jar path/to/src}.</li>
 * </ul>
 */
package io.github.bibekmhj.sloplint;
