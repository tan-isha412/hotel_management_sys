package com.hotel.model;

/**
 * LuxuryRoom - top-tier room with butler service.
 * Demonstrates deepest inheritance with premium tariff calculation.
 */
public class LuxuryRoom extends Room {

    private static final long serialVersionUID = 1L;
    private boolean hasPrivatePool;
    private boolean hasButlerService;
    private String viewType; // Sea, Garden, City

    public LuxuryRoom(String roomNumber, int floor, String viewType) {
        super(roomNumber, floor, "Luxury Suite", 12000.0, "King", 4);
        this.hasPrivatePool = true;
        this.hasButlerService = true;
        this.viewType = viewType;
    }

    /**
     * Polymorphism - Luxury tariff includes per-night butler fee + GST
     */
    @Override
    public double calculateTariff(int days) {
        double base = getBasePrice() * days;
        double butlerFee = 1500.0 * days;
        double gst = (base + butlerFee) * 0.18; // 18% GST
        return base + butlerFee + gst;
    }

    @Override
    public String getRoomCategory() {
        return "Luxury Suite";
    }

    // Amenities interface implementation
    @Override
    public String[] getAmenities() {
        return new String[]{"WiFi", "4K Smart TV", "AC", "Premium Mini Bar",
                "Private Pool", "Butler Service", "Jacuzzi", "Spa Access",
                "Airport Transfer", "Daily Breakfast", "Evening Cocktails",
                viewType + " View"};
    }

    @Override
    public double getAmenitiesCharge() {
        return 1500.0; // Butler per night
    }

    @Override
    public boolean hasWifi() { return true; }

    @Override
    public boolean hasBreakfast() { return true; }

    @Override
    public boolean hasPool() { return true; }

    public boolean isHasPrivatePool() { return hasPrivatePool; }
    public boolean isHasButlerService() { return hasButlerService; }
    public String getViewType() { return viewType; }
    public void setViewType(String viewType) { this.viewType = viewType; }
}
