package org.middleware.pnr.aggregator.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TripResponse {
    private String pnr;
    private String cabinClass;
    private List<PassengerResponse> passengers;
    private List<FlightResponse> flights;

}
