package controller;

import com.muse.model.Transaksi;
import com.muse.service.AlatMusikService;
import com.muse.service.DendaService;
import com.muse.service.TransaksiService;
import com.muse.util.CurrencyFormatter;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller untuk Dashboard.fxml
 * Menampilkan statistik real-time dan transaksi aktif terkini
 */
public class DashboardController implements Initializable {

    @FXML private HBox  notifBox;
    @FXML private Label lblNotif;
    @FXML private Label lblTotalAlat;
    @FXML private Label lblTersedia;
    @FXML private Label lblDisewa;
    @FXML private Label lblDendaAktif;
    @FXML private Label lblJumlahAktif;

    @FXML private TableView<Transaksi>         tableTransaksiAktif;
    @FXML private TableColumn<Transaksi, Integer> colId;
    @FXML private TableColumn<Transaksi, String>  colPelanggan;
    @FXML private TableColumn<Transaksi, String>  colAlat;
    @FXML private TableColumn<Transaksi, LocalDate> colTglKembali;
    @FXML private TableColumn<Transaksi, String>  colStatus;

    private MainController mainController;

    private final AlatMusikService  alatMusikService  = new AlatMusikService();
    private final TransaksiService  transaksiService  = new TransaksiService();
    private final DendaService      dendaService      = new DendaService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        loadData();
    }

    public void setMainController(MainController mc) {
        this.mainController = mc;
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

        colTglKembali.setCellValueFactory(new PropertyValueFactory<>("tanggalKembali"));
        colTglKembali.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(LocalDate val, boolean empty) {
                super.updateItem(val, empty);
                setText(empty || val == null ? null : val.toString());
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
                    case "aktif"    -> "badge-aktif";
                    case "terlambat"-> "badge-terlambat";
                    default         -> "badge-selesai";
                });
                setGraphic(badge); setText(null);
            }
        });
    }

    private void loadData() {
        // Statistik
        lblTotalAlat.setText(String.valueOf(alatMusikService.getTotalAlat()));
        lblTersedia.setText(String.valueOf(alatMusikService.getTotalTersedia()));
        lblDisewa.setText(String.valueOf(alatMusikService.getTotalDisewa()));

        int dendaAktif = dendaService.countDendaAktif();
        lblDendaAktif.setText(String.valueOf(dendaAktif));

        // Notifikasi keterlambatan
        if (dendaAktif > 0) {
            lblNotif.setText("⚠ " + dendaAktif +
                " alat melewati batas waktu pengembalian. Segera proses pengembalian.");
            notifBox.setVisible(true);
            notifBox.setManaged(true);
        } else {
            notifBox.setVisible(false);
            notifBox.setManaged(false);
        }

        // Tabel transaksi aktif
        List<Transaksi> aktif = transaksiService.getTransaksiAktif();
        tableTransaksiAktif.getItems().setAll(aktif);
        lblJumlahAktif.setText("Total transaksi aktif: " + aktif.size());
    }

    @FXML
    private void onTransaksiBaru() {
        if (mainController != null) mainController.navigateTo("TransaksiBaru");
    }

    @FXML
    private void onPengembalian() {
        if (mainController != null) mainController.navigateTo("Pengembalian");
    }
}
