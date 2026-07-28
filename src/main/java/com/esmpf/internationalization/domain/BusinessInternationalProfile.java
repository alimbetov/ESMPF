package com.esmpf.internationalization.domain;

import com.esmpf.shared.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "business_international_profile")
@Getter
@Setter
public class BusinessInternationalProfile extends BaseEntity {

    @Column(name = "business_id", nullable = false, unique = true)
    private UUID businessId;

    @Column(name = "operating_country_code", nullable = false, length = 2)
    private String operatingCountryCode;

    @Column(name = "legal_entity_country_code", nullable = false, length = 2)
    private String legalEntityCountryCode;

    @Column(name = "default_locale", nullable = false, length = 16)
    private String defaultLocale;

    @Column(name = "supported_locales", nullable = false, length = 512)
    private String supportedLocales;

    @Enumerated(EnumType.STRING)
    @Column(name = "default_currency_code", nullable = false, length = 3)
    private CurrencyCode defaultCurrencyCode;

    @Column(name = "supported_currency_codes", nullable = false, length = 128)
    private String supportedCurrencyCodes;

    @Column(name = "timezone", nullable = false, length = 64)
    private String timezone;

    @Enumerated(EnumType.STRING)
    @Column(name = "measurement_system", nullable = false, length = 32)
    private MeasurementSystem measurementSystem;

    @Column(name = "date_format", nullable = false, length = 32)
    private String dateFormat;

    @Column(name = "time_format", nullable = false, length = 32)
    private String timeFormat;

    @Column(name = "number_format", nullable = false, length = 32)
    private String numberFormat;

    @Column(name = "tax_region_code", length = 64)
    private String taxRegionCode;

    @Column(name = "invoice_country_profile_code", nullable = false, length = 32)
    private String invoiceCountryProfileCode;

    @Column(name = "data_region_code", nullable = false, length = 32)
    private String dataRegionCode;
}
