package com.esmpf.content.domain;

import com.esmpf.content.ContentType;
import com.esmpf.content.PublicationStatus;
import com.esmpf.shared.persistence.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "news_article")
class NewsArticle extends TenantEntity {

    @Column(nullable = false, length = 120)
    private String slug;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 500)
    private String summary;

    @Column(nullable = false, columnDefinition = "text")
    private String body;

    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ContentType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PublicationStatus status;

    @Column(nullable = false)
    private boolean featured;

    @Column(name = "publish_at")
    private Instant publishAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "visible_until")
    private Instant visibleUntil;

    @Column(name = "promotion_label", length = 100)
    private String promotionLabel;

    @Column(name = "action_url", length = 500)
    private String actionUrl;

    @Column(name = "promotion_starts_at")
    private Instant promotionStartsAt;

    @Column(name = "promotion_ends_at")
    private Instant promotionEndsAt;

    @Column(name = "created_by_user_id", nullable = false, updatable = false)
    private UUID createdByUserId;

    @Column(name = "published_by_user_id")
    private UUID publishedByUserId;
}
