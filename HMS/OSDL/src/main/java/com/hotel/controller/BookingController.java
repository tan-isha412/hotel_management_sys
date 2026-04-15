package com.hotel.controller;

import com.hotel.model.Booking;
import com.hotel.model.Customer;
import com.hotel.model.Room;
import com.hotel.service.BookingService;
import com.hotel.service.DataStore;
import com.hotel.service.NotificationService;
import com.hotel.util.NavUtil;
import com.hotel.util.AlertUtil;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * BookingController — handles the complete booking flow.
 *
 * Enhancements over v1:
 *  - Past-date prevention (check-in cannot be before today)
 *  - Inline promo code buttons + auto-apply best promo
 *  - Smart room recommendation (budget / availability)
 *  - Live price recalculation on every form change
 *  - Simulated email + SMS notification after booking
 */
public class BookingController implements Initializable {

    // Customer lookup
    @FXML private TextField tfCustomerPhone;
    @FXML private Label lblCustomerInfo;
    @FXML private Label lblBirthdayBadge;

    // Booking form
    @FXML private ComboBox<String> cbRoomType;
    @FXML private ComboBox<String> cbBudgetFilter;
    @FXML private ComboBox<String> cbRoom;
    @FXML private DatePicker dpCheckIn;
    @FXML private DatePicker dpCheckOut;
    @FXML private Label lblDateWarning;
    @FXML private Spinner<Integer> spGuests;
    @FXML private TextField tfPromoCode;
    @FXML private Label lblPromoStatus;

    // Services
    @FXML private CheckBox cbBreakfast;
    @FXML private CheckBox cbAirport;
    @FXML private CheckBox cbSpa;
    @FXML private TextArea taSpecialRequests;

    // Smart recommendation
    @FXML private VBox recommendBox;
    @FXML private Label lblRecommendation;

    // Summary
    @FXML private Label lblRoomCharge;
    @FXML private Label lblServiceCharge;
    @FXML private Label lblDiscount;
    @FXML private Label lblTotal;
    @FXML private Label lblNights;
    @FXML private Label lblBookingCount;

    // Payment overlay
    @FXML private VBox paymentOverlay;
    @FXML private Label lblPaymentStatus;
    @FXML private ProgressBar pbPayment;

    // Bookings table
    @FXML private TableView<Booking> bookingsTable;
    @FXML private TableColumn<Booking, String> colBookingId;
    @FXML private TableColumn<Booking, String> colCustomer;
    @FXML private TableColumn<Booking, String> colRoom;
    @FXML private TableColumn<Booking, String> colCheckIn;
    @FXML private TableColumn<Booking, String> colCheckOut;
    @FXML private TableColumn<Booking, String> colAmount;
    @FXML private TableColumn<Booking, String> colBStatus;

    private DataStore dataStore;
    private BookingService bookingService;
    private Customer selectedCustomer;
    private ObservableList<Booking> bookingData;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dataStore = DataStore.getInstance();
        bookingService = new BookingService();
        setupForm();
        setupTable();
        loadBookings();
        if (paymentOverlay != null) paymentOverlay.setVisible(false);
    }

    // =========================================================
    //  FORM SETUP
    // =========================================================

    private void setupForm() {
        cbRoomType.setItems(FXCollections.observableArrayList("All", "Standard", "Deluxe", "Luxury Suite"));
        cbRoomType.setValue("All");
        cbRoomType.setOnAction(e -> { filterRooms(); updateRecommendation(); });

        cbBudgetFilter.setItems(FXCollections.observableArrayList(
                "Any Budget", "Under ₹3,000", "₹3,000–₹6,000", "Above ₹6,000"));
        cbBudgetFilter.setValue("Any Budget");
        cbBudgetFilter.setOnAction(e -> filterRooms());

        // Date pickers – enforce today as minimum
        dpCheckIn.setValue(LocalDate.now());
        dpCheckOut.setValue(LocalDate.now().plusDays(1));

        // Prevent past dates via day-cell factory
        dpCheckIn.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });
        dpCheckOut.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate checkIn = dpCheckIn.getValue();
                setDisable(empty || (checkIn != null && date.isBefore(checkIn.plusDays(1))));
            }
        });

        dpCheckIn.setOnAction(e -> {
            validateDates();
            updateSummary();
        });
        dpCheckOut.setOnAction(e -> {
            validateDates();
            updateSummary();
        });

        SpinnerValueFactory<Integer> gf = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 6, 1);
        spGuests.setValueFactory(gf);
        spGuests.valueProperty().addListener((obs, o, n) -> updateSummary());

        cbBreakfast.setOnAction(e -> updateSummary());
        cbAirport.setOnAction(e -> updateSummary());
        cbSpa.setOnAction(e -> updateSummary());

        filterRooms();
    }
    @FXML
    private void goToBooking() {
        NavUtil.navigateTo(tfCustomerPhone, "Booking.fxml");
    }
    private void validateDates() {
        LocalDate checkIn = dpCheckIn.getValue();
        LocalDate checkOut = dpCheckOut.getValue();
        boolean hasWarning = false;

        if (checkIn != null && checkIn.isBefore(LocalDate.now())) {
            showDateWarning("⚠️ Check-in date cannot be in the past.");
            hasWarning = true;
        } else if (checkIn != null && checkOut != null && !checkOut.isAfter(checkIn)) {
            showDateWarning("⚠️ Check-out must be after check-in.");
            hasWarning = true;
        }

        if (!hasWarning && lblDateWarning != null) {
            lblDateWarning.setVisible(false);
            lblDateWarning.setManaged(false);
        }
    }

    private void showDateWarning(String msg) {
        if (lblDateWarning != null) {
            lblDateWarning.setText(msg);
            lblDateWarning.setVisible(true);
            lblDateWarning.setManaged(true);
        }
    }

    private void filterRooms() {
        String type = cbRoomType.getValue();
        String budget = cbBudgetFilter.getValue();

        List<Room> available = dataStore.getAvailableRooms().stream()
                .filter(r -> "All".equals(type) || r.getRoomType().equalsIgnoreCase(type))
                .filter(r -> matchesBudget(r, budget))
                .collect(Collectors.toList());

        cbRoom.setItems(FXCollections.observableArrayList(
                available.stream()
                        .map(r -> r.getRoomNumber() + " - " + r.getRoomType()
                                + " (₹" + String.format("%,.0f", r.getBasePrice()) + "/night)")
                        .collect(Collectors.toList())
        ));
        if (!available.isEmpty()) cbRoom.getSelectionModel().selectFirst();
        cbRoom.setOnAction(e -> updateSummary());
        updateSummary();
        updateRecommendation();
    }

    private boolean matchesBudget(Room r, String budget) {
        if (budget == null || "Any Budget".equals(budget)) return true;
        double p = r.getBasePrice();
        return switch (budget) {
            case "Under ₹3,000"    -> p < 3000;
            case "₹3,000–₹6,000"  -> p >= 3000 && p <= 6000;
            case "Above ₹6,000"   -> p > 6000;
            default -> true;
        };
    }

    private void updateRecommendation() {
        if (recommendBox == null) return;
        List<Room> available = dataStore.getAvailableRooms();
        if (available.isEmpty()) {
            recommendBox.setVisible(false);
            recommendBox.setManaged(false);
            return;
        }

        // Recommend best-value: lowest price per amenity count
        Room best = available.stream()
                .min((a, b) -> {
                    double scoreA = a.getBasePrice() / Math.max(1, a.getAmenities().length);
                    double scoreB = b.getBasePrice() / Math.max(1, b.getAmenities().length);
                    return Double.compare(scoreA, scoreB);
                })
                .orElse(available.get(0));

        lblRecommendation.setText("Best value: Room " + best.getRoomNumber()
                + " — " + best.getRoomType()
                + " at ₹" + String.format("%,.0f", best.getBasePrice()) + "/night. "
                + best.getAmenities().length + " amenities included.");
        recommendBox.setVisible(true);
        recommendBox.setManaged(true);
    }

    // =========================================================
    //  CUSTOMER LOOKUP
    // =========================================================

    @FXML
    private void lookupCustomer() {
        String phone = tfCustomerPhone.getText().trim();
        if (phone.length() != 10) {
            AlertUtil.showError("Error", "Enter a valid 10-digit phone number.");
            return;
        }
        Optional<Customer> cOpt = dataStore.getCustomerByPhone(phone);
        if (cOpt.isEmpty()) {
            lblCustomerInfo.setText("❌ Customer not found. Please register first.");
            lblCustomerInfo.setStyle("-fx-text-fill: #f87171;");
            lblBirthdayBadge.setVisible(false);
            lblBirthdayBadge.setManaged(false);
            selectedCustomer = null;
            return;
        }
        selectedCustomer = cOpt.get();
        lblCustomerInfo.setText("✅ " + selectedCustomer.getName()
                + "  |  ID: " + selectedCustomer.getCustomerId()
                + "  |  " + selectedCustomer.getNationality());
        lblCustomerInfo.setStyle("-fx-text-fill: #4ade80;");

        if (selectedCustomer.isBirthday()) {
            lblBirthdayBadge.setText("🎂 Birthday Today! — Free Upgrade or 10% Off!");
            lblBirthdayBadge.setVisible(true);
            lblBirthdayBadge.setManaged(true);
        } else {
            lblBirthdayBadge.setVisible(false);
            lblBirthdayBadge.setManaged(false);
        }
        updateSummary();
    }

    // =========================================================
    //  PROMO CODE BUTTONS (inline)
    // =========================================================

    @FXML private void applyPromoAPRIL10()  { applyPromo("APRIL10");  }
    @FXML private void applyPromoSUMMER20() { applyPromo("SUMMER20"); }
    @FXML private void applyPromoHOTEL15()  { applyPromo("HOTEL15");  }
    @FXML private void applyPromoVIP25()    { applyPromo("VIP25");    }
    @FXML private void applyPromoWELCOME5() { applyPromo("WELCOME5"); }

    private void applyPromo(String code) {
        tfPromoCode.setText(code);
        validatePromoCode();
    }

    @FXML
    private void autoApplyBestPromo() {
        // Find the highest discount promo
        Map<String, Double> codes = BookingService.getPromoCodes();
        String best = codes.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
        if (best != null) {
            applyPromo(best);
            lblPromoStatus.setText("✨ Best promo auto-applied: " + best);
            lblPromoStatus.setStyle("-fx-text-fill: #c9a84c; -fx-font-weight:bold;");
        }
    }

    @FXML
    private void validatePromoCode() {
        String code = tfPromoCode.getText().trim().toUpperCase();
        if (code.isEmpty()) {
            lblPromoStatus.setText("");
            updateSummary();
            return;
        }
        double discount = bookingService.validatePromoCode(code);
        if (discount > 0) {
            lblPromoStatus.setText("✅ Valid! " + (int)(discount * 100) + "% discount applied.");
            lblPromoStatus.setStyle("-fx-text-fill: #4ade80;");
        } else {
            lblPromoStatus.setText("❌ Invalid promo code.");
            lblPromoStatus.setStyle("-fx-text-fill: #f87171;");
        }
        updateSummary();
    }

    // =========================================================
    //  LIVE PRICE CALCULATION
    // =========================================================

    private void updateSummary() {
        String selectedRoom = cbRoom.getValue();
        if (selectedRoom == null || selectedRoom.isEmpty()) return;

        String roomNumber = selectedRoom.split(" - ")[0];
        Optional<Room> roomOpt = dataStore.getRoomByNumber(roomNumber);
        if (roomOpt.isEmpty()) return;

        Room room = roomOpt.get();
        LocalDate checkIn = dpCheckIn.getValue();
        LocalDate checkOut = dpCheckOut.getValue();

        if (checkIn == null || checkOut == null
                || !checkOut.isAfter(checkIn)
                || checkIn.isBefore(LocalDate.now())) {
            lblNights.setText("0 nights");
            return;
        }

        long days = java.time.temporal.ChronoUnit.DAYS.between(checkIn, checkOut);
        lblNights.setText(days + " night" + (days > 1 ? "s" : ""));

        double roomCharge = room.calculateTariff((int) days);

        // Service charges
        double serviceCharge = 0;
        int guests = spGuests.getValue();
        if (cbBreakfast.isSelected()) serviceCharge += 350.0 * days * guests;
        if (cbAirport.isSelected())   serviceCharge += 800.0;
        if (cbSpa.isSelected())       serviceCharge += 1200.0 * days;

        double subtotal = roomCharge + serviceCharge;

        // Discount
        double discountRate = 0;
        String promoCode = tfPromoCode.getText().trim().toUpperCase();
        double promoDiscount = bookingService.validatePromoCode(promoCode);
        if (promoDiscount > 0) discountRate += promoDiscount;
        if (selectedCustomer != null && selectedCustomer.isBirthday()) discountRate += 0.10;

        double discountAmt = subtotal * discountRate;
        double total = subtotal - discountAmt;

        lblRoomCharge.setText("₹" + String.format("%,.2f", roomCharge));
        lblServiceCharge.setText("₹" + String.format("%,.2f", serviceCharge));
        lblDiscount.setText(discountAmt > 0 ? "-₹" + String.format("%,.2f", discountAmt) : "₹0.00");
        lblTotal.setText("₹" + String.format("%,.2f", total));
    }

    // =========================================================
    //  CONFIRM BOOKING
    // =========================================================

    @FXML
    private void confirmBooking() {
        if (selectedCustomer == null) {
            AlertUtil.showError("Error", "Please look up a customer first.");
            return;
        }
        String selectedRoom = cbRoom.getValue();
        if (selectedRoom == null || selectedRoom.isEmpty()) {
            AlertUtil.showError("Error", "Please select a room.");
            return;
        }

        LocalDate checkIn  = dpCheckIn.getValue();
        LocalDate checkOut = dpCheckOut.getValue();

        // Past-date validation
        if (checkIn == null || checkIn.isBefore(LocalDate.now())) {
            AlertUtil.showError("Invalid Date", "Check-in date cannot be in the past.");
            return;
        }
        if (checkOut == null || !checkOut.isAfter(checkIn)) {
            AlertUtil.showError("Invalid Date", "Check-out must be after check-in.");
            return;
        }

        String roomNumber = selectedRoom.split(" - ")[0];
        String promoCode  = tfPromoCode.getText().trim();

        BookingService.BookingResponse response = bookingService.createBooking(
                selectedCustomer.getCustomerId(), roomNumber,
                checkIn, checkOut, spGuests.getValue(),
                promoCode, cbBreakfast.isSelected(),
                cbAirport.isSelected(), cbSpa.isSelected(),
                taSpecialRequests.getText()
        );

        if (response.result != BookingService.BookingResult.SUCCESS) {
            AlertUtil.showError("Booking Failed", response.message);
            return;
        }

        if (response.birthdayOffer) {
            AlertUtil.showOffer(response.message);
        }

        showPaymentProcessing(response.booking);
    }

    private void showPaymentProcessing(Booking booking) {
        if (paymentOverlay != null) {
            paymentOverlay.setVisible(true);
            if (pbPayment != null) pbPayment.setProgress(-1); // indeterminate
        }

        bookingService.processPayment(
                booking,
                msg -> Platform.runLater(() -> {
                    if (lblPaymentStatus != null) lblPaymentStatus.setText(msg);
                }),
                success -> Platform.runLater(() -> {
                    if (paymentOverlay != null) paymentOverlay.setVisible(false);
                    if (success) {
                        // Simulated notifications
                        String email = dataStore.getCustomerById(booking.getCustomerId())
                                .map(c -> c.getEmail()).orElse("customer@email.com");
                        String phone = dataStore.getCustomerById(booking.getCustomerId())
                                .map(c -> c.getPhone()).orElse("XXXXXXXXXX");

                        NotificationService.simulateEmail(email,
                                "Booking Confirmation — " + booking.getBookingId(),
                                buildEmailBody(booking));
                        NotificationService.simulateSMS(phone,
                                "Booking confirmed! ID: " + booking.getBookingId()
                                        + ". Total: ₹" + String.format("%,.0f", booking.getFinalAmount())
                                        + ". Check-in: " + booking.getCheckInDate()
                                        + ". Grand Azure Hotel.");

                        AlertUtil.showBookingConfirmation(booking, email, phone);
                        filterRooms();
                        loadBookings();
                        resetForm();
                    } else {
                        AlertUtil.showError("Payment Failed", "Payment processing failed. Please try again.");
                    }
                })
        );
    }

    private String buildEmailBody(Booking booking) {
        return "Dear Guest,\n\nYour booking is confirmed!\n"
                + "Booking ID : " + booking.getBookingId() + "\n"
                + "Room       : " + booking.getRoomNumber() + "\n"
                + "Check-in   : " + booking.getCheckInDate() + "\n"
                + "Check-out  : " + booking.getCheckOutDate() + "\n"
                + "Amount     : ₹" + String.format("%,.2f", booking.getFinalAmount()) + "\n\n"
                + "Thank you for choosing Grand Azure Hotel!";
    }

    private void resetForm() {
        tfCustomerPhone.clear();
        lblCustomerInfo.setText("Enter phone number and click Find");
        lblCustomerInfo.setStyle("");
        lblBirthdayBadge.setVisible(false);
        lblBirthdayBadge.setManaged(false);
        selectedCustomer = null;
        tfPromoCode.clear();
        lblPromoStatus.setText("");
        cbBreakfast.setSelected(false);
        cbAirport.setSelected(false);
        cbSpa.setSelected(false);
        taSpecialRequests.clear();
        dpCheckIn.setValue(LocalDate.now());
        dpCheckOut.setValue(LocalDate.now().plusDays(1));
        cbRoomType.setValue("All");
        cbBudgetFilter.setValue("Any Budget");
        filterRooms();
    }

    // =========================================================
    //  TABLE
    // =========================================================

    private void setupTable() {
        colBookingId.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBookingId()));
        colCustomer.setCellValueFactory(c -> {
            String cid = c.getValue().getCustomerId();
            return new SimpleStringProperty(
                    dataStore.getCustomerById(cid).map(Customer::getName).orElse(cid));
        });
        colRoom.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getRoomNumber() + " (" + c.getValue().getNumberOfDays() + "n)"));
        colCheckIn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getCheckInDate().toString()));
        colCheckOut.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getCheckOutDate().toString()));
        colAmount.setCellValueFactory(c -> new SimpleStringProperty(
                "₹" + String.format("%,.0f", c.getValue().getFinalAmount())));
        colBStatus.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getStatus().name()));

        bookingData = FXCollections.observableArrayList();
        bookingsTable.setItems(bookingData);
    }

    private void loadBookings() {
        bookingData.setAll(dataStore.getAllBookings());
        if (lblBookingCount != null) {
            lblBookingCount.setText(bookingData.size() + " booking(s)");
        }
    }

    @FXML private void goBack() { NavUtil.navigateTo(tfCustomerPhone, "Rooms.fxml"); }
}
