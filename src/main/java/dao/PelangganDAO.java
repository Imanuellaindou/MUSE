package dao;


import com.muse.model.Pelanggan;
import util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object untuk tabel tbl_pelanggan
 * Implementasi CRUD sesuai proposal
 */

public class PelangganDAO {
    public List<Pelanggan> findAll() {
        List<Pelanggan> list = new ArrayList<>();
        String sql = "SELECT * FROM tbl_pelanggan ORDER BY id";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Pelanggan> findByKeyword(String keyword) {
        List<Pelanggan> list = new ArrayList<>();
        String sql = "SELECT * FROM tbl_pelanggan WHERE LOWER(nama) LIKE ? OR kontak LIKE ? ORDER BY id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String kw = "%" + keyword.toLowerCase() + "%";
            ps.setString(1, kw);
            ps.setString(2, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Pelanggan findById(int id) {
        String sql = "SELECT * FROM tbl_pelanggan WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean insert(Pelanggan pelanggan) {
        String sql = "INSERT INTO tbl_pelanggan (nama, kontak, alamat) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pelanggan.getNama());
            ps.setString(2, pelanggan.getKontak());
            ps.setString(3, pelanggan.getAlamat());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(Pelanggan pelanggan) {
        String sql = "UPDATE tbl_pelanggan SET nama=?, kontak=?, alamat=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pelanggan.getNama());
            ps.setString(2, pelanggan.getKontak());
            ps.setString(3, pelanggan.getAlamat());
            ps.setInt(4, pelanggan.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int countTransaksiByPelanggan(int idPelanggan) {
        String sql = "SELECT COUNT(*) FROM tbl_transaksi WHERE id_pelanggan = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPelanggan);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private Pelanggan mapRow(ResultSet rs) throws SQLException {
        return new Pelanggan(
                rs.getInt("id"),
                rs.getString("nama"),
                rs.getString("kontak"),
                rs.getString("alamat")
        );
    }
}
