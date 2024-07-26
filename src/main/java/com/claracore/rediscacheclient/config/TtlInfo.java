package com.claracore.rediscacheclient.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.concurrent.TimeUnit;

/**
 * Class representing Time-to-Live (TTL) information for caching.
 * <p>
 * This class encapsulates TTL value, the unit of time, and the cache type
 * used for caching operations.
 * </p>
 * <p>
 * Example usage:
 * <pre>
 * CacheTtlConfig ttlConfig = new CacheTtlConfig(10, TimeUnit.MINUTES, CacheType.REDIS);
 * </pre>
 * </p>
 * <p>
 * Note:
 * - Ensure that the TTL values and cache type are properly configured for optimal caching.
 * </p>
 *
 * @author CGI
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TtlInfo {
    private long ttl;
    private TimeUnit timeUnit;
    private TimedCacheable.CacheType cacheType;

    /**
     * Validates the TTL information to ensure that it is correctly configured.
     * This can be useful to prevent misconfigurations in a production environment.
     *
     * @throws IllegalArgumentException if the TTL value is non-positive or timeUnit/cacheType is null
     */
    public void validate() {
        if (ttl <= 0) {
            throw new IllegalArgumentException("TTL must be positive");
        }
        if (timeUnit == null) {
            throw new IllegalArgumentException("TimeUnit must not be null");
        }
        if (cacheType == null) {
            throw new IllegalArgumentException("CacheType must not be null");
        }
    }
}
