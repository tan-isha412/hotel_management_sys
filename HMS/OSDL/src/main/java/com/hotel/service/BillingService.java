package com.hotel.service;

import com.hotel.model.Booking;
import com.hotel.model.Customer;
import com.hotel.model.Room;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * BillingService - generates bill summaries and calculates charges.
 */
public class BillingService {

    private final DataStore dataStore;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    public BillingService() {
        this.dataStore = DataStore.getInstance();
    }

    public static class BillSummary {
        public String bookingId;
        public String customerName;
        public String customerPhone;
        public String roomNumber;
        public String roomType;
        public String checkIn;
        public String checkOut;
        public long numberOfDays;
        public int numberOfGuests;
        public double roomCharges;
        public double serviceCharges;
        public double subtotal;
        public double discount;
        public double promoDiscount;
        public double birthdayDiscount;
        public double totalAmount;
        public String promoCode;
        public boolean hasBreakfast;
        public boolean hasAirportTransfer;
        public boolean hasSpa;
        public boolean upgraded;
        public String originalRoom;
        public String generatedAt;
    }

    public BillSummary generateBill(String bookingId) {
        Optional<Booking> bookingOpt = dataStore.getBookingById(bookingId);
        if (bookingOpt.isEmpty()) return null;

        Booking booking = bookingOpt.get();
        Optional<Customer> customerOpt = dataStore.getCustomerById(booking.getCustomerId());
        Optional<Room> roomOpt = dataStore.getRoomByNumber(booking.getRoomNumber());

        BillSummary bill = new BillSummary();
        bill.bookingId = booking.getBookingId();
        bill.customerName = customerOpt.map(Customer::getName).orElse("Unknown");
        bill.customerPhone = customerOpt.map(Customer::getPhone).orElse("Unknown");
        bill.roomNumber = booking.getRoomNumber();
        bill.roomType = roomOpt.map(Room::getRoomType).orElse("Unknown");
        bill.checkIn = booking.getCheckInDate().format(DATE_FMT);
        bill.checkOut = booking.getCheckOutDate().format(DATE_FMT);
        bill.numberOfDays = booking.getNumberOfDays();
        bill.numberOfGuests = booking.getNumberOfGuests();
        bill.serviceCharges = booking.getServiceChargesTotal();
        bill.roomCharges = booking.getTotalAmount() - booking.getServiceChargesTotal();
        bill.subtotal = booking.getTotalAmount();
        bill.discount = booking.getDiscount();
        bill.totalAmount = booking.getFinalAmount();
        bill.promoCode = booking.getPromoCode();
        bill.hasBreakfast = booking.isExtraBreakfast();
        bill.hasAirportTransfer = booking.isAirportTransfer();
        bill.hasSpa = booking.isSpaService();
        bill.upgraded = booking.isUpgraded();
        bill.originalRoom = booking.getOriginalRoomNumber();
        bill.generatedAt = java.time.LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));
        return bill;
    }

    public String formatBillAsText(BillSummary bill) {
        StringBuilder sb = new StringBuilder();
        String line = "═".repeat(50);
        String thinLine = "─".repeat(50);

        sb.append(line).append("\n");
        sb.append("         🏨 GRAND AZURE HOTEL\n");
        sb.append("         BILLING INVOICE\n");
        sb.append(line).append("\n");
        sb.append(String.format("Booking ID  : %s\n", bill.bookingId));
        sb.append(String.format("Generated   : %s\n", bill.generatedAt));
        sb.append(thinLine).append("\n");
        sb.append("GUEST DETAILS\n");
        sb.append(String.format("Name        : %s\n", bill.customerName));
        sb.append(String.format("Phone       : %s\n", bill.customerPhone));
        sb.append(thinLine).append("\n");
        sb.append("STAY DETAILS\n");
        sb.append(String.format("Room        : %s (%s)\n", bill.roomNumber, bill.roomType));
        if (bill.upgraded) {
            sb.append(String.format("Upgraded From: %s (Birthday Offer!)\n", bill.originalRoom));
        }
        sb.append(String.format("Check-In    : %s\n", bill.checkIn));
        sb.append(String.format("Check-Out   : %s\n", bill.checkOut));
        sb.append(String.format("Nights      : %d | Guests: %d\n", bill.numberOfDays, bill.numberOfGuests));
        sb.append(thinLine).append("\n");
        sb.append("CHARGES\n");
        sb.append(String.format("Room Charges: ₹%,.2f\n", bill.roomCharges));
        if (bill.hasBreakfast) sb.append(String.format("Breakfast   : ₹%,.2f\n", 350.0 * bill.numberOfDays * bill.numberOfGuests));
        if (bill.hasAirportTransfer) sb.append(String.format("Airport Xfr : ₹800.00\n"));
        if (bill.hasSpa) sb.append(String.format("Spa Service : ₹%,.2f\n", 1200.0 * bill.numberOfDays));
        sb.append(String.format("Sub Total   : ₹%,.2f\n", bill.subtotal));
        if (bill.discount > 0) {
            sb.append(String.format("Discount    : -₹%,.2f\n", bill.discount));
            if (bill.promoCode != null) sb.append(String.format("  (Promo: %s)\n", bill.promoCode));
        }
        sb.append(line).append("\n");
        sb.append(String.format("TOTAL       : ₹%,.2f\n", bill.totalAmount));
        sb.append(line).append("\n");
        sb.append("   Thank you for staying with Grand Azure!\n");
        return sb.toString();
    }
}
