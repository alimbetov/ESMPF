package com.esmpf.customer;

import static com.esmpf.customer.CustomerDtos.CustomerReference;
import static com.esmpf.customer.CustomerDtos.ServiceLocationReference;

import java.util.UUID;

public interface CustomerReferenceQuery {
    CustomerReference requireCustomer(UUID customerId);
    ServiceLocationReference requireServiceLocation(UUID locationId);
}
