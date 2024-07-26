package com.claracore.rediscacheclient.config;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * Annotation to enable caching for API methods or classes.
 * <p>
 * This annotation is a custom wrapper around Spring's {@link Cacheable} annotation,
 * providing additional attributes for configuring cache settings specific to API use cases.
 * It allows specifying cache names, keys, and cache managers, as well as setting time-to-live (TTL)
 * and selecting the cache type (e.g., Redis or Hazelcast).
 * <p>
 * Example usage:
 * <pre>
 * {@literal @}TimedCacheable(cacheNames = "getPostRT", key= "#postId", ttl = "${cache.getPostRT}")
 * public Object getPostRT(@RequestParam String postId) {
 *     // method implementation
 * }
 *
 * --- application.properties ---
 *
 * cache.getPostRT.ttl=90
 * cache.getPostRT.timeUnit=s
 * cache.getPostRT.cacheType=REDIS
 * </pre>
 * </p>
 * <p>
 * Note:
 * - Ensure the appropriate cache manager is configured in the application context.
 * - Use TTL values cautiously to balance between performance and data freshness.
 * </p>
 *
 * @author CGI
 * @see Cacheable
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Cacheable
public @interface TimedCacheable {

    @AliasFor("cacheNames")
    String[] value() default {};

    @AliasFor("value")
    String[] cacheNames() default {};

    String key() default "";

    String keyGenerator() default "";

    String cacheManager() default "";

    String cacheResolver() default "";

    String condition() default "";

    String unless() default "";

    boolean sync() default false;

    String ttl();

    /**
     * Time-to-live for the cache entry.
     * Expressed as a string to allow for configuration via properties files.
     */
    TimeUnit timeunit() default TimeUnit.MINUTES;

    /**
     * Cache type to determine which caching implementation to use.
     */
    enum CacheType {
        REDIS, HAZELCAST
    }

    /**
     * Specifies the cache type to use.
     */
    CacheType cacheType() default CacheType.REDIS;
}
