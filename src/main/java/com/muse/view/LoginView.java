package com.muse.view;

import com.muse.service.AuthService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/**
 * Halaman Login
 * Admin mengisi username dan password.
 * Jika berhasil → Dashboard. Jika gagal → pesan error.
 */
public class LoginView {

    private final Stage stage;
    private VBox root;

    public LoginView(Stage stage) {
        this.stage = stage;
        build();
    }

    private void build() {
        // Outer background
        root = new VBox();
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("login-root");
        root.setFillWidth(true);

        // Login card
        VBox card = new VBox(14);
        card.setAlignment(Pos.CENTER_LEFT);
        card.getStyleClass().add("login-card");

        // Header
        VBox header = new VBox(4);
        header.setAlignment(Pos.CENTER);
        Label title = new Label("MUSE");
        title.getStyleClass().add("login-title");
        Label subtitle = new Label("MUSICAL INSTRUMENT RENTAL SYSTEM");
        subtitle.getStyleClass().add("login-subtitle");
        header.getChildren().addAll(title, subtitle);
        header.setPadding(new Insets(0, 0, 16, 0));

        // Username
        Label lblUser = new Label("Username");
        lblUser.getStyleClass().add("login-label");
        TextField tfUser = new TextField();
        tfUser.setPromptText("Masukkan username");
        tfUser.getStyleClass().add("login-field");

        // Password
        Label lblPass = new Label("Password");
        lblPass.getStyleClass().add("login-label");
        PasswordField pfPass = new PasswordField();
        pfPass.setPromptText("Masukkan password");
        pfPass.getStyleClass().add("login-field");

        // Error label
        Label lblError = new Label();
        lblError.getStyleClass().add("login-error");
        lblError.setVisible(false);
        lblError.setManaged(false);

        // Login button
        Button btnLogin = new Button("Login");
        btnLogin.getStyleClass().add("login-btn");

        // Action
        Runnable doLogin = () -> {
            String user = tfUser.getText().trim();
            String pass = pfPass.getText();

            if (user.isEmpty() || pass.isEmpty()) {
                lblError.setText("Username dan password tidak boleh kosong.");
                lblError.setVisible(true);
                lblError.setManaged(true);
                return;
            }

            boolean ok = AuthService.getInstance().login(user, pass);
            if (ok) {
                // Pindah ke halaman utama
                MainView mainView = new MainView(stage);
                stage.getScene().setRoot(mainView.getRoot());
                stage.setWidth(1100);
                stage.setHeight(700);
                stage.centerOnScreen();
            } else {
                lblError.setText("Username atau password salah.");
                lblError.setVisible(true);
                lblError.setManaged(true);
                pfPass.clear();
            }
        };

        btnLogin.setOnAction(e -> doLogin.run());
        pfPass.setOnAction(e -> doLogin.run());
        tfUser.setOnAction(e -> pfPass.requestFocus());

        card.getChildren().addAll(header, lblUser, tfUser, lblPass, pfPass, lblError, btnLogin);
        root.getChildren().add(card);
    }

    public VBox getRoot() {
        return root;
    }
}
