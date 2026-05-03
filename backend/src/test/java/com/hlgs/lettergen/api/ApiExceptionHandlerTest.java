package com.hlgs.lettergen.api;

import com.hlgs.lettergen.config.TransactionIdFilter;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void shouldHandleIllegalArgumentException() {
        MDC.put(TransactionIdFilter.TRANSACTION_ID_MDC_KEY, "txn-123");

        try {
            ResponseEntity<String> response = handler.handleIllegalArgument(new IllegalArgumentException("bad request"));

            assertEquals(400, response.getStatusCode().value());
            assertEquals("bad request", response.getBody());
        } finally {
            MDC.clear();
        }
    }

    @Test
    void shouldHandleGenericException() {
        MDC.put(TransactionIdFilter.TRANSACTION_ID_MDC_KEY, "txn-456");

        try {
            ResponseEntity<String> response = handler.handleGeneric(new RuntimeException("boom"));

            assertEquals(500, response.getStatusCode().value());
            assertEquals("Unexpected server error: boom", response.getBody());
        } finally {
            MDC.clear();
        }
    }
}

// Made with Bob
