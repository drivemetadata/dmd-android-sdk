package com.drivemetadata.enums;

public enum LogLevel {
    /** No logging. */
    NONE,
    /** Log exceptions only. */
    INFO,
    /** Log exceptions and print debug output. */
    DEBUG,
    /**
     * Log exceptions and print debug output.
     *
     * @deprecated Use {@link LogLevel#DEBUG} instead.
     */
    @Deprecated
    BASIC,
    /** Same as {@link LogLevel#DEBUG}, and log transformations in bundled integrations. */
    VERBOSE;

    public boolean log() {
        return this != NONE;
    }
}
