package com.muse.view;

import com.muse.model.AlatMusik;
import com.muse.model.Pelanggan;
import com.muse.service.AlatMusikService;
import com.muse.service.PelangganService;
import com.muse.service.TransaksiService;
import com.muse.util.CurrencyFormatter;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;

/**
 * Halaman Transaksi Penyewaan Baru
 * Admin memilih pelanggan, alat (tersedia), tanggal & durasi.
 * Panel ringkasan menampilkan tanggal kembali & total biaya otomatis.
 */
public class TransaksiBaruView {

    private final MainView mainView;
    private VBox root;

    private ComboBox<Pelanggan> cbPelanggan;
    private ComboBox<AlatMusik> cbAlat;
    private DatePicker dpTanggalPinjam;
    private TextField tfDurasi;
    private TextArea taCatatan;

    // Ringkasan
    private Label lblRingPelanggan, lblRingAlat, lblRingHarga,
                   lblRingDurasi, lblRingTanggalKembali, lblRingTotal;

    private final PelangganService pelangganService = new PelangganService();
    private final AlatMusikService alatMusikService = new AlatMusikService();
    private final TransaksiService transaksiService = new TransaksiService();

    public TransaksiBaruView(MainView mainView) {
        this.mainView = mainView;
        build();
    }

    private void build() {
        root = new VBox(12);
        root.setPadding(new Insets(0));

        Label title = new Label("Form Transaksi Penyewaan Baru");
        title.getStyleClass().add("page-title");

        // Layout utama: form kiri + ringkasan kanan
        HBox mainLayout = new HBox(20);
        VBox.setVgrow(mainLayout, Priority.ALWAYS);

        VBox formPane = buildFormPane();
        HBox.setHgrow(formPane, Priority.ALWAYS);

        VBox summaryPane = buildSummaryPane();
        summaryPane.setMinWidth(240);
        summaryPane.setMaxWidth(240);

        mainLayout.getChildren().addAll(formPane, summaryPane);

        root.getChildren().addAll(title, mainLayout);
    }

    private VBox buildFormPane() {
        VBox pane = new VBox(0);
        pane.getStyleClass().add("form-card");

        Label secTitle = new Label("▼ Data Penyewaan");
        secTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #374151; -fx-padding: 0 0 12 0;");

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(12);
        grid.setPadding(new Insets(12, 0, 16, 0));

        // Pelanggan
        Label lblP = new Label("Pelanggan *");
        lblP.getStyleClass().add("form-label");
        cbPelanggan = new ComboBox<>();
        cbPelanggan.setPromptText("-- Pilih Pelanggan --");
        cbPelanggan.getStyleClass().add("combo-box");
        cbPelanggan.setPrefWidth(240);
        cbPelanggan.getItems().addAll(pelangganService.getAllPelanggan());

        // Alat Musik
        Label lblA = new Label("Alat Musik * (hanya yang tersedia)");
        lblA.getStyleClass().add("form-label");
        cbAlat = new ComboBox<>();
        cbAlat.setPromptText("-- Pilih Alat --");
        cbAlat.getStyleClass().add("combo-box");
        cbAlat.setPrefWidth(240);
        cbAlat.getItems().addAll(alatMusikService.getAlatTersedia());

        // Tanggal Pinjam
        Label lblTgl = new Label("Tanggal Pinjam *");
        lblTgl.getStyleClass().add("form-label");
        dpTanggalPinjam = new DatePicker(LocalDate.now());
        dpTanggalPinjam.setPrefWidth(160);

        // Durasi
        Label lblDur = new Label("Durasi Sewa (hari) *");
        lblDur.getStyleClass().add("form-label");
        tfDurasi = new TextField("1");
        tfDurasi.setPrefWidth(100);
        tfDurasi.getStyleClass().add("form-field");

        // Catatan
        Label lblCat = new Label("Catatan (opsional)");
        lblCat.getStyleClass().add("form-label");
        taCatatan = new TextArea();
        taCatatan.setPromptText("Catatan tambahan...");
        taCatatan.setPrefRowCount(2);
        taCatatan.setPrefWidth(400);

        grid.add(lblP, 0, 0);
        grid.add(cbPelanggan, 1, 0);
        grid.add(lblA, 0, 1);
        grid.add(cbAlat, 1, 1);
        grid.add(lblTgl, 0, 2);
        grid.add(dpTanggalPinjam, 1, 2);
        grid.add(lblDur, 0, 3);
        grid.add(tfDurasi, 1, 3);
        grid.add(lblCat, 0, 4);
        grid.add(taCatatan, 1, 4);

        // Tombol
        HBox btnRow = new HBox(10);
        btnRow.setPadding(new Insets(8, 0, 0, 0));
        Button btnSimpan = new Button("✔ Simpan Transaksi");
        btnSimpan.getStyleClass().add("btn-primary");
        Button btnReset = new Button("↻ Reset Form");
        btnReset.getStyleClass().add("btn-secondary");
        Button btnBatal = new Button("✕ Batal");
        btnBatal.getStyleClass().add("btn-danger");
        btnRow.getChildren().addAll(btnSimpan, btnReset, btnBatal);

        // Events update ringkasan
        cbPelanggan.setOnAction(e -> updateRingkasan());
        cbAlat.setOnAction(e -> updateRingkasan());
        dpTanggalPinjam.setOnAction(e -> updateRingkasan());
        tfDurasi.textProperty().addListener((obs, o, n) -> updateRingkasan());

        btnSimpan.setOnAction(e -> simpanTransaksi());
        btnReset.setOnAction(e -> resetForm());
        btnBatal.setOnAction(e -> mainView.navigateTo("dashboard"));

        pane.getChildren().addAll(secTitle, grid, btnRow);
        return pane;
    }

    private VBox buildSummaryPane() {
        VBox pane = new VBox(8);
        pane.getStyleClass().add("summary-card");

        Label title = new Label("RINGKASAN BIAYA");
        title.getStyleClass().add("summary-title");

        lblRingPelanggan = summaryRow("Pelanggan", "-");
        lblRingAlat = summaryRow("Alat", "-");
        lblRingHarga = summaryRow("Harga/hari", "-");
        lblRingDurasi = summaryRow("Durasi", "-");
        lblRingTanggalKembali = summaryRow("Tgl Kembali", "-");

        Separator sep = new Separator();
        sep.setPadding(new Insets(4, 0, 4, 0));

        HBox totalRow = new HBox();
        totalRow.setAlignment(Pos.CENTER_LEFT);
        Label lblTotalKey = new Label("Total Biaya");
        lblTotalKey.getStyleClass().add("summary-total-label");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        lblRingTotal = new Label("-");
        lblRingTotal.getStyleClass().add("summary-total-value");
        totalRow.getChildren().addAll(lblTotalKey, sp, lblRingTotal);

        pane.getChildren().addAll(title, lblRingPelanggan, lblRingAlat, lblRingHarga,
                lblRingDurasi, lblRingTanggalKembali, sep, totalRow);
        return pane;
    }

    private Label summaryRow(String key, String val) {
        // Returns a container HBox but using Label as placeholder — override to HBox in full build
        Label lbl = new Label(key + ": " + val);
        lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #374151;");
        return lbl;
    }

    private void updateRingkasan() {
        Pelanggan p = cbPelanggan.getValue();
        AlatMusik a = cbAlat.getValue();
        String durStr = tfDurasi.getText().trim();
        LocalDate tglPinjam = dpTanggalPinjam.getValue();

        lblRingPelanggan.setText("Pelanggan: " + (p != null ? p.getNama() : "-"));
        lblRingAlat.setText("Alat: " + (a != null ? a.getNama() : "-"));
        lblRingHarga.setText("Harga/hari: " + (a != null ? CurrencyFormatter.formatSimple(a.getHargaSewa()) : "-"));

        try {
            int dur = Integer.parseInt(durStr);
            lblRingDurasi.setText("Durasi: " + dur + " hari");
            if (tglPinjam != null) {
                LocalDate tglKembali = tglPinjam.plusDays(dur);
                lblRingTanggalKembali.setText("Tgl Kembali: " + tglKembali);
                if (a != null) {
                    double total = dur * a.getHargaSewa();
                    lblRingTotal.setText(CurrencyFormatter.formatSimple(total));
                }
            }
        } catch (NumberFormatException ignored) {
            lblRingDurasi.setText("Durasi: -");
            lblRingTanggalKembali.setText("Tgl Kembali: -");
            lblRingTotal.setText("-");
        }
    }

    private void simpanTransaksi() {
        Pelanggan p = cbPelanggan.getValue();
        AlatMusik a = cbAlat.getValue();
        LocalDate tglPinjam = dpTanggalPinjam.getValue();
        String durStr = tfDurasi.getText().trim();

        if (p == null || a == null || tglPinjam == null || durStr.isEmpty()) {
            alert("Semua field wajib harus diisi.");
            return;
        }

        int durasi;
        try {
            durasi = Integer.parseInt(durStr);
            if (durasi <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            alert("Durasi sewa harus berupa angka positif.");
            return;
        }

        boolean ok = transaksiService.buatTransaksi(p, a, tglPinjam, durasi);
        if (ok) {
            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setTitle("Berhasil");
            info.setHeaderText(null);
            info.setContentText("Transaksi berhasil disimpan!\nAlat: " + a.getNama() +
                "\nPelanggan: " + p.getNama() +
                "\nTanggal Kembali: " + tglPinjam.plusDays(durasi));
            info.showAndWait();
            mainView.navigateTo("dashboard");
        } else {
            alert("Gagal menyimpan transaksi. Pastikan alat masih tersedia.");
        }
    }

    private void resetForm() {
        cbPelanggan.setValue(null);
        cbAlat.getItems().setAll(alatMusikService.getAlatTersedia());
        cbAlat.setValue(null);
        dpTanggalPinjam.setValue(LocalDate.now());
        tfDurasi.setText("1");
        taCatatan.clear();
        updateRingkasan();
    }

    private void alert(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    public VBox getRoot() { return root; }
}
