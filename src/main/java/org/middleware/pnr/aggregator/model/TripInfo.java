package org.middleware.pnr.aggregator.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TripInfo {

    private Booking booking;
    private List<Baggage> baggage;

}
