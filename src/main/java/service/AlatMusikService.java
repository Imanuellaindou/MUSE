package service;


import com.muse.model.AlatMusik;
import dao.AlatMusikDAO;

import java.util.List;

/**
 * Service layer untuk logika bisnis AlatMusik
 * Sesuai Business Logic Layer proposal
 */

public class AlatMusikService {
    private final AlatMusikDAO alatMusikDAO;

    public AlatMusikService() {
        this.alatMusikDAO = new AlatMusikDAO();
    }

    public List<AlatMusik> getAllAlat() {
        return alatMusikDAO.findAll();
    }

    public List<AlatMusik> getAlatTersedia() {
        return alatMusikDAO.findTersedia();
    }

    public List<AlatMusik> searchAlat(String keyword, String status) {
        return alatMusikDAO.findByNamaOrStatus(keyword, status);
    }

    public AlatMusik getAlatById(int id) {
        return alatMusikDAO.findById(id);
    }

    /**
     * Tambah alat baru — validasi nama & harga tidak boleh kosong
     */
    public boolean tambahAlat(String nama, String jenis, double hargaSewa) {
        if (nama == null || nama.isBlank()) return false;
        if (jenis == null || jenis.isBlank()) return false;
        if (hargaSewa <= 0) return false;

        AlatMusik alat = new AlatMusik();
        alat.setNama(nama.trim());
        alat.setJenis(jenis.trim());
        alat.setHargaSewa(hargaSewa);
        alat.setStatus("tersedia");
        return alatMusikDAO.insert(alat);
    }

    /**
     * Update data alat
     */
    public boolean updateAlat(AlatMusik alat) {
        if (alat.getNama() == null || alat.getNama().isBlank()) return false;
        if (alat.getHargaSewa() <= 0) return false;
        return alatMusikDAO.update(alat);
    }

    /**
     * Hapus alat — hanya boleh jika status tersedia
     */
    public boolean hapusAlat(int id) {
        AlatMusik alat = alatMusikDAO.findById(id);
        if (alat == null) return false;
        if (!"tersedia".equalsIgnoreCase(alat.getStatus())) return false;
        return alatMusikDAO.delete(id);
    }

    public int getTotalAlat() { return alatMusikDAO.getTotalAlat(); }
    public int getTotalTersedia() { return alatMusikDAO.getTotalTersedia(); }
    public int getTotalDisewa() { return alatMusikDAO.getTotalDisewa(); }
}
