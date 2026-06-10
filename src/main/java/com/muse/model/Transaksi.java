package com.muse.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Model class untuk data Transaksi
 * Sesuai Class Diagram proposal MUSE
 * Relasi: Pelanggan → Transaksi (1 ke banyak)
 *         AlatMusik → Transaksi (1 ke banyak)
 *         Transaksi → Denda (1 ke 1)
 */
<<<<<<< Updated upstream

=======
>>>>>>> Stashed changes
public class Transaksi {
    private int id;
    private Pelanggan pelanggan;
    private AlatMusik alatMusik;
    private LocalDate tanggalPinjam;
    private LocalDate tanggalKembali;   // estimasi
    private LocalDate tanggalAktual;    // pengembalian sebenarnya
    private String status;              // aktif / selesai / terlambat

    // Foreign keys untuk keperluan DB
    private int idPelanggan;
    private int idAlat;

    // Data join untuk tampil di tabel
    private String namaPelanggan;
    private String namaAlat;
    private double jumlahDenda;

    public Transaksi() {}

    public Transaksi(Pelanggan pelanggan, AlatMusik alatMusik,
                     LocalDate tanggalPinjam, LocalDate tanggalKembali) {
        this.pelanggan = pelanggan;
        this.alatMusik = alatMusik;
        this.tanggalPinjam = tanggalPinjam;
        this.tanggalKembali = tanggalKembali;
        this.status = "aktif";
    }

    // Methods sesuai class diagram
    public void buatTransaksi() {
        this.status = "aktif";
    }

    public double hitungDenda() {
        if (tanggalAktual == null) {
            LocalDate today = LocalDate.now();
            if (today.isAfter(tanggalKembali)) {
                long hariTerlambat = ChronoUnit.DAYS.between(tanggalKembali, today);
                return hariTerlambat * Denda.TARIF_DENDA_PER_HARI;
            }
            return 0;
        }
        if (tanggalAktual.isAfter(tanggalKembali)) {
            long hariTerlambat = ChronoUnit.DAYS.between(tanggalKembali, tanggalAktual);
            return hariTerlambat * Denda.TARIF_DENDA_PER_HARI;
        }
        return 0;
    }

    public int getHariTerlambat() {
        LocalDate acuan = tanggalAktual != null ? tanggalAktual : LocalDate.now();
        if (acuan.isAfter(tanggalKembali)) {
            return (int) ChronoUnit.DAYS.between(tanggalKembali, acuan);
        }
        return 0;
    }

    public boolean isTerlambat() {
        LocalDate acuan = tanggalAktual != null ? tanggalAktual : LocalDate.now();
        return acuan.isAfter(tanggalKembali);
    }

    public long getDurasiSewa() {
        return ChronoUnit.DAYS.between(tanggalPinjam, tanggalKembali);
    }

    public double getTotalBiaya() {
        return getDurasiSewa() * (alatMusik != null ? alatMusik.getHargaSewa() : 0);
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Pelanggan getPelanggan() { return pelanggan; }
    public void setPelanggan(Pelanggan pelanggan) { this.pelanggan = pelanggan; }

    public AlatMusik getAlatMusik() { return alatMusik; }
    public void setAlatMusik(AlatMusik alatMusik) { this.alatMusik = alatMusik; }

    public LocalDate getTanggalPinjam() { return tanggalPinjam; }
    public void setTanggalPinjam(LocalDate tanggalPinjam) { this.tanggalPinjam = tanggalPinjam; }

    public LocalDate getTanggalKembali() { return tanggalKembali; }
    public void setTanggalKembali(LocalDate tanggalKembali) { this.tanggalKembali = tanggalKembali; }

    public LocalDate getTanggalAktual() { return tanggalAktual; }
    public void setTanggalAktual(LocalDate tanggalAktual) { this.tanggalAktual = tanggalAktual; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getIdPelanggan() { return idPelanggan; }
    public void setIdPelanggan(int idPelanggan) { this.idPelanggan = idPelanggan; }

    public int getIdAlat() { return idAlat; }
    public void setIdAlat(int idAlat) { this.idAlat = idAlat; }

    public String getNamaPelanggan() { return namaPelanggan; }
    public void setNamaPelanggan(String namaPelanggan) { this.namaPelanggan = namaPelanggan; }

    public String getNamaAlat() { return namaAlat; }
    public void setNamaAlat(String namaAlat) { this.namaAlat = namaAlat; }

    public double getJumlahDenda() { return jumlahDenda; }
    public void setJumlahDenda(double jumlahDenda) { this.jumlahDenda = jumlahDenda; }
}
