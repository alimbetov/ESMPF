package com.esmpf;

import static org.junit.jupiter.api.Assertions.*;

import com.esmpf.commercial.CommercialRestController;
import com.esmpf.communication.CommunicationInternalRestController;
import com.esmpf.document.DocumentInternalRestController;
import com.esmpf.platform.PlatformInternalRestController;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestMapping;

class RestBoundaryArchitectureTests {

    private static final Set<String> DORMANT_COMMERCIAL = Set.of(
            "createInvoice", "createInvoiceFromEstimate", "getInvoice", "listInvoices",
            "issueInvoice", "markInvoiceOverdue", "voidInvoice", "attachGeneratedDocument",
            "registerPayment", "confirmPayment", "failPayment", "refundPayment", "listPayments"
    );

    @Test
    void invoiceAndPaymentMethodsAreNotPublishedByCommercialController() {
        Set<String> methods = Arrays.stream(CommercialRestController.class.getDeclaredMethods())
                .map(Method::getName)
                .collect(Collectors.toSet());
        assertTrue(DORMANT_COMMERCIAL.stream().noneMatch(methods::contains));
    }

    @Test
    void workerControllersArePublishedOnlyUnderInternalNamespace() {
        assertInternal(PlatformInternalRestController.class, "/internal/v1/platform");
        assertInternal(CommunicationInternalRestController.class, "/internal/v1/notifications");
        assertInternal(DocumentInternalRestController.class, "/internal/v1/generated-documents");
    }

    private static void assertInternal(Class<?> controller, String expected) {
        RequestMapping mapping = controller.getAnnotation(RequestMapping.class);
        assertNotNull(mapping);
        assertArrayEquals(new String[]{expected}, mapping.value());
    }
}
