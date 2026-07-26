package com.esmpf.customer;

import static com.esmpf.customer.CustomerDtos.*;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomerService {
    CustomerResponse createCustomer(CustomerCreateCommand command);
    CustomerResponse getCustomer(UUID customerId);
    Page<CustomerResponse> listCustomers(Pageable pageable);
    CustomerResponse updateCustomer(UUID customerId, CustomerUpdateCommand command);
    CustomerResponse archiveCustomer(UUID customerId, long version);

    ServiceLocationResponse createServiceLocation(ServiceLocationCreateCommand command);
    ServiceLocationResponse getServiceLocation(UUID locationId);
    Page<ServiceLocationResponse> listServiceLocations(UUID customerId, Pageable pageable);
    ServiceLocationResponse updateServiceLocation(UUID locationId, ServiceLocationUpdateCommand command);
    ServiceLocationResponse archiveServiceLocation(UUID locationId, long version);
}
