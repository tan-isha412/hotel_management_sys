package com.hotel.controller;

import com.hotel.model.Room;
import com.hotel.service.DataStore;
import com.hotel.util.NavUtil;
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
 * RoomsController - manages the rooms view with TableView and filtering.
 * The "Add Room" button navigates to AddRoom.fxml.
 */
public class RoomsController implements Initializable {

    @FXML private TableView<Room> roomsTable;
    @FXML private TableColumn<Room, String> colRoomNo;
    @FXML private TableColumn<Room, String> colType;
    @FXML private TableColumn<Room, String> colFloor;
    @FXML private TableColumn<Room, String> colBed;
    @FXML private TableColumn<Room, String> colCapacity;
    @FXML private TableColumn<Room, String> colPrice;
    @FXML private TableColumn<Room, String> colStatus;
    @FXML private TableColumn<Room, String> colAmenities;

    @FXML private ComboBox<String> filterType;
    @FXML private ComboBox<String> filterStatus;
    @FXML private Label lblCount;

    private DataStore dataStore;
    private ObservableList<Room> roomData;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dataStore = DataStore.getInstance();
        setupTable();
        setupFilters();
        loadRooms();
    }

    private void setupTable() {
        colRoomNo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRoomNumber()));
        colType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRoomType()));
        colFloor.setCellValueFactory(c -> new SimpleStringProperty("Floor " + c.getValue().getFloor()));
        colBed.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBedType()));
        colCapacity.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCapacity() + " guests"));
        colPrice.setCellValueFactory(c -> new SimpleStringProperty(
            "₹" + String.format("%,.0f", c.getValue().getBasePrice()) + "/night"));
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));
        colAmenities.setCellValueFactory(c -> new SimpleStringProperty(
            String.join(", ", c.getValue().getAmenities()).substring(0,
                Math.min(40, String.join(", ", c.getValue().getAmenities()).length())) + "..."));

        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("AVAILABLE".equals(item)) {
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    } else if ("BOOKED".equals(item)) {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                    }
                }
            }
        });

        roomData = FXCollections.observableArrayList();
        roomsTable.setItems(roomData);
    }

    private void setupFilters() {
        filterType.setItems(FXCollections.observableArrayList(
            "All", "Standard", "Deluxe", "Luxury Suite"));
        filterType.setValue("All");
        filterStatus.setItems(FXCollections.observableArrayList(
            "All", "AVAILABLE", "BOOKED"));
        filterStatus.setValue("All");
    }

    private void loadRooms() {
        applyFilter();
    }

    @FXML
    private void applyFilter() {
        String typeFilter   = filterType.getValue();
        String statusFilter = filterStatus.getValue();

        List<Room> filtered = dataStore.getAllRooms().stream()
            .filter(r -> "All".equals(typeFilter)   || r.getRoomType().equalsIgnoreCase(typeFilter))
            .filter(r -> "All".equals(statusFilter) || r.getStatus().equals(statusFilter))
            .collect(Collectors.toList());

        roomData.setAll(filtered);
        lblCount.setText("Showing " + filtered.size() + " rooms");
    }

    // "← Back" on Rooms goes to Booking (no more Dashboard)
    @FXML private void goBack()      { NavUtil.navigateTo(roomsTable, "Booking.fxml");  }
    @FXML private void goToBooking() { NavUtil.navigateTo(roomsTable, "Booking.fxml");  }
    @FXML private void goToAddRoom() { NavUtil.navigateTo(roomsTable, "AddRoom.fxml");  }
}
