package org.middleware.pnr.aggregator.constants;

public class AggregatorConstants {
    public AggregatorConstants() {
        throw new RuntimeException("Can't create Object for this class");
    }

    public static final String EVENT_BUS_EVENT_KEY = "pnr.fetched";
    public static final String PNR = "pnr";
    public static final String TIMESTAMP= "timestamp";
    public static final String COLLECTION_ETICKETS = "etickets";
    public static final String COLLECTION_BOOKINGS = "bookings";
    public static final String COLLECTION_BAGGAGES = "baggages";
    public static final int MAX_RETRIES = 3;
    public static final String CUSTOMER_ID= "customerId";



}
