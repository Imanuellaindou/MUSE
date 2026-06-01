package view;

import com.muse.model.Pelanggan;
import com.muse.service.PelangganService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.util.List;

/**
 * Halaman Manajemen Pelanggan
 * Fitur: tambah, edit, cari pelanggan
 * Tidak ada tombol Hapus (data pelanggan terikat riwayat transaksi)
 */
public class PelangganView {

    private VBox root;
    private TableView<Pelanggan> table;
    private TextField tfCari;
    private Label lblInfo;

    private final PelangganService service = new PelangganService();

    public PelangganView() {
        build();
        loadData();
    }

    private void build() {
        root = new VBox(12);
        root.setPadding(new Insets(0));
        VBox.setVgrow(root, Priority.ALWAYS);

        Label title = new Label("Data Pelanggan");
        title.getStyleClass().add("page-title");

        // Toolbar
        HBox toolbar = new HBox(8);
        toolbar.getStyleClass().add("toolbar");
        toolbar.setAlignment(Pos.CENTER_LEFT);

        Button btnTambah = new Button("+ Tambah");
        btnTambah.getStyleClass().add("btn-primary");

        Button btnEdit = new Button("- Edit");
        btnEdit.getStyleClass().add("btn-secondary");

        Button btnRefresh = new Button("↻ Refresh");
        btnRefresh.getStyleClass().add("btn-secondary");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label lblCari = new Label("Cari:");
        lblCari.setStyle("-fx-font-size: 12px; -fx-text-fill: #374151;");
        tfCari = new TextField();
        tfCari.setPromptText("Nama / nomor kontak...");
        tfCari.getStyleClass().add("search-field");
        tfCari.setPrefWidth(220);

        toolbar.getChildren().addAll(btnTambah, btnEdit, btnRefresh, spacer, lblCari, tfCari);

        // Table
        table = buildTable();
        VBox.setVgrow(table, Priority.ALWAYS);

        lblInfo = new Label();
        lblInfo.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280;");

        // Events
        btnTambah.setOnAction(e -> showFormTambah());
        btnEdit.setOnAction(e -> {
            Pelanggan selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { alert("Pilih pelanggan yang akan diedit."); return; }
            showFormEdit(selected);
        });
        btnRefresh.setOnAction(e -> { tfCari.clear(); loadData(); });
        tfCari.textProperty().addListener((obs, o, n) -> filterData());

        root.getChildren().addAll(title, toolbar, table, lblInfo);
    }

    private TableView<Pelanggan> buildTable() {
        TableView<Pelanggan> tv = new TableView<>();
        tv.getStyleClass().add("table-view");
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tv.setPlaceholder(new Label("Tidak ada data pelanggan."));

        TableColumn<Pelanggan, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Integer val, boolean empty) {
                super.updateItem(val, empty);
                setText(empty || val == null ? null : String.format("P%03d", val));
            }
        });
        colId.setPrefWidth(60);

        TableColumn<Pelanggan, String> colNama = new TableColumn<>("Nama");
        colNama.setCellValueFactory(new PropertyValueFactory<>("nama"));

        TableColumn<Pelanggan, String> colKontak = new TableColumn<>("No. Kontak");
        colKontak.setCellValueFactory(new PropertyValueFactory<>("kontak"));
        colKontak.setPrefWidth(130);

        TableColumn<Pelanggan, String> colAlamat = new TableColumn<>("Alamat");
        colAlamat.setCellValueFactory(new PropertyValueFactory<>("alamat"));

        TableColumn<Pelanggan, Integer> colTrx = new TableColumn<>("Total Transaksi");
        colTrx.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Integer val, boolean empty) {
                super.updateItem(val, empty);
                if (empty) { setText(null); return; }
                Pelanggan p = getTableView().getItems().get(getIndex());
                setText(String.valueOf(service.getTotalTransaksiByPelanggan(p.getId())));
            }
        });
        colTrx.setPrefWidth(110);

        tv.getColumns().addAll(colId, colNama, colKontak, colAlamat, colTrx);
        return tv;
    }

    public void refresh() { loadData(); }

    private void loadData() {
        List<Pelanggan> data = service.getAllPelanggan();
        table.getItems().setAll(data);
        lblInfo.setText("Total: " + data.size() + " pelanggan terdaftar");
    }

    private void filterData() {
        List<Pelanggan> data = service.searchPelanggan(tfCari.getText());
        table.getItems().setAll(data);
        lblInfo.setText("Menampilkan " + data.size() + " pelanggan");
    }

    private void showFormTambah() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Tambah Pelanggan");
        dialog.setHeaderText("Form Tambah Pelanggan");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = buildFormGrid(null);
        dialog.getDialogPane().setContent(grid);

        TextField tfNama = (TextField) grid.lookup("#tfNama");
        TextField tfKontak = (TextField) grid.lookup("#tfKontak");
        TextArea taAlamat = (TextArea) grid.lookup("#taAlamat");

        dialog.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                boolean ok = service.tambahPelanggan(tfNama.getText(), tfKontak.getText(), taAlamat.getText());
                if (ok) loadData();
                else alert("Data tidak valid atau gagal disimpan.");
            }
        });
    }

    private void showFormEdit(Pelanggan p) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Pelanggan");
        dialog.setHeaderText("Edit: " + p.getNama());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = buildFormGrid(p);
        dialog.getDialogPane().setContent(grid);

        TextField tfNama = (TextField) grid.lookup("#tfNama");
        TextField tfKontak = (TextField) grid.lookup("#tfKontak");
        TextArea taAlamat = (TextArea) grid.lookup("#taAlamat");

        dialog.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                p.setNama(tfNama.getText().trim());
                p.setKontak(tfKontak.getText().trim());
                p.setAlamat(taAlamat.getText().trim());
                boolean ok = service.updatePelanggan(p);
                if (ok) loadData();
                else alert("Data tidak valid atau gagal diperbarui.");
            }
        });
    }

    private GridPane buildFormGrid(Pelanggan p) {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 40, 20, 20));

        grid.add(new Label("Nama Lengkap *"), 0, 0);
        TextField tfNama = new TextField(p != null ? p.getNama() : "");
        tfNama.setId("tfNama");
        tfNama.setPrefWidth(240);
        grid.add(tfNama, 1, 0);

        grid.add(new Label("No. Kontak *"), 0, 1);
        TextField tfKontak = new TextField(p != null ? p.getKontak() : "");
        tfKontak.setId("tfKontak");
        tfKontak.setPromptText("Nomor HP aktif");
        grid.add(tfKontak, 1, 1);

        grid.add(new Label("Alamat *"), 0, 2);
        TextArea taAlamat = new TextArea(p != null ? p.getAlamat() : "");
        taAlamat.setId("taAlamat");
        taAlamat.setPrefRowCount(3);
        taAlamat.setPrefWidth(240);
        taAlamat.setPromptText("Alamat lengkap pelanggan");
        grid.add(taAlamat, 1, 2);

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
