package org.middleware.pnr.aggregator.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PassengerResponse {
    private int passengerNumber;
    private String customerId;
    private String fullName;
    private String seat;
    private String ticketUrl;
}
