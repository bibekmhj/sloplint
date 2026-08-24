/*
 * Copyright 2026 the sloplint authors
 * Licensed under the Apache License, Version 2.0.
 */
package io.github.bibekmhj.sloplint;

/** How seriously to treat a finding. Users may re-map any rule to any severity. */
public enum Severity {
    /** Informational only. Does not fail the build even with {@code --fail-on=warning}. */
    INFO,
    /** Warning. Fails the build with {@code --fail-on=warning} or stricter. */
    WARNING,
    /** Error. Always fails the build. */
    ERROR
}
