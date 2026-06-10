package com.muse;

import com.muse.util.DatabaseConnection;
import com.muse.util.DatabaseInitializer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Entry point aplikasi MUSE
 * Musical Instrument Rental System
 * Menggunakan JavaFX + FXML + Scene Builder
 */
public class MainApp extends Application {

    public static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;

        // Inisialisasi database (buat tabel + admin default)
        DatabaseInitializer.initialize();

        // Load halaman Login dari FXML
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/fxml/Login.fxml"));
        StackPane loginRoot = loader.load();

        Scene scene = new Scene(loginRoot, 900, 620);
        stage.setTitle("MUSE - Musical Instrument Rental System");
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(620);
        stage.show();
    }

    @Override
    public void stop() {
        DatabaseConnection.closeConnection();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
