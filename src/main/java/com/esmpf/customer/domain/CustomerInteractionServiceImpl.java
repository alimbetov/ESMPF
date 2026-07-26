package com.esmpf.customer.domain;

import static com.esmpf.customer.CustomerInteractionDtos.*;

import com.esmpf.customer.CustomerInteractionService;
import com.esmpf.customer.CustomerReferenceQuery;
import com.esmpf.shared.exception.EntityNotFoundException;
import com.esmpf.shared.tenant.TenantContext;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class CustomerInteractionServiceImpl implements CustomerInteractionService {
    private final TenantContext tenantContext;
    private final CustomerReferenceQuery customerReferences;
    private final CustomerInteractionRepository repository;

    @Override @Transactional
    public CustomerInteractionResponse recordInteraction(CustomerInteractionCommand command) {
        customerReferences.requireCustomer(command.customerId());
        if ((command.relatedSubjectType() == null) != (command.relatedSubjectId() == null)) throw new IllegalArgumentException("Related subject type and id must be supplied together");
        CustomerInteraction e = new CustomerInteraction(); e.setBusinessId(tenant()); e.setCustomerId(command.customerId()); e.setType(command.type()); e.setSubject(command.subject()); e.setContent(command.content()); e.setOccurredAt(command.occurredAt() == null ? Instant.now() : command.occurredAt()); e.setCreatedBy(tenantContext.requireUserId()); e.setRelatedSubjectType(command.relatedSubjectType()); e.setRelatedSubjectId(command.relatedSubjectId());
        return response(repository.saveAndFlush(e));
    }
    @Override @Transactional(readOnly = true) public CustomerInteractionResponse getInteraction(UUID id) { return response(require(id)); }
    @Override @Transactional(readOnly = true) public Page<CustomerInteractionResponse> listInteractions(UUID customerId, Pageable pageable) { customerReferences.requireCustomer(customerId); return repository.findAllByBusinessIdAndCustomerId(tenant(), customerId, pageable).map(this::response); }
    private CustomerInteraction require(UUID id) { return repository.findByIdAndBusinessId(id, tenant()).orElseThrow(() -> new EntityNotFoundException("CustomerInteraction", id)); }
    private UUID tenant() { return tenantContext.requireBusinessId(); }
    private static CustomerInteractionResponse response(CustomerInteraction e) { return new CustomerInteractionResponse(e.getId(), e.getVersion(), e.getCustomerId(), e.getType(), e.getSubject(), e.getContent(), e.getOccurredAt(), e.getCreatedBy(), e.getRelatedSubjectType(), e.getRelatedSubjectId(), e.getCreatedAt(), e.getUpdatedAt()); }
}