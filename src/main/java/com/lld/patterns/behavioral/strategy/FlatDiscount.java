package com.lld.patterns.behavioral.strategy;

/**
 * A discount of a fixed amount, capped at the subtotal.
 *
 * <p>The amount is a non-negative number of cents; a negative amount is an
 * {@link IllegalArgumentException}. The discount returned is
 * {@code min(amountCents, subtotalCents)}, so it can never exceed the subtotal.
 */
public final class FlatDiscount implements DiscountStrategy {

    private final long amountCents;

    public FlatDiscount(long amountCents) {
        if (amountCents < 0) {
            throw new IllegalArgumentException("amountCents must be >= 0, was " + amountCents);
        }
        this.amountCents = amountCents;
    }

    @Override
    public long discountCents(long subtotalCents) {
        return Math.min(amountCents, subtotalCents);
    }
}
