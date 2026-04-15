package com.hotel.model;

/**
 * DeluxeRoom - mid-tier room demonstrating inheritance and polymorphism.
 * Overrides calculateTariff with weekend surcharge logic.
 */
public class DeluxeRoom extends Room {

    private static final long serialVersionUID = 1L;
    private boolean hasBalcony;
    private boolean hasBathtub;

    public DeluxeRoom(String roomNumber, int floor) {
        super(roomNumber, floor, "Deluxe", 5500.0, "Double", 3);
        this.hasBalcony = true;
        this.hasBathtub = true;
    }

    /**
     * Polymorphism - overrides base tariff with a service charge of 10%
     */
    @Override
    public double calculateTariff(int days) {
        double base = getBasePrice() * days;
        double serviceCharge = base * 0.10; // 10% service charge
        return base + serviceCharge;
    }

    @Override
    public String getRoomCategory() {
        return "Deluxe";
    }

    // Amenities interface implementation
    @Override
    public String[] getAmenities() {
        return new String[]{"WiFi", "Smart TV", "AC", "Mini Bar",
                "Balcony", "Bathtub", "Room Service", "Newspaper"};
    }

    @Override
    public double getAmenitiesCharge() {
        return 500.0; // Mini bar stocking charge
    }

    @Override
    public boolean hasWifi() { return true; }

    @Override
    public boolean hasBreakfast() { return true; }

    @Override
    public boolean hasPool() { return false; }

    public boolean isHasBalcony() { return hasBalcony; }
    public boolean isHasBathtub() { return hasBathtub; }
}
