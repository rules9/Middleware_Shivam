package org.middleware.pnr.aggregator.service;

import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.middleware.pnr.aggregator.constants.AggregatorConstants;
import org.middleware.pnr.aggregator.dto.TripResponse;
import org.middleware.pnr.aggregator.exceptions.BookingNotFoundException;
import org.middleware.pnr.aggregator.mapper.TripResponseMapper;
import org.middleware.pnr.aggregator.model.*;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TripInfoService {

    private final BookingService bookingService;
    private final BaggageService baggageService;
    private final ETicketService eTicketService;
    private final TripResponseMapper tripResponseMapper;
    private final EventBus eventBus;

    public Future<TripResponse> getTripInfoByCustomerId(String customerId) {
        log.info("TripInfoService: Fetching tripInfo for customerId: {}", customerId);
        logEventBusEvent(AggregatorConstants.CUSTOMER_ID, customerId);

        return bookingService.getBookingByCustomerId(customerId)
                .compose(booking -> {
                    if (booking.getPnr() == null || booking.getPassengers() == null || booking.getPassengers().isEmpty()) {
                        log.warn("TripInfoService: Booking info for customer: {} is not present, creating empty trip.",
                                customerId);
                        TripInfo tripInfo = new TripInfo();
                        tripInfo.setBooking(booking);
                        tripInfo.setBaggage(Collections.emptyList());
                        return Future.succeededFuture(tripInfo)
                                .compose(tripResponseMapper::mapToTripResponse);
                    }
                    String pnr = booking.getPnr();
                    return getTripInfo(pnr);
                });
    }

    public Future<TripResponse> getTripInfo(String pnr) {
        log.info("TripInfoService: Fetching tripInfo for pnr: {}", pnr);
        logEventBusEvent(AggregatorConstants.PNR, pnr);

        Future<Booking> bookingFuture = bookingService.getBookingByPnr(pnr);
        Future<List<Baggage>> baggageFutureList = baggageService.getBaggageByPNR(pnr)
                .recover(err -> Future.succeededFuture(Collections.emptyList()));


        return Future.all(bookingFuture,baggageFutureList)
                .compose(compositeFuture -> {
                    Booking booking = compositeFuture.resultAt(0);
                    List<Baggage> baggages = compositeFuture.resultAt(1);

                    log.info("TripInfoService: received booking and baggage information.");

                    if(booking.getPnr() == null || booking.getPassengers() == null){
                        log.warn("TripInfoService: Booking info for pnr: {} is not present, creating empty trip.", pnr);
                        return Future.failedFuture(new BookingNotFoundException("Booking not found for pnr: " + pnr));
                    }
                    log.info("TripInfoService: Enriching trip info with eTickets for pnr: {}", pnr);
                    return enrichTripInfoWithETicket(pnr, baggages, booking);
                }).compose(tripResponseMapper::mapToTripResponse);


    }

    private Future<TripInfo> enrichTripInfoWithETicket(String pnr, List<Baggage> baggages, Booking booking) {
        List<Passenger> passengers = booking.getPassengers();
        log.info("TripInfoService: enriching passenger/TripInfo with eTickets for pnr: {}", pnr );

        List<Future<Optional<ETicket>>> etickets =
                passengers.stream().map(passenger ->
                        eTicketService.getETicket(pnr, passenger.getPassengerNumber())
                                .recover(err -> Future.succeededFuture(Optional.empty()))).toList();

        log.info("TripInfoService: enriched passenger/TripInfo with eTickets for pnr: {}", pnr );
        return Future.join(etickets)
                .map(joinResult -> {
                    for (int i = 0; i < etickets.size(); i++) {
                        Optional<ETicket> eTicket = joinResult.resultAt(i);
                        Passenger passenger = passengers.get(i);
                        eTicket.ifPresent(tk -> passenger.setTicketUrl(tk.getUrl()));
                    }
                    TripInfo tripInfo = new TripInfo();
                    tripInfo.setBooking(booking);
                    tripInfo.setBaggage(baggages);
                    return tripInfo;
                });
    }


    private void logEventBusEvent(String key, String value){
        log.info("[EVENT BUS] -> [PUBLISHING] -> [EVENT] PNR.FETCH event publishing for {} : {}", key, value);
        if(AggregatorConstants.CUSTOMER_ID.equalsIgnoreCase(key)) key = AggregatorConstants.CUSTOMER_ID;
        else key = AggregatorConstants.PNR;

        eventBus.publish(AggregatorConstants.EVENT_BUS_EVENT_KEY,
                new JsonObject().put(key, value).put(AggregatorConstants.TIMESTAMP,
                        OffsetDateTime.now().toString()));
        log.info("[EVENT BUS] -> [PUBLISHED] -> [EVENT] PNR.FETCH event published for {}: {}", key, value);

    }

}
