package com.muse.controller;

import com.muse.model.AlatMusik;
import com.muse.model.Pelanggan;
import com.muse.service.AlatMusikService;
import com.muse.service.PelangganService;
import com.muse.service.TransaksiService;
import com.muse.util.CurrencyFormatter;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

/**
 * Controller untuk TransaksiBaru.fxml
 * Pilih pelanggan, alat, tanggal, durasi → tampilkan ringkasan → simpan
 */
public class TransaksiBaruController implements Initializable {

    @FXML private ComboBox<Pelanggan>  cbPelanggan;
    @FXML private ComboBox<AlatMusik>  cbAlat;
    @FXML private DatePicker           dpTanggalPinjam;
    @FXML private TextField            tfDurasi;
    @FXML private TextArea             taCatatan;
    @FXML private Label                lblError;

    // Ringkasan
    @FXML private Label lblRingPelanggan;
    @FXML private Label lblRingAlat;
    @FXML private Label lblRingHarga;
    @FXML private Label lblRingDurasi;
    @FXML private Label lblRingKembali;
    @FXML private Label lblRingTotal;

    private MainController mainController;

    private final PelangganService pelangganService = new PelangganService();
    private final AlatMusikService alatMusikService = new AlatMusikService();
    private final TransaksiService transaksiService = new TransaksiService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Isi ComboBox Pelanggan
        cbPelanggan.getItems().addAll(pelangganService.getAllPelanggan());
        cbPelanggan.setConverter(new StringConverter<>() {
            @Override public String toString(Pelanggan p)    { return p == null ? "" : p.getNama() + " - " + p.getKontak(); }
            @Override public Pelanggan fromString(String s)  { return null; }
        });

        // Isi ComboBox Alat (hanya yang tersedia)
        cbAlat.getItems().addAll(alatMusikService.getAlatTersedia());
        cbAlat.setConverter(new StringConverter<>() {
            @Override public String toString(AlatMusik a)   { return a == null ? "" : a.getNama() + " (" + a.getJenis() + ")"; }
            @Override public AlatMusik fromString(String s) { return null; }
        });

        dpTanggalPinjam.setValue(LocalDate.now());

        // Listener update ringkasan
        cbPelanggan.setOnAction(e -> updateRingkasan());
        cbAlat.setOnAction(e -> updateRingkasan());
        dpTanggalPinjam.setOnAction(e -> updateRingkasan());
        tfDurasi.textProperty().addListener((obs, o, n) -> updateRingkasan());

        updateRingkasan();
    }

    public void setMainController(MainController mc) {
        this.mainController = mc;
    }

    private void updateRingkasan() {
        Pelanggan  p       = cbPelanggan.getValue();
        AlatMusik  a       = cbAlat.getValue();
        LocalDate  tgl     = dpTanggalPinjam.getValue();
        String     durStr  = tfDurasi.getText().trim();

        lblRingPelanggan.setText("Pelanggan: " + (p != null ? p.getNama() : "-"));
        lblRingAlat.setText("Alat: " + (a != null ? a.getNama() : "-"));
        lblRingHarga.setText("Harga/hari: " + (a != null ? CurrencyFormatter.formatSimple(a.getHargaSewa()) : "-"));

        try {
            int dur = Integer.parseInt(durStr);
            if (dur <= 0) throw new NumberFormatException();
            lblRingDurasi.setText("Durasi: " + dur + " hari");
            if (tgl != null) {
                lblRingKembali.setText("Tgl Kembali: " + tgl.plusDays(dur));
                if (a != null) {
                    lblRingTotal.setText(CurrencyFormatter.formatSimple(dur * a.getHargaSewa()));
                }
            }
        } catch (NumberFormatException ex) {
            lblRingDurasi.setText("Durasi: -");
            lblRingKembali.setText("Tgl Kembali: -");
            lblRingTotal.setText("-");
        }
    }

    @FXML
    private void onPelangganChanged() { updateRingkasan(); }

    @FXML
    private void onAlatChanged() { updateRingkasan(); }

    @FXML
    private void onTanggalChanged() { updateRingkasan(); }

    @FXML
    private void onDurasiChanged() { updateRingkasan(); }

    @FXML
    private void onSimpan() {
        Pelanggan p   = cbPelanggan.getValue();
        AlatMusik a   = cbAlat.getValue();
        LocalDate tgl = dpTanggalPinjam.getValue();
        String durStr = tfDurasi.getText().trim();

        if (p == null || a == null || tgl == null || durStr.isEmpty()) {
            showError("Semua field wajib harus diisi.");
            return;
        }

        int durasi;
        try {
            durasi = Integer.parseInt(durStr);
            if (durasi <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showError("Durasi sewa harus berupa angka positif.");
            return;
        }

        boolean ok = transaksiService.buatTransaksi(p, a, tgl, durasi);
        if (ok) {
            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setTitle("Berhasil");
            info.setHeaderText(null);
            info.setContentText("Transaksi berhasil disimpan!\n" +
                "Alat: " + a.getNama() + "\n" +
                "Pelanggan: " + p.getNama() + "\n" +
                "Tgl Kembali: " + tgl.plusDays(durasi));
            info.showAndWait();
            if (mainController != null) mainController.navigateTo("Dashboard");
        } else {
            showError("Gagal menyimpan transaksi. Pastikan alat masih tersedia.");
        }
    }

    @FXML
    private void onReset() {
        cbPelanggan.setValue(null);
        cbAlat.getItems().setAll(alatMusikService.getAlatTersedia());
        cbAlat.setValue(null);
        dpTanggalPinjam.setValue(LocalDate.now());
        tfDurasi.setText("1");
        taCatatan.clear();
        hideError();
        updateRingkasan();
    }

    @FXML
    private void onBatal() {
        if (mainController != null) mainController.navigateTo("Dashboard");
    }

    private void showError(String msg) {
        lblError.setText(msg);
        lblError.setVisible(true);
        lblError.setManaged(true);
    }

    private void hideError() {
        lblError.setVisible(false);
        lblError.setManaged(false);
    }
}
