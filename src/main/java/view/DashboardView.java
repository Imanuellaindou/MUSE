package view;

import com.muse.model.Transaksi;
import com.muse.service.AlatMusikService;
import com.muse.service.DendaService;
import com.muse.service.TransaksiService;
import com.muse.util.CurrencyFormatter;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Halaman Dashboard
 * Menampilkan 4 kartu statistik real-time dan tabel transaksi aktif terkini.
 * Notifikasi keterlambatan berwarna kuning jika ada.
 */
public class DashboardView {

    private final MainView mainView;
    private VBox root;

    private final AlatMusikService alatMusikService = new AlatMusikService();
    private final TransaksiService transaksiService = new TransaksiService();
    private final DendaService dendaService = new DendaService();

    public DashboardView(MainView mainView) {
        this.mainView = mainView;
        build();
    }

    private void build() {
        root = new VBox(16);
        root.setPadding(new Insets(0));
        VBox.setVgrow(root, Priority.ALWAYS);
        refresh();
    }

    public void refresh() {
        root.getChildren().clear();

        // Judul
        Label title = new Label("Dashboard");
        title.getStyleClass().add("page-title");

        // Notifikasi keterlambatan
        int dendaAktif = dendaService.countDendaAktif();
        VBox notifBox = new VBox();
        if (dendaAktif > 0) {
            HBox notif = new HBox(8);
            notif.getStyleClass().add("notif-warning");
            notif.setAlignment(Pos.CENTER_LEFT);
            Label notifText = new Label("⚠ " + dendaAktif + " alat melewati batas waktu pengembalian. Segera proses pengembalian.");
            notifText.getStyleClass().add("notif-warning-text");
            notif.getChildren().add(notifText);
            notifBox.getChildren().add(notif);
        }

        // Kartu statistik
        HBox statsRow = new HBox(16);
        statsRow.setAlignment(Pos.CENTER_LEFT);

        int totalAlat = alatMusikService.getTotalAlat();
        int tersedia = alatMusikService.getTotalTersedia();
        int disewa = alatMusikService.getTotalDisewa();

        statsRow.getChildren().addAll(
            statCard(String.valueOf(totalAlat), "Total Alat", "stat-number-blue"),
            statCard(String.valueOf(tersedia), "Tersedia", "stat-number-green"),
            statCard(String.valueOf(disewa), "Sedang Disewa", "stat-number-orange"),
            statCard(String.valueOf(dendaAktif), "Denda Aktif", "stat-number-red")
        );

        // Tabel transaksi aktif
        Label tblTitle = new Label("Transaksi Aktif Terkini");
        tblTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #374151;");

        TableView<Transaksi> table = buildTable();
        List<Transaksi> aktif = transaksiService.getTransaksiAktif();
        table.getItems().addAll(aktif);
        VBox.setVgrow(table, Priority.ALWAYS);

        Label countLabel = new Label("Total transaksi aktif: " + aktif.size());
        countLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280;");

        // Tombol shortcut
        HBox shortcuts = new HBox(12);
        Button btnTransaksi = new Button("+ Transaksi Baru");
        btnTransaksi.getStyleClass().add("btn-primary");
        btnTransaksi.setOnAction(e -> mainView.navigateTo("transaksi"));

        Button btnKembali = new Button("↩ Proses Pengembalian");
        btnKembali.getStyleClass().add("btn-secondary");
        btnKembali.setOnAction(e -> mainView.navigateTo("pengembalian"));

        shortcuts.getChildren().addAll(btnTransaksi, btnKembali);

        root.getChildren().addAll(title, notifBox, statsRow, tblTitle, table, countLabel, shortcuts);
    }

    private VBox statCard(String number, String label, String numberStyle) {
        VBox card = new VBox(4);
        card.getStyleClass().add("stat-card");
        card.setAlignment(Pos.CENTER);

        Label numLabel = new Label(number);
        numLabel.getStyleClass().addAll("stat-number", numberStyle);

        Label textLabel = new Label(label);
        textLabel.getStyleClass().add("stat-label");

        card.getChildren().addAll(numLabel, textLabel);
        return card;
    }

    private TableView<Transaksi> buildTable() {
        TableView<Transaksi> table = new TableView<>();
        table.getStyleClass().add("table-view");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("Tidak ada transaksi aktif."));

        TableColumn<Transaksi, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Integer val, boolean empty) {
                super.updateItem(val, empty);
                setText(empty || val == null ? null : String.format("TRX-%03d", val));
            }
        });
        colId.setPrefWidth(80);

        TableColumn<Transaksi, String> colPelanggan = new TableColumn<>("Pelanggan");
        colPelanggan.setCellValueFactory(new PropertyValueFactory<>("namaPelanggan"));

        TableColumn<Transaksi, String> colAlat = new TableColumn<>("Alat Musik");
        colAlat.setCellValueFactory(new PropertyValueFactory<>("namaAlat"));

        TableColumn<Transaksi, LocalDate> colKembali = new TableColumn<>("Tgl Kembali");
        colKembali.setCellValueFactory(new PropertyValueFactory<>("tanggalKembali"));
        colKembali.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(LocalDate val, boolean empty) {
                super.updateItem(val, empty);
                setText(empty || val == null ? null : val.toString());
            }
        });

        TableColumn<Transaksi, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) { setText(null); setGraphic(null); return; }
                Label badge = new Label(val.toUpperCase());
                badge.getStyleClass().add("badge");
                badge.getStyleClass().add(getBadgeClass(val));
                setGraphic(badge);
                setText(null);
            }
        });

        table.getColumns().addAll(colId, colPelanggan, colAlat, colKembali, colStatus);
        return table;
    }

    private String getBadgeClass(String status) {
        return switch (status.toLowerCase()) {
            case "aktif" -> "badge-aktif";
            case "selesai" -> "badge-selesai";
            case "terlambat" -> "badge-terlambat";
            default -> "badge-selesai";
        };
    }

    public VBox getRoot() { return root; }
}
