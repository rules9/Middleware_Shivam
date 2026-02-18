package org.middleware.pnr.aggregator.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class FlightResponse {

    private String flightNumber;
    private String departureAirport;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ssXXX")
    private OffsetDateTime departureTimeStamp;
    private String arrivalAirport;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ssXXX")
    private OffsetDateTime arrivalTimeStamp;

}
