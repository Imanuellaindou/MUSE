package com.muse.model;

/**
 * Model class untuk data Alat Musik
 * Sesuai Class Diagram proposal MUSE
 */

public class AlatMusik {
    private int id;
    private String nama;
    private String jenis;
    private double hargaSewa;
    private String status; // "tersedia" / "disewa"

    public AlatMusik() {}

    public AlatMusik(int id, String nama, String jenis, double hargaSewa, String status) {
        this.id = id;
        this.nama = nama;
        this.jenis = jenis;
        this.hargaSewa = hargaSewa;
        this.status = status;
    }

    // Methods sesuai class diagram
    public void tambahAlat() {
        // Handled by AlatMusikDAO
    }

    public void updateAlat() {
        // Handled by AlatMusikDAO
    }

    public void hapusAlat() {
        // Handled by AlatMusikDAO
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public String getJenis() { return jenis; }
    public void setJenis(String jenis) { this.jenis = jenis; }

    public double getHargaSewa() { return hargaSewa; }
    public void setHargaSewa(double hargaSewa) { this.hargaSewa = hargaSewa; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isTersedia() {
        return "tersedia".equalsIgnoreCase(status);
    }

    @Override
    public String toString() {
        return nama + " (" + jenis + ")";
    }
}
