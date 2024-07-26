package org.springframework.cache.annotation;

import com.claracore.rediscacheclient.config.CacheInterceptorApi;
import com.claracore.rediscacheclient.config.TimedCacheable;
import com.claracore.rediscacheclient.config.TtlInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.interceptor.CacheOperation;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.env.Environment;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Custom cache annotation parser that extends the default Spring cache annotation parser.
 * It handles custom annotations such as @TimedCacheable.
 *
 * @author CGI
 */
public class SpringCacheAnnotationParserApi extends SpringCacheAnnotationParser {
    private final Environment env;
    private static final Pattern PATTERN = Pattern.compile("\\$\\{(cache\\.[a-zA-Z][a-zA-Z0-9]*)}");
    private static final Logger LOGGER = LoggerFactory.getLogger(SpringCacheAnnotationParserApi.class);

    public SpringCacheAnnotationParserApi(Environment env) {
        this.env = env;
    }

    /**
     * Parses cache annotations on a given method and handles custom @TimedCacheable annotations.
     *
     * @param method the method to parse annotations on
     * @return a collection of CacheOperation
     */
    @Override
    public Collection<CacheOperation> parseCacheAnnotations(Method method) {
        LOGGER.debug("Parsing cache annotations for method: {}", method.getName());
        Collection<CacheOperation> ops = super.parseCacheAnnotations(method);

        if (ops != null) {
            Collection<TimedCacheable> cacheables = AnnotatedElementUtils.getAllMergedAnnotations(method, TimedCacheable.class);
            for (TimedCacheable cacheable : cacheables) {
                handleCacheableApi(cacheable);
            }
        }
        return ops;
    }

    private void handleCacheableApi(TimedCacheable cacheable) {
        String ttlStr = cacheable.ttl();
        long ttl;
        TimeUnit timeUnit;

        if (!ttlStr.chars().allMatch(Character::isDigit)) {
            Matcher matcher = PATTERN.matcher(ttlStr);
            if (!matcher.matches()) {
                throw new IllegalArgumentException("Invalid format for ttl parameter of @CacheableApi");
            }
            String propertyBase = matcher.group(1);
            ttl = resolveTtl(propertyBase);
            timeUnit = resolveTimeUnit(propertyBase);
        } else {
            ttl = Long.parseLong(ttlStr);
            timeUnit = cacheable.timeunit();
        }

        TimedCacheable.CacheType cacheType = cacheable.cacheType();
        CacheInterceptorApi.getTtlCache().put(cacheable.value()[0], new TtlInfo(ttl, timeUnit, cacheType));
        LOGGER.debug("Added cache configuration for key: {}, TTL: {}, TimeUnit: {}, CacheType: {}", cacheable.key(), ttl, timeUnit, cacheType);
    }

    /**
     * Handles the custom @TimedCacheable annotation, extracting TTL and cache type, and storing it in the CacheInterceptorApi.
     *
     * @param cacheable the TimedCacheable annotation
     */
    private long resolveTtl(String propertyBase) {
        String ttlProperty = env.getProperty(propertyBase + ".ttl");
        if (ttlProperty != null) {
            return Long.parseLong(ttlProperty);
        } else {
            LOGGER.warn("There is no {}.ttl property", propertyBase);
            String defaultTtl = env.getProperty("cache.default.ttl");
            if (defaultTtl == null) {
                throw new IllegalArgumentException("There is no " + propertyBase + ".ttl property and no cache.default.ttl property");
            }
            return Long.parseLong(defaultTtl);
        }
    }

    /**
     * Resolves the TTL value from the environment properties.
     *
     * @param propertyBase the base property name
     * @return the resolved TTL value
     */
    private TimeUnit resolveTimeUnit(String propertyBase) {
        String timeUnitProperty = env.getProperty(propertyBase + ".timeUnit");
        if (timeUnitProperty != null) {
            return parseTimeUnit(timeUnitProperty);
        } else {
            LOGGER.warn("There is no {}.timeUnit property", propertyBase);
            String defaultTimeUnit = env.getProperty("cache.default.timeUnit");
            if (defaultTimeUnit == null) {
                throw new IllegalArgumentException("There is no " + propertyBase + ".timeUnit property and no cache.default.timeUnit property");
            }
            return parseTimeUnit(defaultTimeUnit);
        }
    }

    /**
     * Parses a string representing a TimeUnit.
     *
     * @param timeUnitStr the string representation of the TimeUnit
     * @return the corresponding TimeUnit
     */
    private TimeUnit parseTimeUnit(String timeUnitStr) {
        switch (timeUnitStr) {
            case "s":
                return TimeUnit.SECONDS;
            case "m":
                return TimeUnit.MINUTES;
            case "h":
                return TimeUnit.HOURS;
            default:
                throw new IllegalArgumentException("Invalid timeUnit: " + timeUnitStr);
        }
    }
}
