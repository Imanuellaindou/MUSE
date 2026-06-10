package com.muse.service;

import com.muse.dao.DendaDAO;
import com.muse.model.Denda;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Service layer untuk logika bisnis Denda
 * Sesuai Business Logic Layer proposal
 */
public class DendaService {

    private final DendaDAO dendaDAO;

    public DendaService() {
        this.dendaDAO = new DendaDAO();
    }

    /**
     * Hitung denda berdasarkan tanggal aktual vs estimasi
     */
    public double hitungDenda(LocalDate tanggalKembali, LocalDate tanggalAktual) {
        if (tanggalAktual == null) tanggalAktual = LocalDate.now();
        if (tanggalAktual.isAfter(tanggalKembali)) {
            long hariTerlambat = ChronoUnit.DAYS.between(tanggalKembali, tanggalAktual);
            return hariTerlambat * Denda.TARIF_DENDA_PER_HARI;
        }
        return 0;
    }

    public int hitungHariTerlambat(LocalDate tanggalKembali, LocalDate tanggalAktual) {
        if (tanggalAktual == null) tanggalAktual = LocalDate.now();
        if (tanggalAktual.isAfter(tanggalKembali)) {
            return (int) ChronoUnit.DAYS.between(tanggalKembali, tanggalAktual);
        }
        return 0;
    }

    /**
     * Simpan data denda ke database
     */
    public boolean simpanDenda(int idTransaksi, int hariTerlambat, double jumlahDenda) {
        if (hariTerlambat <= 0) return false;
        Denda denda = new Denda();
        denda.setIdTransaksi(idTransaksi);
        denda.setHariTerlambat(hariTerlambat);
        denda.setJumlahDenda(jumlahDenda);
        return dendaDAO.insert(denda);
    }

    public Denda getDendaByTransaksi(int idTransaksi) {
        return dendaDAO.findByTransaksi(idTransaksi);
    }

    public double getTotalDendaByBulan(int tahun, int bulan) {
        return dendaDAO.getTotalDendaByBulan(tahun, bulan);
    }

    public int countDendaAktif() {
        return dendaDAO.countDendaAktif();
    }
}
