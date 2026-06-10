package com.muse.service;

import com.muse.dao.AlatMusikDAO;
import com.muse.dao.TransaksiDAO;
import com.muse.model.AlatMusik;
import com.muse.model.Pelanggan;
import com.muse.model.Transaksi;

import java.time.LocalDate;
import java.util.List;

/**
 * Service layer untuk logika bisnis Transaksi
 * Sesuai Business Logic Layer proposal
 * Menangani: buatTransaksi(), hitungDenda(), validasi transaksi
 */
public class TransaksiService {

    private final TransaksiDAO transaksiDAO;
    private final AlatMusikDAO alatMusikDAO;
    private final DendaService dendaService;

    public TransaksiService() {
        this.transaksiDAO = new TransaksiDAO();
        this.alatMusikDAO = new AlatMusikDAO();
        this.dendaService = new DendaService();
    }

    /**
     * Buat transaksi baru
     * Otomatis ubah status alat menjadi 'disewa'
     */
    public boolean buatTransaksi(Pelanggan pelanggan, AlatMusik alatMusik,
                                  LocalDate tanggalPinjam, int durasiHari) {
        if (pelanggan == null || alatMusik == null) return false;
        if (!alatMusik.isTersedia()) return false;
        if (durasiHari <= 0) return false;

        LocalDate tanggalKembali = tanggalPinjam.plusDays(durasiHari);

        Transaksi t = new Transaksi(pelanggan, alatMusik, tanggalPinjam, tanggalKembali);
        boolean saved = transaksiDAO.insert(t);

        if (saved) {
            // Update status alat menjadi disewa
            alatMusikDAO.updateStatus(alatMusik.getId(), "disewa");
        }
        return saved;
    }

    /**
     * Proses pengembalian alat
     * Hitung denda jika terlambat, ubah status alat kembali ke tersedia
     */
    public boolean prosesKembali(Transaksi transaksi, LocalDate tanggalAktual) {
        int hariTerlambat = dendaService.hitungHariTerlambat(
            transaksi.getTanggalKembali(), tanggalAktual
        );
        String status = hariTerlambat > 0 ? "terlambat" : "selesai";

        // Update transaksi
        boolean updated = transaksiDAO.updatePengembalian(
            transaksi.getId(), tanggalAktual, "selesai"
        );

        if (updated) {
            // Simpan denda jika ada
            if (hariTerlambat > 0) {
                double jumlahDenda = dendaService.hitungDenda(
                    transaksi.getTanggalKembali(), tanggalAktual
                );
                dendaService.simpanDenda(transaksi.getId(), hariTerlambat, jumlahDenda);
            }
            // Kembalikan status alat menjadi tersedia
            alatMusikDAO.updateStatus(transaksi.getIdAlat(), "tersedia");
        }
        return updated;
    }

    public List<Transaksi> getTransaksiAktif() {
        return transaksiDAO.findAktif();
    }

    public List<Transaksi> getAllTransaksi() {
        return transaksiDAO.findAll();
    }

    public List<Transaksi> getTransaksiWithFilter(String keyword, String status,
                                                    LocalDate dari, LocalDate sampai) {
        return transaksiDAO.findWithFilter(keyword, status, dari, sampai);
    }

    public List<Transaksi> getTransaksiByBulan(int tahun, int bulan) {
        return transaksiDAO.findByBulan(tahun, bulan);
    }

    public int countDendaAktif() {
        return transaksiDAO.countDendaAktif();
    }

    public int countAktif() {
        return transaksiDAO.countAktif();
    }
}
