db = db.getSiblingDB("pnrdb");

/* ================= BOOKINGS ================= */

db.bookings.insertMany([
  {
    pnr: "PNR123",
    cabinClass: "ECONOMY",
    flights: [{
      flightNumber: "AI-202",
      departureAirport: "LHR",
      arrivalAirport: "DXB",
      departureTimeStamp: "2025-11-11T02:25:00+01:00",
      arrivalTimeStamp: "2025-11-11T10:25:00+04:00"
    }],
    passengers: [
      { passengerNumber: 1, firstName: "John", lastName: "Doe", customerId: "CUST-001" },
      { passengerNumber: 2, firstName: "Marry", lastName: "Doe", customerId: "CUST-002" }
    ]
  },
  {
    pnr: "PNR123123",
    cabinClass: "PREMIUM-ECONOMY",
    flights: [{
      flightNumber: "AI-202",
      departureAirport: "DXB",
      arrivalAirport: "DXB",
      departureTimeStamp: "2025-11-11T02:25:00+04:00",
      arrivalTimeStamp: "2025-11-11T10:25:00+04:00"
    }],
    passengers: [
      { passengerNumber: 11, firstName: "John", customerId: "CUST-101" },
      { passengerNumber: 21, firstName: "Marry", customerId: "CUST-102" }
    ]
  },
  {
    pnr: "PNR456",
    cabinClass: "BUSINESS",
    flights: [{
      flightNumber: "AI-909",
      departureAirport: "BKK",
      arrivalAirport: "MAD",
      departureTimeStamp: "2025-12-01T09:00:00+07:00",
      arrivalTimeStamp: "2025-12-01T18:00:00+01:00"
    }],
    passengers: [
      { passengerNumber: 1, firstName: "Alice", lastName: "Smith", customerId: "CUST-003" }
    ]
  },
  {
    pnr: "PNR789",
    cabinClass: "ECONOMY",
    flights: [{
      flightNumber: "AI-111",
      departureAirport: "BER",
      arrivalAirport: "DXB",
      departureTimeStamp: "2025-10-15T18:30:00+02:00",
      arrivalTimeStamp: "2025-10-16T02:30:00+04:00"
    }],
    passengers: [
      { passengerNumber: 1, firstName: "Bob", lastName: "Brown", customerId: "CUST-004" },
      { passengerNumber: 2, firstName: "Charlie", lastName: "Brown", customerId: "CUST-005" },
      { passengerNumber: 3, firstName: "Eve", lastName: "Brown", middleName: "Morgan", customerId: "CUST-006", seat: "32D" }
    ]
  }
]);

/* ================= BAGGAGE ================= */

db.baggages.insertMany([
  {
    pnr: "PNR123",
    passengerNumber: 1,
    allowanceUnit: "KG",
    checkedAllowanceValue: 20,
    carryOnAllowanceValue: 7
  },
  {
    pnr: "PNR123",
    passengerNumber: 2,
    allowanceUnit: "KG",
    checkedAllowanceValue: 25,
    carryOnAllowanceValue: 7
  },
  {
    pnr: "PNR789",
    passengerNumber: 1,
    allowanceUnit: "KG",
    checkedAllowanceValue: 15,
    carryOnAllowanceValue: 7
  }
  // PNR456 → no baggage
]);

/* ================= ETICKETS ================= */

db.etickets.insertMany([
  {
    pnr: "PNR123",
    passengerNumber: 1,
    url: "https://airline.com/tickets/PNR123-1.pdf"
  },
  {
    pnr: "PNR789",
    passengerNumber: 2,
    url: "https://airline.com/tickets/PNR789-2.pdf"
  }
  // PNR456 → no tickets
  // PNR789 passenger 1 & 3 → no tickets
]);
