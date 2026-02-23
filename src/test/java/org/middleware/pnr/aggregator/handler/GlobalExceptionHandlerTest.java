package org.middleware.pnr.aggregator.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.middleware.pnr.aggregator.exceptions.BookingNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("Should return 404 when booking not found by PNR")
    void testHandleBookingNotFoundException_WithPnr_ShouldReturn404() {
        String pnr = "ABC123XYZ";
        BookingNotFoundException exception = new BookingNotFoundException("Booking not found for PNR: " + pnr);
        ResponseEntity<String> response = globalExceptionHandler.handleBookingNotFoundException(exception);
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Booking not found for PNR: " + pnr, response.getBody());
    }

    @Test
    @DisplayName("Should return 404 when booking not found by customer ID")
    void testHandleBookingNotFoundException_WithCustomerId_ShouldReturn404() {
        String customerId = "CUST12345";
        BookingNotFoundException exception = new BookingNotFoundException("Booking not found for customer ID: " + customerId);
        ResponseEntity<String> response = globalExceptionHandler.handleBookingNotFoundException(exception);
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Booking not found for customer ID: " + customerId, response.getBody());
    }

    @Test
    @DisplayName("Should handle null exception message gracefully")
    void testHandleBookingNotFoundException_WithNullMessage_ShouldReturn404() {
        BookingNotFoundException exception = new BookingNotFoundException(null);
        ResponseEntity<String> response = globalExceptionHandler.handleBookingNotFoundException(exception);
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    @DisplayName("Should return empty message when exception has empty string")
    void testHandleBookingNotFoundException_WithEmptyMessage_ShouldReturn404() {
        BookingNotFoundException exception = new BookingNotFoundException("");
        ResponseEntity<String> response = globalExceptionHandler.handleBookingNotFoundException(exception);
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("", response.getBody());
    }
}
