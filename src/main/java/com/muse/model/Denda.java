package com.muse.model;

/**
 * Model class untuk data Denda
 * Sesuai Class Diagram proposal MUSE
 * Relasi: Transaksi → Denda (1 ke 1)
 */

public class Denda {
    private int id;
    private int idTransaksi;
    private int hariTerlambat;
    private double jumlahDenda;

    // Tarif denda per hari (Rp 25.000)
    public static final double TARIF_DENDA_PER_HARI = 25000.0;

    public Denda() {}

    public Denda(int idTransaksi, int hariTerlambat) {
        this.idTransaksi = idTransaksi;
        this.hariTerlambat = hariTerlambat;
        this.jumlahDenda = hitungDenda();
    }

    // Method sesuai class diagram
    public double hitungDenda() {
        this.jumlahDenda = hariTerlambat * TARIF_DENDA_PER_HARI;
        return this.jumlahDenda;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdTransaksi() { return idTransaksi; }
    public void setIdTransaksi(int idTransaksi) { this.idTransaksi = idTransaksi; }

    public int getHariTerlambat() { return hariTerlambat; }
    public void setHariTerlambat(int hariTerlambat) {
        this.hariTerlambat = hariTerlambat;
        hitungDenda();
    }

    public double getJumlahDenda() { return jumlahDenda; }
    public void setJumlahDenda(double jumlahDenda) { this.jumlahDenda = jumlahDenda; }
}
