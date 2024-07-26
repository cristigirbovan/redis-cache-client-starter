package com.claracore.rediscacheclient.config;

import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import io.lettuce.core.resource.Delay;
import io.lettuce.core.resource.DnsResolvers;
import io.lettuce.core.tracing.BraveTracing;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Configuration class for setting up Redis connections and Redis-based caching.
 *
 * <p>
 * This class provides the necessary beans for connecting to a Redis cluster and
 * setting up a RedisTemplate for Redis operations and a CacheManager for caching.
 * </p>
 *
 * @Author CGI
 */
@Configuration
public class RedisConfig {
    @Value("${spring.redis.cluster.nodes}")
    private String clusterNodes;

    @Value("${spring.redis.cluster.max-redirects}")
    private int maxRedirects;

    @Value("${spring.redis.timeout}")
    private Duration timeout;

    /**
     * Creates a LettuceConnectionFactory for connecting to a Redis cluster with connection pooling.
     *
     * @return a configured LettuceConnectionFactory
     */
    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        List<String> nodes = Stream.of(clusterNodes.split(","))
                .collect(Collectors.toList());
        RedisClusterConfiguration clusterConfig = new RedisClusterConfiguration(nodes);
        clusterConfig.setMaxRedirects(maxRedirects);

        LettucePoolingClientConfiguration clientConfig = LettucePoolingClientConfiguration.builder()
                .commandTimeout(timeout)
                .clientResources(clientResources())  // Use the custom ClientResources
                .build();

        return new LettuceConnectionFactory(clusterConfig, clientConfig);
    }

    /**
     * Creates a RedisTemplate for performing Redis operations.
     * The RedisTemplate is configured to use a GenericJackson2JsonRedisSerializer for serializing values.
     *
     * @return a configured RedisTemplate
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        // Use Jackson JSON serializer
        GenericJackson2JsonRedisSerializer jackson2JsonRedisSerializer = new GenericJackson2JsonRedisSerializer();

        // Configure the RedisTemplate with JSON serialization
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(jackson2JsonRedisSerializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(jackson2JsonRedisSerializer);
        template.setDefaultSerializer(jackson2JsonRedisSerializer);

        template.setEnableDefaultSerializer(true);
        return template;
    }

    /**
     * Creates a CacheManager for managing Redis-based caches.
     *
     * @param redisConnectionFactory the Redis connection factory
     * @return a configured RedisCacheManager
     */
    @Bean
    public CacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory) {
        return RedisCacheManager.builder(redisConnectionFactory).build();
    }

    /**
     * Provides ClientResources for customizing client resources.
     * This configuration is suitable for a production environment, with optimized settings for performance and resource management.
     *
     * @return configured ClientResources
     */
    @Bean(destroyMethod = "shutdown")
    public ClientResources clientResources() {
        return DefaultClientResources.builder()
                .ioThreadPoolSize(Runtime.getRuntime().availableProcessors() * 2)  // Customize the number of IO threads
                .computationThreadPoolSize(Runtime.getRuntime().availableProcessors())  // Customize the number of computation threads
                .dnsResolver(DnsResolvers.JVM_DEFAULT)  // Use the JVM's default DNS resolver
                .reconnectDelay(Delay.constant(Duration.ofSeconds(5)))  // Delay for reconnect attempts
                .build();
    }
}
