package com.hotel.model;

import java.io.Serializable;

/**
 * Abstract Room class - base class demonstrating abstraction and encapsulation.
 * All room types inherit from this class.
 */
public abstract class Room implements Serializable, Amenities {

    private static final long serialVersionUID = 1L;

    // Encapsulated fields (private with getters/setters)
    private String roomNumber;
    private int floor;
    private boolean available;
    private String roomType;
    private double basePrice;
    private String bedType;
    private int capacity;
    private String status; // AVAILABLE, BOOKED, MAINTENANCE

    public Room(String roomNumber, int floor, String roomType, double basePrice, String bedType, int capacity) {
        this.roomNumber = roomNumber;
        this.floor = floor;
        this.roomType = roomType;
        this.basePrice = basePrice;
        this.bedType = bedType;
        this.capacity = capacity;
        this.available = true;
        this.status = "AVAILABLE";
    }

    // Abstract method - must be implemented by subclasses (Polymorphism)
    public abstract double calculateTariff(int days);

    public abstract String getRoomCategory();

    // --- Getters and Setters (Encapsulation) ---
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public int getFloor() { return floor; }
    public void setFloor(int floor) { this.floor = floor; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) {
        this.available = available;
        this.status = available ? "AVAILABLE" : "BOOKED";
    }

    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }

    public double getBasePrice() { return basePrice; }
    public void setBasePrice(double basePrice) { this.basePrice = basePrice; }

    public String getBedType() { return bedType; }
    public void setBedType(String bedType) { this.bedType = bedType; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public String getStatus() { return status; }
    public void setStatus(String status) {
        this.status = status;
        this.available = "AVAILABLE".equals(status);
    }

    @Override
    public String toString() {
        return String.format("Room[%s | %s | Floor %d | %s | ₹%.2f/night | %s]",
                roomNumber, roomType, floor, bedType, basePrice, status);
    }
}
