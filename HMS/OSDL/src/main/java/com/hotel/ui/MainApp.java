package com.hotel.ui;

import com.hotel.service.DataStore;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


 public class MainApp extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        // Pre-load DataStore (initializes rooms, loads saved data)
        DataStore.getInstance();

        Parent root = FXMLLoader.load(
            getClass().getResource("/com/hotel/fxml/MainLayout.fxml")
        );
        Scene scene = new Scene(root, 1200, 750);
        scene.getStylesheets().add(
            getClass().getResource("/com/hotel/css/main.css").toExternalForm()
        );

        stage.setTitle("🏨 Grand Azure Hotel Management System");
        stage.setScene(scene);
        stage.setMinWidth(1100);
        stage.setMinHeight(700);
        stage.show();
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void navigateTo(String fxmlFile) {
        try {
            Parent root = FXMLLoader.load(
                MainApp.class.getResource("/com/hotel/fxml/" + fxmlFile)
            );
            Scene scene = new Scene(root, 1200, 750);
            scene.getStylesheets().add(
                MainApp.class.getResource("/com/hotel/css/main.css").toExternalForm()
            );
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
