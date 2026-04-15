package com.hotel.controller;

import com.hotel.model.Booking;
import com.hotel.model.Customer;
import com.hotel.service.BillingService;
import com.hotel.service.DataStore;
import com.hotel.util.NavUtil;
import com.hotel.util.AlertUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * BillingController — billing history, search, invoice generation.
 *
 * Enhancements:
 *  - Search/filter by customer name, booking ID, or room
 *  - Checked-out count stat card
 *  - Improved stats (discount totals, avg bill)
 */
public class BillingController implements Initializable {

    @FXML private TableView<Booking> billingTable;
    @FXML private TableColumn<Booking, String> colBookingId;
    @FXML private TableColumn<Booking, String> colCustomer;
    @FXML private TableColumn<Booking, String> colRoom;
    @FXML private TableColumn<Booking, String> colDates;
    @FXML private TableColumn<Booking, String> colNights;
    @FXML private TableColumn<Booking, String> colSubtotal;
    @FXML private TableColumn<Booking, String> colDiscount;
    @FXML private TableColumn<Booking, String> colTotal;
    @FXML private TableColumn<Booking, String> colPromo;
    @FXML private TableColumn<Booking, String> colStatus;

    @FXML private Label lblTotalRevenue;
    @FXML private Label lblTotalDiscount;
    @FXML private Label lblTotalBookings;
    @FXML private Label lblAvgBill;
    @FXML private Label lblCheckedOut;

    @FXML private TextField tfSearch;

    private DataStore dataStore;
    private BillingService billingService;
    private ObservableList<Booking> billingData;
    private List<Booking> allBookings;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dataStore     = DataStore.getInstance();
        billingService = new BillingService();
        setupTable();
        loadBillingData();
    }

    private void setupTable() {
        colBookingId.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBookingId()));
        colCustomer.setCellValueFactory(c -> {
            String cid = c.getValue().getCustomerId();
            return new SimpleStringProperty(
                    dataStore.getCustomerById(cid).map(Customer::getName).orElse(cid));
        });
        colRoom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRoomNumber()));
        colDates.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getCheckInDate() + " → " + c.getValue().getCheckOutDate()));
        colNights.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getNumberOfDays() + "n"));
        colSubtotal.setCellValueFactory(c -> new SimpleStringProperty(
                "₹" + String.format("%,.0f", c.getValue().getTotalAmount())));
        colDiscount.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getDiscount() > 0
                        ? "-₹" + String.format("%,.0f", c.getValue().getDiscount())
                        : "—"));
        colTotal.setCellValueFactory(c -> new SimpleStringProperty(
                "₹" + String.format("%,.0f", c.getValue().getFinalAmount())));
        colPromo.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getPromoCode() != null ? c.getValue().getPromoCode() : "—"));
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getStatus().name()));

        billingData = FXCollections.observableArrayList();
        billingTable.setItems(billingData);
    }

    private void loadBillingData() {
        allBookings = dataStore.getAllBookings();
        billingData.setAll(allBookings);
        updateStats(allBookings);
    }

    private void updateStats(List<Booking> source) {
        double totalRevenue  = source.stream().mapToDouble(Booking::getFinalAmount).sum();
        double totalDiscount = source.stream().mapToDouble(Booking::getDiscount).sum();
        int    count         = source.size();
        double avg           = count > 0 ? totalRevenue / count : 0;
        long   checkedOut    = source.stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.CHECKED_OUT)
                .count();

        lblTotalRevenue.setText("₹" + String.format("%,.0f", totalRevenue));
        lblTotalDiscount.setText("₹" + String.format("%,.0f", totalDiscount));
        lblTotalBookings.setText(String.valueOf(count));
        lblAvgBill.setText("₹" + String.format("%,.0f", avg));
        if (lblCheckedOut != null) lblCheckedOut.setText(String.valueOf(checkedOut));
    }

    // =========================================================
    //  SEARCH / FILTER
    // =========================================================

    @FXML
    private void searchBilling() {
        if (allBookings == null) return;
        String query = tfSearch != null ? tfSearch.getText().trim().toLowerCase() : "";

        if (query.isEmpty()) {
            billingData.setAll(allBookings);
            updateStats(allBookings);
            return;
        }

        List<Booking> filtered = allBookings.stream()
                .filter(b -> {
                    String custName = dataStore.getCustomerById(b.getCustomerId())
                            .map(Customer::getName).orElse("").toLowerCase();
                    return b.getBookingId().toLowerCase().contains(query)
                            || custName.contains(query)
                            || b.getRoomNumber().toLowerCase().contains(query)
                            || (b.getPromoCode() != null && b.getPromoCode().toLowerCase().contains(query));
                })
                .collect(Collectors.toList());

        billingData.setAll(filtered);
        updateStats(filtered);
    }

    @FXML
    private void clearSearch() {
        if (tfSearch != null) tfSearch.clear();
        loadBillingData();
    }

    @FXML
    private void generateSelectedBill() {
        Booking selected = billingTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("No Selection", "Please select a booking to generate bill.");
            return;
        }
        BillingService.BillSummary bill = billingService.generateBill(selected.getBookingId());
        if (bill != null) {
            AlertUtil.showBillDialog("Invoice — " + selected.getBookingId(),
                    billingService.formatBillAsText(bill));
        }
    }

    @FXML private void refresh()  { loadBillingData(); }
    @FXML private void goBack()   { NavUtil.navigateTo(billingTable, "Rooms.fxml"); }
}
