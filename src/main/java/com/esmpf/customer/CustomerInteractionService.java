package com.esmpf.customer;

import static com.esmpf.customer.CustomerInteractionDtos.*;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomerInteractionService {
    CustomerInteractionResponse recordInteraction(CustomerInteractionCommand command);
    CustomerInteractionResponse getInteraction(UUID interactionId);
    Page<CustomerInteractionResponse> listInteractions(UUID customerId, Pageable pageable);
}