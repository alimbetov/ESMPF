package com.esmpf.shared.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.util.List;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableCaching
public class CacheConfiguration {

    @Bean
    CacheManager cacheManager() {
        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(List.of(
                cache(CacheNames.CATALOG_EQUIPMENT_TYPE, Duration.ofMinutes(10), 5_000),
                cache(CacheNames.CATALOG_JOB_TYPE, Duration.ofMinutes(10), 5_000),
                cache(CacheNames.CATALOG_CHECKLIST_TEMPLATE, Duration.ofMinutes(15), 5_000),
                cache(CacheNames.CATALOG_MAINTENANCE_TEMPLATE, Duration.ofMinutes(15), 5_000),
                cache(CacheNames.CATALOG_UNIT, Duration.ofMinutes(15), 2_000),
                cache(CacheNames.CUSTOMER_REFERENCE, Duration.ofMinutes(2), 10_000),
                cache(CacheNames.SERVICE_LOCATION_REFERENCE, Duration.ofMinutes(2), 20_000),
                cache(CacheNames.EQUIPMENT_REFERENCE, Duration.ofMinutes(2), 20_000),
                cache(CacheNames.IDENTITY_USER_REFERENCE, Duration.ofMinutes(2), 10_000),
                cache(CacheNames.CURRENT_BUSINESS, Duration.ofMinutes(5), 1_000)
        ));
        return manager;
    }

    private static CaffeineCache cache(String name, Duration ttl, long maximumSize) {
        return new CaffeineCache(
                name,
                Caffeine.newBuilder()
                        .expireAfterWrite(ttl)
                        .maximumSize(maximumSize)
                        .recordStats()
                        .build());
    }
}
