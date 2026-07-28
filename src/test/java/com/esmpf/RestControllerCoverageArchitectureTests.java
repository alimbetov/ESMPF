package com.esmpf;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.esmpf.catalog.CatalogRestController;
import com.esmpf.catalog.CatalogService;
import com.esmpf.commercial.CommercialRestController;
import com.esmpf.commercial.CommercialService;
import com.esmpf.communication.CommunicationRestController;
import com.esmpf.communication.CommunicationService;
import com.esmpf.content.ContentRestController;
import com.esmpf.content.ContentService;
import com.esmpf.customer.CustomerInteractionService;
import com.esmpf.customer.CustomerRestController;
import com.esmpf.customer.CustomerService;
import com.esmpf.document.DocumentRestController;
import com.esmpf.document.DocumentService;
import com.esmpf.equipment.EquipmentRestController;
import com.esmpf.equipment.EquipmentService;
import com.esmpf.identity.IdentityRestController;
import com.esmpf.identity.IdentityService;
import com.esmpf.identity.auth.AuthenticationRestController;
import com.esmpf.identity.auth.AuthenticationService;
import com.esmpf.maintenance.MaintenanceRestController;
import com.esmpf.maintenance.MaintenanceService;
import com.esmpf.platform.PlatformRestController;
import com.esmpf.platform.PlatformService;
import com.esmpf.service.ServiceManagementRestController;
import com.esmpf.service.ServiceManagementService;
import com.esmpf.service.ServiceSupportRestController;
import com.esmpf.service.ServiceSupportService;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RestController;

class RestControllerCoverageArchitectureTests {

    private static final Map<Class<?>, Class<?>> SERVICE_TO_CONTROLLER = Map.ofEntries(
            Map.entry(AuthenticationService.class, AuthenticationRestController.class),
            Map.entry(IdentityService.class, IdentityRestController.class),
            Map.entry(CustomerService.class, CustomerRestController.class),
            Map.entry(CustomerInteractionService.class, CustomerRestController.class),
            Map.entry(CatalogService.class, CatalogRestController.class),
            Map.entry(EquipmentService.class, EquipmentRestController.class),
            Map.entry(MaintenanceService.class, MaintenanceRestController.class),
            Map.entry(ServiceManagementService.class, ServiceManagementRestController.class),
            Map.entry(ServiceSupportService.class, ServiceSupportRestController.class),
            Map.entry(CommercialService.class, CommercialRestController.class),
            Map.entry(DocumentService.class, DocumentRestController.class),
            Map.entry(CommunicationService.class, CommunicationRestController.class),
            Map.entry(PlatformService.class, PlatformRestController.class),
            Map.entry(ContentService.class, ContentRestController.class)
    );

    @Test
    void everyServiceMethodHasAControllerMethodWithTheSameName() {
        SERVICE_TO_CONTROLLER.forEach((service, controller) -> {
            assertTrue(controller.isAnnotationPresent(RestController.class), controller.getName());
            Set<String> controllerMethods = List.of(controller.getDeclaredMethods()).stream()
                    .map(Method::getName)
                    .collect(Collectors.toSet());
            for (Method serviceMethod : service.getDeclaredMethods()) {
                assertTrue(controllerMethods.contains(serviceMethod.getName()),
                        service.getSimpleName() + "." + serviceMethod.getName());
            }
        });
    }
}
