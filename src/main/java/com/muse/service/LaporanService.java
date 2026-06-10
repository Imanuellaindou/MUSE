package com.muse.service;

import com.muse.dao.TransaksiDAO;
import com.muse.model.Transaksi;

import java.util.List;

/**
 * Service layer untuk Laporan Penyewaan
 * Sesuai Business Logic Layer proposal
 */
public class LaporanService {

    private final TransaksiDAO transaksiDAO;
    private final DendaService dendaService;

    public LaporanService() {
        this.transaksiDAO = new TransaksiDAO();
        this.dendaService = new DendaService();
    }

    public List<Transaksi> getTransaksiByBulan(int tahun, int bulan) {
        return transaksiDAO.findByBulan(tahun, bulan);
    }

    public int getTotalTransaksi(List<Transaksi> list) {
        return list.size();
    }

    public double getTotalPendapatan(List<Transaksi> list) {
        double total = 0;
        for (Transaksi t : list) {
            if (t.getTanggalPinjam() != null && t.getTanggalKembali() != null
                    && t.getAlatMusik() != null) {
                long hari = java.time.temporal.ChronoUnit.DAYS.between(
                    t.getTanggalPinjam(), t.getTanggalKembali()
                );
                total += hari * t.getAlatMusik().getHargaSewa();
            }
            total += t.getJumlahDenda();
        }
        return total;
    }

    public double getTotalDenda(int tahun, int bulan) {
        return dendaService.getTotalDendaByBulan(tahun, bulan);
    }

    /**
     * Hitung jumlah transaksi per minggu dalam bulan tertentu
     * Minggu 1: tgl 1-7, Minggu 2: 8-14, Minggu 3: 15-21, Minggu 4: 22-akhir
     */
    public int[] getTransaksiPerMinggu(List<Transaksi> list) {
        int[] minggu = new int[4];
        for (Transaksi t : list) {
            if (t.getTanggalPinjam() == null) continue;
            int tgl = t.getTanggalPinjam().getDayOfMonth();
            if (tgl <= 7) minggu[0]++;
            else if (tgl <= 14) minggu[1]++;
            else if (tgl <= 21) minggu[2]++;
            else minggu[3]++;
        }
        return minggu;
    }
}
