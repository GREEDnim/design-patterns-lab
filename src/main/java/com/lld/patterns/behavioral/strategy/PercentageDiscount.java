package com.lld.patterns.behavioral.strategy;

/**
 * A discount of a fixed percentage of the subtotal.
 *
 * <p>The percentage is a valid value in {@code 0..100} (inclusive); anything else is
 * an {@link IllegalArgumentException}. The discount is {@code floor(subtotal * percent / 100)}.
 * Keep the percentage in a {@code final} field so the instance stays immutable and
 * safe to share.
 */
public final class PercentageDiscount implements DiscountStrategy {

    public PercentageDiscount(int percent) {
        throw new UnsupportedOperationException("TODO: validate and store percent");
    }

    @Override
    public long discountCents(long subtotalCents) {
        throw new UnsupportedOperationException("TODO: implement PercentageDiscount");
    }
}
