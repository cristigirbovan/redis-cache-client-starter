# Redis Cache Client Starter

## Overview
The Redis Cache Client Starter is a Spring Boot starter for integrating Redis-based caching using Lettuce. This starter simplifies the configuration and management of Redis connections and caching within a Spring Boot application.

## Features
- **Redis Cluster Configuration**: Connects to a Redis cluster using Lettuce.
- **Cache Management**: Provides a CacheManager for managing Redis-based caches.
- **RedisTemplate**: Configured to use `GenericJackson2JsonRedisSerializer` for value serialization.
- **Client Resources**: Optimized settings for performance and resource management.
- **Custom Cache Annotations**: Supports `TimedCacheable` annotation for TTL configuration.

## Getting Started

### Prerequisites

- Java 11 or higher
- Maven

### Installation

1. Clone the repository:
    ```sh
    git clone https://github.com/cristigirbovan/redis-cache-client-starter.git
    ```
2. Navigate to the project directory:
    ```sh
    cd redis-cache-client-starter
    ```
3. Build the project using Maven:
    ```sh
    mvn clean install
    ```


### Configuration

Configure the following properties in your `application.properties`:

```application.properties
#redis-cluster
spring.redis.cluster.nodes=127.0.0.1:7000,127.0.0.1:7001,127.0.0.1:7002
spring.redis.cluster.max-redirects=3
spring.redis.timeout=5000ms
redis.client.io-thread-pool-size=${processor.count} * 2
redis.client.computation-thread-pool-size=${processor.count}
redis.client.reconnect-delay=5000

#custom cache properties
cache.default.ttl=30
cache.default.timeUnit=s
cache.default.cacheType=REDIS

cache.getPostRT.ttl=90
cache.getPostRT.timeUnit=s
cache.getPostRT.cacheType=REDIS

#enable DEBUG
logging.level.org.springframework.cache.annotation.SpringCacheAnnotationParserApi=DEBUG
logging.level.com.claracore.rediscacheclient=DEBUG
```

### Dependencies

Add the following dependency to your pom.xml:

```pom.xml
<dependency>
    <groupId>com.claracore</groupId>
    <artifactId>redis-cache-client-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Usage

#### Caching result of a single API Request

```java
@GetMapping("/getPostRT")
@TimedCacheable(cacheNames = "getPostRT", key = "#postId", ttl = "${cache.getPostRT}")
public Object getPostRT(@RequestParam String postId) throws Throwable {
  String url = "https://jsonplaceholder.typicode.com/posts/" + postId;
  LOGGER.debug("postId {} not returned from cache!", postId);
  return externalApiService.makeRequest(url, "RestTemplate", "GET", false, 5000, 5, null, Object.class);
}
```

Nothing else :)
