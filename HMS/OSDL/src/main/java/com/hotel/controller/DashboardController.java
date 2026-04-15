package com.hotel.controller;

import com.hotel.service.DataStore;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/**
 * DashboardController — shows live stats and provides quick navigation
 * to all other screens via the MainController content area.
 */
public class DashboardController implements Initializable {

    // Stats
    @FXML private Label lblTotalRooms;
    @FXML private Label lblAvailableRooms;
    @FXML private Label lblBookedRooms;
    @FXML private Label lblTotalCustomers;
    @FXML private Label lblActiveBookings;
    @FXML private Label lblTotalRevenue;

    // Analytics
    @FXML private Label lblOccupancyRate;
    @FXML private ProgressBar pbOccupancy;
    @FXML private Label lblAnalyticsRooms;
    @FXML private Label lblAnalyticsRevenue;
    @FXML private Label lblAnalyticsAvgStay;

    // Quick stats
    @FXML private Label lblQuickStat1;
    @FXML private Label lblQuickStat2;
    @FXML private Label lblQuickStat3;

    // Header date/time
    @FXML private Label lblCurrentDate;
    @FXML private Label lblCurrentTime;

    private DataStore dataStore;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        System.out.println("Dashboard initialized");   // ADD THIS
        dataStore = DataStore.getInstance();
        refreshStats();
    }

    // ── Navigation helpers ────────────────────────────────────────────────────
    // Each method walks up the scene graph to find the MainController's
    // contentArea StackPane, then swaps its content — keeping the sidebar alive.

    @FXML private void goToRooms()     { loadIntoContentArea("Rooms.fxml");    }
    @FXML private void goToCustomers() { loadIntoContentArea("Customers.fxml");}
    @FXML private void goToBooking()   { loadIntoContentArea("Booking.fxml");  }
    @FXML private void goToCheckout()  { loadIntoContentArea("Checkout.fxml"); }
    @FXML private void goToBilling()   { loadIntoContentArea("Billing.fxml");  }

    /**
     * Finds the StackPane with fx:id="contentArea" that lives inside
     * MainLayout.fxml and replaces its content with the requested page.
     * This keeps the sidebar visible at all times.
     */
    private void loadIntoContentArea(String fxmlFile) {
        try {
            // The Dashboard VBox is a child of the contentArea StackPane.
            // Walk up: Dashboard VBox → contentArea StackPane
            StackPane contentArea = (StackPane) lblTotalRooms.getScene()
                    .lookup("#contentArea");

            if (contentArea == null) {
                System.err.println("contentArea not found – cannot navigate to " + fxmlFile);
                return;
            }

            Node page = FXMLLoader.load(
                    getClass().getResource("/com/hotel/fxml/" + fxmlFile)
            );
            contentArea.getChildren().setAll(page);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ── Stats ─────────────────────────────────────────────────────────────────

    private void refreshStats() {
        int total     = dataStore.getAllRooms().size();
        int available = dataStore.getAvailableRooms().size();
        int booked    = total - available;
        int customers = dataStore.getAllCustomers().size();
        int active    = dataStore.getActiveBookings().size();

        double revenue = dataStore.getAllBookings().stream()
                .mapToDouble(b -> b.getFinalAmount())
                .sum();
        double avgBill = dataStore.getAllBookings().isEmpty()
                ? 0
                : revenue / dataStore.getAllBookings().size();

        lblTotalRooms.setText(String.valueOf(total));
        lblAvailableRooms.setText(String.valueOf(available));
        lblBookedRooms.setText(String.valueOf(booked));
        lblTotalCustomers.setText(String.valueOf(customers));
        lblActiveBookings.setText(String.valueOf(active));
        lblTotalRevenue.setText("₹" + formatAmount(revenue));

        double occupancyRate = total > 0 ? (double) booked / total : 0;
        lblOccupancyRate.setText(String.format("%.0f%%", occupancyRate * 100));
        pbOccupancy.setProgress(occupancyRate);
        lblAnalyticsRooms.setText("Rooms: " + booked + " occupied / " + total + " total");
        lblAnalyticsRevenue.setText("Session revenue: ₹" + formatAmount(revenue));
        lblAnalyticsAvgStay.setText("Avg booking value: ₹" + formatAmount(avgBill));

        long checkedOut = dataStore.getAllBookings().stream()
                .filter(b -> b.getStatus().name().equals("CHECKED_OUT"))
                .count();
        double totalDiscount = dataStore.getAllBookings().stream()
                .mapToDouble(b -> b.getDiscount())
                .sum();

        lblQuickStat1.setText("✅ " + active + " active booking(s)");
        lblQuickStat2.setText("🚪 " + checkedOut + " checkout(s) completed");
        lblQuickStat3.setText("🎟️ Savings given: ₹" + formatAmount(totalDiscount));
    }

    private void startClock() {
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy");
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("hh:mm:ss a");

        lblCurrentDate.setText(LocalDate.now().format(dateFmt));
        lblCurrentTime.setText(LocalTime.now().format(timeFmt));

        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            lblCurrentDate.setText(LocalDate.now().format(dateFmt));
            lblCurrentTime.setText(LocalTime.now().format(timeFmt));
        }));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }

    private String formatAmount(double amount) {
        if (amount >= 100000) {
            return String.format("%.1fL", amount / 100000);
        }
        return String.format("%,.0f", amount);
    }
}
