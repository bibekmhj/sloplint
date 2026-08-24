# Rule catalog

Every rule has a stable code (`SLxxx`), a stable name (`kebab-case`), a default severity, and a plain-English description. Users may re-map any rule to any severity in configuration.

---

## SL001 — hardcoded-api-secret

**Default:** `ERROR`

Detects real-looking API keys committed as string literals or comments.

Currently recognized:
- AWS access key IDs (`AKIA…`, `ASIA…`)
- OpenAI `sk-…` and `sk-proj-…` (realistic length ≥ 40 chars)
- Anthropic `sk-ant-…`
- GitHub personal access tokens (`ghp_…`) and fine-grained tokens (`github_pat_…`)
- Google API keys (`AIza…`)
- Stripe live keys (`sk_live_…`, `pk_live_…`)
- PEM private-key block headers (`-----BEGIN RSA PRIVATE KEY-----`)

Not detected (too many false positives): random base64, JWT tokens, bcrypt hashes.

**How to fix:** Rotate the secret immediately, then read it from environment or a secret manager.

---

## SL002 — placeholder-marker

**Default:** `ERROR`

Placeholder text an LLM emitted that a human should have replaced before merge.

Patterns:
- `YOUR_..._HERE`, `YOUR_..._KEY`, `YOUR_..._TOKEN`
- `<REPLACE_ME>`, `<CHANGE_ME>`, `<YOUR_...>`, `<INSERT_...>`
- `CHANGE_ME` / `CHANGEME`
- Weak defaults in string literals: `password123`, `admin123`
- Six or more `x`/`X` characters inside a string (scaffold placeholders)
- `// TODO: add ...`, `// TODO: implement ...`, `// TODO: fill ...`

**How to fix:** Replace with a real value from configuration.

---

## SL003 — placeholder-url

**Default:** `WARNING`

URLs pointing to reserved-for-documentation domains or common invented placeholders.

Currently recognized:
- IANA reserved: `example.com`, `example.org`, `example.net`
- Common invented: `your-domain.com`, `mydomain.com`, `mysite.com`, `yoursite.com`, `foo.bar`

Skipped: matches inside comments beginning `// e.g.` or `* e.g.` (documentation examples).

**How to fix:** Point at the real endpoint. If the URL is intentionally illustrative in a comment, prefix with `// e.g.`.

---

## SL004 — todo-throw

**Default:** `WARNING`

Stub methods that throw an "unimplemented" exception with a TODO / not-implemented / stub marker in the message.

Patterns:
- `throw new UnsupportedOperationException("TODO...")`
- `throw new UnsupportedOperationException("not implemented...")`
- `throw new NotImplementedException(...)`
- `throw new RuntimeException("TODO...")`

Not flagged: `UnsupportedOperationException` with a legitimate message like `"immutable"`.

**How to fix:** Implement the method, or delete it if it isn't needed.

---

## SL005 — silent-catch

**Default:** `WARNING`

`catch` blocks that swallow the exception instead of handling, logging, or rethrowing it.

Flagged patterns:
- Empty catch body: `catch (Exception e) { }`
- Catch that only calls `e.printStackTrace()` — hides the error from real observability.

Not flagged: catches that log via a real logger, wrap and rethrow, or perform recovery.

**How to fix:** Log the exception with your logger, and either recover, rethrow, or return an error result.

---

## SL006 — stray-println

**Default:** `WARNING`

`System.out.println` / `System.err.println` / `System.out.print` calls in classes that are not a CLI `main`.

Skipped: calls inside `public static void main(String[] args)`.

**How to fix:** Use your project's logger. `System.out` isn't observable in production.

---

## SL007 — trivial-test

**Default:** `WARNING`

`@Test` methods that don't assert anything meaningful.

Flagged patterns:
- Empty test body: `@Test void x() { }`
- `assertTrue(true)` / `assertFalse(false)` as the only assertion
- `assertEquals(x, x)` with two identical literals as the only assertion
- `assertNotNull(new Something())` — the argument can never be null

**How to fix:** Delete the test if you don't know what to assert. A missing test is honest; a fake test is a lie.
