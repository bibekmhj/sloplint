# sloplint

### Catch AI-generated code smells in Java before they ship.

[![Java 17+](https://img.shields.io/badge/Java-17%2B-informational)](#compatibility)
[![License: Apache-2.0](https://img.shields.io/badge/License-Apache%202.0-blue)](LICENSE)
[![CI](https://github.com/bibekmhj/sloplint/actions/workflows/ci.yml/badge.svg)](https://github.com/bibekmhj/sloplint/actions)

**The first AI-slop linter for the JVM.** Every existing "AI-slop detector" is Python or JavaScript. sloplint is built for the Java world — where AI-assisted coding is now colliding with enterprise quality gates.

## The problem

When an LLM writes your Java code, it leaves a specific set of fingerprints that human review often misses because they don't look wrong at a glance:

- **`YOUR_API_KEY_HERE`** as a string literal — never replaced.
- **`throw new UnsupportedOperationException("TODO")`** in a class that ships.
- **`catch (Exception e) { e.printStackTrace(); }`** — the error is now invisible.
- **`@Test void x() { assertTrue(true); }`** — a test that will pass forever, asserting nothing.
- **A real GitHub personal access token** pasted into a `String` literal.
- **`System.out.println("debug")`** left in a `Service` class.
- **`https://example.com/foo`** as an API base URL.

Traditional Java linters (SpotBugs, Checkstyle, Error Prone) don't check for any of these. sloplint does.

## The 7 rules that ship with 0.1

| Code  | Rule                    | Default   | What it catches |
|-------|-------------------------|-----------|-----------------|
| SL001 | `hardcoded-api-secret`  | ERROR     | AWS, OpenAI, Anthropic, GitHub, Stripe, Google API keys and PEM private keys committed to source |
| SL002 | `placeholder-marker`    | ERROR     | `YOUR_..._HERE`, `<REPLACE_ME>`, `CHANGE_ME`, `password123`, `xxxxxxxx` scaffolds |
| SL003 | `placeholder-url`       | WARNING   | `example.com`, `your-domain.com`, `mysite.com`, `foo.bar` — reserved or invented domains |
| SL004 | `todo-throw`            | WARNING   | `UnsupportedOperationException("TODO")` and its cousins — stub methods that pretend to work |
| SL005 | `silent-catch`          | WARNING   | Empty catches and catches that only call `e.printStackTrace()` |
| SL006 | `stray-println`         | WARNING   | `System.out.println` / `System.err.println` outside a `main` method |
| SL007 | `trivial-test`          | WARNING   | `@Test` methods with empty bodies, `assertTrue(true)`, `assertEquals(1, 1)` |

Every rule is either false-positive-safe or has a documented way to suppress.

## Install

sloplint ships as a standalone jar. No project changes required.

Download from the [latest release](https://github.com/bibekmhj/sloplint/releases/latest):

```bash
curl -L -o sloplint.jar \
  https://github.com/bibekmhj/sloplint/releases/latest/download/sloplint-cli.jar
```

Or from Maven Central once 0.1.0 is published:

```xml
<dependency>
    <groupId>io.github.bibekmhj</groupId>
    <artifactId>sloplint</artifactId>
    <version>0.1.0</version>
</dependency>
```

## Run it

```bash
java -jar sloplint.jar src/main/java
```

Sample output on a sloppy file:

```
src/main/java/example/PaymentService.java:12:22: ERROR [SL001 hardcoded-api-secret] hardcoded secret matching a known provider format: sk_liv…KLmn
    | private String stripeKey = "sk_live_51KabcXYZabcdefghijKLmn";
src/main/java/example/PaymentService.java:14:22: ERROR [SL002 placeholder-marker] placeholder marker not replaced: YOUR_WEBHOOK_SECRET_HERE
    | private String webhook = "YOUR_WEBHOOK_SECRET_HERE";
src/main/java/example/PaymentService.java:25:9:  WARNING [SL005 silent-catch] catch calls only e.printStackTrace()
    | catch (Exception e) { e.printStackTrace(); }
src/main/java/example/PaymentServiceTest.java:8:5: WARNING [SL007 trivial-test] test testCharge() asserts only assertTrue(true)
    | @Test void testCharge()

scanned 12 files in 340ms — 2 errors, 2 warnings
```

## Common integrations

### GitHub Actions (with inline PR annotations via SARIF)

```yaml
- name: Run sloplint
  run: |
    java -jar sloplint.jar --format sarif --output sloplint.sarif src/
- name: Upload SARIF to Code Scanning
  if: always()
  uses: github/codeql-action/upload-sarif@v3
  with:
    sarif_file: sloplint.sarif
```

### Pre-commit hook

```yaml
# .pre-commit-config.yaml
- repo: local
  hooks:
    - id: sloplint
      name: sloplint
      language: system
      entry: java -jar tools/sloplint.jar --fail-on error
      files: '\.java$'
      pass_filenames: false
```

## CLI options

```
usage:
  sloplint [options] <path>...

options:
  --format text|json|sarif    output format (default: text)
  --enable  SL001,SL002,...   run only these rules
  --disable SL001,SL002,...   never run these rules
  --exclude 'glob,glob,...'   skip files matching (globs relative to root)
  --fail-on error|warning     exit non-zero on findings at this level (default: error)
  --output PATH               write report to file instead of stdout
  --no-color                  disable ANSI colors in text output
  --list-rules                print the rule catalog and exit
  -h, --help                  this help
```

## Why not just add these to SpotBugs / Checkstyle?

You should - and we may ship those integrations later. sloplint exists as a standalone tool because:

1. **Zero-config.** No project changes, no plugin registration, no dependency graph.
2. **Runs on any source tree, even without a build.** Great for reviewing an AI-generated PR from someone whose local build you don't have.
3. **SARIF-first output.** Drops directly into GitHub Code Scanning without wrappers.
4. **The rules are opinionated for the AI-generated-code case.** They'd be a poor fit for a general-purpose linter's rule set.

Coexists with SpotBugs, PMD, Checkstyle, and Error Prone. Nothing overlaps.

## Compatibility

| Component       | Supported                              |
|-----------------|----------------------------------------|
| Java (runtime)  | 17, 21, 22, 23                         |
| Java (analyzed) | 8 through 23                           |
| Build tool      | Any — sloplint scans source files      |

Kotlin scanning is a v0.2 goal. See [issues](https://github.com/bibekmhj/sloplint/issues).

## Roadmap

- **v0.2** — Maven plugin, Gradle plugin, Kotlin source support, `// sloplint:disable` inline suppression, `.sloplint.toml` config file.
- **v0.3** — Rules for hallucinated imports (needs classpath resolution), suspiciously generic Javadoc, duplicated boilerplate methods.
- **v1.0** — Stable rule IDs, semver on the CLI flags, published Maven Central release with GPG signatures.

## Contributing

Rules are the fun part. If you have a specific AI-generated Java pattern that keeps slipping past code review, file an issue with the offending snippet and a proposed fix. Rule PRs need:

1. A test file demonstrating a true positive and a true negative.
2. A one-sentence rule description.
3. A default severity with rationale.

See [`CONTRIBUTING.md`](CONTRIBUTING.md) for the full flow.

## License

Apache 2.0. See [`LICENSE`](LICENSE).
