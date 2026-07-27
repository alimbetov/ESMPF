package com.esmpf.content;

import com.esmpf.content.ContentDtos.ArticleResponse;
import com.esmpf.content.ContentDtos.CreateArticleCommand;
import com.esmpf.content.ContentDtos.PublishedArticleResponse;
import com.esmpf.content.ContentDtos.UpdateDraftArticleCommand;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ContentService {

    ArticleResponse createArticle(CreateArticleCommand command);

    ArticleResponse getArticle(UUID articleId);

    PublishedArticleResponse getPublishedArticle(String slug);

    Page<ArticleResponse> listArticles(
            PublicationStatus status,
            ContentType type,
            Pageable pageable
    );

    Page<PublishedArticleResponse> listPublishedArticles(
            ContentType type,
            Pageable pageable
    );

    ArticleResponse updateDraftArticle(
            UUID articleId,
            UpdateDraftArticleCommand command
    );

    ArticleResponse scheduleArticle(
            UUID articleId,
            long version,
            Instant publishAt
    );

    ArticleResponse publishArticle(UUID articleId, long version);

    ArticleResponse archiveArticle(UUID articleId, long version);
}
