package com.muse.view;

import com.muse.model.Transaksi;
import com.muse.service.RiwayatService;
import com.muse.util.CurrencyFormatter;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

/**
 * Halaman Riwayat Transaksi
 * Filter: teks, tanggal, status. Export CSV.
 */
public class RiwayatView {

    private VBox root;
    private TableView<Transaksi> table;
    private TextField tfSearch;
    private ComboBox<String> cbStatus;
    private DatePicker dpDari, dpSampai;
    private Label lblTotal;

    private final RiwayatService riwayatService = new RiwayatService();
    private List<Transaksi> currentData;

    public RiwayatView() {
        build();
        loadData();
    }

    private void build() {
        root = new VBox(12);
        root.setPadding(new Insets(0));
        VBox.setVgrow(root, Priority.ALWAYS);

        // Filter bar
        HBox filterBar = new HBox(8);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        filterBar.setStyle("-fx-padding: 0 0 4 0;");

        Label lblFilter = new Label("Filter:");
        lblFilter.setStyle("-fx-font-size: 12px; -fx-text-fill: #374151;");

        tfSearch = new TextField();
        tfSearch.setPromptText("Cari pelanggan / alat...");
        tfSearch.getStyleClass().add("search-field");
        tfSearch.setPrefWidth(200);

        cbStatus = new ComboBox<>();
        cbStatus.getItems().addAll("Semua Status", "Aktif", "Selesai", "Terlambat");
        cbStatus.setValue("Semua Status");
        cbStatus.getStyleClass().add("combo-box");
        cbStatus.setPrefWidth(130);

        Label lblDari = new Label("Dari:");
        lblDari.setStyle("-fx-font-size: 12px;");
        dpDari = new DatePicker();
        dpDari.setPromptText("dd/mm/yyyy");
        dpDari.setPrefWidth(140);

        Label lblSampai = new Label("Sampai:");
        lblSampai.setStyle("-fx-font-size: 12px;");
        dpSampai = new DatePicker();
        dpSampai.setPromptText("dd/mm/yyyy");
        dpSampai.setPrefWidth(140);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnExport = new Button("▣ Export CSV");
        btnExport.getStyleClass().add("btn-secondary");
        btnExport.setOnAction(e -> exportCSV());

        filterBar.getChildren().addAll(lblFilter, tfSearch, cbStatus,
                lblDari, dpDari, lblSampai, dpSampai, spacer, btnExport);

        // Title
        Label title = new Label("Riwayat Transaksi");
        title.getStyleClass().add("page-title");

        // Table
        table = buildTable();
        VBox.setVgrow(table, Priority.ALWAYS);

        lblTotal = new Label();
        lblTotal.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280;");

        // Events
        tfSearch.textProperty().addListener((obs, o, n) -> loadData());
        cbStatus.setOnAction(e -> loadData());
        dpDari.setOnAction(e -> loadData());
        dpSampai.setOnAction(e -> loadData());

        root.getChildren().addAll(title, filterBar, table, lblTotal);
    }

    private TableView<Transaksi> buildTable() {
        TableView<Transaksi> tv = new TableView<>();
        tv.getStyleClass().add("table-com.muse.view");
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tv.setPlaceholder(new Label("Tidak ada data transaksi."));

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

        TableColumn<Transaksi, LocalDate> colPinjam = new TableColumn<>("Tgl Pinjam");
        colPinjam.setCellValueFactory(new PropertyValueFactory<>("tanggalPinjam"));
        colPinjam.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(LocalDate val, boolean empty) {
                super.updateItem(val, empty);
                setText(empty || val == null ? null : val.toString());
            }
        });
        colPinjam.setPrefWidth(100);

        TableColumn<Transaksi, LocalDate> colKembali = new TableColumn<>("Tgl Kembali");
        colKembali.setCellValueFactory(new PropertyValueFactory<>("tanggalKembali"));
        colKembali.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(LocalDate val, boolean empty) {
                super.updateItem(val, empty);
                setText(empty || val == null ? null : val.toString());
            }
        });
        colKembali.setPrefWidth(100);

        TableColumn<Transaksi, Double> colDenda = new TableColumn<>("Denda");
        colDenda.setCellValueFactory(new PropertyValueFactory<>("jumlahDenda"));
        colDenda.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) { setText(null); return; }
                if (val > 0) {
                    setText(CurrencyFormatter.formatSimple(val));
                    setStyle("-fx-text-fill: #DC2626; -fx-font-weight: bold;");
                } else {
                    setText("—");
                    setStyle("-fx-text-fill: #9CA3AF;");
                }
            }
        });
        colDenda.setPrefWidth(100);

        TableColumn<Transaksi, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) { setGraphic(null); return; }
                Label badge = new Label(val.substring(0,1).toUpperCase() + val.substring(1));
                badge.getStyleClass().add("badge");
                badge.getStyleClass().add(switch(val.toLowerCase()) {
                    case "aktif" -> "badge-aktif";
                    case "terlambat" -> "badge-terlambat";
                    default -> "badge-selesai";
                });
                setGraphic(badge); setText(null);
            }
        });
        colStatus.setPrefWidth(90);

        tv.getColumns().addAll(colId, colPelanggan, colAlat, colPinjam, colKembali, colDenda, colStatus);
        return tv;
    }

    public void refresh() { loadData(); }

    private void loadData() {
        String keyword = tfSearch.getText().trim();
        String status = cbStatus.getValue().equals("Semua Status") ? null : cbStatus.getValue();
        LocalDate dari = dpDari.getValue();
        LocalDate sampai = dpSampai.getValue();

        currentData = riwayatService.getRiwayat(keyword, status, dari, sampai);
        table.getItems().setAll(currentData);
        lblTotal.setText("Menampilkan " + currentData.size() + " dari total transaksi");
    }

    private void exportCSV() {
        if (currentData == null || currentData.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Tidak ada data untuk diekspor.").showAndWait();
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Simpan File CSV");
        chooser.setInitialFileName("riwayat_transaksi.csv");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV File", "*.csv"));
        File file = chooser.showSaveDialog(root.getScene().getWindow());
        if (file != null) {
            boolean ok = riwayatService.exportCSV(currentData, file.getAbsolutePath());
            if (ok) {
                new Alert(Alert.AlertType.INFORMATION, "Data berhasil diekspor ke:\n" + file.getAbsolutePath()).showAndWait();
            } else {
                new Alert(Alert.AlertType.ERROR, "Gagal mengekspor data.").showAndWait();
            }
        }
    }

    public VBox getRoot() { return root; }
}
