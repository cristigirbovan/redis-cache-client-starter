package com.claracore.rediscacheclient.config;

import org.springframework.cache.annotation.AnnotationCacheOperationSource;
import org.springframework.cache.annotation.CacheAnnotationParser;

/**
 * Custom implementation of {@link AnnotationCacheOperationSource}.
 * This class extends the default {@link AnnotationCacheOperationSource} and allows
 * for custom {@link CacheAnnotationParser} to be used.
 *
 * @author CGI
 */
public class AnnotationCacheOperationSourceApi extends AnnotationCacheOperationSource {

    public AnnotationCacheOperationSourceApi(CacheAnnotationParser annotationParser) {
        super(annotationParser);
    }

}
