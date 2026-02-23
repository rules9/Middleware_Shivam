package org.middleware.pnr.aggregator.util;

import io.vertx.circuitbreaker.OpenCircuitException;
import io.vertx.core.Future;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.middleware.pnr.aggregator.exceptions.BookingNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@DisplayName("ControllerUtil Tests")
class ControllerUtilTest {

    @Test
    @DisplayName("Should complete exceptionally when BookingNotFoundException is thrown")
    void testToCompletableFuture_WhenBookingNotFoundException_ShouldCompleteExceptionally(){
        BookingNotFoundException exception = new BookingNotFoundException("Booking not found for PNR: TEST123");
        Future<ResponseEntity<String>> future = Future.failedFuture(exception);
        CompletableFuture<ResponseEntity<String>> result = ControllerUtil.toCompletableFuture(future);
        assertTrue(result.isDone());
        assertTrue(result.isCompletedExceptionally());
        ExecutionException executionException = assertThrows(ExecutionException.class, () -> result.get());
        assertTrue(executionException.getCause() instanceof BookingNotFoundException);
        assertEquals("Booking not found for PNR: TEST123", executionException.getCause().getMessage());
    }

    @Test
    @DisplayName("Should complete exceptionally when BookingNotFoundException is wrapped as cause")
    void testToCompletableFuture_WhenBookingNotFoundExceptionAsCause_ShouldCompleteExceptionally() {
        BookingNotFoundException cause = new BookingNotFoundException("Booking not found for customer ID: CUST123");
        RuntimeException wrapper = new RuntimeException("Wrapped exception", cause);
        Future<ResponseEntity<String>> future = Future.failedFuture(wrapper);
        CompletableFuture<ResponseEntity<String>> result = ControllerUtil.toCompletableFuture(future);
        assertTrue(result.isDone());
        assertTrue(result.isCompletedExceptionally());
        ExecutionException executionException = assertThrows(ExecutionException.class, () -> result.get());
        assertTrue(executionException.getCause() instanceof BookingNotFoundException);
        assertEquals("Booking not found for customer ID: CUST123", executionException.getCause().getMessage());
    }

    @Test
    @DisplayName("Should return 503 when OpenCircuitException is thrown")
    void testToCompletableFuture_WhenOpenCircuitException_ShouldReturn503() throws Exception {
        OpenCircuitException exception = mock(OpenCircuitException.class);
        Future<ResponseEntity<String>> future = Future.failedFuture(exception);
        CompletableFuture<ResponseEntity<String>> result = ControllerUtil.toCompletableFuture(future);
        assertTrue(result.isDone());
        assertFalse(result.isCompletedExceptionally());
        ResponseEntity<String> response = result.get();
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
    }

    @Test
    @DisplayName("Should return response when Future succeeds")
    void testToCompletableFuture_WhenSuccess_ShouldReturnResponse() throws Exception {
        ResponseEntity<String> successResponse = ResponseEntity.ok("Success");
        Future<ResponseEntity<String>> future = Future.succeededFuture(successResponse);
        CompletableFuture<ResponseEntity<String>> result = ControllerUtil.toCompletableFuture(future);
        assertTrue(result.isDone());
        assertFalse(result.isCompletedExceptionally());
        ResponseEntity<String> response = result.get();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Success", response.getBody());
    }

    @Test
    void testToCompletableFuture_WhenGenericException_ShouldReturn500() throws Exception {
        RuntimeException exception = new RuntimeException("Generic error");
        Future<ResponseEntity<String>> future = Future.failedFuture(exception);
        CompletableFuture<ResponseEntity<String>> result = ControllerUtil.toCompletableFuture(future);
        assertTrue(result.isDone());
        assertFalse(result.isCompletedExceptionally());
        ResponseEntity<String> response = result.get();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}
