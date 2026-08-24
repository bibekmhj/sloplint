# Contributing to sloplint

Rules are the interesting part. If you have a specific AI-generated Java pattern that keeps slipping past code review, file an issue with the offending snippet.

## Adding a rule

Every rule needs three things:

1. **A test file with a true positive and a true negative** in `src/test/java/io/github/bibekmhj/sloplint/rules/`. Follow the pattern of the existing `*RuleTest.java` classes.
2. **A one-sentence description**, plain English, that would fit in the README rule table.
3. **A default severity with rationale** — most new rules should be `WARNING`; reserve `ERROR` for genuine correctness or security issues that should fail the build by default.

For regex-based rules, extend `AbstractRegexRule`. For AST-based rules, extend `AbstractAstRule` and use JavaParser to walk the tree.

Add your rule to `Sloplint.defaultRules()` so it ships in the built-in set.

## Rules we won't accept

- Anything that duplicates SpotBugs, Checkstyle, PMD, or Error Prone. Those tools are excellent; sloplint's value is the AI-specific patterns they don't cover.
- Rules with a false-positive rate above ~5% on real production Java. Every false positive erodes trust in the tool.
- Style-only rules (spacing, brace placement, import order). That's what a formatter is for.
- Rules that require the user to change their build to use them. sloplint runs on any source tree.

## Local development

```bash
mvn -q verify
```

Java 17+ required. Tests run offline; no network.

## Coding conventions

- Public APIs are immutable value types with `Builder`s where they need to grow.
- All public types have Javadoc. All public methods have a one-line summary minimum.
- `-Xlint:all -Werror` must stay clean.
- No emoji in code or commit messages.

## Commit style

- Present-tense imperative subject line, ≤ 72 chars. Example: `Add SL008 rule for missing null checks`.
- Body explains *why*, not what.
- One logical change per commit.

## PR checklist

- [ ] Tests added or updated
- [ ] `mvn -q verify` passes locally
- [ ] Public APIs documented
- [ ] `CHANGELOG.md` entry under `## Unreleased`
- [ ] Rule added to `Sloplint.defaultRules()` if it's a new rule
