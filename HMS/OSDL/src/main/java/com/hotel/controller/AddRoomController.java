package com.hotel.controller;

import com.hotel.model.DeluxeRoom;
import com.hotel.model.LuxuryRoom;
import com.hotel.model.Room;
import com.hotel.model.StandardRoom;
import com.hotel.service.DataStore;
import com.hotel.util.AlertUtil;
import com.hotel.util.NavUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * AddRoomController — handles the Add New Room form.
 *
 * Supports all three room types (Standard / Deluxe / Luxury Suite).
 * Validates inputs and adds the room to DataStore so it immediately
 * appears in the Rooms list and Booking dropdown.
 */
public class AddRoomController implements Initializable {

    // ── Form fields ───────────────────────────────────────────────────────────
    @FXML private TextField  tfRoomNumber;
    @FXML private ComboBox<String> cbRoomType;
    @FXML private TextField  tfFloor;
    @FXML private TextField  tfBasePrice;
    @FXML private TextField  tfCapacity;

    // Luxury-only section
    @FXML private VBox       boxViewType;
    @FXML private ComboBox<String> cbViewType;

    // Inline error labels
    @FXML private Label lblRoomNumberError;
    @FXML private Label lblRoomTypeError;
    @FXML private Label lblFloorError;
    @FXML private Label lblBasePriceError;

    // Amenities preview
    @FXML private Label lblAmenitiesPreview;

    private DataStore dataStore;

    // ── Defaults per room type ────────────────────────────────────────────────
    private static final double DEFAULT_PRICE_STANDARD   = 2500.0;
    private static final double DEFAULT_PRICE_DELUXE     = 5500.0;
    private static final double DEFAULT_PRICE_LUXURY     = 12000.0;

    private static final int CAP_STANDARD = 2;
    private static final int CAP_DELUXE   = 3;
    private static final int CAP_LUXURY   = 4;

    // ── Initialise ────────────────────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dataStore = DataStore.getInstance();

        cbRoomType.setItems(FXCollections.observableArrayList(
                "Standard", "Deluxe", "Luxury Suite"));

        cbViewType.setItems(FXCollections.observableArrayList(
                "Sea View", "Garden View", "City View",
                "Pool View", "Mountain View"));
        cbViewType.setValue("Sea View");
    }

    // ── Event: room type changed ──────────────────────────────────────────────
    @FXML
    private void onRoomTypeChanged() {
        String type = cbRoomType.getValue();
        if (type == null) return;

        clearErrors();

        switch (type) {
            case "Standard" -> {
                tfBasePrice.setText(String.valueOf((int) DEFAULT_PRICE_STANDARD));
                tfCapacity.setText(String.valueOf(CAP_STANDARD));
                setLuxuryVisible(false);
                lblAmenitiesPreview.setText("WiFi, TV, AC, Room Service");
            }
            case "Deluxe" -> {
                tfBasePrice.setText(String.valueOf((int) DEFAULT_PRICE_DELUXE));
                tfCapacity.setText(String.valueOf(CAP_DELUXE));
                setLuxuryVisible(false);
                lblAmenitiesPreview.setText(
                        "WiFi, Smart TV, AC, Mini Bar, Balcony, Bathtub, Room Service, Newspaper");
            }
            case "Luxury Suite" -> {
                tfBasePrice.setText(String.valueOf((int) DEFAULT_PRICE_LUXURY));
                tfCapacity.setText(String.valueOf(CAP_LUXURY));
                setLuxuryVisible(true);
                lblAmenitiesPreview.setText(
                        "WiFi, 4K Smart TV, AC, Premium Mini Bar, Private Pool, " +
                        "Butler Service, Jacuzzi, Spa Access, Airport Transfer, " +
                        "Daily Breakfast, Evening Cocktails + View");
            }
        }
    }

    private void setLuxuryVisible(boolean visible) {
        boxViewType.setVisible(visible);
        boxViewType.setManaged(visible);
    }

    // ── Event: Add Room button ────────────────────────────────────────────────
    @FXML
    private void addRoom() {
        if (!validateForm()) return;

        String roomNumber = tfRoomNumber.getText().trim();
        String type       = cbRoomType.getValue();
        int    floor      = Integer.parseInt(tfFloor.getText().trim());
        double price      = Double.parseDouble(tfBasePrice.getText().trim());

        // Capacity — user override or type default
        int capacity = defaultCapacity(type);
        String capText = tfCapacity.getText().trim();
        if (!capText.isEmpty()) {
            try { capacity = Integer.parseInt(capText); }
            catch (NumberFormatException ignored) { /* already validated */ }
        }

        // Build the room using existing subclass constructors, then override price/capacity
        Room newRoom;
        switch (type) {
            case "Deluxe" -> {
                newRoom = new DeluxeRoom(roomNumber, floor);
            }
            case "Luxury Suite" -> {
                String view = cbViewType.getValue() != null
                        ? cbViewType.getValue() : "Sea View";
                newRoom = new LuxuryRoom(roomNumber, floor, view);
            }
            default -> {
                newRoom = new StandardRoom(roomNumber, floor);
            }
        }

        // Apply any user-overridden price / capacity
        newRoom.setBasePrice(price);
        newRoom.setCapacity(capacity);

        // Persist via DataStore (addRoom already calls saveRooms())
        dataStore.addRoom(newRoom);

        AlertUtil.showInfo(
                "Room Added",
                "Room " + roomNumber + " (" + type + ") has been added successfully.\n" +
                "It is now available for booking."
        );

        clearForm();
    }

    // ── Event: Clear Form button ──────────────────────────────────────────────
    @FXML
    private void clearForm() {
        tfRoomNumber.clear();
        cbRoomType.setValue(null);
        tfFloor.clear();
        tfBasePrice.clear();
        tfCapacity.clear();
        cbViewType.setValue("Sea View");
        setLuxuryVisible(false);
        lblAmenitiesPreview.setText("Select a room type to see included amenities.");
        clearErrors();
    }

    // ── Navigation ────────────────────────────────────────────────────────────
    @FXML
    private void goBack() {
        NavUtil.navigateTo(tfRoomNumber, "Rooms.fxml");
    }

    // ── Validation ────────────────────────────────────────────────────────────
    private boolean validateForm() {
        clearErrors();
        boolean valid = true;

        // Room Number — required, not blank, no duplicate
        String roomNum = tfRoomNumber.getText().trim();
        if (roomNum.isEmpty()) {
            lblRoomNumberError.setText("Room number is required.");
            valid = false;
        } else if (dataStore.getRoomByNumber(roomNum).isPresent()) {
            lblRoomNumberError.setText("Room " + roomNum + " already exists.");
            valid = false;
        }

        // Room Type — required
        if (cbRoomType.getValue() == null) {
            lblRoomTypeError.setText("Please select a room type.");
            valid = false;
        }

        // Floor — required, must be a positive integer
        String floorText = tfFloor.getText().trim();
        if (floorText.isEmpty()) {
            lblFloorError.setText("Floor is required.");
            valid = false;
        } else {
            try {
                int f = Integer.parseInt(floorText);
                if (f < 1 || f > 50) {
                    lblFloorError.setText("Floor must be between 1 and 50.");
                    valid = false;
                }
            } catch (NumberFormatException e) {
                lblFloorError.setText("Floor must be a whole number.");
                valid = false;
            }
        }

        // Base Price — required, must be a positive number
        String priceText = tfBasePrice.getText().trim();
        if (priceText.isEmpty()) {
            lblBasePriceError.setText("Base price is required.");
            valid = false;
        } else {
            try {
                double p = Double.parseDouble(priceText);
                if (p <= 0) {
                    lblBasePriceError.setText("Price must be greater than zero.");
                    valid = false;
                }
            } catch (NumberFormatException e) {
                lblBasePriceError.setText("Price must be a valid number.");
                valid = false;
            }
        }

        // Capacity — optional, but if provided must be a positive integer
        String capText = tfCapacity.getText().trim();
        if (!capText.isEmpty()) {
            try {
                int c = Integer.parseInt(capText);
                if (c < 1 || c > 20) {
                    AlertUtil.showError("Validation Error",
                            "Capacity must be between 1 and 20.");
                    valid = false;
                }
            } catch (NumberFormatException e) {
                AlertUtil.showError("Validation Error",
                        "Capacity must be a whole number.");
                valid = false;
            }
        }

        return valid;
    }

    private void clearErrors() {
        lblRoomNumberError.setText("");
        lblRoomTypeError.setText("");
        lblFloorError.setText("");
        lblBasePriceError.setText("");
    }

    private int defaultCapacity(String type) {
        return switch (type) {
            case "Deluxe"      -> CAP_DELUXE;
            case "Luxury Suite" -> CAP_LUXURY;
            default             -> CAP_STANDARD;
        };
    }
}
