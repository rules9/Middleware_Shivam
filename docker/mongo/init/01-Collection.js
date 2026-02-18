db = db.getSiblingDB("pnrdb");

db.createCollection("bookings");
db.createCollection("baggages");
db.createCollection("etickets");
