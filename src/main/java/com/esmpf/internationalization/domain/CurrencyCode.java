package com.esmpf.internationalization.domain;

import java.util.Currency;

public enum CurrencyCode {
    USD,
    EUR,
    GBP,
    CHF,
    PLN,
    CZK,
    SEK,
    NOK,
    DKK,
    KZT;

    public Currency currency() {
        return Currency.getInstance(name());
    }

    public int defaultFractionDigits() {
        return currency().getDefaultFractionDigits();
    }
}
