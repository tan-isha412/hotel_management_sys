package com.hotel.service;

import com.hotel.model.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * BookingService - handles booking logic, promo codes,
 * birthday discounts, and multithreaded payment processing.
 */
public class BookingService {

    private final DataStore dataStore;
    private final ExecutorService executor;

    // Promo code map
    private static final Map<String, Double> PROMO_CODES = new HashMap<>();

    static {
        PROMO_CODES.put("APRIL10", 0.10);   // 10% off
        PROMO_CODES.put("SUMMER20", 0.20);  // 20% off
        PROMO_CODES.put("HOTEL15", 0.15);   // 15% off
        PROMO_CODES.put("WELCOME5", 0.05);  // 5% off
        PROMO_CODES.put("VIP25", 0.25);     // 25% off
    }

    public BookingService() {
        this.dataStore = DataStore.getInstance();
        this.executor = Executors.newFixedThreadPool(3);
    }

    public enum BookingResult {
        SUCCESS, ROOM_NOT_AVAILABLE, ROOM_NOT_FOUND, INVALID_DATES, CUSTOMER_NOT_FOUND
    }

    public static class BookingResponse {
        public BookingResult result;
        public Booking booking;
        public String message;
        public boolean birthdayOffer;
        public boolean upgraded;
        public Room upgradedRoom;

        public BookingResponse(BookingResult result, String message) {
            this.result = result;
            this.message = message;
        }
    }

    /**
     * Main booking method with all features.
     */
    public BookingResponse createBooking(String customerId, String roomNumber,
                                         LocalDate checkIn, LocalDate checkOut,
                                         int guests, String promoCode,
                                         boolean extraBreakfast, boolean airportTransfer,
                                         boolean spaService, String specialRequests) {

        // Validate dates
        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            return new BookingResponse(BookingResult.INVALID_DATES,
                    "Invalid dates. Check-out must be after check-in.");
        }

        // Validate customer
        Optional<Customer> customerOpt = dataStore.getCustomerById(customerId);
        if (customerOpt.isEmpty()) {
            return new BookingResponse(BookingResult.CUSTOMER_NOT_FOUND, "Customer not found.");
        }
        Customer customer = customerOpt.get();

        // Validate room
        Optional<Room> roomOpt = dataStore.getRoomByNumber(roomNumber);
        if (roomOpt.isEmpty()) {
            return new BookingResponse(BookingResult.ROOM_NOT_FOUND, "Room not found.");
        }
        Room room = roomOpt.get();

        // Check availability
        if (!room.isAvailable()) {
            return new BookingResponse(BookingResult.ROOM_NOT_AVAILABLE,
                    "Room " + roomNumber + " is not available.");
        }

        // Check auto-upgrade opportunity
        BookingResponse response = new BookingResponse(BookingResult.SUCCESS, "Booking confirmed!");
        Room finalRoom = room;

        // Auto-upgrade check (if next tier room available)
        Room upgradeRoom = findUpgradeRoom(room);
        if (upgradeRoom != null && customer.isBirthday()) {
            response.upgraded = true;
            response.upgradedRoom = upgradeRoom;
            response.birthdayOffer = true;
            finalRoom = upgradeRoom;
            response.message = "🎂 Happy Birthday! You've been upgraded to " +
                    upgradeRoom.getRoomType() + " - " + upgradeRoom.getRoomNumber() + "!";
        }

        // Create booking
        String bookingId = dataStore.generateBookingId();
        Booking booking = new Booking(bookingId, customerId, finalRoom.getRoomNumber(),
                checkIn, checkOut, guests);
        booking.setSpecialRequests(specialRequests);
        booking.setExtraBreakfast(extraBreakfast);
        booking.setAirportTransfer(airportTransfer);
        booking.setSpaService(spaService);

        // Calculate tariff
        long days = booking.getNumberOfDays();
        double baseAmount = finalRoom.calculateTariff((int) days);
        double serviceCharges = booking.getServiceChargesTotal();
        double totalBeforeDiscount = baseAmount + serviceCharges;

        // Apply discounts
        double discountRate = 0.0;
        if (promoCode != null && !promoCode.isBlank()) {
            Double promoDiscount = PROMO_CODES.get(promoCode.toUpperCase());
            if (promoDiscount != null) {
                discountRate += promoDiscount;
                booking.setPromoCode(promoCode.toUpperCase());
            }
        }
        if (customer.isBirthday() && !response.upgraded) {
            discountRate += 0.10; // 10% birthday discount if not upgraded
            booking.setBirthdayDiscount(true);
            response.birthdayOffer = true;
        }

        double discountAmount = totalBeforeDiscount * discountRate;
        double finalAmount = totalBeforeDiscount - discountAmount;

        booking.setTotalAmount(totalBeforeDiscount);
        booking.setDiscount(discountAmount);
        booking.setFinalAmount(finalAmount);
        booking.setServiceCharges(serviceCharges);
        booking.setUpgraded(response.upgraded);
        if (response.upgraded) booking.setOriginalRoomNumber(roomNumber);

        // Mark room as booked
        finalRoom.setAvailable(false);
        dataStore.updateRoom(finalRoom);

        // Save booking
        dataStore.addBooking(booking);
        response.booking = booking;

        return response;
    }

    /**
     * Checkout - releases room and calculates final bill.
     */
    public boolean checkout(String bookingId) {
        Optional<Booking> bookingOpt = dataStore.getBookingById(bookingId);
        if (bookingOpt.isEmpty()) return false;

        Booking booking = bookingOpt.get();
        booking.setStatus(Booking.BookingStatus.CHECKED_OUT);

        Optional<Room> roomOpt = dataStore.getRoomByNumber(booking.getRoomNumber());
        roomOpt.ifPresent(r -> {
            r.setAvailable(true);
            dataStore.updateRoom(r);
        });

        dataStore.updateBooking(booking);
        return true;
    }

    /**
     * Cancel a booking.
     */
    public boolean cancelBooking(String bookingId) {
        Optional<Booking> bookingOpt = dataStore.getBookingById(bookingId);
        if (bookingOpt.isEmpty()) return false;

        Booking booking = bookingOpt.get();
        booking.setStatus(Booking.BookingStatus.CANCELLED);

        Optional<Room> roomOpt = dataStore.getRoomByNumber(booking.getRoomNumber());
        roomOpt.ifPresent(r -> {
            r.setAvailable(true);
            dataStore.updateRoom(r);
        });

        dataStore.updateBooking(booking);
        return true;
    }

    /**
     * Multithreaded payment processing simulation.
     */
    public void processPayment(Booking booking, Consumer<String> onProgress,
                                Consumer<Boolean> onComplete) {
        executor.submit(() -> {
            try {
                updateProgress(onProgress, "Connecting to payment gateway...");
                Thread.sleep(600);
                updateProgress(onProgress, "Verifying booking details...");
                Thread.sleep(400);
                updateProgress(onProgress, "Processing payment of ₹" +
                        String.format("%.2f", booking.getFinalAmount()) + "...");
                Thread.sleep(700);
                updateProgress(onProgress, "Generating invoice...");
                Thread.sleep(400);
                updateProgress(onProgress, "Sending confirmation to " +
                        dataStore.getCustomerById(booking.getCustomerId())
                                .map(Customer::getEmail).orElse("customer") + "...");
                Thread.sleep(400);
                onComplete.accept(true);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                onComplete.accept(false);
            }
        });
    }

    private void updateProgress(Consumer<String> onProgress, String msg) {
        if (onProgress != null) {
            javafx.application.Platform.runLater(() -> onProgress.accept(msg));
        }
    }

    /**
     * Find upgrade room (next tier up from current room type).
     */
    private Room findUpgradeRoom(Room current) {
        String nextType = null;
        if (current instanceof StandardRoom) nextType = "Deluxe";
        else if (current instanceof DeluxeRoom) nextType = "Luxury Suite";

        if (nextType == null) return null;
        String finalNextType = nextType;
        return dataStore.getAvailableRooms().stream()
                .filter(r -> r.getRoomType().equalsIgnoreCase(finalNextType))
                .findFirst()
                .orElse(null);
    }

    /**
     * Validate promo code and return discount rate.
     */
    public double validatePromoCode(String code) {
        if (code == null || code.isBlank()) return 0.0;
        return PROMO_CODES.getOrDefault(code.toUpperCase(), -1.0);
    }

    public static Map<String, Double> getPromoCodes() {
        return Collections.unmodifiableMap(PROMO_CODES);
    }

    public void shutdown() {
        executor.shutdown();
    }
}
