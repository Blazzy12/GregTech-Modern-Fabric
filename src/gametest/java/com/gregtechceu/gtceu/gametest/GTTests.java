package com.gregtechceu.gtceu.gametest;

import java.util.Arrays;

/** Picks which client game tests run: all of them, or only those named in GTCEU_TESTS (comma separated). */
final class GTTests {

    private static final String ONLY = System.getenv("GTCEU_TESTS");

    private GTTests() {}

    static boolean enabled(String name) {
        if (GTRemoteServerGameTest.ADDRESS != null) return false;
        return ONLY == null || Arrays.asList(ONLY.split(",")).contains(name);
    }
}
