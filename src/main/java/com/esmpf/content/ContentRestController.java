package com.esmpf.content;

import static com.esmpf.content.ContentDtos.*;
import static com.esmpf.web.ApiActionRequests.ScheduledRequest;
import static com.esmpf.web.ApiActionRequests.VersionRequest;

import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ContentRestController {
    private final ContentService service;

    @PostMapping("/content/articles") @ResponseStatus(HttpStatus.CREATED)
    public ArticleResponse createArticle(@Valid @RequestBody CreateArticleCommand command) { return service.createArticle(command); }
    @GetMapping("/content/articles/{articleId}") public ArticleResponse getArticle(@PathVariable UUID articleId) { return service.getArticle(articleId); }
    @GetMapping("/content/articles") public Page<ArticleResponse> listArticles(@RequestParam(required = false) PublicationStatus status, @RequestParam(required = false) ContentType type, Pageable pageable) { return service.listArticles(status, type, pageable); }
    @PutMapping("/content/articles/{articleId}") public ArticleResponse updateDraftArticle(@PathVariable UUID articleId, @Valid @RequestBody UpdateDraftArticleCommand command) { return service.updateDraftArticle(articleId, command); }
    @PostMapping("/content/articles/{articleId}/actions/schedule") public ArticleResponse scheduleArticle(@PathVariable UUID articleId, @Valid @RequestBody ScheduledRequest request) { return service.scheduleArticle(articleId, request.version(), request.scheduledAt()); }
    @PostMapping("/content/articles/{articleId}/actions/publish") public ArticleResponse publishArticle(@PathVariable UUID articleId, @Valid @RequestBody VersionRequest request) { return service.publishArticle(articleId, request.version()); }
    @PostMapping("/content/articles/{articleId}/actions/archive") public ArticleResponse archiveArticle(@PathVariable UUID articleId, @Valid @RequestBody VersionRequest request) { return service.archiveArticle(articleId, request.version()); }

    @GetMapping("/public/articles/{slug}") public PublishedArticleResponse getPublishedArticle(@PathVariable String slug) { return service.getPublishedArticle(slug); }
    @GetMapping("/public/articles") public Page<PublishedArticleResponse> listPublishedArticles(@RequestParam(required = false) ContentType type, Pageable pageable) { return service.listPublishedArticles(type, pageable); }
}
