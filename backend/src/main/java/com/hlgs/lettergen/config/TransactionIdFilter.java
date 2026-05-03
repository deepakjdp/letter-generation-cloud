package com.hlgs.lettergen.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class TransactionIdFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(TransactionIdFilter.class);

    public static final String TRANSACTION_ID_HEADER = "X-Transaction-Id";
    public static final String TRANSACTION_ID_MDC_KEY = "transactionId";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String transactionId = resolveTransactionId(request);
        MDC.put(TRANSACTION_ID_MDC_KEY, transactionId);
        response.setHeader(TRANSACTION_ID_HEADER, transactionId);

        log.info("event=request_started method={} path={} transactionId={}",
                request.getMethod(), request.getRequestURI(), transactionId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            log.info("event=request_completed method={} path={} status={} transactionId={}",
                    request.getMethod(), request.getRequestURI(), response.getStatus(), transactionId);
            MDC.remove(TRANSACTION_ID_MDC_KEY);
        }
    }

    private String resolveTransactionId(HttpServletRequest request) {
        String headerValue = request.getHeader(TRANSACTION_ID_HEADER);
        if (headerValue == null || headerValue.trim().isEmpty()) {
            return UUID.randomUUID().toString();
        }
        return headerValue.trim();
    }
}

// Made with Bob