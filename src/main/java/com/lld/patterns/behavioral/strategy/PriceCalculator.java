package com.lld.patterns.behavioral.strategy;

/**
 * The Strategy context: computes a final price by delegating the discount to a
 * {@link DiscountStrategy}.
 *
 * <p>The context knows <em>when</em> to price; the strategy knows <em>how</em>. The
 * strategy can be swapped at runtime via {@link #setStrategy(DiscountStrategy)}.
 *
 * <p>Stateless strategies are shareable, so the strategy field only needs safe
 * publication (make it {@code volatile}) — not a lock.
 */
public final class PriceCalculator {

    private DiscountStrategy strategy;

    public PriceCalculator(DiscountStrategy strategy) {
        this.strategy = strategy;
    }

    /** Swap the discount rule. Must reject null. */
    public void setStrategy(DiscountStrategy strategy) {
        throw new UnsupportedOperationException("TODO: implement setStrategy");
    }

    /**
     * @param subtotalCents a non-negative subtotal; negative values are rejected
     * @return the subtotal minus the strategy's discount, with the discount clamped
     *         into {@code [0, subtotalCents]}
     */
    public long finalPriceCents(long subtotalCents) {
        throw new UnsupportedOperationException("TODO: implement finalPriceCents");
    }
}
