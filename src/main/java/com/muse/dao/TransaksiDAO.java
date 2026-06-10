package com.muse.dao;

import com.muse.model.Transaksi;
import com.muse.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object untuk tabel tbl_transaksi
 * Sesuai arsitektur Data Layer proposal
 */
public class TransaksiDAO {

    public boolean insert(Transaksi t) {
        String sql = """
            INSERT INTO tbl_transaksi 
            (id_pelanggan, id_alat, tanggal_pinjam, tanggal_kembali, status)
            VALUES (?, ?, ?, ?, 'aktif')
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, t.getPelanggan().getId());
            ps.setInt(2, t.getAlatMusik().getId());
            ps.setString(3, t.getTanggalPinjam().toString());
            ps.setString(4, t.getTanggalKembali().toString());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updatePengembalian(int id, LocalDate tanggalAktual, String status) {
        String sql = "UPDATE tbl_transaksi SET tanggal_aktual=?, status=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tanggalAktual.toString());
            ps.setString(2, status);
            ps.setInt(3, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Transaksi> findAktif() {
        return findByStatus("aktif", "terlambat");
    }

    public List<Transaksi> findAll() {
        String sql = buildSelectSQL() + " ORDER BY t.id DESC";
        return executeQuery(sql);
    }

    public List<Transaksi> findWithFilter(String keyword, String status, LocalDate dari, LocalDate sampai) {
        StringBuilder sql = new StringBuilder(buildSelectSQL());
        sql.append(" WHERE 1=1");
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (LOWER(p.nama) LIKE '%").append(keyword.toLowerCase()).append("%'")
               .append(" OR LOWER(a.nama) LIKE '%").append(keyword.toLowerCase()).append("%')");
        }
        if (status != null && !status.equals("Semua Status")) {
            sql.append(" AND LOWER(t.status) = '").append(status.toLowerCase()).append("'");
        }
        if (dari != null) {
            sql.append(" AND t.tanggal_pinjam >= '").append(dari).append("'");
        }
        if (sampai != null) {
            sql.append(" AND t.tanggal_pinjam <= '").append(sampai).append("'");
        }
        sql.append(" ORDER BY t.id DESC");
        return executeQuery(sql.toString());
    }

    public List<Transaksi> findByBulan(int tahun, int bulan) {
        String sql = buildSelectSQL() +
            " WHERE strftime('%Y', t.tanggal_pinjam) = '" + tahun + "'" +
            " AND strftime('%m', t.tanggal_pinjam) = '" + String.format("%02d", bulan) + "'" +
            " ORDER BY t.tanggal_pinjam";
        return executeQuery(sql);
    }

    public int countDendaAktif() {
        String sql = "SELECT COUNT(*) FROM tbl_transaksi WHERE status IN ('aktif', 'terlambat')" +
                     " AND tanggal_kembali < date('now')";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int countAktif() {
        String sql = "SELECT COUNT(*) FROM tbl_transaksi WHERE status IN ('aktif', 'terlambat')";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private List<Transaksi> findByStatus(String... statuses) {
        StringBuilder inClause = new StringBuilder();
        for (int i = 0; i < statuses.length; i++) {
            inClause.append("'").append(statuses[i]).append("'");
            if (i < statuses.length - 1) inClause.append(",");
        }
        String sql = buildSelectSQL() + " WHERE t.status IN (" + inClause + ") ORDER BY t.id DESC";
        return executeQuery(sql);
    }

    private String buildSelectSQL() {
        return """
            SELECT t.*, p.nama as nama_pelanggan, a.nama as nama_alat,
                   a.harga_sewa, a.jenis,
                   p.kontak, p.alamat,
                   COALESCE(d.jumlah_denda, 0) as jumlah_denda
            FROM tbl_transaksi t
            JOIN tbl_pelanggan p ON t.id_pelanggan = p.id
            JOIN tbl_alat_musik a ON t.id_alat = a.id
            LEFT JOIN tbl_denda d ON t.id = d.id_transaksi
        """;
    }

    private List<Transaksi> executeQuery(String sql) {
        List<Transaksi> list = new ArrayList<>();
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

    private Transaksi mapRow(ResultSet rs) throws SQLException {
        Transaksi t = new Transaksi();
        t.setId(rs.getInt("id"));
        t.setIdPelanggan(rs.getInt("id_pelanggan"));
        t.setIdAlat(rs.getInt("id_alat"));
        t.setNamaPelanggan(rs.getString("nama_pelanggan"));
        t.setNamaAlat(rs.getString("nama_alat"));
        t.setStatus(rs.getString("status"));
        t.setJumlahDenda(rs.getDouble("jumlah_denda"));

        String tPinjam = rs.getString("tanggal_pinjam");
        String tKembali = rs.getString("tanggal_kembali");
        String tAktual = rs.getString("tanggal_aktual");

        if (tPinjam != null) t.setTanggalPinjam(LocalDate.parse(tPinjam));
        if (tKembali != null) t.setTanggalKembali(LocalDate.parse(tKembali));
        if (tAktual != null) t.setTanggalAktual(LocalDate.parse(tAktual));

        // Set objek pelanggan dan alat (partial, untuk display)
        com.muse.model.Pelanggan p = new com.muse.model.Pelanggan();
        p.setId(rs.getInt("id_pelanggan"));
        p.setNama(rs.getString("nama_pelanggan"));
        try {
            p.setKontak(rs.getString("kontak"));
            p.setAlamat(rs.getString("alamat"));
        } catch (Exception ignored) {}
        t.setPelanggan(p);

        com.muse.model.AlatMusik a = new com.muse.model.AlatMusik();
        a.setId(rs.getInt("id_alat"));
        a.setNama(rs.getString("nama_alat"));
        try {
            a.setHargaSewa(rs.getDouble("harga_sewa"));
            a.setJenis(rs.getString("jenis"));
        } catch (Exception ignored) {}
        t.setAlatMusik(a);

        return t;
    }
}
