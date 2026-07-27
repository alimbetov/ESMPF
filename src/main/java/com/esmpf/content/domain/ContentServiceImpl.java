package com.esmpf.content.domain;

import com.esmpf.content.ContentDtos.ArticleResponse;
import com.esmpf.content.ContentDtos.CreateArticleCommand;
import com.esmpf.content.ContentDtos.PublishedArticleResponse;
import com.esmpf.content.ContentDtos.UpdateDraftArticleCommand;
import com.esmpf.content.ContentService;
import com.esmpf.content.ContentType;
import com.esmpf.content.PublicationStatus;
import com.esmpf.shared.tenant.TenantContext;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
class ContentServiceImpl implements ContentService {

    private final NewsArticleRepository repository;
    private final TenantContext tenantContext;

    @Override
    public ArticleResponse createArticle(CreateArticleCommand command) {
        UUID businessId = tenantContext.requireBusinessId();
        String slug = normalizeSlug(command.slug());
        requireUniqueSlug(businessId, slug, null);
        validateDates(command.visibleUntil(), command.promotionStartsAt(), command.promotionEndsAt());

        NewsArticle article = new NewsArticle();
        article.setBusinessId(businessId);
        article.setCreatedByUserId(tenantContext.requireUserId());
        article.setStatus(PublicationStatus.DRAFT);
        applyEditableFields(article, command);
        article.setSlug(slug);

        return toArticleResponse(repository.saveAndFlush(article));
    }

    @Override
    @Transactional(readOnly = true)
    public ArticleResponse getArticle(UUID articleId) {
        return toArticleResponse(requireArticle(articleId));
    }

    @Override
    @Transactional(readOnly = true)
    public PublishedArticleResponse getPublishedArticle(String slug) {
        UUID businessId = tenantContext.requireBusinessId();
        NewsArticle article = repository.findPublishedBySlug(
                        businessId,
                        normalizeSlug(slug),
                        PublicationStatus.PUBLISHED,
                        Instant.now())
                .orElseThrow(() -> new EntityNotFoundException("Published article not found: " + slug));
        return toPublishedResponse(article);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ArticleResponse> listArticles(
            PublicationStatus status,
            ContentType type,
            Pageable pageable
    ) {
        return repository.findForAdministration(
                        tenantContext.requireBusinessId(),
                        status,
                        type,
                        pageable)
                .map(this::toArticleResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PublishedArticleResponse> listPublishedArticles(
            ContentType type,
            Pageable pageable
    ) {
        return repository.findPublished(
                        tenantContext.requireBusinessId(),
                        PublicationStatus.PUBLISHED,
                        type,
                        Instant.now(),
                        pageable)
                .map(this::toPublishedResponse);
    }

    @Override
    public ArticleResponse updateDraftArticle(
            UUID articleId,
            UpdateDraftArticleCommand command
    ) {
        NewsArticle article = requireArticle(articleId);
        requireVersion(article, command.version());
        requireStatus(article, PublicationStatus.DRAFT, "Only a draft article can be edited");

        String slug = normalizeSlug(command.slug());
        requireUniqueSlug(article.getBusinessId(), slug, article.getId());
        validateDates(command.visibleUntil(), command.promotionStartsAt(), command.promotionEndsAt());

        article.setSlug(slug);
        article.setTitle(command.title().trim());
        article.setSummary(command.summary().trim());
        article.setBody(command.body().trim());
        article.setCoverImageUrl(trimToNull(command.coverImageUrl()));
        article.setType(command.type());
        article.setFeatured(command.featured());
        article.setVisibleUntil(command.visibleUntil());
        article.setPromotionLabel(trimToNull(command.promotionLabel()));
        article.setActionUrl(trimToNull(command.actionUrl()));
        article.setPromotionStartsAt(command.promotionStartsAt());
        article.setPromotionEndsAt(command.promotionEndsAt());

        return toArticleResponse(repository.saveAndFlush(article));
    }

    @Override
    public ArticleResponse scheduleArticle(UUID articleId, long version, Instant publishAt) {
        NewsArticle article = requireArticle(articleId);
        requireVersion(article, version);
        requireStatus(article, PublicationStatus.DRAFT, "Only a draft article can be scheduled");
        if (publishAt == null || !publishAt.isAfter(Instant.now())) {
            throw new IllegalArgumentException("publishAt must be in the future");
        }
        if (article.getVisibleUntil() != null && !article.getVisibleUntil().isAfter(publishAt)) {
            throw new IllegalArgumentException("visibleUntil must be after publishAt");
        }

        article.setPublishAt(publishAt);
        article.setStatus(PublicationStatus.SCHEDULED);
        return toArticleResponse(repository.saveAndFlush(article));
    }

    @Override
    public ArticleResponse publishArticle(UUID articleId, long version) {
        NewsArticle article = requireArticle(articleId);
        requireVersion(article, version);
        if (article.getStatus() != PublicationStatus.DRAFT
                && article.getStatus() != PublicationStatus.SCHEDULED) {
            throw new IllegalStateException("Only a draft or scheduled article can be published");
        }

        Instant now = Instant.now();
        if (article.getVisibleUntil() != null && !article.getVisibleUntil().isAfter(now)) {
            throw new IllegalStateException("Article visibility period has already ended");
        }

        article.setStatus(PublicationStatus.PUBLISHED);
        article.setPublishAt(article.getPublishAt() == null ? now : article.getPublishAt());
        article.setPublishedAt(now);
        article.setPublishedByUserId(tenantContext.requireUserId());
        return toArticleResponse(repository.saveAndFlush(article));
    }

    @Override
    public ArticleResponse archiveArticle(UUID articleId, long version) {
        NewsArticle article = requireArticle(articleId);
        requireVersion(article, version);
        if (article.getStatus() == PublicationStatus.ARCHIVED) {
            throw new IllegalStateException("Article is already archived");
        }
        article.setStatus(PublicationStatus.ARCHIVED);
        return toArticleResponse(repository.saveAndFlush(article));
    }

    private NewsArticle requireArticle(UUID articleId) {
        return repository.findByIdAndBusinessId(articleId, tenantContext.requireBusinessId())
                .orElseThrow(() -> new EntityNotFoundException("NewsArticle not found: " + articleId));
    }

    private void applyEditableFields(NewsArticle article, CreateArticleCommand command) {
        article.setTitle(command.title().trim());
        article.setSummary(command.summary().trim());
        article.setBody(command.body().trim());
        article.setCoverImageUrl(trimToNull(command.coverImageUrl()));
        article.setType(command.type());
        article.setFeatured(command.featured());
        article.setVisibleUntil(command.visibleUntil());
        article.setPromotionLabel(trimToNull(command.promotionLabel()));
        article.setActionUrl(trimToNull(command.actionUrl()));
        article.setPromotionStartsAt(command.promotionStartsAt());
        article.setPromotionEndsAt(command.promotionEndsAt());
    }

    private void requireUniqueSlug(UUID businessId, String slug, UUID excludedId) {
        boolean exists = excludedId == null
                ? repository.existsByBusinessIdAndSlugIgnoreCase(businessId, slug)
                : repository.existsByBusinessIdAndSlugIgnoreCaseAndIdNot(businessId, slug, excludedId);
        if (exists) {
            throw new IllegalArgumentException("Article slug already exists: " + slug);
        }
    }

    private void requireVersion(NewsArticle article, long expectedVersion) {
        if (article.getVersion() != expectedVersion) {
            throw new OptimisticLockingFailureException(
                    "NewsArticle version mismatch: expected " + expectedVersion
                            + ", actual " + article.getVersion());
        }
    }

    private void requireStatus(
            NewsArticle article,
            PublicationStatus expected,
            String message
    ) {
        if (article.getStatus() != expected) {
            throw new IllegalStateException(message);
        }
    }

    private void validateDates(
            Instant visibleUntil,
            Instant promotionStartsAt,
            Instant promotionEndsAt
    ) {
        if (promotionStartsAt != null
                && promotionEndsAt != null
                && promotionEndsAt.isBefore(promotionStartsAt)) {
            throw new IllegalArgumentException(
                    "promotionEndsAt must not be before promotionStartsAt");
        }
        if (visibleUntil != null && visibleUntil.isBefore(Instant.now())) {
            throw new IllegalArgumentException("visibleUntil must be in the future");
        }
    }

    private String normalizeSlug(String value) {
        String normalized = value == null
                ? ""
                : value.trim().toLowerCase(Locale.ROOT);
        if (!normalized.matches("[a-z0-9]+(?:-[a-z0-9]+)*")) {
            throw new IllegalArgumentException(
                    "slug must contain lowercase latin letters, digits and single hyphens");
        }
        return normalized;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private ArticleResponse toArticleResponse(NewsArticle article) {
        return new ArticleResponse(
                article.getId(),
                article.getVersion(),
                article.getSlug(),
                article.getTitle(),
                article.getSummary(),
                article.getBody(),
                article.getCoverImageUrl(),
                article.getType(),
                article.getStatus(),
                article.isFeatured(),
                article.getPublishAt(),
                article.getPublishedAt(),
                article.getVisibleUntil(),
                article.getPromotionLabel(),
                article.getActionUrl(),
                article.getPromotionStartsAt(),
                article.getPromotionEndsAt(),
                article.getCreatedByUserId(),
                article.getPublishedByUserId(),
                article.getCreatedAt(),
                article.getUpdatedAt());
    }

    private PublishedArticleResponse toPublishedResponse(NewsArticle article) {
        return new PublishedArticleResponse(
                article.getId(),
                article.getSlug(),
                article.getTitle(),
                article.getSummary(),
                article.getBody(),
                article.getCoverImageUrl(),
                article.getType(),
                article.isFeatured(),
                article.getPublishedAt(),
                article.getVisibleUntil(),
                article.getPromotionLabel(),
                article.getActionUrl(),
                article.getPromotionStartsAt(),
                article.getPromotionEndsAt());
    }
}
