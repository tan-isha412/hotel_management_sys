package com.hotel.controller;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 * MainController — manages the persistent sidebar and content area.
 * Dashboard has been removed; the app now starts on the Rooms screen.
 * "Add Room" is wired to AddRoom.fxml.
 */
public class MainController {

    @FXML private StackPane contentArea;

    // Sidebar nav buttons (fx:id must match MainLayout.fxml)
    @FXML private Button btnNavRooms;
    @FXML private Button btnNavAddRoom;
    @FXML private Button btnNavCustomers;
    @FXML private Button btnNavBooking;
    @FXML private Button btnNavCheckout;
    @FXML private Button btnNavBilling;

    private Button activeButton;

    public void initialize() {
        // Start on Rooms screen instead of Dashboard
        loadPage("Rooms.fxml", btnNavRooms);
    }

    private void loadPage(String fxml, Button activeBtn) {
        try {
            Node node = FXMLLoader.load(
                    getClass().getResource("/com/hotel/fxml/" + fxml)
            );

            FadeTransition fade = new FadeTransition(Duration.millis(200), node);
            fade.setFromValue(0.0);
            fade.setToValue(1.0);

            contentArea.getChildren().setAll(node);
            fade.play();

            if (activeButton != null) {
                activeButton.getStyleClass().remove("nav-btn-active");
            }
            if (activeBtn != null) {
                activeBtn.getStyleClass().add("nav-btn-active");
                activeButton = activeBtn;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML public void showRooms()      { loadPage("Rooms.fxml",      btnNavRooms);      }
    @FXML public void showAddRoom()    { loadPage("AddRoom.fxml",    btnNavAddRoom);    }
    @FXML public void showCustomers()  { loadPage("Customers.fxml",  btnNavCustomers);  }
    @FXML public void showBooking()    { loadPage("Booking.fxml",    btnNavBooking);    }
    @FXML public void showCheckout()   { loadPage("Checkout.fxml",   btnNavCheckout);   }
    @FXML public void showBilling()    { loadPage("Billing.fxml",    btnNavBilling);    }
}
