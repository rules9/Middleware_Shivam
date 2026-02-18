package org.middleware.pnr.aggregator.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ETicket {
    private String pnr;
    private int passengerNumber;
    private String url;
    private OffsetDateTime ticketGeneratedAt;
    private String ticketNumber;

}
