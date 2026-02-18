package org.middleware.pnr.aggregator.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Flight {

    private String flightNumber;
    private String departureAirport;
    private OffsetDateTime departureTimeStamp;
    private String arrivalAirport;
    private OffsetDateTime arrivalTimeStamp;

}
