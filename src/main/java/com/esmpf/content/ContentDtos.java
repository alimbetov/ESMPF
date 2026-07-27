package com.esmpf.content;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

public final class ContentDtos {

    private ContentDtos() {
    }

    public record CreateArticleCommand(
            @NotBlank @Size(max = 120) String slug,
            @NotBlank @Size(max = 200) String title,
            @NotBlank @Size(max = 500) String summary,
            @NotBlank String body,
            @Size(max = 500) String coverImageUrl,
            @NotNull ContentType type,
            boolean featured,
            Instant visibleUntil,
            @Size(max = 100) String promotionLabel,
            @Size(max = 500) String actionUrl,
            Instant promotionStartsAt,
            Instant promotionEndsAt
    ) {
    }

    public record UpdateDraftArticleCommand(
            long version,
            @NotBlank @Size(max = 120) String slug,
            @NotBlank @Size(max = 200) String title,
            @NotBlank @Size(max = 500) String summary,
            @NotBlank String body,
            @Size(max = 500) String coverImageUrl,
            @NotNull ContentType type,
            boolean featured,
            Instant visibleUntil,
            @Size(max = 100) String promotionLabel,
            @Size(max = 500) String actionUrl,
            Instant promotionStartsAt,
            Instant promotionEndsAt
    ) {
    }

    public record ArticleResponse(
            UUID id,
            long version,
            String slug,
            String title,
            String summary,
            String body,
            String coverImageUrl,
            ContentType type,
            PublicationStatus status,
            boolean featured,
            Instant publishAt,
            Instant publishedAt,
            Instant visibleUntil,
            String promotionLabel,
            String actionUrl,
            Instant promotionStartsAt,
            Instant promotionEndsAt,
            UUID createdByUserId,
            UUID publishedByUserId,
            Instant createdAt,
            Instant updatedAt
    ) {
    }

    public record PublishedArticleResponse(
            UUID id,
            String slug,
            String title,
            String summary,
            String body,
            String coverImageUrl,
            ContentType type,
            boolean featured,
            Instant publishedAt,
            Instant visibleUntil,
            String promotionLabel,
            String actionUrl,
            Instant promotionStartsAt,
            Instant promotionEndsAt
    ) {
    }
}
