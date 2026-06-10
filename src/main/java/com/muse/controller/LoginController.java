package com.muse.controller;

import com.muse.service.AuthService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Controller untuk Login.fxml
 * Menghubungkan UI login ke AuthService
 */
public class LoginController {

    @FXML private TextField     tfUsername;
    @FXML private PasswordField pfPassword;
    @FXML private Label         lblError;

    @FXML
    private void onUsernameEnter() {
        pfPassword.requestFocus();
    }

    @FXML
    private void onLogin() {
        String username = tfUsername.getText().trim();
        String password = pfPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Username dan password tidak boleh kosong.");
            return;
        }

        boolean ok = AuthService.getInstance().login(username, password);
        if (ok) {
            navigateToMain();
        } else {
            showError("Username atau password salah.");
            pfPassword.clear();
            pfPassword.requestFocus();
        }
    }

    private void showError(String msg) {
        lblError.setText(msg);
        lblError.setVisible(true);
        lblError.setManaged(true);
    }

    private void navigateToMain() {
        try {
            Stage stage = (Stage) tfUsername.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/Main.fxml"));
            BorderPane mainRoot = loader.load();

            Scene scene = tfUsername.getScene();
            scene.setRoot(mainRoot);
            stage.setWidth(1100);
            stage.setHeight(700);
            stage.centerOnScreen();

        } catch (Exception e) {
            showError("Gagal memuat halaman utama: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
