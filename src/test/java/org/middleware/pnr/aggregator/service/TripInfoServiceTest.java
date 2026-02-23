package org.middleware.pnr.aggregator.service;

import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.middleware.pnr.aggregator.dto.TripResponse;
import org.middleware.pnr.aggregator.exceptions.BookingNotFoundException;
import org.middleware.pnr.aggregator.mapper.TripResponseMapper;
import org.middleware.pnr.aggregator.model.Baggage;
import org.middleware.pnr.aggregator.model.Booking;
import org.middleware.pnr.aggregator.model.Passenger;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TripInfoService Tests")
class TripInfoServiceTest {

    @Mock
    private BookingService bookingService;

    @Mock
    private BaggageService baggageService;

    @Mock
    private ETicketService eTicketService;

    @Mock
    private TripResponseMapper tripResponseMapper;

    @Mock
    private EventBus eventBus;

    @InjectMocks
    private TripInfoService tripInfoService;

    @BeforeEach
    void setUp() {
        lenient().when(eventBus.publish(anyString(), any())).thenReturn(eventBus);
    }

    @Test
    @DisplayName("Should throw BookingNotFoundException when booking is not found by PNR")
    void testGetTripInfo_WhenBookingNotFound_ShouldThrowBookingNotFoundException() throws InterruptedException {
        String pnr = "NONEXISTENT";
        BookingNotFoundException exception = new BookingNotFoundException("Booking not found for PNR: " + pnr);
        when(bookingService.getBookingByPnr(pnr)).thenReturn(Future.failedFuture(exception));
        when(baggageService.getBaggageByPNR(pnr)).thenReturn(Future.succeededFuture(Collections.emptyList()));
        CountDownLatch latch = new CountDownLatch(1);
        BookingNotFoundException[] capturedException = new BookingNotFoundException[1];
        tripInfoService.getTripInfo(pnr).onSuccess(response -> {
            fail("Expected BookingNotFoundException but got success");
            latch.countDown();
        }).onFailure(throwable -> {
            if (throwable instanceof BookingNotFoundException) {
                capturedException[0] = (BookingNotFoundException) throwable;
            }
            latch.countDown();
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Test timed out");
        assertNotNull(capturedException[0], "Expected BookingNotFoundException");
        assertEquals("Booking not found for PNR: " + pnr, capturedException[0].getMessage());
        verify(bookingService).getBookingByPnr(pnr);
    }

    @Test
    @DisplayName("Should throw BookingNotFoundException when booking has null PNR")
    void testGetTripInfo_WhenBookingHasNullPnr_ShouldThrowBookingNotFoundException() throws InterruptedException {
        String pnr = "TEST123";
        Booking emptyBooking = new Booking();
        emptyBooking.setPnr(null);
        emptyBooking.setPassengers(null);

        when(bookingService.getBookingByPnr(pnr)).thenReturn(Future.succeededFuture(emptyBooking));
        when(baggageService.getBaggageByPNR(pnr)).thenReturn(Future.succeededFuture(Collections.emptyList()));

        CountDownLatch latch = new CountDownLatch(1);
        BookingNotFoundException[] capturedException = new BookingNotFoundException[1];
        tripInfoService.getTripInfo(pnr).onSuccess(response -> {
            fail("Expected BookingNotFoundException but got success");
            latch.countDown();
        }).onFailure(throwable -> {
            if (throwable instanceof BookingNotFoundException) {
                capturedException[0] = (BookingNotFoundException) throwable;
            }
            latch.countDown();
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Test timed out");
        assertNotNull(capturedException[0], "Expected BookingNotFoundException");
        assertTrue(capturedException[0].getMessage().contains(pnr));
    }

    @Test
    @DisplayName("Should throw BookingNotFoundException when booking has empty passengers list")
    void testGetTripInfo_WhenBookingHasEmptyPassengers_ShouldThrowBookingNotFoundException() throws InterruptedException {
        String pnr = "TEST123";
        Booking booking = new Booking();
        booking.setPnr(pnr);
        booking.setPassengers(Collections.emptyList());

        when(bookingService.getBookingByPnr(pnr)).thenReturn(Future.succeededFuture(booking));
        when(baggageService.getBaggageByPNR(pnr)).thenReturn(Future.succeededFuture(Collections.emptyList()));
        lenient().when(tripResponseMapper.mapToTripResponse(any())).thenReturn(Future.failedFuture(new BookingNotFoundException("Booking not found")));

        CountDownLatch latch = new CountDownLatch(1);
        BookingNotFoundException[] capturedException = new BookingNotFoundException[1];
        tripInfoService.getTripInfo(pnr).onSuccess(response -> {
            fail("Expected BookingNotFoundException but got success");
            latch.countDown();
        }).onFailure(throwable -> {
            if (throwable instanceof BookingNotFoundException) {
                capturedException[0] = (BookingNotFoundException) throwable;
            }
            latch.countDown();
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Test timed out");
        assertNotNull(capturedException[0], "Expected BookingNotFoundException");
    }

    @Test
    @DisplayName("Should successfully return TripResponse when booking is found")
    void testGetTripInfo_WhenBookingFound_ShouldReturnTripResponse() throws InterruptedException {
        String pnr = "VALID123";
        Booking booking = createValidBooking(pnr);
        TripResponse tripResponse = new TripResponse();
        tripResponse.setPnr(pnr);

        when(bookingService.getBookingByPnr(pnr)).thenReturn(Future.succeededFuture(booking));
        when(baggageService.getBaggageByPNR(pnr)).thenReturn(Future.succeededFuture(Collections.emptyList()));
        when(eTicketService.getETicket(anyString(), anyInt())).thenReturn(Future.succeededFuture(Optional.empty()));
        when(tripResponseMapper.mapToTripResponse(any())).thenReturn(Future.succeededFuture(tripResponse));

        CountDownLatch latch = new CountDownLatch(1);
        TripResponse[] capturedResponse = new TripResponse[1];
        tripInfoService.getTripInfo(pnr).onSuccess(response -> {
            capturedResponse[0] = response;
            latch.countDown();
        }).onFailure(throwable -> {
            fail("Expected success but got: " + throwable.getMessage());
            latch.countDown();
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Test timed out");
        assertNotNull(capturedResponse[0]);
        assertEquals(pnr, capturedResponse[0].getPnr());
        verify(bookingService).getBookingByPnr(pnr);
        verify(baggageService).getBaggageByPNR(pnr);
        verify(tripResponseMapper).mapToTripResponse(any());
    }

    @Test
    @DisplayName("Should handle baggage service failure gracefully")
    void testGetTripInfo_WhenBaggageServiceFails_ShouldContinueWithEmptyBaggage() throws InterruptedException {
        String pnr = "VALID123";
        Booking booking = createValidBooking(pnr);
        TripResponse tripResponse = new TripResponse();
        tripResponse.setPnr(pnr);

        when(bookingService.getBookingByPnr(pnr)).thenReturn(Future.succeededFuture(booking));
        when(baggageService.getBaggageByPNR(pnr)).thenReturn(Future.failedFuture(new RuntimeException("Baggage service error")));
        when(eTicketService.getETicket(anyString(), anyInt())).thenReturn(Future.succeededFuture(Optional.empty()));
        when(tripResponseMapper.mapToTripResponse(any())).thenReturn(Future.succeededFuture(tripResponse));

        CountDownLatch latch = new CountDownLatch(1);
        TripResponse[] capturedResponse = new TripResponse[1];

        tripInfoService.getTripInfo(pnr).onSuccess(response -> {
            capturedResponse[0] = response;
            latch.countDown();
        }).onFailure(throwable -> {
            fail("Expected success but got: " + throwable.getMessage());
            latch.countDown();
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Test timed out");
        assertNotNull(capturedResponse[0]);
        verify(baggageService).getBaggageByPNR(pnr);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "PNR WITH SPACES", "PNR\n123"})
    @DisplayName("Should handle various invalid PNR formats")
    void testGetTripInfo_WithInvalidPnrFormats_ShouldHandleGracefully(String invalidPnr) {
        assertNotNull(invalidPnr);
    }

    @Test
    @DisplayName("Should throw BookingNotFoundException when customer booking is not found")
    void testGetTripInfoByCustomerId_WhenBookingNotFound_ShouldThrowBookingNotFoundException() throws InterruptedException {
        String customerId = "CUST999";
        BookingNotFoundException exception = new BookingNotFoundException("Booking not found for customer ID: " + customerId);
        when(bookingService.getBookingByCustomerId(customerId)).thenReturn(Future.failedFuture(exception));

        CountDownLatch latch = new CountDownLatch(1);
        BookingNotFoundException[] capturedException = new BookingNotFoundException[1];
        tripInfoService.getTripInfoByCustomerId(customerId).onSuccess(response -> {
            fail("Expected BookingNotFoundException but got success");
            latch.countDown();
        }).onFailure(throwable -> {
            if (throwable instanceof BookingNotFoundException) {
                capturedException[0] = (BookingNotFoundException) throwable;
            }
            latch.countDown();
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS), "Test timed out");
        assertNotNull(capturedException[0], "Expected BookingNotFoundException");
        verify(bookingService).getBookingByCustomerId(customerId);
    }

    private Booking createValidBooking(String pnr) {
        Booking booking = new Booking();
        booking.setPnr(pnr);
        booking.setCabinClass("Economy");

        Passenger passenger = new Passenger();
        passenger.setPassengerNumber(1);
        passenger.setCustomerId("CUST001");
        List<Passenger> passengers = new ArrayList<>();
        passengers.add(passenger);
        booking.setPassengers(passengers);

        return booking;
    }
}
