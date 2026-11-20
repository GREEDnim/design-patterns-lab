package com.lld.patterns.behavioral.strategy;

/**
 * A discount whose percentage depends on the subtotal tier.
 *
 * <p>Tiers (all in cents):
 * <ul>
 *   <li>{@code < 10_000} → 0%</li>
 *   <li>{@code 10_000 .. 49_999} → 5%</li>
 *   <li>{@code >= 50_000} → 10%</li>
 * </ul>
 * Percentages floor to whole cents, as in {@link PercentageDiscount}.
 */
public final class TieredDiscount implements DiscountStrategy {

    @Override
    public long discountCents(long subtotalCents) {
        throw new UnsupportedOperationException("TODO: implement TieredDiscount");
    }
}
