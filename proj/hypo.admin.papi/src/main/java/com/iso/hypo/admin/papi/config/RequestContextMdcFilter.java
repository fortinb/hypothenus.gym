package com.iso.hypo.admin.papi.config;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Filter that copies values from the incoming HttpServletRequest headers into SLF4J MDC
 * for the duration of the HTTP request. This avoids accessing a request-scoped bean
 * before the request scope is active.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestContextMdcFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            String tracking = null;
            String username = null;
            if (request instanceof HttpServletRequest) {
                HttpServletRequest httpReq = (HttpServletRequest) request;
                tracking = httpReq.getHeader("x-tracking-number");
                // Prefer authenticated principal username if available
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.getName() != null) {
                    username = auth.getName();
                } else {
                    username = httpReq.getHeader("x-credentials");
                }
            }

            if (tracking != null) {
                MDC.put("trackingNumber", tracking);
            }
            if (username != null) {
                MDC.put("username", username);
            }

            chain.doFilter(request, response);
        } finally {
            MDC.remove("trackingNumber");
            MDC.remove("username");
        }
    }
}