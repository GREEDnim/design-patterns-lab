package com.lld.patterns.behavioral.strategy;

/**
 * The Strategy seam for order pricing: one interchangeable discount algorithm.
 *
 * <p>This is the abstract strategy. Concrete strategies (percentage, flat, tiered,
 * none) implement it, and a {@code PriceCalculator} context holds one and delegates
 * to it. Because pricing is a pure computation, implementations must be
 * <em>stateless and immutable</em> — a single instance can then be shared across
 * threads and swapped at runtime without locks.
 *
 * <p>Being a {@link FunctionalInterface} is deliberate: a one-off rule can be a
 * lambda, e.g. {@code subtotal -> subtotal / 4}.
 */
@FunctionalInterface
public interface DiscountStrategy {

    /**
     * @param subtotalCents the order subtotal in cents; never negative
     * @return the discount to apply, in cents, within {@code [0, subtotalCents]}
     */
    long discountCents(long subtotalCents);
}
