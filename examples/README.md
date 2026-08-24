# examples

## `sloppy-app/`

A deliberately terrible Java project that trips every sloplint rule. Useful for verifying an install works.

```bash
# from the sloplint repo root, after mvn package
java -jar target/sloplint-*-cli.jar examples/sloppy-app/src
```

Expected output: eight or so findings across two files, mixing errors and warnings.

If sloplint finds *nothing* in this folder, something is wrong with the install.
