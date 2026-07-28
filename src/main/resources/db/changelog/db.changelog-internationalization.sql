--liquibase formatted sql

--changeset esmpf:230-international-profile-foundation dbms:postgresql
CREATE TABLE business_international_profile (
    id UUID PRIMARY KEY,
    business_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    operating_country_code VARCHAR(2) NOT NULL,
    legal_entity_country_code VARCHAR(2) NOT NULL,
    default_locale VARCHAR(16) NOT NULL,
    supported_locales VARCHAR(512) NOT NULL,
    default_currency_code VARCHAR(3) NOT NULL,
    supported_currency_codes VARCHAR(128) NOT NULL,
    timezone VARCHAR(64) NOT NULL,
    measurement_system VARCHAR(32) NOT NULL,
    date_format VARCHAR(32) NOT NULL,
    time_format VARCHAR(32) NOT NULL,
    number_format VARCHAR(32) NOT NULL,
    tax_region_code VARCHAR(64),
    invoice_country_profile_code VARCHAR(32) NOT NULL,
    data_region_code VARCHAR(32) NOT NULL,
    CONSTRAINT fk_business_international_profile_business
        FOREIGN KEY (business_id) REFERENCES business(id),
    CONSTRAINT uk_business_international_profile_business UNIQUE (business_id),
    CONSTRAINT ck_business_international_profile_operating_country
        CHECK (operating_country_code ~ '^[A-Z]{2}$'),
    CONSTRAINT ck_business_international_profile_legal_country
        CHECK (legal_entity_country_code ~ '^[A-Z]{2}$'),
    CONSTRAINT ck_business_international_profile_currency
        CHECK (default_currency_code IN ('USD','EUR','GBP','CHF','PLN','CZK','SEK','NOK','DKK','KZT')),
    CONSTRAINT ck_business_international_profile_measurement
        CHECK (measurement_system IN ('METRIC','US_CUSTOMARY'))
);
CREATE INDEX idx_business_international_profile_region
    ON business_international_profile(data_region_code);
CREATE INDEX idx_business_international_profile_country
    ON business_international_profile(operating_country_code);
--rollback DROP TABLE business_international_profile CASCADE;
