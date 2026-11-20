package com.lld.patterns.behavioral.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Functional behaviour (the spec) for the Strategy pattern.
 *
 * <p>Covers each concrete {@link DiscountStrategy} and the {@link PriceCalculator}
 * context that holds one: the context must delegate to the injected strategy and
 * swap it at runtime without callers caring.
 *
 * <p>Every assertion checks a concrete number, so an implementation that is off by
 * a rounding rule, forgets to cap a discount, or silently ignores the injected
 * strategy fails here.
 */
class StrategyTest {

    /** Convenience: assert a strategy yields exactly this discount. */
    private static void assertDiscount(DiscountStrategy strategy, long subtotal, long expected) {
        assertEquals(expected, strategy.discountCents(subtotal),
                "strategy " + strategy.getClass().getSimpleName() + " on subtotal " + subtotal);
    }

    // ---- NoDiscount ------------------------------------------------------

    /** The null strategy: a real object that always returns no discount. */
    @Test
    void no_discount_always_returns_zero() {
        assertDiscount(new NoDiscount(), 0, 0);
        assertDiscount(new NoDiscount(), 999, 0);
        assertDiscount(new NoDiscount(), 1_000_000, 0);
    }

    // ---- PercentageDiscount ---------------------------------------------

    /** A pure percentage of the subtotal, floored to whole cents. */
    @Test
    void percentage_discount_is_a_fraction_of_the_subtotal() {
        assertDiscount(new PercentageDiscount(10), 1_000, 100);
        assertDiscount(new PercentageDiscount(33), 100, 33);
        assertDiscount(new PercentageDiscount(15), 999, 149); // floor(999 * 15 / 100)
    }

    /** Boundary percentages: 0% is no discount, 100% is the whole subtotal. */
    @Test
    void percentage_boundaries_are_handled() {
        assertDiscount(new PercentageDiscount(0), 5_000, 0);
        assertDiscount(new PercentageDiscount(100), 5_000, 5_000);
    }

    /** A zero subtotal discounts to zero no matter the percentage. */
    @Test
    void percentage_of_zero_subtotal_is_zero() {
        assertDiscount(new PercentageDiscount(50), 0, 0);
    }

    /** Percentages outside 0..100 are rejected at construction. */
    @Test
    void percentage_rejects_out_of_range_values() {
        assertThrows(IllegalArgumentException.class, () -> new PercentageDiscount(-1));
        assertThrows(IllegalArgumentException.class, () -> new PercentageDiscount(101));
    }

    // ---- FlatDiscount ----------------------------------------------------

    /** A fixed amount is subtracted from the subtotal. */
    @Test
    void flat_discount_subtracts_a_fixed_amount() {
        assertDiscount(new FlatDiscount(500), 3_000, 500);
        assertDiscount(new FlatDiscount(0), 3_000, 0);
    }

    /** A flat discount may never exceed the subtotal (the price can't go negative). */
    @Test
    void flat_discount_is_capped_at_the_subtotal() {
        assertDiscount(new FlatDiscount(500), 300, 300);
        assertDiscount(new FlatDiscount(500), 500, 500);
    }

    /** A negative flat amount is rejected at construction. */
    @Test
    void flat_discount_rejects_a_negative_amount() {
        assertThrows(IllegalArgumentException.class, () -> new FlatDiscount(-1));
    }

    // ---- TieredDiscount --------------------------------------------------

    /** Below the first threshold: no discount. */
    @Test
    void tiered_below_the_first_threshold_gives_no_discount() {
        assertDiscount(new TieredDiscount(), 0, 0);
        assertDiscount(new TieredDiscount(), 9_999, 0);
    }

    /** Middle tier (>= $100 and < $500): 5%. */
    @Test
    void tiered_middle_tier_applies_five_percent() {
        assertDiscount(new TieredDiscount(), 10_000, 500);
        assertDiscount(new TieredDiscount(), 20_000, 1_000);
        assertDiscount(new TieredDiscount(), 49_999, 2_499); // floor(49_999 * 5 / 100)
    }

    /** Top tier (>= $500): 10%. */
    @Test
    void tiered_top_tier_applies_ten_percent() {
        assertDiscount(new TieredDiscount(), 50_000, 5_000);
        assertDiscount(new TieredDiscount(), 123_456, 12_345); // floor(123_456 * 10 / 100)
    }

    // ---- PriceCalculator (the context) ----------------------------------

    /** The context delegates to the injected strategy and subtracts the discount. */
    @Test
    void context_applies_the_injected_strategy() {
        PriceCalculator calculator = new PriceCalculator(new PercentageDiscount(10));
        assertEquals(900, calculator.finalPriceCents(1_000));
    }

    /** The strategy can be swapped at runtime; the next call uses the new one. */
    @Test
    void context_swaps_strategy_at_runtime() {
        PriceCalculator calculator = new PriceCalculator(new PercentageDiscount(10));
        assertEquals(900, calculator.finalPriceCents(1_000));

        calculator.setStrategy(new FlatDiscount(200));
        assertEquals(800, calculator.finalPriceCents(1_000), "the new strategy takes effect");
    }

    /**
     * The context defends against a strategy that breaks its contract: a discount
     * larger than the subtotal is clamped so the price never goes negative, and a
     * negative discount is clamped to zero so the price never exceeds the subtotal.
     */
    @Test
    void context_clamps_a_strategy_that_breaks_its_contract() {
        PriceCalculator overshooting = new PriceCalculator(subtotal -> subtotal + 10_000);
        assertEquals(0, overshooting.finalPriceCents(1_000));

        PriceCalculator negative = new PriceCalculator(subtotal -> -5);
        assertEquals(1_000, negative.finalPriceCents(1_000));
    }

    /** A negative subtotal is nonsense and is rejected. */
    @Test
    void context_rejects_a_negative_subtotal() {
        PriceCalculator calculator = new PriceCalculator(new NoDiscount());
        assertThrows(IllegalArgumentException.class, () -> calculator.finalPriceCents(-1));
    }

    /** A null strategy is rejected in the constructor and in the setter. */
    @Test
    void context_rejects_a_null_strategy() {
        assertThrows(NullPointerException.class, () -> new PriceCalculator(null));

        PriceCalculator calculator = new PriceCalculator(new NoDiscount());
        assertThrows(NullPointerException.class, () -> calculator.setStrategy(null));
    }

    /**
     * A lambda is a strategy: the seam is a {@link FunctionalInterface}, so a one-off
     * rule needs no class at all.
     */
    @Test
    void a_lambda_can_serve_as_a_strategy() {
        PriceCalculator calculator = new PriceCalculator(subtotal -> subtotal / 4);
        assertEquals(750, calculator.finalPriceCents(1_000));
    }
}
