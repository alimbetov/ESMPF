package com.esmpf.identity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public final class IdentityDtos {
    private IdentityDtos() {}

    public record BusinessCreateCommand(String name, String code, String timezone, String defaultLanguage,
                                        String currency, String settingsJson) {}
    public record BusinessUpdateCommand(long version, String name, String timezone, String defaultLanguage,
                                        String currency, String settingsJson) {}
    public record BusinessResponse(UUID id, long version, String name, String code, String timezone,
                                   String defaultLanguage, String currency, String status,
                                   String settingsJson, Instant createdAt, Instant updatedAt) {}

    public record BusinessLocationCommand(long version, String name, String address, Double latitude,
                                          Double longitude, String timezone) {}
    public record BusinessLocationResponse(UUID id, long version, String name, String address,
                                           Double latitude, Double longitude, String timezone,
                                           boolean active, Instant createdAt, Instant updatedAt) {}

    public record UserAccountCreateCommand(String email, String phone, String passwordHash, String fullName,
                                           String role, boolean worker, String externalProvider,
                                           String externalSubject) {}
    public record UserAccountUpdateCommand(long version, String email, String phone, String fullName,
                                           String role, boolean worker, String externalProvider,
                                           String externalSubject) {}
    public record UserAccountResponse(UUID id, long version, String email, String phone, String fullName,
                                      String role, boolean worker, boolean active,
                                      String externalProvider, String externalSubject,
                                      Instant createdAt, Instant updatedAt) {}

    public record WorkerQualificationCommand(long version, UUID userId, String type, String name,
                                             String issuer, String referenceNumber, LocalDate validFrom,
                                             LocalDate validUntil, UUID attachmentId) {}
    public record WorkerQualificationResponse(UUID id, long version, UUID userId, String type, String name,
                                              String issuer, String referenceNumber, LocalDate validFrom,
                                              LocalDate validUntil, UUID attachmentId, String status,
                                              Instant createdAt, Instant updatedAt) {}

    public record UserReference(UUID id, boolean active, boolean worker, String role) {}
}