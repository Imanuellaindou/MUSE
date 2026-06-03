package controller;

import com.muse.model.AlatMusik;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import service.AlatMusikService;

/**
 * Controller untuk AlatMusikForm.fxml (dialog tambah/edit)
 */

public class AlatMusikFormController {
    @FXML
    private Label lblFormTitle;
    @FXML private TextField tfNama;
    @FXML private TextField tfJenis;
    @FXML private TextField tfHarga;
    @FXML private Label     lblError;

    private AlatMusik editTarget;
    private AlatMusikController   parentController;
    private final AlatMusikService service = new AlatMusikService();

    public void setData(AlatMusik alat, AlatMusikController parent) {
        this.editTarget       = alat;
        this.parentController = parent;

        if (alat != null) {
            lblFormTitle.setText("Edit Alat Musik");
            tfNama.setText(alat.getNama());
            tfJenis.setText(alat.getJenis());
            tfHarga.setText(String.valueOf((int) alat.getHargaSewa()));
        } else {
            lblFormTitle.setText("Tambah Alat Musik Baru");
        }
    }

    @FXML
    private void onSimpan() {
        String nama  = tfNama.getText().trim();
        String jenis = tfJenis.getText().trim();
        String hargaStr = tfHarga.getText().trim().replaceAll("[^0-9]", "");

        if (nama.isEmpty() || jenis.isEmpty() || hargaStr.isEmpty()) {
            showError("Semua field wajib harus diisi.");
            return;
        }

        double harga;
        try {
            harga = Double.parseDouble(hargaStr);
            if (harga <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showError("Harga sewa harus berupa angka positif.");
            return;
        }

        boolean ok;
        if (editTarget == null) {
            ok = service.tambahAlat(nama, jenis, harga);
        } else {
            editTarget.setNama(nama);
            editTarget.setJenis(jenis);
            editTarget.setHargaSewa(harga);
            ok = service.updateAlat(editTarget);
        }

        if (ok) {
            if (parentController != null) parentController.onFormSaved();
            closeDialog();
        } else {
            showError("Gagal menyimpan data. Periksa kembali input Anda.");
        }
    }

    @FXML
    private void onBatal() {
        closeDialog();
    }

    private void showError(String msg) {
        lblError.setText(msg);
        lblError.setVisible(true);
        lblError.setManaged(true);
    }

    private void closeDialog() {
        ((Stage) tfNama.getScene().getWindow()).close();
    }
}
