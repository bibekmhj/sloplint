# Changelog

## Unreleased

## 0.1.0 — 2026-08-23

Initial release.

### Added

- Seven built-in rules covering the most common AI-generated Java code smells:
  - `SL001` hardcoded-api-secret (ERROR) — AWS, OpenAI, Anthropic, GitHub, Stripe, Google API keys and PEM private keys
  - `SL002` placeholder-marker (ERROR) — `YOUR_..._HERE`, `<REPLACE_ME>`, weak default passwords
  - `SL003` placeholder-url (WARNING) — `example.com`, `your-domain.com`, invented placeholders
  - `SL004` todo-throw (WARNING) — `UnsupportedOperationException("TODO")` stub methods
  - `SL005` silent-catch (WARNING) — empty catch blocks and `e.printStackTrace()`-only catches
  - `SL006` stray-println (WARNING) — `System.out.println` outside `main`
  - `SL007` trivial-test (WARNING) — `@Test` methods that assert nothing meaningful
- CLI with `--format text|json|sarif`, `--enable`/`--disable`/`--exclude` filtering, `--fail-on error|warning` gating, `--output` file redirection.
- Library API (`Sloplint`, `ScanRequest`, `ScanResult`, `Finding`) for embedding.
- SARIF 2.1.0 output for GitHub Code Scanning integration.
