package org.middleware.pnr.aggregator.util;

import org.middleware.pnr.aggregator.model.Passenger;

public class TripInfoUtil {
    
    public static String buildFullName(Passenger passenger) {
        return buildFullName(
            passenger.getFirstName(),
            passenger.getMiddleName(),
            passenger.getLastName()
        );
    }
    

    public static String buildFullName(String firstName, String middleName, String lastName) {
        StringBuilder fullName = new StringBuilder();
        
        if (firstName != null && !firstName.isEmpty()) {
            fullName.append(firstName);
        }
        
        if (middleName != null && !middleName.isEmpty()) {
            if (fullName.length() > 0) {
                fullName.append(" ");
            }
            fullName.append(middleName);
        }
        
        if (lastName != null && !lastName.isEmpty()) {
            if (fullName.length() > 0) {
                fullName.append(" ");
            }
            fullName.append(lastName);
        }
        
        return fullName.toString();
    }
}
