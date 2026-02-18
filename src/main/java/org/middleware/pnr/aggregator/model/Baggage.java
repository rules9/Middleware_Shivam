package org.middleware.pnr.aggregator.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Baggage {

    private String pnr;
    private int passengerNumber;
    private String allowanceUnit;
    private int checkedAllowanceValue;
    private int carryOnAllowanceValue;

}
