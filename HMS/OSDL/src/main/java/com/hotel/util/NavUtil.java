package com.hotel.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

/**
 * NavUtil — loads an FXML page into the MainLayout contentArea StackPane.
 * Use this in any controller instead of MainApp.navigateTo() so the
 * sidebar stays visible.
 *
 * Usage:  NavUtil.navigateTo(anyFxmlField, "Booking.fxml");
 */
public class NavUtil {

    /**
     * @param anyNode  Any node that belongs to the current scene
     *                 (e.g. pass a @FXML-injected Label or TableView).
     * @param fxmlFile Filename only, e.g. "Booking.fxml"
     */
    public static void navigateTo(Node anyNode, String fxmlFile) {
        try {
            StackPane contentArea = (StackPane)
                    anyNode.getScene().lookup("#contentArea");

            if (contentArea == null) {
                System.err.println("[NavUtil] #contentArea not found. "
                        + "Is MainLayout.fxml the root?");
                return;
            }

            Node page = FXMLLoader.load(
                    NavUtil.class.getResource("/com/hotel/fxml/" + fxmlFile)
            );
            contentArea.getChildren().setAll(page);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
