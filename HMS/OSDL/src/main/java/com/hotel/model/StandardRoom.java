package com.hotel.model;

/**
 * StandardRoom - basic room type.
 * Demonstrates inheritance from Room.
 */
public class StandardRoom extends Room {

    private static final long serialVersionUID = 1L;

    public StandardRoom(String roomNumber, int floor) {
        super(roomNumber, floor, "Standard", 2500.0, "Single", 2);
    }

    @Override
    public double calculateTariff(int days) {
        return getBasePrice() * days;
    }

    @Override
    public String getRoomCategory() {
        return "Standard";
    }

    // Amenities interface implementation
    @Override
    public String[] getAmenities() {
        return new String[]{"WiFi", "TV", "AC", "Room Service"};
    }

    @Override
    public double getAmenitiesCharge() {
        return 0.0; // included in base price
    }

    @Override
    public boolean hasWifi() { return true; }

    @Override
    public boolean hasBreakfast() { return false; }

    @Override
    public boolean hasPool() { return false; }
}
