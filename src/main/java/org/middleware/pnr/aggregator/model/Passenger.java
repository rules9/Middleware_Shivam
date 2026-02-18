package org.middleware.pnr.aggregator.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Passenger {

    private int passengerNumber;
    private String firstName;
    private String middleName;
    private String lastName;
    private String customerId;
    private String seat;
    private String ticketUrl;

}
