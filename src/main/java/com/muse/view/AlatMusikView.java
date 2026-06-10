package com.muse.view;

import com.muse.model.AlatMusik;
import com.muse.service.AlatMusikService;
import com.muse.util.CurrencyFormatter;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.util.List;

/**
 * Halaman Manajemen Alat Musik
 * Fitur: tambah, edit, hapus, cari, filter status
 * Tombol Hapus hanya aktif jika status alat = Tersedia
 */
public class AlatMusikView {

    private VBox root;
    private TableView<AlatMusik> table;
    private TextField tfCari;
    private ComboBox<String> cbStatus;
    private Label lblInfo;

    private final AlatMusikService service = new AlatMusikService();

    public AlatMusikView() {
        build();
        loadData();
    }

    private void build() {
        root = new VBox(12);
        root.setPadding(new Insets(0));
        VBox.setVgrow(root, Priority.ALWAYS);

        // Judul
        Label title = new Label("Data Alat Musik");
        title.getStyleClass().add("page-title");

        // Toolbar
        HBox toolbar = new HBox(8);
        toolbar.getStyleClass().add("toolbar");
        toolbar.setAlignment(Pos.CENTER_LEFT);

        Button btnTambah = new Button("+ Tambah");
        btnTambah.getStyleClass().add("btn-primary");

        Button btnEdit = new Button("- Edit");
        btnEdit.getStyleClass().add("btn-secondary");

        Button btnHapus = new Button("■ Hapus");
        btnHapus.getStyleClass().add("btn-danger");

        Button btnRefresh = new Button("↻ Refresh");
        btnRefresh.getStyleClass().add("btn-secondary");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label lblCari = new Label("Cari:");
        lblCari.setStyle("-fx-font-size: 12px; -fx-text-fill: #374151;");
        tfCari = new TextField();
        tfCari.setPromptText("Nama alat...");
        tfCari.getStyleClass().add("search-field");
        tfCari.setPrefWidth(180);

        cbStatus = new ComboBox<>();
        cbStatus.getItems().addAll("Semua Status", "Tersedia", "Disewa");
        cbStatus.setValue("Semua Status");
        cbStatus.getStyleClass().add("combo-box");
        cbStatus.setPrefWidth(140);

        toolbar.getChildren().addAll(btnTambah, btnEdit, btnHapus, btnRefresh, spacer, lblCari, tfCari, cbStatus);

        // Table
        table = buildTable();
        VBox.setVgrow(table, Priority.ALWAYS);

        // Info label
        lblInfo = new Label();
        lblInfo.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280;");

        // Events
        btnTambah.setOnAction(e -> showFormTambah());
        btnEdit.setOnAction(e -> {
            AlatMusik selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                alert("Pilih alat musik yang akan diedit.");
                return;
            }
            showFormEdit(selected);
        });
        btnHapus.setOnAction(e -> hapusAlat());
        btnRefresh.setOnAction(e -> {
            tfCari.clear();
            cbStatus.setValue("Semua Status");
            loadData();
        });

        tfCari.textProperty().addListener((obs, o, n) -> filterData());
        cbStatus.setOnAction(e -> filterData());

        root.getChildren().addAll(title, toolbar, table, lblInfo);
    }

    private TableView<AlatMusik> buildTable() {
        TableView<AlatMusik> tv = new TableView<>();
        tv.getStyleClass().add("table-com.muse.view");
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tv.setPlaceholder(new Label("Tidak ada data alat musik."));

        TableColumn<AlatMusik, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Integer val, boolean empty) {
                super.updateItem(val, empty);
                setText(empty || val == null ? null : String.format("%03d", val));
            }
        });
        colId.setPrefWidth(60);

        TableColumn<AlatMusik, String> colNama = new TableColumn<>("Nama Alat");
        colNama.setCellValueFactory(new PropertyValueFactory<>("nama"));

        TableColumn<AlatMusik, String> colJenis = new TableColumn<>("Jenis");
        colJenis.setCellValueFactory(new PropertyValueFactory<>("jenis"));
        colJenis.setPrefWidth(100);

        TableColumn<AlatMusik, Double> colHarga = new TableColumn<>("Harga Sewa/hari");
        colHarga.setCellValueFactory(new PropertyValueFactory<>("hargaSewa"));
        colHarga.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double val, boolean empty) {
                super.updateItem(val, empty);
                setText(empty || val == null ? null : CurrencyFormatter.formatSimple(val));
            }
        });
        colHarga.setPrefWidth(140);

        TableColumn<AlatMusik, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) { setGraphic(null); setText(null); return; }
                Label badge = new Label(val.substring(0, 1).toUpperCase() + val.substring(1));
                badge.getStyleClass().add("badge");
                badge.getStyleClass().add("tersedia".equalsIgnoreCase(val) ? "badge-tersedia" : "badge-disewa");
                setGraphic(badge);
                setText(null);
            }
        });
        colStatus.setPrefWidth(100);

        tv.getColumns().addAll(colId, colNama, colJenis, colHarga, colStatus);
        return tv;
    }

    public void refresh() { loadData(); }

    private void loadData() {
        List<AlatMusik> data = service.getAllAlat();
        table.getItems().setAll(data);
        int t = service.getTotalAlat(), s = service.getTotalTersedia(), d = service.getTotalDisewa();
        lblInfo.setText("Total: " + t + " alat terdaftar · " + s + " tersedia · " + d + " sedang disewa");
    }

    private void filterData() {
        String keyword = tfCari.getText().trim();
        String status = cbStatus.getValue();
        List<AlatMusik> data = service.searchAlat(keyword, status);
        table.getItems().setAll(data);
        lblInfo.setText("Menampilkan " + data.size() + " alat");
    }

    private void hapusAlat() {
        AlatMusik selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) { alert("Pilih alat musik yang akan dihapus."); return; }
        if (!"tersedia".equalsIgnoreCase(selected.getStatus())) {
            alert("Alat tidak dapat dihapus karena sedang dalam status disewa.\nSelesaikan transaksi terlebih dahulu.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Konfirmasi Hapus");
        confirm.setHeaderText("Hapus Alat Musik");
        confirm.setContentText("Hapus alat \"" + selected.getNama() + "\"?");
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                boolean ok = service.hapusAlat(selected.getId());
                if (ok) loadData();
                else alert("Gagal menghapus alat musik.");
            }
        });
    }

    private void showFormTambah() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Tambah Alat Musik");
        dialog.setHeaderText("Form Tambah Alat Musik");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = buildFormGrid(null);
        dialog.getDialogPane().setContent(grid);

        TextField tfNama = (TextField) grid.lookup("#tfNama");
        TextField tfJenis = (TextField) grid.lookup("#tfJenis");
        TextField tfHarga = (TextField) grid.lookup("#tfHarga");

        dialog.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                try {
                    double harga = Double.parseDouble(tfHarga.getText().trim().replace(",", "").replace(".", ""));
                    boolean ok = service.tambahAlat(tfNama.getText(), tfJenis.getText(), harga);
                    if (ok) loadData();
                    else alert("Data tidak valid atau gagal disimpan.");
                } catch (NumberFormatException ex) {
                    alert("Harga sewa harus berupa angka.");
                }
            }
        });
    }

    private void showFormEdit(AlatMusik alat) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Alat Musik");
        dialog.setHeaderText("Edit: " + alat.getNama());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = buildFormGrid(alat);
        dialog.getDialogPane().setContent(grid);

        TextField tfNama = (TextField) grid.lookup("#tfNama");
        TextField tfJenis = (TextField) grid.lookup("#tfJenis");
        TextField tfHarga = (TextField) grid.lookup("#tfHarga");

        dialog.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                try {
                    alat.setNama(tfNama.getText().trim());
                    alat.setJenis(tfJenis.getText().trim());
                    alat.setHargaSewa(Double.parseDouble(tfHarga.getText().trim().replace(",", "").replace(".", "")));
                    boolean ok = service.updateAlat(alat);
                    if (ok) loadData();
                    else alert("Data tidak valid atau gagal diperbarui.");
                } catch (NumberFormatException ex) {
                    alert("Harga sewa harus berupa angka.");
                }
            }
        });
    }

    private GridPane buildFormGrid(AlatMusik alat) {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 40, 20, 20));

        grid.add(new Label("Nama Alat *"), 0, 0);
        TextField tfNama = new TextField(alat != null ? alat.getNama() : "");
        tfNama.setId("tfNama");
        tfNama.setPrefWidth(240);
        grid.add(tfNama, 1, 0);

        grid.add(new Label("Jenis *"), 0, 1);
        TextField tfJenis = new TextField(alat != null ? alat.getJenis() : "");
        tfJenis.setPromptText("Contoh: Gitar, Drum, Keyboard");
        tfJenis.setId("tfJenis");
        grid.add(tfJenis, 1, 1);

        grid.add(new Label("Harga Sewa/hari (Rp) *"), 0, 2);
        TextField tfHarga = new TextField(alat != null ? String.valueOf((int)alat.getHargaSewa()) : "");
        tfHarga.setPromptText("Contoh: 50000");
        tfHarga.setId("tfHarga");
        grid.add(tfHarga, 1, 2);

        return grid;
    }

    private void alert(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    public VBox getRoot() { return root; }
}
