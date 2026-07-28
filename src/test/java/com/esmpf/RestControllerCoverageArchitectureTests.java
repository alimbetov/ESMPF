package com.esmpf;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.esmpf.catalog.*;
import com.esmpf.commercial.*;
import com.esmpf.communication.*;
import com.esmpf.content.*;
import com.esmpf.customer.*;
import com.esmpf.document.*;
import com.esmpf.equipment.*;
import com.esmpf.identity.*;
import com.esmpf.identity.auth.*;
import com.esmpf.maintenance.*;
import com.esmpf.platform.*;
import com.esmpf.service.*;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RestController;

class RestControllerCoverageArchitectureTests {

    private static final Map<Class<?>, List<Class<?>>> SERVICE_TO_CONTROLLERS = Map.ofEntries(
            Map.entry(AuthenticationService.class, List.of(AuthenticationRestController.class)),
            Map.entry(IdentityService.class, List.of(IdentityRestController.class)),
            Map.entry(CustomerService.class, List.of(CustomerRestController.class)),
            Map.entry(CustomerInteractionService.class, List.of(CustomerRestController.class)),
            Map.entry(CatalogService.class, List.of(CatalogRestController.class)),
            Map.entry(EquipmentService.class, List.of(EquipmentRestController.class)),
            Map.entry(MaintenanceService.class, List.of(MaintenanceRestController.class)),
            Map.entry(ServiceManagementService.class, List.of(ServiceManagementRestController.class)),
            Map.entry(ServiceSupportService.class, List.of(ServiceSupportRestController.class)),
            Map.entry(CommercialService.class, List.of(CommercialRestController.class)),
            Map.entry(DocumentService.class, List.of(DocumentRestController.class, DocumentInternalRestController.class)),
            Map.entry(CommunicationService.class, List.of(CommunicationRestController.class, CommunicationInternalRestController.class)),
            Map.entry(PlatformService.class, List.of(PlatformRestController.class, PlatformInternalRestController.class)),
            Map.entry(ContentService.class, List.of(ContentRestController.class))
    );

    private static final Set<String> DORMANT_COMMERCIAL_METHODS = Set.of(
            "createInvoice", "createInvoiceFromEstimate", "getInvoice", "listInvoices",
            "issueInvoice", "markInvoiceOverdue", "voidInvoice", "attachGeneratedDocument",
            "registerPayment", "confirmPayment", "failPayment", "refundPayment", "listPayments"
    );

    @Test
    void everyPublishedServiceMethodHasAControllerMethod() {
        SERVICE_TO_CONTROLLERS.forEach((service, controllers) -> {
            controllers.forEach(controller -> assertTrue(
                    controller.isAnnotationPresent(RestController.class), controller.getName()));
            Set<String> controllerMethods = controllers.stream()
                    .flatMap(controller -> Arrays.stream(controller.getDeclaredMethods()))
                    .map(Method::getName)
                    .collect(Collectors.toSet());
            for (Method serviceMethod : service.getDeclaredMethods()) {
                if (service == CommercialService.class && DORMANT_COMMERCIAL_METHODS.contains(serviceMethod.getName())) {
                    continue;
                }
                assertTrue(controllerMethods.contains(serviceMethod.getName()),
                        service.getSimpleName() + "." + serviceMethod.getName());
            }
        });
    }
}
