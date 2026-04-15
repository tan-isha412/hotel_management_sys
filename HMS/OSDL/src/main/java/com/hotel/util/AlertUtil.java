package com.hotel.util;

import com.hotel.model.Booking;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;

import java.util.Optional;

/**
 * AlertUtil — utility for showing styled JavaFX dialogs.
 *
 * Enhancements:
 *  - showBookingConfirmation(...)  — rich confirmation popup with email/SMS simulation message
 *  - showCheckoutConfirmation(...) — post-checkout popup with invoice summary + notification log
 *  - All dialogs are dark-themed via CSS
 */
public class AlertUtil {

    // =========================================================
    //  BASIC DIALOGS
    // =========================================================

    public static void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleAlert(alert);
        alert.showAndWait();
    }

    public static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleAlert(alert);
        alert.showAndWait();
    }

    public static void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleAlert(alert);
        alert.showAndWait();
    }

    public static boolean showConfirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleAlert(alert);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    public static void showOffer(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("🎉 Special Offer!");
        alert.setHeaderText("You have a special offer!");
        alert.setContentText(message);
        styleAlert(alert);
        alert.showAndWait();
    }

    // =========================================================
    //  BILL DIALOG
    // =========================================================

    public static void showBillDialog(String title, String billText) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText("Invoice Generated");

        TextArea textArea = new TextArea(billText);
        textArea.setEditable(false);
        textArea.setWrapText(false);
        textArea.setStyle(
                "-fx-font-family: 'Courier New';" +
                        "-fx-font-size: 13px;" +
                        "-fx-text-fill: #0d1b2a;" +                     // dark text
                        "-fx-control-inner-background: #ffffff;" +      // white background
                        "-fx-highlight-fill: #c9a84c;" +
                        "-fx-highlight-text-fill: black;"
        );
        textArea.setPrefWidth(520);
        textArea.setPrefHeight(480);

        GridPane gp = new GridPane();
        gp.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(textArea, Priority.ALWAYS);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        gp.add(textArea, 0, 0);

        alert.getDialogPane().setContent(gp);
        alert.getDialogPane().setPrefWidth(540);
        styleAlert(alert);
        alert.showAndWait();
    }

    // =========================================================
    //  BOOKING CONFIRMATION (with simulated notification log)
    // =========================================================

    /**
     * Shows a rich booking confirmation dialog.
     * Displays booking summary + simulated email/SMS notification status.
     */
    public static void showBookingConfirmation(Booking booking, String email, String phone) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("✅ Booking Confirmed!");
        alert.setHeaderText(null);

        VBox content = new VBox(14);
        content.setPadding(new Insets(4, 4, 4, 4));
        content.setStyle("-fx-background-color: transparent;");

        // Success banner
        Label banner = new Label("🎉  Booking Confirmed Successfully!");
        banner.setStyle("-fx-font-size:16px; -fx-font-weight:bold; -fx-text-fill:#c9a84c;");

        // Booking summary grid
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(7);
        grid.setStyle("-fx-background-color:#0f2035; -fx-padding:14; -fx-background-radius:8;");

        addGridRow(grid, 0, "📋 Booking ID",   booking.getBookingId());
        addGridRow(grid, 1, "🛏️ Room",         booking.getRoomNumber());
        addGridRow(grid, 2, "📅 Check-In",      booking.getCheckInDate().toString());
        addGridRow(grid, 3, "📅 Check-Out",     booking.getCheckOutDate().toString());
        addGridRow(grid, 4, "🌙 Duration",      booking.getNumberOfDays() + " night(s)");
        addGridRow(grid, 5, "💰 Total Paid",    "₹" + String.format("%,.2f", booking.getFinalAmount()));
        if (booking.getDiscount() > 0) {
            addGridRow(grid, 6, "🎟️ You Saved",
                    "₹" + String.format("%,.2f", booking.getDiscount()));
        }

        // Notification status
        VBox notifBox = new VBox(8);
        notifBox.setStyle("-fx-background-color:#0f2035; -fx-padding:12; -fx-background-radius:8;"
                + "-fx-border-color:#1e3a5f; -fx-border-width:1; -fx-border-radius:8;");
        Label notifTitle = new Label("📬  Notifications Sent");
        notifTitle.setStyle("-fx-font-weight:bold; -fx-text-fill:#c9a84c; -fx-font-size:12px;");
        Label emailSent = new Label("✉️  Email sent to: " + email);
        emailSent.setStyle("-fx-text-fill:#4ade80; -fx-font-size:12px;");
        Label smsSent = new Label("📱  SMS sent to: +91 " + phone);
        smsSent.setStyle("-fx-text-fill:#4ade80; -fx-font-size:12px;");
        Label simNote = new Label("(Simulated — check console for details)");
        simNote.setStyle("-fx-text-fill:#4a6580; -fx-font-size:10px; -fx-font-style:italic;");
        notifBox.getChildren().addAll(notifTitle, emailSent, smsSent, simNote);

        content.getChildren().addAll(banner, grid, notifBox);
        alert.getDialogPane().setContent(content);
        alert.getDialogPane().setPrefWidth(480);
        styleAlert(alert);
        alert.showAndWait();
    }

    // =========================================================
    //  CHECKOUT CONFIRMATION
    // =========================================================

    public static void showCheckoutConfirmation(Booking booking, String email, String phone) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("🚪 Checkout Complete");
        alert.setHeaderText(null);

        VBox content = new VBox(14);
        content.setPadding(new Insets(4, 4, 4, 4));

        Label banner = new Label("✅  Guest Checked Out Successfully");
        banner.setStyle("-fx-font-size:16px; -fx-font-weight:bold; -fx-text-fill:#4ade80;");

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(7);
        grid.setStyle("-fx-background-color:#0f2035; -fx-padding:14; -fx-background-radius:8;");

        addGridRow(grid, 0, "📋 Booking ID",  booking.getBookingId());
        addGridRow(grid, 1, "🛏️ Room",        booking.getRoomNumber() + " (now available)");
        addGridRow(grid, 2, "🌙 Stay",        booking.getNumberOfDays() + " night(s)");
        addGridRow(grid, 3, "💰 Total Paid",  "₹" + String.format("%,.2f", booking.getFinalAmount()));

        VBox notifBox = new VBox(8);
        notifBox.setStyle("-fx-background-color:#0f2035; -fx-padding:12; -fx-background-radius:8;"
                + "-fx-border-color:#1e3a5f; -fx-border-width:1; -fx-border-radius:8;");
        Label notifTitle = new Label("📬  Invoice Sent to Guest");
        notifTitle.setStyle("-fx-font-weight:bold; -fx-text-fill:#c9a84c; -fx-font-size:12px;");
        Label emailSent = new Label("✉️  Invoice emailed to: " + email);
        emailSent.setStyle("-fx-text-fill:#4ade80; -fx-font-size:12px;");
        Label smsSent = new Label("📱  Summary SMS sent to: +91 " + phone);
        smsSent.setStyle("-fx-text-fill:#4ade80; -fx-font-size:12px;");
        Label simNote = new Label("(Simulated — check console for details)");
        simNote.setStyle("-fx-text-fill:#4a6580; -fx-font-size:10px; -fx-font-style:italic;");
        notifBox.getChildren().addAll(notifTitle, emailSent, smsSent, simNote);

        Label thankYou = new Label("Thank you for staying at Grand Azure Hotel! 🏨");
        thankYou.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:12px; -fx-font-style:italic;");
        thankYou.setTextAlignment(TextAlignment.CENTER);

        content.getChildren().addAll(banner, grid, notifBox, thankYou);
        alert.getDialogPane().setContent(content);
        alert.getDialogPane().setPrefWidth(480);
        styleAlert(alert);
        alert.showAndWait();
    }

    // =========================================================
    //  HELPER
    // =========================================================

    private static void addGridRow(GridPane grid, int row, String key, String value) {
        Label keyLabel = new Label(key);
        keyLabel.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:12px;");
        keyLabel.setMinWidth(120);
        Label valLabel = new Label(value);
        valLabel.setStyle("-fx-text-fill:#eaf0fb; -fx-font-size:12px; -fx-font-weight:bold;");
        grid.add(keyLabel, 0, row);
        grid.add(valLabel, 1, row);
    }

    private static void styleAlert(Alert alert) {
        try {
            alert.getDialogPane().getStylesheets().add(
                    AlertUtil.class.getResource("/com/hotel/css/main.css").toExternalForm()
            );
            alert.getDialogPane().setStyle("-fx-background-color: #0d1b2a;");
        } catch (Exception ignored) {}
    }
}
