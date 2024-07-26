package com.claracore.rediscacheclient.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.annotation.SpringCacheAnnotationParserApi;
import org.springframework.cache.interceptor.CacheInterceptor;
import org.springframework.cache.interceptor.CacheOperationSource;
import org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Configuration class for setting up cache-related beans and managing cache configurations.
 * Enables caching and scheduling in the application context.
 *
 * @author CGI
 */
@Configuration
@EnableCaching
@EnableScheduling
public class CacheConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(CacheConfig.class);

    private final CacheProperties cacheProperties;
    private final Environment env;
    private final RedisTemplate<String, Object> redisTemplate;

    public CacheConfig(Environment env, CacheProperties cacheProperties, RedisTemplate<String, Object> redisTemplate) {
        this.env = env;
        this.cacheProperties = cacheProperties;
        this.redisTemplate = redisTemplate;
    }

    @Bean
    public CacheOperationSource customCacheOperationSource() {
        return new AnnotationCacheOperationSourceApi(new SpringCacheAnnotationParserApi(env));
    }

    /**
     * Bean definition for custom CacheInterceptor.
     *
     * @return a new instance of CacheInterceptorApi
     */
    @Primary
    @Bean
    public CacheInterceptor customCacheInterceptor() {
        CacheInterceptor interceptor = new CacheInterceptorApi(redisTemplate);
        interceptor.setCacheOperationSources(customCacheOperationSource());
        return interceptor;
    }

    /**
     * Event listener for handling RefreshScopeRefreshedEvent.
     * Updates TTL information for caches based on configuration properties.
     *
     * @param event the refresh event
     */
    @EventListener(RefreshScopeRefreshedEvent.class)
    public void onRefresh(RefreshScopeRefreshedEvent event) {
        for (String cacheName : cacheProperties.getCache().keySet()) {
            if (cacheName.equalsIgnoreCase(CacheConstants.DEFAULT_CACHE_NAME) || !(cacheProperties.getCache().get(cacheName) instanceof Map)) {
                continue;
            }

            @SuppressWarnings("unchecked")
            Map<String, String> cacheConfig = (Map<String, String>) cacheProperties.getCache().get(cacheName);
            long ttl = Long.parseLong(cacheConfig.get(CacheConstants.TTL));
            TimeUnit timeUnit = getTimeUnit(cacheConfig.get(CacheConstants.TIME_UNIT));
            TimedCacheable.CacheType cacheType = TimedCacheable.CacheType.valueOf(cacheConfig.get(CacheConstants.CACHE_TYPE).toUpperCase());

            if (CacheInterceptorApi.getTtlCache().get(cacheName) != null) {
                CacheInterceptorApi.getTtlCache().get(cacheName).setTtl(ttl);
                CacheInterceptorApi.getTtlCache().get(cacheName).setTimeUnit(timeUnit);
                CacheInterceptorApi.getTtlCache().get(cacheName).setCacheType(cacheType);
            } else {
                CacheInterceptorApi.getTtlCache().put(cacheName, new TtlInfo(ttl, timeUnit, cacheType));
            }
        }
    }

    /**
     * Converts a time unit string representation to a TimeUnit enum.
     *
     * @param timeLetter the string representation of the time unit
     * @return the corresponding TimeUnit enum
     */
    private TimeUnit getTimeUnit(String timeLetter) {
        TimeUnit timeUnit = TimeUnit.MINUTES;
        if (timeLetter == null) {
            return timeUnit;
        }
        switch (timeLetter) {
            case "s":
                timeUnit = TimeUnit.SECONDS;
                break;
            case "m":
                timeUnit = TimeUnit.MINUTES;
                break;
            case "h":
                timeUnit = TimeUnit.HOURS;
                break;
            default:
        }
        return timeUnit;
    }
}
