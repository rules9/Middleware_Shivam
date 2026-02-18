package org.middleware.pnr.aggregator.constants;

public class AggregatorConstants {
    public AggregatorConstants() {
        throw new RuntimeException("Can't create Object for this class");
    }

    public static String EVENT_BUS_EVENT_KEY = "pnr.fetched";
    public static String PNR = "pnr";
    public static String TIMESTAMP= "timestamp";
    public static String COLLECTION_ETICKETS = "etickets";
    public static String COLLECTION_BOOKINGS = "bookings";
    public static String COLLECTION_BAGGAGES = "baggages";
    public static int MAX_RETRIES = 3;
    public static String CUSTOMER_ID= "customerId";



}
