package com.esmpf.internationalization.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class MoneyTests {

    @Test
    void normalizesUsingCurrencyFractionDigitsAndHalfEvenRounding() {
        Money money = new Money(new BigDecimal("10.125"), CurrencyCode.USD);

        assertEquals(new BigDecimal("10.12"), money.amount());
    }

    @Test
    void addsAmountsInSameCurrency() {
        Money result = new Money(new BigDecimal("10.10"), CurrencyCode.EUR)
                .add(new Money(new BigDecimal("2.25"), CurrencyCode.EUR));

        assertEquals(new BigDecimal("12.35"), result.amount());
        assertEquals(CurrencyCode.EUR, result.currency());
    }

    @Test
    void rejectsArithmeticAcrossCurrencies() {
        Money usd = new Money(new BigDecimal("10.00"), CurrencyCode.USD);
        Money eur = new Money(new BigDecimal("10.00"), CurrencyCode.EUR);

        assertThrows(CurrencyMismatchException.class, () -> usd.add(eur));
    }
}
