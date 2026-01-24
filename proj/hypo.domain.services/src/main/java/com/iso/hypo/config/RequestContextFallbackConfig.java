package com.iso.hypo.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.iso.hypo.common.context.RequestContext;

@Configuration
public class RequestContextFallbackConfig {

    @Bean
    @ConditionalOnMissingBean(RequestContext.class)
    public RequestContext requestContext() {
        // Return an empty RequestContext for non-web contexts (unit tests)
        return new RequestContext();
    }
}
