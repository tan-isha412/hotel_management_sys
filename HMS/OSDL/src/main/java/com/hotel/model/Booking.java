package com.hotel.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Booking model - represents a hotel booking/reservation.
 */
public class Booking implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum BookingStatus { CONFIRMED, CHECKED_IN, CHECKED_OUT, CANCELLED }

    private String bookingId;
    private String customerId;
    private String roomNumber;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private int numberOfGuests;
    private double totalAmount;
    private double discount;
    private double finalAmount;
    private String promoCode;
    private boolean birthdayDiscount;
    private boolean upgraded;
    private String originalRoomNumber; // if upgraded
    private BookingStatus status;
    private String specialRequests;
    private LocalDate bookingDate;

    // Extra services
    private boolean extraBreakfast;
    private boolean airportTransfer;
    private boolean spaService;
    private double serviceCharges;

    public Booking(String bookingId, String customerId, String roomNumber,
                   LocalDate checkInDate, LocalDate checkOutDate, int numberOfGuests) {
        this.bookingId = bookingId;
        this.customerId = customerId;
        this.roomNumber = roomNumber;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.numberOfGuests = numberOfGuests;
        this.status = BookingStatus.CONFIRMED;
        this.bookingDate = LocalDate.now();
        this.discount = 0.0;
        this.serviceCharges = 0.0;
    }

    public long getNumberOfDays() {
        return ChronoUnit.DAYS.between(checkInDate, checkOutDate);
    }

    public double getServiceChargesTotal() {
        double total = 0;
        if (extraBreakfast) total += 350.0 * getNumberOfDays() * numberOfGuests;
        if (airportTransfer) total += 800.0;
        if (spaService) total += 1200.0 * getNumberOfDays();
        return total;
    }

    // Getters and Setters
    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }

    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }

    public int getNumberOfGuests() { return numberOfGuests; }
    public void setNumberOfGuests(int numberOfGuests) { this.numberOfGuests = numberOfGuests; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public double getDiscount() { return discount; }
    public void setDiscount(double discount) { this.discount = discount; }

    public double getFinalAmount() { return finalAmount; }
    public void setFinalAmount(double finalAmount) { this.finalAmount = finalAmount; }

    public String getPromoCode() { return promoCode; }
    public void setPromoCode(String promoCode) { this.promoCode = promoCode; }

    public boolean isBirthdayDiscount() { return birthdayDiscount; }
    public void setBirthdayDiscount(boolean birthdayDiscount) { this.birthdayDiscount = birthdayDiscount; }

    public boolean isUpgraded() { return upgraded; }
    public void setUpgraded(boolean upgraded) { this.upgraded = upgraded; }

    public String getOriginalRoomNumber() { return originalRoomNumber; }
    public void setOriginalRoomNumber(String originalRoomNumber) { this.originalRoomNumber = originalRoomNumber; }

    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }

    public String getSpecialRequests() { return specialRequests; }
    public void setSpecialRequests(String specialRequests) { this.specialRequests = specialRequests; }

    public LocalDate getBookingDate() { return bookingDate; }

    public boolean isExtraBreakfast() { return extraBreakfast; }
    public void setExtraBreakfast(boolean extraBreakfast) { this.extraBreakfast = extraBreakfast; }

    public boolean isAirportTransfer() { return airportTransfer; }
    public void setAirportTransfer(boolean airportTransfer) { this.airportTransfer = airportTransfer; }

    public boolean isSpaService() { return spaService; }
    public void setSpaService(boolean spaService) { this.spaService = spaService; }

    public double getServiceCharges() { return serviceCharges; }
    public void setServiceCharges(double serviceCharges) { this.serviceCharges = serviceCharges; }
}
