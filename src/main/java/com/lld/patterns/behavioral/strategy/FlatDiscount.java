package com.lld.patterns.behavioral.strategy;

/**
 * A discount of a fixed amount, capped at the subtotal.
 *
 * <p>The amount is a non-negative number of cents; a negative amount is an
 * {@link IllegalArgumentException}. The discount returned is
 * {@code min(amountCents, subtotalCents)}, so it can never exceed the subtotal.
 */
public final class FlatDiscount implements DiscountStrategy {

    public FlatDiscount(long amountCents) {
        throw new UnsupportedOperationException("TODO: validate and store amountCents");
    }

    @Override
    public long discountCents(long subtotalCents) {
        throw new UnsupportedOperationException("TODO: implement FlatDiscount");
    }
}
