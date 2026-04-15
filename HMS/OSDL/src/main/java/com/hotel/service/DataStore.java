package com.hotel.service;

import com.hotel.model.*;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * DataStore - manages in-memory collections (ArrayList, HashMap)
 * and handles file serialization/deserialization.
 */
public class DataStore {

    private static final String DATA_DIR = System.getProperty("user.home") + "/.hotel_management/";
    private static final String ROOMS_FILE = DATA_DIR + "rooms.dat";
    private static final String CUSTOMERS_FILE = DATA_DIR + "customers.dat";
    private static final String BOOKINGS_FILE = DATA_DIR + "bookings.dat";

    // Collections - ArrayList and HashMap as required
    private ArrayList<Room> rooms;
    private ArrayList<Customer> customers;
    private ArrayList<Booking> bookings;
    private HashMap<String, String> roomToCustomerMap; // roomNumber -> customerId

    private static DataStore instance;

    private DataStore() {
        rooms = new ArrayList<>();
        customers = new ArrayList<>();
        bookings = new ArrayList<>();
        roomToCustomerMap = new HashMap<>();
        initDataDirectory();
        loadData();
        if (rooms.isEmpty()) {
            seedDefaultRooms();
        }
    }

    public static synchronized DataStore getInstance() {
        if (instance == null) instance = new DataStore();
        return instance;
    }

    private void initDataDirectory() {
        File dir = new File(DATA_DIR);
        if (!dir.exists()) dir.mkdirs();
    }

    // ==================== ROOM OPERATIONS ====================

    public void addRoom(Room room) {
        rooms.add(room);
        saveRooms();
    }

    public List<Room> getAllRooms() {
        return Collections.unmodifiableList(rooms);
    }

    public List<Room> getAvailableRooms() {
        return rooms.stream()
                .filter(Room::isAvailable)
                .collect(Collectors.toList());
    }

    public List<Room> getRoomsByType(String type) {
        return rooms.stream()
                .filter(r -> r.getRoomType().equalsIgnoreCase(type))
                .collect(Collectors.toList());
    }

    public Optional<Room> getRoomByNumber(String roomNumber) {
        return rooms.stream()
                .filter(r -> r.getRoomNumber().equals(roomNumber))
                .findFirst();
    }

    public void updateRoom(Room room) {
        saveRooms();
    }

    // ==================== CUSTOMER OPERATIONS ====================

    public void addCustomer(Customer customer) {
        customers.add(customer);
        saveCustomers();
    }

    public List<Customer> getAllCustomers() {
        return Collections.unmodifiableList(customers);
    }

    public Optional<Customer> getCustomerById(String id) {
        return customers.stream()
                .filter(c -> c.getCustomerId().equals(id))
                .findFirst();
    }

    public Optional<Customer> getCustomerByPhone(String phone) {
        return customers.stream()
                .filter(c -> c.getPhone().equals(phone))
                .findFirst();
    }

    public String generateCustomerId() {
        return "CUST" + String.format("%04d", customers.size() + 1);
    }

    // ==================== BOOKING OPERATIONS ====================

    public void addBooking(Booking booking) {
        bookings.add(booking);
        roomToCustomerMap.put(booking.getRoomNumber(), booking.getCustomerId());
        saveBookings();
    }

    public List<Booking> getAllBookings() {
        return Collections.unmodifiableList(bookings);
    }

    public List<Booking> getActiveBookings() {
        return bookings.stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.CONFIRMED
                        || b.getStatus() == Booking.BookingStatus.CHECKED_IN)
                .collect(Collectors.toList());
    }

    public Optional<Booking> getBookingById(String id) {
        return bookings.stream()
                .filter(b -> b.getBookingId().equals(id))
                .findFirst();
    }

    public Optional<Booking> getActiveBookingForRoom(String roomNumber) {
        return bookings.stream()
                .filter(b -> b.getRoomNumber().equals(roomNumber)
                        && (b.getStatus() == Booking.BookingStatus.CONFIRMED
                            || b.getStatus() == Booking.BookingStatus.CHECKED_IN))
                .findFirst();
    }

    public void updateBooking(Booking booking) {
        if (booking.getStatus() == Booking.BookingStatus.CHECKED_OUT
                || booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            roomToCustomerMap.remove(booking.getRoomNumber());
        }
        saveBookings();
    }

    public String generateBookingId() {
        return "BKG" + String.format("%05d", bookings.size() + 1);
    }

    public HashMap<String, String> getRoomToCustomerMap() {
        return new HashMap<>(roomToCustomerMap);
    }

    // ==================== SERIALIZATION ====================

    @SuppressWarnings("unchecked")
    private void loadData() {
        rooms = loadFromFile(ROOMS_FILE, new ArrayList<>());
        customers = loadFromFile(CUSTOMERS_FILE, new ArrayList<>());
        bookings = loadFromFile(BOOKINGS_FILE, new ArrayList<>());

        // Rebuild roomToCustomerMap
        for (Booking b : bookings) {
            if (b.getStatus() == Booking.BookingStatus.CONFIRMED
                    || b.getStatus() == Booking.BookingStatus.CHECKED_IN) {
                roomToCustomerMap.put(b.getRoomNumber(), b.getCustomerId());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T> ArrayList<T> loadFromFile(String path, ArrayList<T> defaultVal) {
        File f = new File(path);
        if (!f.exists()) return defaultVal;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            return (ArrayList<T>) ois.readObject();
        } catch (Exception e) {
            System.err.println("Could not load " + path + ": " + e.getMessage());
            return defaultVal;
        }
    }

    public void saveRooms() {
        saveToFile(ROOMS_FILE, rooms);
    }

    public void saveCustomers() {
        saveToFile(CUSTOMERS_FILE, customers);
    }

    public void saveBookings() {
        saveToFile(BOOKINGS_FILE, bookings);
    }

    private void saveToFile(String path, Object data) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path))) {
            oos.writeObject(data);
        } catch (Exception e) {
            System.err.println("Could not save " + path + ": " + e.getMessage());
        }
    }

    // ==================== SEED DATA ====================

    private void seedDefaultRooms() {
        // Standard Rooms (Floor 1)
        for (int i = 1; i <= 5; i++) {
            rooms.add(new StandardRoom("10" + i, 1));
        }
        // Deluxe Rooms (Floor 2)
        for (int i = 1; i <= 5; i++) {
            rooms.add(new DeluxeRoom("20" + i, 2));
        }
        // Luxury Suites (Floor 3)
        String[] views = {"Sea View", "Garden View", "City View", "Pool View", "Mountain View"};
        for (int i = 1; i <= 5; i++) {
            rooms.add(new LuxuryRoom("30" + i, 3, views[i - 1]));
        }
        saveRooms();
    }
}
