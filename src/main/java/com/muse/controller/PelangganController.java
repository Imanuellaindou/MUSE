package com.muse.controller;

import com.muse.model.Pelanggan;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import service.PelangganService;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller untuk Pelanggan.fxml
 * Tambah & edit pelanggan, pencarian. Tidak ada hapus.
 */

public class PelangganController implements Initializable {

    @FXML private TextField          tfCari;
    @FXML private Label              lblInfo;

    @FXML private TableView<Pelanggan> tablePelanggan;
    @FXML private TableColumn<Pelanggan, Integer> colId;
    @FXML private TableColumn<Pelanggan, String>  colNama;
    @FXML private TableColumn<Pelanggan, String>  colKontak;
    @FXML private TableColumn<Pelanggan, String>  colAlamat;
    @FXML private TableColumn<Pelanggan, Integer> colJmlTrx;

    private final PelangganService service = new PelangganService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        loadData();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Integer val, boolean empty) {
                super.updateItem(val, empty);
                setText(empty || val == null ? null : String.format("P%03d", val));
            }
        });

        colNama.setCellValueFactory(new PropertyValueFactory<>("nama"));
        colKontak.setCellValueFactory(new PropertyValueFactory<>("kontak"));
        colAlamat.setCellValueFactory(new PropertyValueFactory<>("alamat"));

        // Total transaksi — query per baris
        colJmlTrx.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Integer val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setText(null); return;
                }
                Pelanggan p = getTableView().getItems().get(getIndex());
                setText(String.valueOf(service.getTotalTransaksiByPelanggan(p.getId())));
            }
        });
    }

    private void loadData() {
        List<Pelanggan> data = service.getAllPelanggan();
        tablePelanggan.getItems().setAll(data);
        lblInfo.setText("Total: " + data.size() + " pelanggan terdaftar");
    }

    @FXML private void onCari() {
        List<Pelanggan> data = service.searchPelanggan(tfCari.getText());
        tablePelanggan.getItems().setAll(data);
        lblInfo.setText("Menampilkan " + data.size() + " pelanggan");
    }

    @FXML
    private void onTambah() { openForm(null); }

    @FXML
    private void onEdit() {
        Pelanggan selected = tablePelanggan.getSelectionModel().getSelectedItem();
        if (selected == null) { alert("Pilih pelanggan yang akan diedit."); return; }
        openForm(selected);
    }

    @FXML
    private void onRefresh() {
        tfCari.clear();
        loadData();
    }

    private void openForm(Pelanggan pelanggan) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/PelangganForm.fxml"));
            javafx.scene.layout.VBox formRoot = loader.load();
            PelangganFormController fc = loader.getController();
            fc.setData(pelanggan, this);

            Stage dialog = new Stage();
            dialog.setTitle(pelanggan == null ? "Tambah Pelanggan" : "Edit Pelanggan");
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(tablePelanggan.getScene().getWindow());
            dialog.setScene(new Scene(formRoot));
            dialog.setResizable(false);
            dialog.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onFormSaved() { loadData(); }

    private void alert(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}