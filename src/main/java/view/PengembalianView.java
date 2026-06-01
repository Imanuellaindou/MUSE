package view;

import com.muse.model.Transaksi;
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
 * Halaman Pengembalian Alat
 * Panel kiri: daftar transaksi aktif/terlambat
 * Panel kanan: detail + kalkulasi denda
 * Konfirmasi kembali → status selesai, alat tersedia, denda tersimpan
 */
public class PengembalianView {

    private VBox root;
    private TableView<Transaksi> tableAktif;
    private VBox panelDetail;
    private TextField tfSearch;

    private Label lblDetPelanggan, lblDetAlat, lblDetPinjam,
                   lblDetKembali, lblDetAktual;
    private VBox dendaBox;
    private Label lblDendaHari, lblDendaTarif, lblDendaTotal;
    private Button btnKonfirmasi;

    private Transaksi selectedTransaksi;

    private final TransaksiService transaksiService = new TransaksiService();
    private final DendaService dendaService = new DendaService();

    public PengembalianView() {
        build();
        loadAktif();
    }

    private void build() {
        root = new VBox(12);
        root.setPadding(new Insets(0));
        VBox.setVgrow(root, Priority.ALWAYS);

        Label title = new Label("Proses Pengembalian Alat");
        title.getStyleClass().add("page-title");

        // Search bar
        HBox searchBar = new HBox(8);
        searchBar.setAlignment(Pos.CENTER_LEFT);
        tfSearch = new TextField();
        tfSearch.setPromptText("Cari ID transaksi / nama pelanggan...");
        tfSearch.getStyleClass().add("search-field");
        tfSearch.setPrefWidth(300);
        Button btnCari = new Button("Cari");
        btnCari.getStyleClass().add("btn-secondary");
        searchBar.getChildren().addAll(tfSearch, btnCari);

        // Main content: 2 panel
        HBox content = new HBox(16);
        VBox.setVgrow(content, Priority.ALWAYS);

        VBox leftPanel = buildLeftPanel();
        HBox.setHgrow(leftPanel, Priority.ALWAYS);

        panelDetail = buildDetailPanel();
        panelDetail.setMinWidth(320);
        panelDetail.setMaxWidth(340);

        content.getChildren().addAll(leftPanel, panelDetail);

        // Events
        btnCari.setOnAction(e -> filterAktif());
        tfSearch.textProperty().addListener((obs, o, n) -> filterAktif());

        root.getChildren().addAll(title, searchBar, content);
    }

    private VBox buildLeftPanel() {
        VBox pane = new VBox(8);

        Label subTitle = new Label("Transaksi Aktif / Terlambat:");
        subTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #374151;");

        tableAktif = new TableView<>();
        tableAktif.getStyleClass().add("table-view");
        tableAktif.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableAktif.setPlaceholder(new Label("Tidak ada transaksi aktif."));
        VBox.setVgrow(tableAktif, Priority.ALWAYS);

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

        TableColumn<Transaksi, String> colAlat = new TableColumn<>("Alat");
        colAlat.setCellValueFactory(new PropertyValueFactory<>("namaAlat"));
        colAlat.setPrefWidth(80);

        TableColumn<Transaksi, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) { setGraphic(null); return; }
                Label badge = new Label(val.toUpperCase());
                badge.getStyleClass().add("badge");
                badge.getStyleClass().add("terlambat".equalsIgnoreCase(val) ? "badge-terlambat" : "badge-aktif");
                setGraphic(badge); setText(null);
            }
        });
        colStatus.setPrefWidth(90);

        tableAktif.getColumns().addAll(colId, colPelanggan, colAlat, colStatus);

        // Klik baris → tampilkan detail
        tableAktif.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) showDetail(n);
        });

        pane.getChildren().addAll(subTitle, tableAktif);
        return pane;
    }

    private VBox buildDetailPanel() {
        VBox pane = new VBox(10);
        pane.getStyleClass().add("form-card");
        pane.setVisible(false);

        Label detTitle = new Label("▼ Detail");
        detTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #374151;");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(8);

        lblDetPelanggan = detRow(grid, "Pelanggan", "-", 0);
        lblDetAlat = detRow(grid, "Alat Musik", "-", 1);
        lblDetPinjam = detRow(grid, "Tgl Pinjam", "-", 2);
        lblDetKembali = detRow(grid, "Tgl Kembali", "-", 3);
        lblDetAktual = detRow(grid, "Tgl Aktual", LocalDate.now().toString(), 4);
        lblDetAktual.setStyle("-fx-font-weight: bold; -fx-text-fill: #DC2626;");

        // Denda box (tersembunyi jika tidak ada)
        dendaBox = new VBox(6);
        dendaBox.getStyleClass().add("denda-box");
        dendaBox.setVisible(false);
        dendaBox.setManaged(false);

        Label lblDendaTitle = new Label("⚠ DENDA KETERLAMBATAN");
        lblDendaTitle.getStyleClass().add("denda-title");

        GridPane dendaGrid = new GridPane();
        dendaGrid.setHgap(12);
        dendaGrid.setVgap(6);
        lblDendaHari = new Label("-");
        lblDendaTarif = new Label("-");
        lblDendaTotal = new Label("-");
        lblDendaTotal.getStyleClass().add("denda-total");

        dendaGrid.add(new Label("Hari terlambat"), 0, 0);
        dendaGrid.add(lblDendaHari, 1, 0);
        dendaGrid.add(new Label("Tarif denda/hari"), 0, 1);
        dendaGrid.add(lblDendaTarif, 1, 1);

        HBox totalDendaRow = new HBox();
        Label lblTDKey = new Label("Total Denda");
        lblTDKey.getStyleClass().add("denda-title");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        totalDendaRow.getChildren().addAll(lblTDKey, sp, lblDendaTotal);

        dendaBox.getChildren().addAll(lblDendaTitle, dendaGrid, new Separator(), totalDendaRow);

        // Tombol konfirmasi
        btnKonfirmasi = new Button("✔ Konfirmasi Kembali");
        btnKonfirmasi.getStyleClass().add("btn-primary");
        btnKonfirmasi.setPrefWidth(Double.MAX_VALUE);
        btnKonfirmasi.setOnAction(e -> konfirmasiKembali());

        Button btnBatal = new Button("✕ Batal");
        btnBatal.getStyleClass().add("btn-secondary");
        btnBatal.setPrefWidth(Double.MAX_VALUE);
        btnBatal.setOnAction(e -> {
            tableAktif.getSelectionModel().clearSelection();
            pane.setVisible(false);
        });

        pane.getChildren().addAll(detTitle, grid, dendaBox, btnKonfirmasi, btnBatal);
        return pane;
    }

    private Label detRow(GridPane grid, String key, String val, int row) {
        Label kLabel = new Label(key);
        kLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280;");
        Label vLabel = new Label(val);
        vLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #111827; -fx-font-weight: bold;");
        grid.add(kLabel, 0, row);
        grid.add(vLabel, 1, row);
        return vLabel;
    }

    private void showDetail(Transaksi t) {
        selectedTransaksi = t;
        panelDetail.setVisible(true);

        lblDetPelanggan.setText(t.getNamaPelanggan());
        lblDetAlat.setText(t.getNamaAlat());
        lblDetPinjam.setText(t.getTanggalPinjam() != null ? t.getTanggalPinjam().toString() : "-");
        lblDetKembali.setText(t.getTanggalKembali() != null ? t.getTanggalKembali().toString() : "-");

        LocalDate today = LocalDate.now();
        lblDetAktual.setText(today + " (hari ini)");

        // Cek denda
        int hariTerlambat = dendaService.hitungHariTerlambat(t.getTanggalKembali(), today);
        if (hariTerlambat > 0) {
            double totalDenda = dendaService.hitungDenda(t.getTanggalKembali(), today);
            lblDendaHari.setText(hariTerlambat + " hari");
            lblDendaTarif.setText(CurrencyFormatter.formatSimple(com.muse.model.Denda.TARIF_DENDA_PER_HARI));
            lblDendaTotal.setText(CurrencyFormatter.formatSimple(totalDenda));
            dendaBox.setVisible(true);
            dendaBox.setManaged(true);
        } else {
            dendaBox.setVisible(false);
            dendaBox.setManaged(false);
        }
    }

    private void konfirmasiKembali() {
        if (selectedTransaksi == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Konfirmasi Pengembalian");
        confirm.setHeaderText("Konfirmasi pengembalian alat?");
        confirm.setContentText("Alat: " + selectedTransaksi.getNamaAlat() +
                "\nPelanggan: " + selectedTransaksi.getNamaPelanggan());
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                boolean ok = transaksiService.prosesKembali(selectedTransaksi, LocalDate.now());
                if (ok) {
                    Alert info = new Alert(Alert.AlertType.INFORMATION);
                    info.setTitle("Berhasil");
                    info.setHeaderText(null);
                    info.setContentText("Pengembalian berhasil diproses.\nAlat kembali tersedia.");
                    info.showAndWait();
                    selectedTransaksi = null;
                    panelDetail.setVisible(false);
                    loadAktif();
                } else {
                    new Alert(Alert.AlertType.ERROR, "Gagal memproses pengembalian.").showAndWait();
                }
            }
        });
    }

    private void loadAktif() {
        List<Transaksi> data = transaksiService.getTransaksiAktif();
        tableAktif.getItems().setAll(data);
    }

    private void filterAktif() {
        String kw = tfSearch.getText().trim().toLowerCase();
        List<Transaksi> all = transaksiService.getTransaksiAktif();
        if (kw.isEmpty()) {
            tableAktif.getItems().setAll(all);
        } else {
            tableAktif.getItems().setAll(all.stream()
                .filter(t -> t.getNamaPelanggan().toLowerCase().contains(kw)
                          || String.format("trx-%03d", t.getId()).contains(kw))
                .toList());
        }
    }

    public VBox getRoot() { return root; }
}
