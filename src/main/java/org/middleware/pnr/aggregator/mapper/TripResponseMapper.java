package org.middleware.pnr.aggregator.mapper;

import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.middleware.pnr.aggregator.dto.FlightResponse;
import org.middleware.pnr.aggregator.dto.PassengerResponse;
import org.middleware.pnr.aggregator.dto.TripResponse;
import org.middleware.pnr.aggregator.model.Booking;
import org.middleware.pnr.aggregator.model.Flight;
import org.middleware.pnr.aggregator.model.Passenger;
import org.middleware.pnr.aggregator.model.TripInfo;
import org.middleware.pnr.aggregator.util.TripInfoUtil;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TripResponseMapper {

    public Future<TripResponse> mapToTripResponse(TripInfo tripInfo) {
        log.info("TripResponseMapper: mapping tripResponse");
        TripResponse tripResponse = new TripResponse();
        Booking booking = tripInfo.getBooking();
        
        tripResponse.setPnr(booking.getPnr());
        tripResponse.setCabinClass(booking.getCabinClass());
        tripResponse.setPassengers(mapPassengers(booking.getPassengers()));
        tripResponse.setFlights(mapFlights(booking.getFlights()));
        
        return Future.succeededFuture(tripResponse);
    }

    private List<PassengerResponse> mapPassengers(List<Passenger> passengers) {
        log.info("TripResponseMapper: mapping passengers");
        if (passengers == null) {
            return Collections.emptyList();
        }
        return passengers.stream()
                .map(this::mapPassenger)
                .collect(Collectors.toList());
    }

    private PassengerResponse mapPassenger(Passenger passenger) {
        log.info("TripResponseMapper: mapping each passenger");
        PassengerResponse response = new PassengerResponse();
        response.setPassengerNumber(passenger.getPassengerNumber());
        response.setFullName(TripInfoUtil.buildFullName(passenger));
        
        if (passenger.getCustomerId() != null) {
            response.setCustomerId(passenger.getCustomerId());
        }
        if (passenger.getSeat() != null) {
            response.setSeat(passenger.getSeat());
        }
        if (passenger.getTicketUrl() != null) {
            response.setTicketUrl(passenger.getTicketUrl());
        }
        
        return response;
    }

    private List<FlightResponse> mapFlights(List<Flight> flights) {
        log.info("TripResponseMapper: mapping flights");
        if (flights == null) {
            return Collections.emptyList();
        }
        return flights.stream()
                .map(this::mapFlight)
                .collect(Collectors.toList());
    }

    private FlightResponse mapFlight(Flight flight) {
        log.info("TripResponseMapper: mapping each flight");
        FlightResponse response = new FlightResponse();
        response.setFlightNumber(flight.getFlightNumber());
        response.setDepartureAirport(flight.getDepartureAirport());
        response.setDepartureTimeStamp(flight.getDepartureTimeStamp());
        response.setArrivalAirport(flight.getArrivalAirport());
        response.setArrivalTimeStamp(flight.getArrivalTimeStamp());
        return response;
    }
}
