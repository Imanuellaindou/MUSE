package com.muse.controller;

import com.muse.model.Transaksi;
import com.muse.service.DendaService;
import com.muse.service.TransaksiService;
import com.muse.util.CurrencyFormatter;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller untuk Pengembalian.fxml
 * Panel kiri daftar aktif, panel kanan detail + denda
 */
public class PengembalianController implements Initializable {

    @FXML private TextField tfSearch;

    @FXML private TableView<Transaksi>            tableAktif;
    @FXML private TableColumn<Transaksi, Integer> colId;
    @FXML private TableColumn<Transaksi, String>  colPelanggan;
    @FXML private TableColumn<Transaksi, String>  colAlat;
    @FXML private TableColumn<Transaksi, String>  colStatus;

    @FXML private VBox  panelDetail;
    @FXML private Label lblDetPelanggan;
    @FXML private Label lblDetAlat;
    @FXML private Label lblDetPinjam;
    @FXML private Label lblDetKembali;
    @FXML private Label lblDetAktual;

    @FXML private VBox  dendaBox;
    @FXML private Label lblDendaHari;
    @FXML private Label lblDendaTarif;
    @FXML private Label lblDendaTotal;

    @FXML private Button btnKonfirmasi;

    private Transaksi selectedTransaksi;

    private final TransaksiService transaksiService = new TransaksiService();
    private final DendaService     dendaService     = new DendaService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        loadAktif();
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

        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) { setGraphic(null); return; }
                Label badge = new Label(val.toUpperCase());
                badge.getStyleClass().add("badge");
                badge.getStyleClass().add(
                    "terlambat".equalsIgnoreCase(val) ? "badge-terlambat" : "badge-aktif");
                setGraphic(badge); setText(null);
            }
        });
    }

    private void loadAktif() {
        List<Transaksi> data = transaksiService.getTransaksiAktif();
        tableAktif.getItems().setAll(data);
    }

    @FXML
    private void onRowSelected(MouseEvent event) {
        Transaksi t = tableAktif.getSelectionModel().getSelectedItem();
        if (t != null) showDetail(t);
    }

    @FXML
    private void onSearch() {
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

    private void showDetail(Transaksi t) {
        selectedTransaksi = t;
        panelDetail.setVisible(true);
        panelDetail.setManaged(true);

        lblDetPelanggan.setText(t.getNamaPelanggan());
        lblDetAlat.setText(t.getNamaAlat());
        lblDetPinjam.setText(t.getTanggalPinjam() != null ? t.getTanggalPinjam().toString() : "-");
        lblDetKembali.setText(t.getTanggalKembali() != null ? t.getTanggalKembali().toString() : "-");

        LocalDate today = LocalDate.now();
        lblDetAktual.setText(today + " (hari ini)");

        // Kalkulasi denda
        int hari = dendaService.hitungHariTerlambat(t.getTanggalKembali(), today);
        if (hari > 0) {
            double total = dendaService.hitungDenda(t.getTanggalKembali(), today);
            lblDendaHari.setText(hari + " hari");
            lblDendaTarif.setText(CurrencyFormatter.formatSimple(
                com.muse.model.Denda.TARIF_DENDA_PER_HARI));
            lblDendaTotal.setText(CurrencyFormatter.formatSimple(total));
            dendaBox.setVisible(true);
            dendaBox.setManaged(true);
        } else {
            dendaBox.setVisible(false);
            dendaBox.setManaged(false);
        }
    }

    @FXML
    private void onKonfirmasi() {
        if (selectedTransaksi == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Konfirmasi Pengembalian");
        confirm.setHeaderText(null);
        confirm.setContentText("Konfirmasi pengembalian?\n" +
            "Alat: " + selectedTransaksi.getNamaAlat() + "\n" +
            "Pelanggan: " + selectedTransaksi.getNamaPelanggan());

        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                boolean ok = transaksiService.prosesKembali(selectedTransaksi, LocalDate.now());
                if (ok) {
                    new Alert(Alert.AlertType.INFORMATION,
                        "Pengembalian berhasil diproses.\nAlat kembali tersedia.")
                        .showAndWait();
                    selectedTransaksi = null;
                    panelDetail.setVisible(false);
                    panelDetail.setManaged(false);
                    loadAktif();
                } else {
                    new Alert(Alert.AlertType.ERROR,
                        "Gagal memproses pengembalian.").showAndWait();
                }
            }
        });
    }

    @FXML
    private void onBatal() {
        tableAktif.getSelectionModel().clearSelection();
        panelDetail.setVisible(false);
        panelDetail.setManaged(false);
        selectedTransaksi = null;
    }
}
