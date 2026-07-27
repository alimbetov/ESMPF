package com.esmpf.web;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public final class ApiActionRequests {
    private ApiActionRequests() {}

    public record VersionRequest(@Min(0) long version) {}

    public record ReasonRequest(@Min(0) long version, @NotBlank String reason) {}

    public record ReferenceRequest(@Min(0) long version, @NotNull UUID referenceId) {}

    public record TextRequest(@Min(0) long version, @NotBlank String value) {}

    public record JsonRequest(@Min(0) long version, @NotBlank String dataJson) {}

    public record ProgressRequest(@Min(0) long version, @Min(0) @Max(100) int progress) {}

    public record ScheduledRequest(@Min(0) long version, @NotNull Instant scheduledAt) {}

    public record NotificationFailureRequest(
            @Min(0) long version,
            @NotBlank String error,
            Instant nextAttemptAt
    ) {}

    public record VisitStartRequest(@Min(0) long version, @NotBlank String arrivalDataJson) {}

    public record VisitCompleteRequest(
            @Min(0) long version,
            @NotBlank String completionDataJson,
            String customerConfirmationJson
    ) {}

    public record GeneratedDocumentCompleteRequest(
            @Min(0) long version,
            @NotNull UUID attachmentId,
            @NotBlank String checksum
    ) {}

    public record InvoiceFromEstimateRequest(
            @Min(0) long estimateVersion,
            @NotBlank String invoiceNumber,
            @NotNull LocalDate dueDate
    ) {}
}
