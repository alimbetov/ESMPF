package com.esmpf.customer;

import static com.esmpf.customer.CustomerDtos.*;
import static com.esmpf.customer.CustomerInteractionDtos.*;
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
public class CustomerRestController {
    private final CustomerService customerService;
    private final CustomerInteractionService interactionService;

    @PostMapping("/customers")
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse createCustomer(@Valid @RequestBody CustomerCreateCommand command) {
        return customerService.createCustomer(command);
    }

    @GetMapping("/customers/{customerId}")
    public CustomerResponse getCustomer(@PathVariable UUID customerId) {
        return customerService.getCustomer(customerId);
    }

    @GetMapping("/customers")
    public Page<CustomerResponse> listCustomers(Pageable pageable) {
        return customerService.listCustomers(pageable);
    }

    @PutMapping("/customers/{customerId}")
    public CustomerResponse updateCustomer(@PathVariable UUID customerId,
                                            @Valid @RequestBody CustomerUpdateCommand command) {
        return customerService.updateCustomer(customerId, command);
    }

    @PostMapping("/customers/{customerId}/actions/archive")
    public CustomerResponse archiveCustomer(@PathVariable UUID customerId,
                                             @Valid @RequestBody VersionRequest request) {
        return customerService.archiveCustomer(customerId, request.version());
    }

    @PostMapping("/service-locations")
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceLocationResponse createServiceLocation(@Valid @RequestBody ServiceLocationCreateCommand command) {
        return customerService.createServiceLocation(command);
    }

    @GetMapping("/service-locations/{locationId}")
    public ServiceLocationResponse getServiceLocation(@PathVariable UUID locationId) {
        return customerService.getServiceLocation(locationId);
    }

    @GetMapping("/customers/{customerId}/service-locations")
    public Page<ServiceLocationResponse> listServiceLocations(@PathVariable UUID customerId, Pageable pageable) {
        return customerService.listServiceLocations(customerId, pageable);
    }

    @PutMapping("/service-locations/{locationId}")
    public ServiceLocationResponse updateServiceLocation(@PathVariable UUID locationId,
            @Valid @RequestBody ServiceLocationUpdateCommand command) {
        return customerService.updateServiceLocation(locationId, command);
    }

    @PostMapping("/service-locations/{locationId}/actions/archive")
    public ServiceLocationResponse archiveServiceLocation(@PathVariable UUID locationId,
            @Valid @RequestBody VersionRequest request) {
        return customerService.archiveServiceLocation(locationId, request.version());
    }

    @PostMapping("/customer-interactions")
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerInteractionResponse recordInteraction(@Valid @RequestBody CustomerInteractionCommand command) {
        return interactionService.recordInteraction(command);
    }

    @GetMapping("/customer-interactions/{interactionId}")
    public CustomerInteractionResponse getInteraction(@PathVariable UUID interactionId) {
        return interactionService.getInteraction(interactionId);
    }

    @GetMapping("/customers/{customerId}/interactions")
    public Page<CustomerInteractionResponse> listInteractions(@PathVariable UUID customerId, Pageable pageable) {
        return interactionService.listInteractions(customerId, pageable);
    }
}
