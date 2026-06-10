package com.muse.controller;

import com.muse.model.Transaksi;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import service.RiwayatService;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller untuk Riwayat.fxml
 * Filter kombinasi + export CSV
 */

public class RiwayatController {
    @FXML
    private TextField tfSearch;
    @FXML private ComboBox<String> cbStatus;
    @FXML private DatePicker dpDari;
    @FXML private DatePicker       dpSampai;
    @FXML private Label lblTotal;

    @FXML private TableView<Transaksi> tableRiwayat;
    @FXML private TableColumn<Transaksi, Integer>  colId;
    @FXML private TableColumn<Transaksi, String>   colPelanggan;
    @FXML private TableColumn<Transaksi, String>   colAlat;
    @FXML private TableColumn<Transaksi, LocalDate> colPinjam;
    @FXML private TableColumn<Transaksi, LocalDate> colKembali;
    @FXML private TableColumn<Transaksi, Double>   colDenda;
    @FXML private TableColumn<Transaksi, String>   colStatus;

    private final RiwayatService riwayatService = new RiwayatService();
    private List<Transaksi> currentData;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cbStatus.getItems().addAll("Semua Status", "Aktif", "Selesai", "Terlambat");
        cbStatus.setValue("Semua Status");
        setupTable();
        onFilter();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Integer val, boolean empty) {
                super.updateItem(val, empty);
                setText(empty || val == null ? null : String.format("TRX-%03d", val));
            }
        });

        colPelanggan.setCellValueFactory(new PropertyValueFactory<>("namaPelanggan"));
        colAlat.setCellValueFactory(new PropertyValueFactory<>("namaAlat"));

        colPinjam.setCellValueFactory(new PropertyValueFactory<>("tanggalPinjam"));
        colPinjam.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(LocalDate val, boolean empty) {
                super.updateItem(val, empty);
                setText(empty || val == null ? null : val.toString());
            }
        });

        colKembali.setCellValueFactory(new PropertyValueFactory<>("tanggalKembali"));
        colKembali.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(LocalDate val, boolean empty) {
                super.updateItem(val, empty);
                setText(empty || val == null ? null : val.toString());
            }
        });

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

        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) { setGraphic(null); return; }
                Label badge = new Label(val.substring(0,1).toUpperCase() + val.substring(1));
                badge.getStyleClass().add("badge");
                badge.getStyleClass().add(switch (val.toLowerCase()) {
                    case "aktif"     -> "badge-aktif";
                    case "terlambat" -> "badge-terlambat";
                    default          -> "badge-selesai";
                });
                setGraphic(badge); setText(null);
            }
        });
    }

    @FXML
    private void onFilter() {
        String keyword = tfSearch.getText().trim();
        String status  = cbStatus.getValue().equals("Semua Status") ? null : cbStatus.getValue();
        LocalDate dari   = dpDari.getValue();
        LocalDate sampai = dpSampai.getValue();

        currentData = riwayatService.getRiwayat(keyword, status, dari, sampai);
        tableRiwayat.getItems().setAll(currentData);
        lblTotal.setText("Menampilkan " + currentData.size() + " transaksi");
    }

    @FXML
    private void onExportCSV() {
        if (currentData == null || currentData.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Tidak ada data untuk diekspor.").showAndWait();
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Simpan File CSV");
        chooser.setInitialFileName("riwayat_transaksi.csv");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV File", "*.csv"));
        File file = chooser.showSaveDialog(tableRiwayat.getScene().getWindow());
        if (file != null) {
            boolean ok = riwayatService.exportCSV(currentData, file.getAbsolutePath());
            if (ok)
                new Alert(Alert.AlertType.INFORMATION,
                        "Data berhasil diekspor ke:\n" + file.getAbsolutePath()).showAndWait();
            else
                new Alert(Alert.AlertType.ERROR, "Gagal mengekspor data.").showAndWait();
        }
    }
}
