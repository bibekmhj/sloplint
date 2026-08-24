package example;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * These tests all pass forever without asserting anything real —
 * exactly the kind of "coverage padding" sloplint's SL007 rule catches.
 */
class PaymentServiceTest {

    @Test
    void testCharge() {
        // SL007 — asserts nothing
        assertTrue(true);
    }

    @Test
    void testChargeAgain() {
        // SL007 — identical-literal comparison
        assertEquals(1, 1);
    }

    @Test
    void testConfig() {
        // SL007 — empty body
    }
}
