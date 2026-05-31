package service;


import com.muse.model.Pelanggan;
import dao.PelangganDAO;

import java.util.List;

/**
 * Service layer untuk logika bisnis Pelanggan
 * Sesuai Business Logic Layer proposal
 */

public class PelangganService {
    private final PelangganDAO pelangganDAO;

    public PelangganService() {
        this.pelangganDAO = new PelangganDAO();
    }

    public List<Pelanggan> getAllPelanggan() {
        return pelangganDAO.findAll();
    }

    public List<Pelanggan> searchPelanggan(String keyword) {
        if (keyword == null || keyword.isBlank()) return getAllPelanggan();
        return pelangganDAO.findByKeyword(keyword);
    }

    public Pelanggan getPelangganById(int id) {
        return pelangganDAO.findById(id);
    }

    /**
     * Tambah pelanggan baru — validasi field wajib
     */
    public boolean tambahPelanggan(String nama, String kontak, String alamat) {
        if (nama == null || nama.isBlank()) return false;
        if (kontak == null || kontak.isBlank()) return false;
        if (alamat == null || alamat.isBlank()) return false;

        Pelanggan p = new Pelanggan();
        p.setNama(nama.trim());
        p.setKontak(kontak.trim());
        p.setAlamat(alamat.trim());
        return pelangganDAO.insert(p);
    }

    /**
     * Update data pelanggan
     */
    public boolean updatePelanggan(Pelanggan pelanggan) {
        if (pelanggan.getNama() == null || pelanggan.getNama().isBlank()) return false;
        if (pelanggan.getKontak() == null || pelanggan.getKontak().isBlank()) return false;
        return pelangganDAO.update(pelanggan);
    }

    /**
     * Jumlah riwayat transaksi pelanggan
     */
    public int getTotalTransaksiByPelanggan(int idPelanggan) {
        return pelangganDAO.countTransaksiByPelanggan(idPelanggan);
    }
}
