package com.claracore.rediscacheclient.config;

/**
 * Utility class containing constants for cache configuration.
 * This class defines commonly used keys and values related to cache settings.
 *
 * @author CGI
 */
public final class CacheConstants {
    public static final String DEFAULT_CACHE_NAME = "default";
    public static final String TTL = "ttl";
    public static final String TIME_UNIT = "timeUnit";
    public static final String CACHE_TYPE = "cacheType";

    // Private constructor to prevent instantiation
    private CacheConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
