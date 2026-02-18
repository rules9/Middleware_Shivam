package org.middleware.pnr.aggregator.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Booking {
    private String pnr;
    private String cabinClass;
    private List<Passenger> passengers;
    private List<Flight> flights;
}
