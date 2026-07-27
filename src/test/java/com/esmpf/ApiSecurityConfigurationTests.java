package com.esmpf;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.esmpf.content.ContentRestController;
import com.esmpf.content.ContentService;
import com.esmpf.customer.CustomerInteractionService;
import com.esmpf.customer.CustomerRestController;
import com.esmpf.customer.CustomerService;
import com.esmpf.platform.PlatformRestController;
import com.esmpf.platform.PlatformService;
import com.esmpf.web.ApiExceptionHandler;
import com.esmpf.web.ApiSecurityConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({CustomerRestController.class, ContentRestController.class, PlatformRestController.class})
@Import({ApiSecurityConfiguration.class, ApiExceptionHandler.class})
class ApiSecurityConfigurationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private CustomerService customerService;

    @MockitoBean
    private CustomerInteractionService customerInteractionService;

    @MockitoBean
    private ContentService contentService;

    @MockitoBean
    private PlatformService platformService;

    @Test
    void protectedApiRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/customers"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));

        verifyNoInteractions(customerService);
    }

    @Test
    void authenticatedUserCanReachProtectedControllerBeforePermissionMatrix() throws Exception {
        given(customerService.listCustomers(any(Pageable.class))).willReturn(Page.empty());

        mockMvc.perform(get("/api/v1/customers").with(user("user")))
                .andExpect(status().isOk());
    }

    @Test
    void publishedContentRemainsPublic() throws Exception {
        given(contentService.listPublishedArticles(isNull(), any(Pageable.class)))
                .willReturn(Page.empty());

        mockMvc.perform(get("/api/v1/public/articles"))
                .andExpect(status().isOk());
    }

    @Test
    void infrastructureLifecycleEndpointsAreNeverExposedToUserAuthentication() throws Exception {
        mockMvc.perform(post("/api/v1/platform/outbox-events")
                        .with(user("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));

        verifyNoInteractions(platformService);
    }
}
