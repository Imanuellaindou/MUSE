package com.muse.view;

import com.muse.service.AuthService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Layout utama aplikasi MUSE.
 * Terdiri dari sidebar navigasi + content area.
 * Sidebar tersedia di semua halaman kecuali Login.
 */
public class MainView {

    private final Stage stage;
    private BorderPane root;
    private VBox sidebar;
    private StackPane contentArea;

    // Halaman-halaman
    private DashboardView dashboardView;
    private AlatMusikView alatMusikView;
    private PelangganView pelangganView;
    private TransaksiBaruView transaksiBaruView;
    private PengembalianView pengembalianView;
    private RiwayatView riwayatView;
    private LaporanView laporanView;

    // Tombol sidebar untuk tracking active state
    private Button btnDashboard, btnAlatMusik, btnPelanggan,
                    btnTransaksiMinus, btnPengembalian, btnRiwayat, btnLaporan;

    public MainView(Stage stage) {
        this.stage = stage;
        build();
        navigateTo("dashboard");
    }

    private void build() {
        root = new BorderPane();
        root.getStyleClass().add("main-root");

        buildSidebar();
        buildContentArea();
        buildStatusBar();

        root.setLeft(sidebar);
        root.setCenter(contentArea);
    }

    private void buildSidebar() {
        sidebar = new VBox(0);
        sidebar.getStyleClass().add("sidebar");
        VBox.setVgrow(sidebar, Priority.ALWAYS);

        // Header
        VBox header = new VBox(2);
        header.getStyleClass().add("sidebar-header");
        header.setPadding(new Insets(16, 20, 16, 20));
        Label title = new Label("MUSE");
        title.getStyleClass().add("sidebar-title");
        Label subtitle = new Label("Rental System");
        subtitle.getStyleClass().add("sidebar-subtitle");
        header.getChildren().addAll(title, subtitle);

        // Section: MENU
        Label secMenu = new Label("MENU");
        secMenu.getStyleClass().add("sidebar-section-label");

        btnDashboard = sidebarBtn("  ⊞  Dashboard");
        btnAlatMusik = sidebarBtn("  ♪  Alat Musik");
        btnPelanggan = sidebarBtn("  ♟  Pelanggan");
        btnTransaksiMinus = sidebarBtn("  +  Transaksi Baru");
        btnPengembalian = sidebarBtn("  ↩  Pengembalian");

        // Section: LAPORAN
        Label secLaporan = new Label("LAPORAN");
        secLaporan.getStyleClass().add("sidebar-section-label");

        btnRiwayat = sidebarBtn("  ⏱  Riwayat");
        btnLaporan = sidebarBtn("  ▤  Laporan");

        // Section: AKUN
        Label secAkun = new Label("AKUN");
        secAkun.getStyleClass().add("sidebar-section-label");

        Button btnLogout = new Button("  ⏻  Logout");
        btnLogout.getStyleClass().add("sidebar-logout");
        btnLogout.setMaxWidth(Double.MAX_VALUE);

        // Actions
        btnDashboard.setOnAction(e -> navigateTo("dashboard"));
        btnAlatMusik.setOnAction(e -> navigateTo("alatmusik"));
        btnPelanggan.setOnAction(e -> navigateTo("pelanggan"));
        btnTransaksiMinus.setOnAction(e -> navigateTo("transaksi"));
        btnPengembalian.setOnAction(e -> navigateTo("pengembalian"));
        btnRiwayat.setOnAction(e -> navigateTo("riwayat"));
        btnLaporan.setOnAction(e -> navigateTo("laporan"));

        btnLogout.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Konfirmasi Logout");
            alert.setHeaderText(null);
            alert.setContentText("Apakah Anda yakin ingin logout?");
            alert.showAndWait().ifPresent(r -> {
                if (r == ButtonType.OK) {
                    AuthService.getInstance().logout();
                    LoginView loginView = new LoginView(stage);
                    stage.getScene().setRoot(loginView.getRoot());
                    stage.setWidth(900);
                    stage.setHeight(620);
                    stage.centerOnScreen();
                }
            });
        });

        // Spacer
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        sidebar.getChildren().addAll(
            header,
            secMenu,
            btnDashboard, btnAlatMusik, btnPelanggan, btnTransaksiMinus, btnPengembalian,
            secLaporan,
            btnRiwayat, btnLaporan,
            secAkun,
            btnLogout,
            spacer
        );
    }

    private Button sidebarBtn(String text) {
        Button btn = new Button(text);
        btn.getStyleClass().add("sidebar-btn");
        btn.setMaxWidth(Double.MAX_VALUE);
        return btn;
    }

    private void buildContentArea() {
        contentArea = new StackPane();
        contentArea.getStyleClass().add("content-area");
        VBox.setVgrow(contentArea, Priority.ALWAYS);
        HBox.setHgrow(contentArea, Priority.ALWAYS);
    }

    private void buildStatusBar() {
        HBox statusBar = new HBox(24);
        statusBar.getStyleClass().add("status-bar");
        statusBar.setAlignment(Pos.CENTER_LEFT);

        String adminName = AuthService.getInstance().getAdminLogin() != null
            ? AuthService.getInstance().getAdminLogin().getUsername()
            : "Admin";

        Label lblAdmin = new Label("Login sebagai: " + adminName);
        lblAdmin.getStyleClass().add("status-text");

        String tanggal = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy",
            Locale.forLanguageTag("id-ID")));
        Label lblTanggal = new Label(tanggal);
        lblTanggal.getStyleClass().add("status-text");

        statusBar.getChildren().addAll(lblAdmin, lblTanggal);
        root.setBottom(statusBar);
    }

    public void navigateTo(String page) {
        // Reset semua button style
        for (Button b : new Button[]{btnDashboard, btnAlatMusik, btnPelanggan,
                btnTransaksiMinus, btnPengembalian, btnRiwayat, btnLaporan}) {
            b.getStyleClass().removeAll("sidebar-btn-active");
        }

        contentArea.getChildren().clear();

        switch (page) {
            case "dashboard" -> {
                btnDashboard.getStyleClass().add("sidebar-btn-active");
                if (dashboardView == null) dashboardView = new DashboardView(this);
                else dashboardView.refresh();
                contentArea.getChildren().add(dashboardView.getRoot());
            }
            case "alatmusik" -> {
                btnAlatMusik.getStyleClass().add("sidebar-btn-active");
                if (alatMusikView == null) alatMusikView = new AlatMusikView();
                else alatMusikView.refresh();
                contentArea.getChildren().add(alatMusikView.getRoot());
            }
            case "pelanggan" -> {
                btnPelanggan.getStyleClass().add("sidebar-btn-active");
                if (pelangganView == null) pelangganView = new PelangganView();
                else pelangganView.refresh();
                contentArea.getChildren().add(pelangganView.getRoot());
            }
            case "transaksi" -> {
                btnTransaksiMinus.getStyleClass().add("sidebar-btn-active");
                transaksiBaruView = new TransaksiBaruView(this);
                contentArea.getChildren().add(transaksiBaruView.getRoot());
            }
            case "pengembalian" -> {
                btnPengembalian.getStyleClass().add("sidebar-btn-active");
                pengembalianView = new PengembalianView();
                contentArea.getChildren().add(pengembalianView.getRoot());
            }
            case "riwayat" -> {
                btnRiwayat.getStyleClass().add("sidebar-btn-active");
                if (riwayatView == null) riwayatView = new RiwayatView();
                else riwayatView.refresh();
                contentArea.getChildren().add(riwayatView.getRoot());
            }
            case "laporan" -> {
                btnLaporan.getStyleClass().add("sidebar-btn-active");
                if (laporanView == null) laporanView = new LaporanView();
                contentArea.getChildren().add(laporanView.getRoot());
            }
        }
    }

    public BorderPane getRoot() {
        return root;
    }
}
