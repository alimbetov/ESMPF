package com.esmpf.commercial;

import static com.esmpf.commercial.CommercialDtos.*;
import static com.esmpf.web.ApiActionRequests.JsonRequest;
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
public class CommercialRestController {
    private final CommercialService service;

    @PostMapping("/estimates")
    @ResponseStatus(HttpStatus.CREATED)
    public EstimateResponse createEstimate(@Valid @RequestBody EstimateCreateCommand command) {
        return service.createEstimate(command);
    }

    @GetMapping("/estimates/{estimateId}")
    public EstimateResponse getEstimate(@PathVariable UUID estimateId) {
        return service.getEstimate(estimateId);
    }

    @GetMapping("/estimates")
    public Page<EstimateResponse> listEstimates(Pageable pageable) {
        return service.listEstimates(pageable);
    }

    @PutMapping("/estimates/{estimateId}")
    public EstimateResponse updateDraftEstimate(@PathVariable UUID estimateId,
                                                @Valid @RequestBody EstimateUpdateCommand command) {
        return service.updateDraftEstimate(estimateId, command);
    }

    @PostMapping("/estimates/{estimateId}/actions/send")
    public EstimateResponse sendEstimate(@PathVariable UUID estimateId,
                                         @Valid @RequestBody VersionRequest request) {
        return service.sendEstimate(estimateId, request.version());
    }

    @PostMapping("/estimates/{estimateId}/actions/approve")
    public EstimateResponse approveEstimate(@PathVariable UUID estimateId,
                                            @Valid @RequestBody JsonRequest request) {
        return service.approveEstimate(estimateId, request.version(), request.dataJson());
    }

    @PostMapping("/estimates/{estimateId}/actions/reject")
    public EstimateResponse rejectEstimate(@PathVariable UUID estimateId,
                                           @Valid @RequestBody JsonRequest request) {
        return service.rejectEstimate(estimateId, request.version(), request.dataJson());
    }
}
