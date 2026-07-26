package com.esmpf.customer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public final class CustomerDtos {
    private CustomerDtos() {
    }

    public record CustomerCreateCommand(
            @NotBlank String type,
            @NotBlank String name,
            String primaryPhone,
            @Email String primaryEmail,
            String preferredLanguage,
            String contactsJson,
            String notificationPreferencesJson,
            String billingDataJson,
            String consentsJson
    ) {
    }

    public record CustomerUpdateCommand(
            long version,
            String type,
            String name,
            String primaryPhone,
            @Email String primaryEmail,
            String preferredLanguage,
            String contactsJson,
            String notificationPreferencesJson,
            String billingDataJson,
            String consentsJson
    ) {
    }

    public record CustomerResponse(
            UUID id,
            String type,
            String name,
            String primaryPhone,
            String primaryEmail,
            String preferredLanguage,
            String contactsJson,
            String notificationPreferencesJson,
            String billingDataJson,
            String consentsJson,
            String status,
            Instant createdAt,
            Instant updatedAt,
            long version
    ) {
    }

    public record ServiceLocationCreateCommand(
            @NotNull UUID customerId,
            UUID parentLocationId,
            @NotBlank String name,
            String type,
            String address,
            Double latitude,
            Double longitude,
            String timezone,
            String accessInstructions
    ) {
    }

    public record ServiceLocationUpdateCommand(
            long version,
            UUID parentLocationId,
            String name,
            String type,
            String address,
            Double latitude,
            Double longitude,
            String timezone,
            String accessInstructions
    ) {
    }

    public record ServiceLocationResponse(
            UUID id,
            UUID customerId,
            UUID parentLocationId,
            String name,
            String type,
            String address,
            Double latitude,
            Double longitude,
            String timezone,
            String accessInstructions,
            String status,
            Instant createdAt,
            Instant updatedAt,
            long version
    ) {
    }

    public record CustomerReference(UUID id, String name, String status) {
    }

    public record ServiceLocationReference(UUID id, UUID customerId, String name, String status) {
    }
}
