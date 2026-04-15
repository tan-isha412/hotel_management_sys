package com.hotel.model;

/**
 * Amenities interface - defines contract for rooms providing amenities.
 * Demonstrates abstraction in OOP.
 */
public interface Amenities {
    String[] getAmenities();
    double getAmenitiesCharge();
    boolean hasWifi();
    boolean hasBreakfast();
    boolean hasPool();
}
