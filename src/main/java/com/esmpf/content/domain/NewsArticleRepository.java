package com.esmpf.content.domain;

import com.esmpf.content.ContentType;
import com.esmpf.content.PublicationStatus;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface NewsArticleRepository extends JpaRepository<NewsArticle, UUID> {

    Optional<NewsArticle> findByIdAndBusinessId(UUID id, UUID businessId);

    boolean existsByBusinessIdAndSlugIgnoreCase(UUID businessId, String slug);

    boolean existsByBusinessIdAndSlugIgnoreCaseAndIdNot(
            UUID businessId,
            String slug,
            UUID id
    );

    @Query("""
            select article
              from NewsArticle article
             where article.businessId = :businessId
               and (:status is null or article.status = :status)
               and (:type is null or article.type = :type)
            """)
    Page<NewsArticle> findForAdministration(
            @Param("businessId") UUID businessId,
            @Param("status") PublicationStatus status,
            @Param("type") ContentType type,
            Pageable pageable
    );

    @Query("""
            select article
              from NewsArticle article
             where article.businessId = :businessId
               and article.status = :status
               and article.publishedAt <= :now
               and (article.visibleUntil is null or article.visibleUntil > :now)
               and (:type is null or article.type = :type)
            """)
    Page<NewsArticle> findPublished(
            @Param("businessId") UUID businessId,
            @Param("status") PublicationStatus status,
            @Param("type") ContentType type,
            @Param("now") Instant now,
            Pageable pageable
    );

    @Query("""
            select article
              from NewsArticle article
             where article.businessId = :businessId
               and lower(article.slug) = lower(:slug)
               and article.status = :status
               and article.publishedAt <= :now
               and (article.visibleUntil is null or article.visibleUntil > :now)
            """)
    Optional<NewsArticle> findPublishedBySlug(
            @Param("businessId") UUID businessId,
            @Param("slug") String slug,
            @Param("status") PublicationStatus status,
            @Param("now") Instant now
    );
}
