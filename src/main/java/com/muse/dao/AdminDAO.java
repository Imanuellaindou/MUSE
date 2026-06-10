package com.muse.dao;

import com.muse.model.Admin;
import com.muse.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Data Access Object untuk tabel tbl_admin
 * Sesuai arsitektur Data Layer proposal
 */
public class AdminDAO {

    public Admin findByUsername(String username) {
        String sql = "SELECT * FROM tbl_admin WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Admin findById(int id) {
        String sql = "SELECT * FROM tbl_admin WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Admin mapRow(ResultSet rs) throws SQLException {
        return new Admin(
            rs.getInt("id"),
            rs.getString("username"),
            rs.getString("password")
        );
    }
}
