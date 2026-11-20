package com.lld.patterns.behavioral.strategy;

import java.util.Objects;

/**
 * The Strategy context: computes a final price by delegating the discount to a
 * {@link DiscountStrategy}.
 *
 * <p>The context knows <em>when</em> to price; the strategy knows <em>how</em>. The
 * strategy can be swapped at runtime via {@link #setStrategy(DiscountStrategy)}.
 *
 * <p>Stateless strategies are shareable, so the strategy field only needs safe
 * publication (it is {@code volatile}) — not a lock.
 */
public final class PriceCalculator {

    private volatile DiscountStrategy strategy;

    public PriceCalculator(DiscountStrategy strategy) {
        this.strategy = Objects.requireNonNull(strategy, "strategy");
    }

    /** Swap the discount rule. Rejects null. */
    public void setStrategy(DiscountStrategy strategy) {
        this.strategy = Objects.requireNonNull(strategy, "strategy");
    }

    /**
     * @param subtotalCents a non-negative subtotal; negative values are rejected
     * @return the subtotal minus the strategy's discount, with the discount clamped
     *         into {@code [0, subtotalCents]}
     */
    public long finalPriceCents(long subtotalCents) {
        if (subtotalCents < 0) {
            throw new IllegalArgumentException("subtotalCents must be >= 0, was " + subtotalCents);
        }
        long discount = strategy.discountCents(subtotalCents);
        return subtotalCents - Math.max(Math.min(discount, subtotalCents), 0);
    }
}
