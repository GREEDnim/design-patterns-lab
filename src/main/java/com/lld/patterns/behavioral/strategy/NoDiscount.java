package com.lld.patterns.behavioral.strategy;

/**
 * The null strategy: a real object that always applies no discount.
 *
 * <p>Implement {@link #discountCents(long)} to return {@code 0} for every subtotal.
 */
public final class NoDiscount implements DiscountStrategy {

    @Override
    public long discountCents(long subtotalCents) {
        return 0;
    }
}
