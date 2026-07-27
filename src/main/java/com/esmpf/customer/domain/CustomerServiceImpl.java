package com.esmpf.customer.domain;

import static com.esmpf.customer.CustomerDtos.*;

import com.esmpf.customer.CustomerReferenceQuery;
import com.esmpf.customer.CustomerService;
import com.esmpf.shared.exception.EntityNotFoundException;
import com.esmpf.shared.exception.StaleEntityException;
import com.esmpf.shared.tenant.TenantContext;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class CustomerServiceImpl implements CustomerService, CustomerReferenceQuery {

    private final TenantContext tenantContext;
    private final CustomerRepository customerRepository;
    private final ServiceLocationRepository locationRepository;
    private final CustomerMapper mapper;

    @Override
    @Transactional
    public CustomerResponse createCustomer(CustomerCreateCommand command) {
        Customer entity = mapper.toEntity(command);
        entity.setBusinessId(tenantContext.requireBusinessId());
        return mapper.toResponse(customerRepository.saveAndFlush(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getCustomer(UUID customerId) {
        return mapper.toResponse(requireCustomerEntity(customerId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CustomerResponse> listCustomers(Pageable pageable) {
        return customerRepository
                .findAllByBusinessId(tenantContext.requireBusinessId(), pageable)
                .map(mapper::toResponse);
    }

    @Override
    @Transactional
    public CustomerResponse updateCustomer(UUID customerId, CustomerUpdateCommand command) {
        Customer entity = requireCustomerEntity(customerId);
        requireVersion("Customer", entity.getId(), command.version(), entity.getVersion());
        requireEditable(entity.getStatus(), "Customer");
        mapper.update(command, entity);
        return mapper.toResponse(customerRepository.saveAndFlush(entity));
    }

    @Override
    @Transactional
    public CustomerResponse archiveCustomer(UUID customerId, long version) {
        Customer entity = requireCustomerEntity(customerId);
        requireVersion("Customer", entity.getId(), version, entity.getVersion());
        entity.setStatus("ARCHIVED");
        return mapper.toResponse(customerRepository.saveAndFlush(entity));
    }

    @Override
    @Transactional
    public ServiceLocationResponse createServiceLocation(ServiceLocationCreateCommand command) {
        Customer customer = requireCustomerEntity(command.customerId());
        requireActive(customer.getStatus(), "Customer");
        validateParent(command.customerId(), command.parentLocationId(), null);

        ServiceLocation entity = mapper.toEntity(command);
        entity.setBusinessId(tenantContext.requireBusinessId());
        return mapper.toResponse(locationRepository.saveAndFlush(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceLocationResponse getServiceLocation(UUID locationId) {
        return mapper.toResponse(requireLocationEntity(locationId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ServiceLocationResponse> listServiceLocations(UUID customerId, Pageable pageable) {
        requireCustomerEntity(customerId);
        return locationRepository
                .findAllByBusinessIdAndCustomerId(
                        tenantContext.requireBusinessId(), customerId, pageable)
                .map(mapper::toResponse);
    }

    @Override
    @Transactional
    public ServiceLocationResponse updateServiceLocation(
            UUID locationId,
            ServiceLocationUpdateCommand command
    ) {
        ServiceLocation entity = requireLocationEntity(locationId);
        requireVersion("ServiceLocation", entity.getId(), command.version(), entity.getVersion());
        requireEditable(entity.getStatus(), "ServiceLocation");
        validateParent(entity.getCustomerId(), command.parentLocationId(), entity.getId());
        mapper.update(command, entity);
        return mapper.toResponse(locationRepository.saveAndFlush(entity));
    }

    @Override
    @Transactional
    public ServiceLocationResponse archiveServiceLocation(UUID locationId, long version) {
        ServiceLocation entity = requireLocationEntity(locationId);
        requireVersion("ServiceLocation", entity.getId(), version, entity.getVersion());
        if (locationRepository.existsByBusinessIdAndParentLocationId(
                tenantContext.requireBusinessId(), locationId)) {
            throw new IllegalStateException("Service location has active child locations");
        }
        entity.setStatus("ARCHIVED");
        return mapper.toResponse(locationRepository.saveAndFlush(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerReference requireCustomer(UUID customerId) {
        Customer entity = requireCustomerEntity(customerId);
        return new CustomerReference(entity.getId(), entity.getName(), entity.getStatus());
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceLocationReference requireServiceLocation(UUID locationId) {
        ServiceLocation entity = requireLocationEntity(locationId);
        return new ServiceLocationReference(
                entity.getId(), entity.getCustomerId(), entity.getName(), entity.getStatus());
    }

    private Customer requireCustomerEntity(UUID id) {
        return customerRepository
                .findByIdAndBusinessId(id, tenantContext.requireBusinessId())
                .orElseThrow(() -> new EntityNotFoundException("Customer", id));
    }

    private ServiceLocation requireLocationEntity(UUID id) {
        return locationRepository
                .findByIdAndBusinessId(id, tenantContext.requireBusinessId())
                .orElseThrow(() -> new EntityNotFoundException("ServiceLocation", id));
    }

    private void validateParent(UUID customerId, UUID parentId, UUID currentId) {
        if (parentId == null) {
            return;
        }
        if (parentId.equals(currentId)) {
            throw new IllegalArgumentException("Service location cannot be its own parent");
        }
        ServiceLocation parent = requireLocationEntity(parentId);
        if (!parent.getCustomerId().equals(customerId)) {
            throw new IllegalArgumentException("Parent location belongs to another customer");
        }
        requireActive(parent.getStatus(), "Parent service location");
    }

    private static void requireVersion(
            String entityName,
            UUID id,
            long expected,
            long actual
    ) {
        if (expected != actual) {
            throw new StaleEntityException(entityName, id, expected, actual);
        }
    }

    private static void requireEditable(String status, String entityName) {
        if ("ARCHIVED".equals(status)) {
            throw new IllegalStateException(entityName + " is archived");
        }
    }

    private static void requireActive(String status, String entityName) {
        if (!"ACTIVE".equals(status)) {
            throw new IllegalStateException(entityName + " is not active");
        }
    }
}
