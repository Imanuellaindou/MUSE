package com.muse.controller;

import com.muse.model.Pelanggan;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import service.PelangganService;

/**
 * Controller untuk PelangganForm.fxml (dialog tambah/edit)
 */

public class PelangganFormController {
    @FXML
    private Label lblFormTitle;
    @FXML private TextField tfNama;
    @FXML private TextField tfKontak;
    @FXML private TextArea taAlamat;
    @FXML private Label     lblError;

    private Pelanggan editTarget;
    private PelangganController parentController;
    private final PelangganService service = new PelangganService();

    public void setData(Pelanggan pelanggan, PelangganController parent) {
        this.editTarget       = pelanggan;
        this.parentController = parent;

        if (pelanggan != null) {
            lblFormTitle.setText("Edit Pelanggan");
            tfNama.setText(pelanggan.getNama());
            tfKontak.setText(pelanggan.getKontak());
            taAlamat.setText(pelanggan.getAlamat());
        } else {
            lblFormTitle.setText("Tambah Pelanggan Baru");
        }
    }

    @FXML
    private void onSimpan() {
        String nama    = tfNama.getText().trim();
        String kontak  = tfKontak.getText().trim();
        String alamat  = taAlamat.getText().trim();

        if (nama.isEmpty() || kontak.isEmpty() || alamat.isEmpty()) {
            showError("Semua field wajib harus diisi.");
            return;
        }

        boolean ok;
        if (editTarget == null) {
            ok = service.tambahPelanggan(nama, kontak, alamat);
        } else {
            editTarget.setNama(nama);
            editTarget.setKontak(kontak);
            editTarget.setAlamat(alamat);
            ok = service.updatePelanggan(editTarget);
        }

        if (ok) {
            if (parentController != null) parentController.onFormSaved();
            closeDialog();
        } else {
            showError("Gagal menyimpan data pelanggan.");
        }
    }

    @FXML
    private void onBatal() { closeDialog(); }

    private void showError(String msg) {
        lblError.setText(msg);
        lblError.setVisible(true);
        lblError.setManaged(true);
    }

    private void closeDialog() {
        ((Stage) tfNama.getScene().getWindow()).close();
    }
}
