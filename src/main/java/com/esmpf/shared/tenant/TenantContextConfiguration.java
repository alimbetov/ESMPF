package com.esmpf.shared.tenant;

import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class TenantContextConfiguration {

    @Bean
    @ConditionalOnMissingBean(TenantContext.class)
    TenantContext unconfiguredTenantContext() {
        return new TenantContext() {
            @Override
            public UUID requireBusinessId() {
                throw new IllegalStateException("TenantContext is not configured for this execution");
            }

            @Override
            public UUID requireUserId() {
                throw new IllegalStateException("TenantContext is not configured for this execution");
            }
        };
    }
}
