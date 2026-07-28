package com.esmpf.internationalization.domain;

public final class CurrencyMismatchException extends IllegalArgumentException {

    public CurrencyMismatchException(CurrencyCode left, CurrencyCode right) {
        super("Currency mismatch: " + left + " and " + right);
    }
}
