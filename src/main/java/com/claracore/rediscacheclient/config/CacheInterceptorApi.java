package com.claracore.rediscacheclient.config;

import lombok.Getter;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheInterceptor;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Custom CacheInterceptor that handles caching operations for different cache types.
 * It extends the default {@link CacheInterceptor} to add specific logic for Redis and Hazelcast caches.
 *
 * @author CGI
 */
public class CacheInterceptorApi extends CacheInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(CacheInterceptorApi.class);

    @Getter
    private static final Map<String, TtlInfo> ttlCache = new HashMap<>();
    private final ThreadLocal<String> methodName = new ThreadLocal<>();
    private final RedisTemplate<String, Object> redisTemplate;

    public CacheInterceptorApi(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Retrieves a value from the cache, considering the TTL and cache type.
     * For Redis, it fetches the value using redisTemplate.
     * Placeholder for Hazelcast cache type is included.
     *
     * @param cache the cache to retrieve the value from
     * @param key the key of the value to retrieve
     * @return the cached value, or null if not found or TTL is invalid
     */
    @Override
    protected Cache.ValueWrapper doGet(Cache cache, Object key) {
        TtlInfo ttlInfo = ttlCache.get(cache.getName());
        if (ttlInfo == null || ttlInfo.getTtl() < 0) {
            return null;
        }

        Cache.ValueWrapper value = null;
        try {
            if (ttlInfo.getCacheType() == TimedCacheable.CacheType.HAZELCAST) {
                // Placeholder for Hazelcast get operation
                LOGGER.warn("Hazelcast cache type is not implemented.");
                return null;
            } else if (ttlInfo.getCacheType() == TimedCacheable.CacheType.REDIS) {
                Object redisValue = redisTemplate.opsForValue().get(key);
                if (redisValue != null) {
                    value = new SimpleValueWrapper(redisValue);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error in doGet: " + e.getMessage(), e);
        }

        if (value != null && value.get() != null) {
            LOGGER.debug("Method {} returned from cache {}", methodName.get(), cache.getName());
            methodName.remove();
        }
        return value;
    }

    /**
     * Puts a value into the cache, setting the TTL and cache type.
     * For Redis, it stores the value using redisTemplate.
     * Placeholder for Hazelcast cache type is included.
     *
     * @param cache the cache to store the value in
     * @param key the key of the value to store
     * @param result the value to store in the cache
     */
    @Override
    protected void doPut(Cache cache, Object key, Object result) {
        TtlInfo ttlInfo = ttlCache.get(cache.getName());
        if (ttlInfo == null || ttlInfo.getTtl() < 0) {
            return;
        }

        try {
            if (ttlInfo.getCacheType() == TimedCacheable.CacheType.HAZELCAST) {
                // Placeholder for Hazelcast put operation
                LOGGER.warn("Hazelcast cache type is not implemented.");
                return;
            } else if (ttlInfo.getCacheType() == TimedCacheable.CacheType.REDIS) {
                redisTemplate.opsForValue().set((String) key, result, ttlInfo.getTtl(), ttlInfo.getTimeUnit());
            }
        } catch (Exception e) {
            LOGGER.error("Error in doPut: " + e.getMessage(), e);
        }
    }

    /**
     * Intercepts the method invocation to set the method name in the thread-local variable.
     *
     * @param invocation the method invocation
     * @return the result of the method invocation
     * @throws Throwable if the method invocation fails
     */
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        methodName.set(invocation.getMethod().getName());
        return super.invoke(invocation);
    }

}
