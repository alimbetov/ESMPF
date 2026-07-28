package com.esmpf.internationalization.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record Money(BigDecimal amount, CurrencyCode currency) {

    public Money {
        Objects.requireNonNull(amount, "amount is required");
        Objects.requireNonNull(currency, "currency is required");
        amount = normalize(amount, currency);
    }

    public static Money zero(CurrencyCode currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    public Money add(Money other) {
        requireSameCurrency(other);
        return new Money(amount.add(other.amount), currency);
    }

    public Money subtract(Money other) {
        requireSameCurrency(other);
        return new Money(amount.subtract(other.amount), currency);
    }

    public Money multiply(BigDecimal multiplier) {
        Objects.requireNonNull(multiplier, "multiplier is required");
        return new Money(amount.multiply(multiplier), currency);
    }

    public boolean isNegative() {
        return amount.signum() < 0;
    }

    private void requireSameCurrency(Money other) {
        Objects.requireNonNull(other, "money is required");
        if (currency != other.currency) {
            throw new CurrencyMismatchException(currency, other.currency);
        }
    }

    private static BigDecimal normalize(BigDecimal amount, CurrencyCode currency) {
        return amount.setScale(currency.defaultFractionDigits(), RoundingMode.HALF_EVEN);
    }
}
