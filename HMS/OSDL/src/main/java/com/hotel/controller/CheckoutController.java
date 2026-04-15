package com.hotel.controller;

import com.hotel.model.Booking;
import com.hotel.model.Customer;
import com.hotel.service.BillingService;
import com.hotel.service.BookingService;
import com.hotel.service.DataStore;
import com.hotel.service.NotificationService;
import com.hotel.util.NavUtil;
import com.hotel.util.AlertUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * CheckoutController — checkout, cancellation, and inline invoice preview.
 *
 * Enhancements:
 *  - Inline invoice breakdown shown before confirming checkout
 *  - Simulated email + SMS notifications after checkout
 *  - Active booking count label
 */
public class CheckoutController implements Initializable {

    @FXML private TextField tfBookingId;

    // Inline invoice preview panel
    @FXML private VBox invoicePreviewBox;
    @FXML private VBox noSelectionBox;

    @FXML private Label lblInvoiceId;
    @FXML private Label lblCustomerName;
    @FXML private Label lblCustomerPhone;
    @FXML private Label lblRoom;
    @FXML private Label lblCheckIn;
    @FXML private Label lblCheckOut;
    @FXML private Label lblDates;
    @FXML private Label lblRoomCharges;
    @FXML private Label lblServiceCharges;
    @FXML private Label lblSubtotal;
    @FXML private Label lblDiscountRow;
    @FXML private Label lblAmount;
    @FXML private Label lblStatus;

    @FXML private Label lblActiveCount;

    @FXML private TableView<Booking> activeBookingsTable;
    @FXML private TableColumn<Booking, String> colBookingId;
    @FXML private TableColumn<Booking, String> colCustomer;
    @FXML private TableColumn<Booking, String> colRoom;
    @FXML private TableColumn<Booking, String> colCheckIn;
    @FXML private TableColumn<Booking, String> colCheckOut;
    @FXML private TableColumn<Booking, String> colAmount;
    @FXML private TableColumn<Booking, String> colStatus;

    @FXML private Button btnCheckout;
    @FXML private Button btnCancel;
    @FXML private Button btnViewBill;

    private DataStore dataStore;
    private BookingService bookingService;
    private BillingService billingService;
    private ObservableList<Booking> activeBookingData;
    private Booking selectedBooking;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dataStore     = DataStore.getInstance();
        bookingService = new BookingService();
        billingService = new BillingService();
        setupTable();
        loadActiveBookings();
        setButtonsEnabled(false);
        showNoSelection();
    }

    // =========================================================
    //  TABLE SETUP
    // =========================================================

    private void setupTable() {
        colBookingId.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBookingId()));
        colCustomer.setCellValueFactory(c -> {
            String cid = c.getValue().getCustomerId();
            return new SimpleStringProperty(
                    dataStore.getCustomerById(cid).map(Customer::getName).orElse(cid));
        });
        colRoom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRoomNumber()));
        colCheckIn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getCheckInDate().toString()));
        colCheckOut.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getCheckOutDate().toString()));
        colAmount.setCellValueFactory(c -> new SimpleStringProperty(
                "₹" + String.format("%,.0f", c.getValue().getFinalAmount())));
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getStatus().name()));

        activeBookingData = FXCollections.observableArrayList();
        activeBookingsTable.setItems(activeBookingData);

        activeBookingsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, newSel) -> {
                    if (newSel != null) {
                        tfBookingId.setText(newSel.getBookingId());
                        displayBookingDetails(newSel);
                    }
                }
        );
    }

    private void loadActiveBookings() {
        activeBookingData.setAll(dataStore.getActiveBookings());
        if (lblActiveCount != null) {
            lblActiveCount.setText(activeBookingData.size() + " active");
        }
    }

    // =========================================================
    //  LOOKUP
    // =========================================================

    @FXML
    private void lookupBooking() {
        String bid = tfBookingId.getText().trim();
        Optional<Booking> bookingOpt = dataStore.getBookingById(bid);
        if (bookingOpt.isEmpty()) {
            AlertUtil.showError("Not Found", "No booking found with ID: " + bid);
            selectedBooking = null;
            setButtonsEnabled(false);
            showNoSelection();
            return;
        }
        selectedBooking = bookingOpt.get();
        displayBookingDetails(selectedBooking);
    }

    // =========================================================
    //  INVOICE PREVIEW (inline)
    // =========================================================

    private void displayBookingDetails(Booking booking) {
        selectedBooking = booking;
        Optional<Customer> cOpt = dataStore.getCustomerById(booking.getCustomerId());

        String custName  = cOpt.map(Customer::getName).orElse("Unknown");
        String custPhone = cOpt.map(Customer::getPhone).orElse("—");

        // Invoice header
        lblInvoiceId.setText("Booking: " + booking.getBookingId()
                + "  |  Booked: " + booking.getBookingDate());

        // Guest
        lblCustomerName.setText(custName);
        lblCustomerPhone.setText(custPhone);

        // Room & dates
        lblRoom.setText("Room " + booking.getRoomNumber()
                + (booking.isUpgraded() ? " (upgraded from " + booking.getOriginalRoomNumber() + ")" : ""));
        lblCheckIn.setText(booking.getCheckInDate().toString());
        lblCheckOut.setText(booking.getCheckOutDate().toString());
        lblDates.setText(booking.getNumberOfDays() + " night(s), "
                + booking.getNumberOfGuests() + " guest(s)");

        // Charges
        double serviceAmt = booking.getServiceChargesTotal();
        double roomAmt    = booking.getTotalAmount() - serviceAmt;

        lblRoomCharges.setText("₹" + String.format("%,.2f", roomAmt));
        lblServiceCharges.setText(serviceAmt > 0
                ? "₹" + String.format("%,.2f", serviceAmt)
                : "—");
        lblSubtotal.setText("₹" + String.format("%,.2f", booking.getTotalAmount()));
        lblDiscountRow.setText(booking.getDiscount() > 0
                ? "-₹" + String.format("%,.2f", booking.getDiscount())
                : "—");
        lblAmount.setText("₹" + String.format("%,.2f", booking.getFinalAmount()));
        lblStatus.setText("Status: " + booking.getStatus().name());

        // Show invoice, hide placeholder
        invoicePreviewBox.setVisible(true);
        invoicePreviewBox.setManaged(true);
        noSelectionBox.setVisible(false);
        noSelectionBox.setManaged(false);

        setButtonsEnabled(
                booking.getStatus() == Booking.BookingStatus.CONFIRMED
                || booking.getStatus() == Booking.BookingStatus.CHECKED_IN
        );
    }

    private void showNoSelection() {
        if (invoicePreviewBox != null) {
            invoicePreviewBox.setVisible(false);
            invoicePreviewBox.setManaged(false);
        }
        if (noSelectionBox != null) {
            noSelectionBox.setVisible(true);
            noSelectionBox.setManaged(true);
        }
    }

    // =========================================================
    //  ACTIONS
    // =========================================================

    @FXML
    private void checkout() {
        if (selectedBooking == null) return;
        if (!AlertUtil.showConfirm("Confirm Checkout",
                "Checkout booking " + selectedBooking.getBookingId() + "?\n"
                + "Room will be marked available.")) return;

        boolean success = bookingService.checkout(selectedBooking.getBookingId());
        if (success) {
            // Simulate notifications
            Optional<Customer> cOpt = dataStore.getCustomerById(selectedBooking.getCustomerId());
            String email = cOpt.map(Customer::getEmail).orElse("guest@email.com");
            String phone = cOpt.map(Customer::getPhone).orElse("XXXXXXXXXX");
            String custName = cOpt.map(Customer::getName).orElse("Guest");

            NotificationService.simulateEmail(email,
                    "Checkout Summary — " + selectedBooking.getBookingId(),
                    buildCheckoutEmail(selectedBooking, custName));
            NotificationService.simulateSMS(phone,
                    "Checkout complete! Booking " + selectedBooking.getBookingId()
                    + ". Thank you, " + custName + "! Visit again — Grand Azure Hotel.");

            AlertUtil.showCheckoutConfirmation(selectedBooking, email, phone);
            loadActiveBookings();
            setButtonsEnabled(false);
            selectedBooking = null;
            showNoSelection();
            tfBookingId.clear();
        } else {
            AlertUtil.showError("Error", "Checkout failed. Please try again.");
        }
    }

    @FXML
    private void cancelBooking() {
        if (selectedBooking == null) return;
        if (!AlertUtil.showConfirm("Confirm Cancellation",
                "Cancel booking " + selectedBooking.getBookingId() + "?\n"
                + "This action cannot be undone.")) return;

        boolean success = bookingService.cancelBooking(selectedBooking.getBookingId());
        if (success) {
            AlertUtil.showInfo("Cancelled",
                    "Booking " + selectedBooking.getBookingId() + " has been cancelled.");
            loadActiveBookings();
            setButtonsEnabled(false);
            selectedBooking = null;
            showNoSelection();
            tfBookingId.clear();
        } else {
            AlertUtil.showError("Error", "Cancellation failed.");
        }
    }

    @FXML
    private void viewBill() {
        if (selectedBooking == null) return;
        BillingService.BillSummary bill = billingService.generateBill(selectedBooking.getBookingId());
        if (bill != null) {
            AlertUtil.showBillDialog("Invoice — " + selectedBooking.getBookingId(),
                    billingService.formatBillAsText(bill));
        }
    }

    private void setButtonsEnabled(boolean enabled) {
        if (btnCheckout != null) btnCheckout.setDisable(!enabled);
        if (btnCancel   != null) btnCancel.setDisable(!enabled);
        if (btnViewBill != null) btnViewBill.setDisable(!enabled);
    }

    private String buildCheckoutEmail(Booking booking, String name) {
        return "Dear " + name + ",\n\n"
                + "Thank you for staying at Grand Azure Hotel!\n"
                + "Booking ID  : " + booking.getBookingId() + "\n"
                + "Room        : " + booking.getRoomNumber() + "\n"
                + "Check-in    : " + booking.getCheckInDate() + "\n"
                + "Check-out   : " + booking.getCheckOutDate() + "\n"
                + "Total Paid  : ₹" + String.format("%,.2f", booking.getFinalAmount()) + "\n\n"
                + "We hope to see you again soon!";
    }

    @FXML private void goBack() { NavUtil.navigateTo(tfBookingId, "Rooms.fxml"); }
}
