package com.esmpf;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.esmpf.catalog.CatalogService;
import com.esmpf.commercial.CommercialService;
import com.esmpf.communication.CommunicationService;
import com.esmpf.customer.CustomerInteractionService;
import com.esmpf.customer.CustomerService;
import com.esmpf.document.DocumentService;
import com.esmpf.equipment.EquipmentService;
import com.esmpf.identity.IdentityService;
import com.esmpf.maintenance.MaintenanceService;
import com.esmpf.platform.PlatformService;
import com.esmpf.service.ServiceManagementService;
import com.esmpf.service.ServiceSupportService;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import org.junit.jupiter.api.Test;

class ServiceContractArchitectureTests {

    private static final List<Class<?>> SERVICE_CONTRACTS = List.of(
            CustomerService.class,
            CustomerInteractionService.class,
            CatalogService.class,
            EquipmentService.class,
            MaintenanceService.class,
            ServiceManagementService.class,
            ServiceSupportService.class,
            IdentityService.class,
            CommercialService.class,
            DocumentService.class,
            CommunicationService.class,
            PlatformService.class
    );

    @Test
    void publicServiceContractsNeverAcceptTenantIdentifiers() {
        for (Class<?> serviceContract : SERVICE_CONTRACTS) {
            assertTrue(serviceContract.isInterface(), serviceContract.getName());
            assertTrue(serviceContract.getDeclaredMethods().length > 0, serviceContract.getName());

            for (Method method : serviceContract.getDeclaredMethods()) {
                for (Parameter parameter : method.getParameters()) {
                    String parameterName = parameter.getName().toLowerCase();
                    assertFalse(parameterName.equals("tenantid"), method.toGenericString());
                    assertFalse(parameterName.equals("businessid"), method.toGenericString());
                }
            }
        }
    }

    @Test
    void serviceContractsNeverExposeDomainEntitiesAsDirectResults() {
        for (Class<?> serviceContract : SERVICE_CONTRACTS) {
            for (Method method : serviceContract.getDeclaredMethods()) {
                Package resultPackage = method.getReturnType().getPackage();
                String packageName = resultPackage == null ? "" : resultPackage.getName();
                assertFalse(packageName.contains(".domain"), method.toGenericString());
            }
        }
    }
}
