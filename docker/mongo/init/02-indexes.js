db = db.getSiblingDB("pnrdb");

db.bookings.createIndex({ pnr: 1 }, { unique: true });

db.baggages.createIndex({ pnr: 1 });

db.etickets.createIndex(
  { pnr: 1, passengerNumber: 1 },
  { unique: true }
);
