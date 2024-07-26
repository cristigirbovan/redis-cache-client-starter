package com.claracore.rediscacheclient.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration class for cache properties.
 *
 * <p>
 * This class is used to load cache-related properties from external configuration sources.
 * It uses Spring Boot's @ConfigurationProperties to map properties with a specified prefix.
 * The properties are refreshable at runtime, thanks to the @RefreshScope annotation.
 * </p>
 *
 * <p>
 * The cache properties are stored in a map where the key is the cache name and the value is an object
 * that contains the cache configuration details.
 * </p>
 *
 * <p>
 * Example configuration in application.properties or application.yml:
 * <pre>
 * cache:
 *   myCache:
 *     ttl: 3600
 *     timeUnit: s
 *     cacheType: REDIS
 * </pre>
 * </p>
 *
 * @author CGI
 */
@Configuration
@RefreshScope
@ConfigurationProperties(value = "")
public class CacheProperties {
    private Map<String, Object> cache = new HashMap<>();

    public Map<String, Object> getCache() {
        return cache;
    }

    public void setCache(Map<String, Object> cache) {
        this.cache = cache;
    }
}
