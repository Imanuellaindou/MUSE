package dao;

import com.muse.model.AlatMusik;
import util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object untuk tabel tbl_alat_musik
 * Implementasi CRUD sesuai proposal
 */

public class AlatMusikDAO {
    public List<AlatMusik> findAll() {
        List<AlatMusik> list = new ArrayList<>();
        String sql = "SELECT * FROM tbl_alat_musik ORDER BY id";
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

    public List<AlatMusik> findTersedia() {
        List<AlatMusik> list = new ArrayList<>();
        String sql = "SELECT * FROM tbl_alat_musik WHERE status = 'tersedia' ORDER BY nama";
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

    public List<AlatMusik> findByNamaOrStatus(String keyword, String status) {
        List<AlatMusik> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM tbl_alat_musik WHERE 1=1");
        List<String> params = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND LOWER(nama) LIKE ?");
            params.add("%" + keyword.toLowerCase() + "%");
        }
        if (status != null && !status.equals("Semua Status")) {
            sql.append(" AND LOWER(status) = ?");
            params.add(status.toLowerCase());
        }
        sql.append(" ORDER BY id");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setString(i + 1, params.get(i));
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public AlatMusik findById(int id) {
        String sql = "SELECT * FROM tbl_alat_musik WHERE id = ?";
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

    public boolean insert(AlatMusik alat) {
        String sql = "INSERT INTO tbl_alat_musik (nama, jenis, harga_sewa, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, alat.getNama());
            ps.setString(2, alat.getJenis());
            ps.setDouble(3, alat.getHargaSewa());
            ps.setString(4, "tersedia");
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(AlatMusik alat) {
        String sql = "UPDATE tbl_alat_musik SET nama=?, jenis=?, harga_sewa=?, status=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, alat.getNama());
            ps.setString(2, alat.getJenis());
            ps.setDouble(3, alat.getHargaSewa());
            ps.setString(4, alat.getStatus());
            ps.setInt(5, alat.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateStatus(int id, String status) {
        String sql = "UPDATE tbl_alat_musik SET status=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM tbl_alat_musik WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int getTotalAlat() {
        String sql = "SELECT COUNT(*) FROM tbl_alat_musik";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getTotalTersedia() {
        String sql = "SELECT COUNT(*) FROM tbl_alat_musik WHERE status='tersedia'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getTotalDisewa() {
        String sql = "SELECT COUNT(*) FROM tbl_alat_musik WHERE status='disewa'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private AlatMusik mapRow(ResultSet rs) throws SQLException {
        return new AlatMusik(
                rs.getInt("id"),
                rs.getString("nama"),
                rs.getString("jenis"),
                rs.getDouble("harga_sewa"),
                rs.getString("status")
        );
    }
}
