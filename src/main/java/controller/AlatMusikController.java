package controller;

import com.muse.model.AlatMusik;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import service.AlatMusikService;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller untuk AlatMusik.fxml
 * CRUD alat musik, pencarian & filter status
 */
public class AlatMusikController implements Initializable {

    @FXML
    private TextField tfCari;
    @FXML private ComboBox<String> cbStatus;
    @FXML private Label lblInfo;

    @FXML private TableView<AlatMusik> tableAlat;
    @FXML private TableColumn<AlatMusik, Integer> colId;
    @FXML private TableColumn<AlatMusik, String>  colNama;
    @FXML private TableColumn<AlatMusik, String>  colJenis;
    @FXML private TableColumn<AlatMusik, Double>  colHarga;
    @FXML private TableColumn<AlatMusik, String>  colStatus;

    private final AlatMusikService service = new AlatMusikService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cbStatus.getItems().addAll("Semua Status", "Tersedia", "Disewa");
        cbStatus.setValue("Semua Status");
        setupTable();
        loadData();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Integer val, boolean empty) {
                super.updateItem(val, empty);
                setText(empty || val == null ? null : String.format("%03d", val));
            }
        });

        colNama.setCellValueFactory(new PropertyValueFactory<>("nama"));
        colJenis.setCellValueFactory(new PropertyValueFactory<>("jenis"));

        colHarga.setCellValueFactory(new PropertyValueFactory<>("hargaSewa"));
        colHarga.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double val, boolean empty) {
                super.updateItem(val, empty);
                setText(empty || val == null ? null : CurrencyFormatter.formatSimple(val));
            }
        });

        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) { setGraphic(null); return; }
                Label badge = new Label(val.substring(0,1).toUpperCase() + val.substring(1));
                badge.getStyleClass().add("badge");
                badge.getStyleClass().add(
                        "tersedia".equalsIgnoreCase(val) ? "badge-tersedia" : "badge-disewa");
                setGraphic(badge); setText(null);
            }
        });
    }

    private void loadData() {
        List<AlatMusik> data = service.getAllAlat();
        tableAlat.getItems().setAll(data);
        int t = service.getTotalAlat(), s = service.getTotalTersedia(), d = service.getTotalDisewa();
        lblInfo.setText("Total: " + t + " alat · " + s + " tersedia · " + d + " sedang disewa");
    }

    @FXML private void onCari()         { filterData(); }
    @FXML private void onFilterStatus() { filterData(); }

    private void filterData() {
        String keyword = tfCari.getText().trim();
        String status  = cbStatus.getValue();
        List<AlatMusik> data = service.searchAlat(keyword, status);
        tableAlat.getItems().setAll(data);
        lblInfo.setText("Menampilkan " + data.size() + " alat");
    }

    @FXML
    private void onTambah() {
        openForm(null);
    }

    @FXML
    private void onEdit() {
        AlatMusik selected = tableAlat.getSelectionModel().getSelectedItem();
        if (selected == null) { alert("Pilih alat musik yang akan diedit."); return; }
        openForm(selected);
    }

    @FXML
    private void onHapus() {
        AlatMusik selected = tableAlat.getSelectionModel().getSelectedItem();
        if (selected == null) { alert("Pilih alat musik yang akan dihapus."); return; }
        if (!"tersedia".equalsIgnoreCase(selected.getStatus())) {
            alert("Alat tidak dapat dihapus.\nAlat sedang berstatus 'Disewa'.\n" +
                    "Selesaikan transaksi terlebih dahulu.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Konfirmasi Hapus");
        confirm.setHeaderText("Hapus Alat Musik");
        confirm.setContentText("Hapus \"" + selected.getNama() + "\"?");
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                boolean ok = service.hapusAlat(selected.getId());
                if (ok) loadData();
                else alert("Gagal menghapus alat musik.");
            }
        });
    }

    @FXML
    private void onRefresh() {
        tfCari.clear();
        cbStatus.setValue("Semua Status");
        loadData();
    }

    private void openForm(AlatMusik alat) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/AlatMusikForm.fxml"));
            javafx.scene.layout.VBox formRoot = loader.load();
            AlatMusikFormController fc = loader.getController();
            fc.setData(alat, this);

            Stage dialog = new Stage();
            dialog.setTitle(alat == null ? "Tambah Alat Musik" : "Edit Alat Musik");
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(tableAlat.getScene().getWindow());
            dialog.setScene(new Scene(formRoot));
            dialog.setResizable(false);
            dialog.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Dipanggil oleh form setelah simpan berhasil */
    public void onFormSaved() { loadData(); }

    private void alert(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
