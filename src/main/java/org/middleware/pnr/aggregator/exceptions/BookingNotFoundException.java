package org.middleware.pnr.aggregator.exceptions;

public class BookingNotFoundException extends AggregatorException {
    public BookingNotFoundException(String message) {
        super(message);
    }
}
