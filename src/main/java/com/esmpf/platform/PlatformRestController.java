package com.esmpf.platform;

import static com.esmpf.platform.PlatformDtos.*;
import static com.esmpf.web.ApiActionRequests.VersionRequest;

import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/platform")
@RequiredArgsConstructor
public class PlatformRestController {
    private final PlatformService service;

    @PostMapping("/public-tokens")
    @ResponseStatus(HttpStatus.CREATED)
    public PublicTokenResponse createPublicToken(@Valid @RequestBody PublicTokenCommand command) {
        return service.createPublicToken(command);
    }

    @PostMapping("/public-tokens/{tokenId}/actions/consume")
    public PublicTokenResponse consumePublicToken(@PathVariable UUID tokenId) {
        return service.consumePublicToken(tokenId);
    }

    @PostMapping("/public-tokens/{tokenId}/actions/revoke")
    public PublicTokenResponse revokePublicToken(@PathVariable UUID tokenId,
                                                 @Valid @RequestBody VersionRequest request) {
        return service.revokePublicToken(tokenId, request.version());
    }

    @PostMapping("/data-jobs")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public DataJobResponse createDataJob(@Valid @RequestBody DataJobCommand command) {
        return service.createDataJob(command);
    }

    @GetMapping("/data-jobs")
    public Page<DataJobResponse> listDataJobs(Pageable pageable) {
        return service.listDataJobs(pageable);
    }

    @GetMapping("/audit-events")
    public Page<AuditResponse> listAudit(Pageable pageable) {
        return service.listAudit(pageable);
    }

    @PostMapping("/integrations")
    @ResponseStatus(HttpStatus.CREATED)
    public IntegrationResponse createIntegration(@Valid @RequestBody IntegrationCommand command) {
        return service.createIntegration(command);
    }

    @GetMapping("/integrations/{connectionId}")
    public IntegrationResponse getIntegration(@PathVariable UUID connectionId) {
        return service.getIntegration(connectionId);
    }

    @PutMapping("/integrations/{connectionId}")
    public IntegrationResponse updateIntegration(@PathVariable UUID connectionId,
                                                 @Valid @RequestBody IntegrationCommand command) {
        return service.updateIntegration(connectionId, command);
    }

    @PostMapping("/integrations/{connectionId}/actions/activate")
    public IntegrationResponse activateIntegration(@PathVariable UUID connectionId,
                                                    @Valid @RequestBody VersionRequest request) {
        return service.activateIntegration(connectionId, request.version());
    }

    @PostMapping("/integrations/{connectionId}/actions/suspend")
    public IntegrationResponse suspendIntegration(@PathVariable UUID connectionId,
                                                   @Valid @RequestBody VersionRequest request) {
        return service.suspendIntegration(connectionId, request.version());
    }

    @GetMapping("/integrations")
    public Page<IntegrationResponse> listIntegrations(Pageable pageable) {
        return service.listIntegrations(pageable);
    }

    @PostMapping("/document-numbers/actions/allocate")
    public String allocateDocumentNumber(@RequestParam String documentType,
                                         @RequestParam int year,
                                         @RequestParam(required = false, defaultValue = "") String prefix) {
        return service.allocateDocumentNumber(documentType, year, prefix);
    }
}
