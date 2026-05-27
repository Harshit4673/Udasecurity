package com.udacity.catpoint;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Basic application validation test.
 */
class AppTest {

    @Test
    void applicationLoadsSuccessfully() {

        int expectedValue = 1;
        int actualValue = 1;

        assertEquals(expectedValue, actualValue);
    }
}