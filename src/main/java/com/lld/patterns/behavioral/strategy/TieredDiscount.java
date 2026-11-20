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

    private static final long MIDDLE_TIER_MIN_CENTS = 10_000;
    private static final long TOP_TIER_MIN_CENTS = 50_000;
    private static final int MIDDLE_TIER_PERCENT = 5;
    private static final int TOP_TIER_PERCENT = 10;

    @Override
    public long discountCents(long subtotalCents) {
        int percent;
        if (subtotalCents < MIDDLE_TIER_MIN_CENTS) {
            percent = 0;
        } else if (subtotalCents < TOP_TIER_MIN_CENTS) {
            percent = MIDDLE_TIER_PERCENT;
        } else {
            percent = TOP_TIER_PERCENT;
        }
        return subtotalCents * percent / 100;
    }
}
