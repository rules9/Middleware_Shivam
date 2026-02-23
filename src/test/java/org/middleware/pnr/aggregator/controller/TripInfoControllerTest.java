package org.middleware.pnr.aggregator.controller;

import io.vertx.circuitbreaker.OpenCircuitException;
import io.vertx.core.Future;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.middleware.pnr.aggregator.dto.TripResponse;
import org.middleware.pnr.aggregator.exceptions.BookingNotFoundException;
import org.middleware.pnr.aggregator.service.RateLimiterService;
import org.middleware.pnr.aggregator.service.TripInfoService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TripInfoController Tests")
class TripInfoControllerTest {

    @Mock
    private TripInfoService tripInfoService;

    @Mock
    private RateLimiterService rateLimiterService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private TripInfoController tripInfoController;

    @BeforeEach
    void setUp() {
        lenient().when(request.getHeader("X-Client-IP")).thenReturn(null);
        lenient().when(request.getRemoteAddr()).thenReturn("127.0.0.1");
    }

    @Test
    @DisplayName("Should return 200:OK when trip info is successfully retrieved by PNR")
    void testGetTrip_WhenSuccessful_ShouldReturn200() throws Exception{
        String pnr = "PNR123";
        TripResponse tripResponse = new TripResponse();
        tripResponse.setPnr(pnr);
        
        when(rateLimiterService.isAllowed(anyString())).thenReturn(true);
        when(tripInfoService.getTripInfo(pnr))
                .thenReturn(Future.succeededFuture(tripResponse));

        CompletableFuture<ResponseEntity<TripResponse>> result = 
                tripInfoController.getTrip(pnr, request);

        assertNotNull(result);
        ResponseEntity<TripResponse> response = result.get();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(pnr, response.getBody().getPnr());
        verify(tripInfoService).getTripInfo(pnr);
    }

    @Test
    @DisplayName("Should propagate BookingNotFoundException when booking is not found by PNR")
    void testGetTrip_WhenBookingNotFound_ShouldPropagateBookingNotFoundException(){
        String pnr = "NONEXISTENT";
        when(rateLimiterService.isAllowed(anyString())).thenReturn(true);
        
        BookingNotFoundException exception = new BookingNotFoundException("Booking not found for PNR: " + pnr);
        when(tripInfoService.getTripInfo(pnr))
                .thenReturn(Future.failedFuture(exception));
        CompletableFuture<ResponseEntity<TripResponse>> result = 
                tripInfoController.getTrip(pnr, request);
        assertNotNull(result);
        assertTrue(result.isCompletedExceptionally());
        ExecutionException executionException = assertThrows(ExecutionException.class, result::get);
        Throwable cause = executionException.getCause();
        assertTrue(cause instanceof BookingNotFoundException || 
                  (cause != null && cause.getCause() instanceof BookingNotFoundException));
    }

    @Test
    @DisplayName("Should return 200:OK when trip info is successfully retrieved by customer ID")
    void testGetTripByCustomerId_WhenSuccessful_ShouldReturn200() throws Exception {
        String customerId = "CUST123";
        TripResponse tripResponse = new TripResponse();
        tripResponse.setPnr("PNR456");
        
        when(rateLimiterService.isAllowed(anyString())).thenReturn(true);
        when(tripInfoService.getTripInfoByCustomerId(customerId))
                .thenReturn(Future.succeededFuture(tripResponse));
        CompletableFuture<ResponseEntity<TripResponse>> result = 
                tripInfoController.getTripByCustomerId(customerId, request);
        assertNotNull(result);
        ResponseEntity<TripResponse> response = result.get();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(tripInfoService).getTripInfoByCustomerId(customerId);
    }

    @Test
    @DisplayName("Should propagate BookingNotFoundException when booking is not found by customer ID")
    void testGetTripByCustomerId_WhenBookingNotFound_ShouldPropagateBookingNotFoundException() throws Exception {
        String customerId = "CUST999";
        when(rateLimiterService.isAllowed(anyString())).thenReturn(true);
        BookingNotFoundException exception = new BookingNotFoundException("Booking not found for customer ID: " + customerId);
        when(tripInfoService.getTripInfoByCustomerId(customerId))
                .thenReturn(Future.failedFuture(exception));
        CompletableFuture<ResponseEntity<TripResponse>> result = 
                tripInfoController.getTripByCustomerId(customerId, request);
        assertNotNull(result);
        assertTrue(result.isCompletedExceptionally());
        ExecutionException executionException = assertThrows(ExecutionException.class, () -> result.get());
        Throwable cause = executionException.getCause();
        assertTrue(cause instanceof BookingNotFoundException || 
                  (cause != null && cause.getCause() instanceof BookingNotFoundException));
    }

    @Test
    @DisplayName("Should return 503 when circuit breaker is open")
    void testGetTrip_WhenCircuitBreakerOpen_ShouldReturn503() throws Exception {
        String pnr = "PNR123";
        when(rateLimiterService.isAllowed(anyString())).thenReturn(true);
        OpenCircuitException circuitException = mock(OpenCircuitException.class);
        when(tripInfoService.getTripInfo(pnr))
                .thenReturn(Future.failedFuture(circuitException));
        CompletableFuture<ResponseEntity<TripResponse>> result = 
                tripInfoController.getTrip(pnr, request);
        assertNotNull(result);
        ResponseEntity<TripResponse> response = result.get();
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
    }

    @Test
    @DisplayName("Should return 500 when generic exception occurs")
    void testGetTrip_WhenGenericException_ShouldReturn500() throws Exception {
        String pnr = "PNR123";
        when(rateLimiterService.isAllowed(anyString())).thenReturn(true);
        when(tripInfoService.getTripInfo(pnr))
                .thenReturn(Future.failedFuture(new RuntimeException("Database connection failed")));
        CompletableFuture<ResponseEntity<TripResponse>> result = 
                tripInfoController.getTrip(pnr, request);
        assertNotNull(result);
        ResponseEntity<TripResponse> response = result.get();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    @DisplayName("Should return 429 when rate limit is exceeded")
    void testGetTrip_WhenRateLimitExceeded_ShouldReturn429() throws Exception {
        String pnr = "PNR123";
        when(rateLimiterService.isAllowed(anyString())).thenReturn(false);
        CompletableFuture<ResponseEntity<TripResponse>> result = 
                tripInfoController.getTrip(pnr, request);
        assertNotNull(result);
        ResponseEntity<TripResponse> response = result.get();
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        verify(tripInfoService, never()).getTripInfo(anyString());
    }

    @Test
    @DisplayName("Should return 400 when PNR is blank")
    void testGetTrip_WhenPnrIsBlank_ShouldReturn400() throws Exception {
        String pnr = "   ";
        when(rateLimiterService.isAllowed(anyString())).thenReturn(true);
        CompletableFuture<ResponseEntity<TripResponse>> result = 
                tripInfoController.getTrip(pnr, request);
        assertNotNull(result);
        ResponseEntity<TripResponse> response = result.get();
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(tripInfoService, never()).getTripInfo(anyString());
    }

    @Test
    @DisplayName("Should return 400 when PNR contains spaces")
    void testGetTrip_WhenPnrContainsSpace_ShouldReturn400() throws Exception {
        String pnr = "PNR 123";
        when(rateLimiterService.isAllowed(anyString())).thenReturn(true);
        CompletableFuture<ResponseEntity<TripResponse>> result =
                tripInfoController.getTrip(pnr, request);
        assertNotNull(result);
        ResponseEntity<TripResponse> response = result.get();
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(tripInfoService, never()).getTripInfo(anyString());
    }

    @Test
    @DisplayName("Should trim PNR before processing")
    void testGetTrip_WithWhitespacePaddedPnr_ShouldTrimAndProcess() throws Exception {
        String pnr = "  PNR123  ";
        String trimmedPnr = "PNR123";
        when(rateLimiterService.isAllowed(anyString())).thenReturn(true);
        TripResponse tripResponse = new TripResponse();
        when(tripInfoService.getTripInfo(trimmedPnr))
                .thenReturn(Future.succeededFuture(tripResponse));
        CompletableFuture<ResponseEntity<TripResponse>> result = 
                tripInfoController.getTrip(pnr, request);
        assertNotNull(result);
        result.get();
        verify(tripInfoService).getTripInfo(trimmedPnr);
    }

    @Test
    @DisplayName("Should return 503 when circuit breaker is open for customer endpoint")
    void testGetTripByCustomerId_WhenCircuitBreakerOpen_ShouldReturn503() throws Exception {
        String customerId = "CUST123";
        when(rateLimiterService.isAllowed(anyString())).thenReturn(true);
        OpenCircuitException circuitException = mock(OpenCircuitException.class);
        when(tripInfoService.getTripInfoByCustomerId(customerId))
                .thenReturn(Future.failedFuture(circuitException));
        CompletableFuture<ResponseEntity<TripResponse>> result =
                tripInfoController.getTripByCustomerId(customerId, request);
        assertNotNull(result);
        ResponseEntity<TripResponse> response = result.get();
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
    }

    @Test
    @DisplayName("Should return 500 when generic exception occurs for customer endpoint")
    void testGetTripByCustomerId_WhenGenericException_ShouldReturn500() throws Exception {
        String customerId = "CUST123";
        when(rateLimiterService.isAllowed(anyString())).thenReturn(true);
        when(tripInfoService.getTripInfoByCustomerId(customerId))
                .thenReturn(Future.failedFuture(new RuntimeException("Service unavailable")));
        CompletableFuture<ResponseEntity<TripResponse>> result =
                tripInfoController.getTripByCustomerId(customerId, request);
        assertNotNull(result);
        ResponseEntity<TripResponse> response = result.get();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    @DisplayName("Should return 429 when rate limit exceeded for customer endpoint")
    void testGetTripByCustomerId_WhenRateLimitExceeded_ShouldReturn429() throws Exception {
        String customerId = "CUST123";
        when(rateLimiterService.isAllowed(anyString())).thenReturn(false);
        CompletableFuture<ResponseEntity<TripResponse>> result =
                tripInfoController.getTripByCustomerId(customerId, request);
        assertNotNull(result);
        ResponseEntity<TripResponse> response = result.get();
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        verify(tripInfoService, never()).getTripInfoByCustomerId(anyString());
    }

    @Test
    @DisplayName("Should return 400 when customer ID is blank")
    void testGetTripByCustomerId_WhenCustomerIdIsBlank_ShouldReturn400() throws Exception {
        String customerId = "   ";
        lenient().when(rateLimiterService.isAllowed(anyString())).thenReturn(true);
        CompletableFuture<ResponseEntity<TripResponse>> result =
                tripInfoController.getTripByCustomerId(customerId, request);
        assertNotNull(result);
        ResponseEntity<TripResponse> response = result.get();
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(tripInfoService, never()).getTripInfoByCustomerId(anyString());
    }

    @Test
    @DisplayName("Should trim customer ID before processing")
    void testGetTripByCustomerId_WithWhitespacePaddedCustomerId_ShouldTrimAndProcess() throws Exception {
        String customerId = "  CUST123  ";
        String trimmedCustomerId = "CUST123";
        when(rateLimiterService.isAllowed(anyString())).thenReturn(true);
        TripResponse tripResponse = new TripResponse();
        when(tripInfoService.getTripInfoByCustomerId(trimmedCustomerId))
                .thenReturn(Future.succeededFuture(tripResponse));
        CompletableFuture<ResponseEntity<TripResponse>> result =
                tripInfoController.getTripByCustomerId(customerId, request);
        assertNotNull(result);
        result.get();
        verify(tripInfoService).getTripInfoByCustomerId(trimmedCustomerId);
    }

    @Test
    @DisplayName("Should extract client IP from X-Client-IP header")
    void testGetTrip_WithXClientIpHeader_ShouldUseHeaderValue() throws Exception {
        String pnr = "PNR123";
        String clientIp = "192.168.1.100";
        when(request.getHeader("X-Client-IP")).thenReturn(clientIp);
        when(rateLimiterService.isAllowed(clientIp)).thenReturn(true);
        TripResponse tripResponse = new TripResponse();
        when(tripInfoService.getTripInfo(pnr))
                .thenReturn(Future.succeededFuture(tripResponse));
        tripInfoController.getTrip(pnr, request).get();
        verify(rateLimiterService).isAllowed(clientIp);
    }

    @Test
    @DisplayName("Should use remote address when X-Client-IP header is not present")
    void testGetTrip_WithoutXClientIpHeader_ShouldUseRemoteAddr() throws Exception {
        String pnr = "PNR123";
        String remoteAddr = "10.0.0.1";
        when(request.getHeader("X-Client-IP")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn(remoteAddr);
        when(rateLimiterService.isAllowed(remoteAddr)).thenReturn(true);
        TripResponse tripResponse = new TripResponse();
        when(tripInfoService.getTripInfo(pnr))
                .thenReturn(Future.succeededFuture(tripResponse));
        tripInfoController.getTrip(pnr, request).get();
        verify(rateLimiterService).isAllowed(remoteAddr);
    }

}
