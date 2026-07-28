package com.esmpf;

import static org.junit.jupiter.api.Assertions.*;

import com.esmpf.commercial.CommercialRestController;
import com.esmpf.web.InternalWorkerRestController;
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
    void workerControllerIsPublishedOnlyUnderInternalNamespace() {
        RequestMapping mapping = InternalWorkerRestController.class.getAnnotation(RequestMapping.class);
        assertNotNull(mapping);
        assertArrayEquals(new String[]{"/internal/v1"}, mapping.value());
    }
}
