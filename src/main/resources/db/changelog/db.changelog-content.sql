--liquibase formatted sql

--changeset esmpf:170-content-news dbms:postgresql
CREATE TABLE news_article (
    id UUID PRIMARY KEY,
    business_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    slug VARCHAR(120) NOT NULL,
    title VARCHAR(200) NOT NULL,
    summary VARCHAR(500) NOT NULL,
    body TEXT NOT NULL,
    cover_image_url VARCHAR(500),
    type VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    featured BOOLEAN NOT NULL DEFAULT FALSE,
    publish_at TIMESTAMPTZ,
    published_at TIMESTAMPTZ,
    visible_until TIMESTAMPTZ,
    promotion_label VARCHAR(100),
    action_url VARCHAR(500),
    promotion_starts_at TIMESTAMPTZ,
    promotion_ends_at TIMESTAMPTZ,
    created_by_user_id UUID NOT NULL,
    published_by_user_id UUID,
    CONSTRAINT fk_news_article_business
        FOREIGN KEY (business_id) REFERENCES business(id),
    CONSTRAINT uk_news_article_business_id_id
        UNIQUE (business_id, id),
    CONSTRAINT ck_news_article_type
        CHECK (type IN ('NEWS', 'PROMOTION', 'ANNOUNCEMENT', 'ADVICE')),
    CONSTRAINT ck_news_article_status
        CHECK (status IN ('DRAFT', 'SCHEDULED', 'PUBLISHED', 'ARCHIVED')),
    CONSTRAINT ck_news_article_visibility
        CHECK (visible_until IS NULL OR publish_at IS NULL OR visible_until > publish_at),
    CONSTRAINT ck_news_article_promotion_dates
        CHECK (
            promotion_ends_at IS NULL
            OR promotion_starts_at IS NULL
            OR promotion_ends_at >= promotion_starts_at
        )
);

CREATE UNIQUE INDEX uk_news_article_business_slug
    ON news_article (business_id, lower(slug));

CREATE INDEX idx_news_article_publication
    ON news_article (business_id, status, published_at DESC, id DESC);

CREATE INDEX idx_news_article_type_publication
    ON news_article (business_id, type, status, published_at DESC, id DESC);

CREATE INDEX idx_news_article_featured
    ON news_article (business_id, featured, published_at DESC, id DESC)
    WHERE status = 'PUBLISHED';

--rollback DROP TABLE news_article CASCADE;
