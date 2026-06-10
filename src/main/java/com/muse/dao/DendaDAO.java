package com.muse.dao;

import com.muse.model.Denda;
import com.muse.util.DatabaseConnection;

import java.sql.*;

/**
 * Data Access Object untuk tabel tbl_denda
 * Sesuai arsitektur Data Layer proposal
 */
public class DendaDAO {

    public boolean insert(Denda denda) {
        String sql = "INSERT INTO tbl_denda (id_transaksi, hari_terlambat, jumlah_denda) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, denda.getIdTransaksi());
            ps.setInt(2, denda.getHariTerlambat());
            ps.setDouble(3, denda.getJumlahDenda());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Denda findByTransaksi(int idTransaksi) {
        String sql = "SELECT * FROM tbl_denda WHERE id_transaksi = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idTransaksi);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Denda d = new Denda();
                d.setId(rs.getInt("id"));
                d.setIdTransaksi(rs.getInt("id_transaksi"));
                d.setHariTerlambat(rs.getInt("hari_terlambat"));
                d.setJumlahDenda(rs.getDouble("jumlah_denda"));
                return d;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public double getTotalDendaByBulan(int tahun, int bulan) {
        String sql = """
            SELECT COALESCE(SUM(d.jumlah_denda), 0)
            FROM tbl_denda d
            JOIN tbl_transaksi t ON d.id_transaksi = t.id
            WHERE strftime('%Y', t.tanggal_pinjam) = ?
              AND strftime('%m', t.tanggal_pinjam) = ?
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, String.valueOf(tahun));
            ps.setString(2, String.format("%02d", bulan));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int countDendaAktif() {
        String sql = """
            SELECT COUNT(*) FROM tbl_transaksi
            WHERE status IN ('aktif', 'terlambat')
            AND tanggal_kembali < date('now')
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
