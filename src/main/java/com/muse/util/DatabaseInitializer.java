package com.muse.util;

import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;

/**
 * ============================================================
 * DatabaseInitializer — Membuat Struktur Tabel & Data Awal
 * ============================================================
 *
 * ALUR INISIALISASI (dipanggil SEKALI saat app start):
 *
 *  MainApp.start()
 *      └─→ DatabaseInitializer.initialize()
 *              ├─→ DatabaseConnection.getConnection()  [buka / buat muse.db]
 *              ├─→ PRAGMA foreign_keys = ON            [aktifkan relasi FK]
 *              ├─→ CREATE TABLE IF NOT EXISTS ...      [buat 5 tabel]
 *              ├─→ insertDefaultAdmin()                [buat admin jika belum ada]
 *              └─→ insertDataDummy()                   [isi data contoh jika kosong]
 *
 * STRUKTUR DATABASE (5 tabel):
 *
 *  tbl_admin       → data login admin
 *  tbl_alat_musik  → inventaris alat
 *  tbl_pelanggan   → data pelanggan
 *  tbl_transaksi   → transaksi penyewaan (FK ke pelanggan & alat)
 *  tbl_denda       → denda keterlambatan (FK ke transaksi, 1:1)
 *
 * RELASI:
 *  Pelanggan  1 ──── * Transaksi
 *  AlatMusik  1 ──── * Transaksi
 *  Transaksi  1 ──── 0..1 Denda
 */
public class DatabaseInitializer {

    public static void initialize() {
        System.out.println("[DB] Memulai inisialisasi database...");
        System.out.println("[DB] Lokasi file: " + DatabaseConnection.getDatabasePath());

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            // ── LANGKAH 1: Aktifkan Foreign Key constraint ──────────────────
            // SQLite secara default TIDAK enforce FK, harus diaktifkan manual
            stmt.execute("PRAGMA foreign_keys = ON");

            // ── LANGKAH 2: Buat Tabel ───────────────────────────────────────
            createTableAdmin(stmt);
            createTableAlatMusik(stmt);
            createTablePelanggan(stmt);
            createTableTransaksi(stmt);
            createTableDenda(stmt);

            System.out.println("[DB] Semua tabel berhasil dibuat / sudah ada.");

            // ── LANGKAH 3: Insert Data Default ──────────────────────────────
            insertDefaultAdmin(conn);
            insertDataDummy(conn);

            System.out.println("[DB] Inisialisasi selesai ✓");

        } catch (SQLException e) {
            System.err.println("[DB] GAGAL inisialisasi: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ════════════════════════════════════════════════════════════
    // CREATE TABLE
    // ════════════════════════════════════════════════════════════

    /**
     * Tabel admin — menyimpan kredensial login
     *
     * Kolom:
     *  id       : PRIMARY KEY auto increment
     *  username : unik, tidak boleh sama
     *  password : di-hash menggunakan BCrypt (bukan plain text!)
     */
    private static void createTableAdmin(Statement stmt) throws SQLException {
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS tbl_admin (
                id       INTEGER      PRIMARY KEY AUTOINCREMENT,
                username VARCHAR(50)  NOT NULL UNIQUE,
                password VARCHAR(255) NOT NULL
            )
        """);
        System.out.println("[DB]  ✓ tbl_admin");
    }

    /**
     * Tabel alat_musik — inventaris semua alat yang bisa disewa
     *
     * Kolom:
     *  id         : PRIMARY KEY auto increment
     *  nama       : nama alat (misal: Gitar Akustik Yamaha)
     *  jenis      : kategori (Gitar, Drum, Keyboard, dll)
     *  harga_sewa : tarif per hari dalam Rupiah
     *  status     : 'tersedia' atau 'disewa'
     */
    private static void createTableAlatMusik(Statement stmt) throws SQLException {
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS tbl_alat_musik (
                id         INTEGER      PRIMARY KEY AUTOINCREMENT,
                nama       VARCHAR(100) NOT NULL,
                jenis      VARCHAR(50)  NOT NULL,
                harga_sewa REAL         NOT NULL CHECK(harga_sewa > 0),
                status     VARCHAR(20)  NOT NULL DEFAULT 'tersedia'
                               CHECK(status IN ('tersedia', 'disewa'))
            )
        """);
        System.out.println("[DB]  ✓ tbl_alat_musik");
    }

    /**
     * Tabel pelanggan — data customer yang menyewa
     *
     * Kolom:
     *  id     : PRIMARY KEY auto increment
     *  nama   : nama lengkap pelanggan
     *  kontak : nomor HP aktif
     *  alamat : alamat lengkap
     *
     * NOTE: Tidak ada DELETE pelanggan karena terikat histori transaksi
     */
    private static void createTablePelanggan(Statement stmt) throws SQLException {
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS tbl_pelanggan (
                id     INTEGER      PRIMARY KEY AUTOINCREMENT,
                nama   VARCHAR(100) NOT NULL,
                kontak VARCHAR(20)  NOT NULL,
                alamat TEXT         NOT NULL
            )
        """);
        System.out.println("[DB]  ✓ tbl_pelanggan");
    }

    /**
     * Tabel transaksi — inti sistem penyewaan
     *
     * Kolom:
     *  id              : PRIMARY KEY auto increment
     *  id_pelanggan    : FK → tbl_pelanggan.id
     *  id_alat         : FK → tbl_alat_musik.id
     *  tanggal_pinjam  : tanggal mulai sewa
     *  tanggal_kembali : estimasi tanggal pengembalian
     *  tanggal_aktual  : tanggal aktual dikembalikan (NULL jika belum kembali)
     *  status          : 'aktif' / 'selesai' / 'terlambat'
     *
     * RELASI:
     *  FOREIGN KEY(id_pelanggan) → tbl_pelanggan(id)
     *  FOREIGN KEY(id_alat)      → tbl_alat_musik(id)
     */
    private static void createTableTransaksi(Statement stmt) throws SQLException {
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS tbl_transaksi (
                id              INTEGER     PRIMARY KEY AUTOINCREMENT,
                id_pelanggan    INTEGER     NOT NULL,
                id_alat         INTEGER     NOT NULL,
                tanggal_pinjam  DATE        NOT NULL,
                tanggal_kembali DATE        NOT NULL,
                tanggal_aktual  DATE,
                status          VARCHAR(20) NOT NULL DEFAULT 'aktif'
                                    CHECK(status IN ('aktif', 'selesai', 'terlambat')),
                FOREIGN KEY (id_pelanggan) REFERENCES tbl_pelanggan(id),
                FOREIGN KEY (id_alat)      REFERENCES tbl_alat_musik(id)
            )
        """);
        System.out.println("[DB]  ✓ tbl_transaksi");
    }

    /**
     * Tabel denda — rekap keterlambatan pengembalian
     *
     * Kolom:
     *  id             : PRIMARY KEY auto increment
     *  id_transaksi   : FK → tbl_transaksi.id (UNIQUE = 1 transaksi max 1 denda)
     *  hari_terlambat : jumlah hari melewati tanggal_kembali
     *  jumlah_denda   : hari_terlambat × Rp 25.000
     *
     * RELASI: Transaksi → Denda (1 ke 0..1)
     */
    private static void createTableDenda(Statement stmt) throws SQLException {
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS tbl_denda (
                id             INTEGER PRIMARY KEY AUTOINCREMENT,
                id_transaksi   INTEGER NOT NULL UNIQUE,
                hari_terlambat INTEGER NOT NULL CHECK(hari_terlambat > 0),
                jumlah_denda   REAL    NOT NULL CHECK(jumlah_denda > 0),
                FOREIGN KEY (id_transaksi) REFERENCES tbl_transaksi(id)
            )
        """);
        System.out.println("[DB]  ✓ tbl_denda");
    }

    // ════════════════════════════════════════════════════════════
    // INSERT DATA DEFAULT & DUMMY
    // ════════════════════════════════════════════════════════════

    /**
     * Insert admin default jika tbl_admin kosong.
     * Password di-hash dengan BCrypt sebelum disimpan.
     * Username: admin | Password: admin123
     */
    private static void insertDefaultAdmin(Connection conn) throws SQLException {
        String cek = "SELECT COUNT(*) FROM tbl_admin";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(cek)) {
            if (rs.next() && rs.getInt(1) == 0) {
                String hashed = BCrypt.hashpw("admin123", BCrypt.gensalt(12));
                String sql = "INSERT INTO tbl_admin (username, password) VALUES (?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, "admin");
                    ps.setString(2, hashed);
                    ps.executeUpdate();
                }
                System.out.println("[DB]  ✓ Admin default: username=admin, password=admin123");
            } else {
                System.out.println("[DB]  ✓ Admin sudah ada, skip.");
            }
        }
    }

    /**
     * Insert data dummy untuk demo/testing.
     * Hanya dijalankan jika tbl_alat_musik dan tbl_pelanggan kosong.
     *
     * Data dummy:
     *  - 5 Alat Musik (3 tersedia, 2 disewa)
     *  - 4 Pelanggan
     *  - 3 Transaksi (2 aktif, 1 selesai)
     *  - 1 Denda (dari transaksi yang terlambat)
     */
    private static void insertDataDummy(Connection conn) throws SQLException {
        // Cek apakah alat sudah ada
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM tbl_alat_musik")) {
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("[DB]  ✓ Data dummy sudah ada, skip.");
                return;
            }
        }

        System.out.println("[DB]  Memasukkan data dummy...");

        // ── ALAT MUSIK ─────────────────────────────────────────────────────
        String sqlAlat = "INSERT INTO tbl_alat_musik (nama, jenis, harga_sewa, status) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sqlAlat)) {

            // ID 1 - Gitar (disewa oleh transaksi aktif)
            ps.setString(1, "Gitar Akustik Yamaha");
            ps.setString(2, "Gitar");
            ps.setDouble(3, 50000);
            ps.setString(4, "disewa");
            ps.addBatch();

            // ID 2 - Keyboard (disewa oleh transaksi terlambat)
            ps.setString(1, "Keyboard Roland E-X10");
            ps.setString(2, "Keyboard");
            ps.setDouble(3, 80000);
            ps.setString(4, "disewa");
            ps.addBatch();

            // ID 3 - Drum (tersedia)
            ps.setString(1, "Drum Pearl Export");
            ps.setString(2, "Drum");
            ps.setDouble(3, 120000);
            ps.setString(4, "tersedia");
            ps.addBatch();

            // ID 4 - Bass (tersedia)
            ps.setString(1, "Bass Fender Squier");
            ps.setString(2, "Gitar Bass");
            ps.setDouble(3, 60000);
            ps.setString(4, "tersedia");
            ps.addBatch();

            // ID 5 - Biola (tersedia)
            ps.setString(1, "Biola Suzuki 4/4");
            ps.setString(2, "Biola");
            ps.setDouble(3, 75000);
            ps.setString(4, "tersedia");
            ps.addBatch();

            ps.executeBatch();
            System.out.println("[DB]    → 5 alat musik ditambahkan");
        }

        // ── PELANGGAN ──────────────────────────────────────────────────────
        String sqlPel = "INSERT INTO tbl_pelanggan (nama, kontak, alamat) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sqlPel)) {

            ps.setString(1, "Budi Santoso");
            ps.setString(2, "081234567890");
            ps.setString(3, "Jl. Malioboro No. 15, Yogyakarta");
            ps.addBatch();

            ps.setString(1, "Siti Rahayu");
            ps.setString(2, "082198765432");
            ps.setString(3, "Jl. Mawar No. 12, Sleman");
            ps.addBatch();

            ps.setString(1, "Agus Prasetyo");
            ps.setString(2, "085612345678");
            ps.setString(3, "Jl. Anggrek No. 3, Bantul");
            ps.addBatch();

            ps.setString(1, "Dewi Kurniawati");
            ps.setString(2, "087812345678");
            ps.setString(3, "Jl. Kenanga No. 7, Gunungkidul");
            ps.addBatch();

            ps.executeBatch();
            System.out.println("[DB]    → 4 pelanggan ditambahkan");
        }

        // ── TRANSAKSI ──────────────────────────────────────────────────────
        // Gunakan tanggal relatif dari hari ini
        String today      = java.time.LocalDate.now().toString();
        String min3       = java.time.LocalDate.now().minusDays(3).toString();
        String min10      = java.time.LocalDate.now().minusDays(10).toString();
        String plus4      = java.time.LocalDate.now().plusDays(4).toString();
        String min5       = java.time.LocalDate.now().minusDays(5).toString();
        String min12      = java.time.LocalDate.now().minusDays(12).toString();

        String sqlTrx = """
            INSERT INTO tbl_transaksi
            (id_pelanggan, id_alat, tanggal_pinjam, tanggal_kembali, tanggal_aktual, status)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement ps = conn.prepareStatement(sqlTrx)) {

            // TRX-1: Budi sewa Gitar Akustik, AKTIF (belum dikembalikan)
            ps.setInt(1, 1);         // id_pelanggan = Budi
            ps.setInt(2, 1);         // id_alat = Gitar Akustik
            ps.setString(3, min3);   // pinjam 3 hari lalu
            ps.setString(4, plus4);  // estimasi kembali 4 hari lagi
            ps.setNull(5, Types.DATE); // belum dikembalikan
            ps.setString(6, "aktif");
            ps.addBatch();

            // TRX-2: Siti sewa Keyboard, TERLAMBAT (estimasi sudah lewat)
            ps.setInt(1, 2);         // id_pelanggan = Siti
            ps.setInt(2, 2);         // id_alat = Keyboard
            ps.setString(3, min10);  // pinjam 10 hari lalu
            ps.setString(4, min5);   // estimasi kembali 5 hari lalu (TERLAMBAT)
            ps.setNull(5, Types.DATE); // belum dikembalikan
            ps.setString(6, "terlambat");
            ps.addBatch();

            // TRX-3: Agus sewa Drum, SELESAI (sudah dikembalikan)
            ps.setInt(1, 3);         // id_pelanggan = Agus
            ps.setInt(2, 3);         // id_alat = Drum (sekarang tersedia)
            ps.setString(3, min12);  // pinjam 12 hari lalu
            ps.setString(4, min5);   // estimasi kembali 5 hari lalu
            ps.setString(5, min5);   // dikembalikan tepat waktu
            ps.setString(6, "selesai");
            ps.addBatch();

            ps.executeBatch();
            System.out.println("[DB]    → 3 transaksi ditambahkan");
        }

        // ── DENDA ──────────────────────────────────────────────────────────
        // TRX-2 (Siti, Keyboard) terlambat 5 hari → denda 5 × 25.000 = 125.000
        String sqlDenda = """
            INSERT INTO tbl_denda (id_transaksi, hari_terlambat, jumlah_denda)
            VALUES (?, ?, ?)
        """;
        try (PreparedStatement ps = conn.prepareStatement(sqlDenda)) {
            ps.setInt(1, 2);        // id_transaksi = TRX-2
            ps.setInt(2, 5);        // 5 hari terlambat
            ps.setDouble(3, 125000); // 5 × 25.000
            ps.executeUpdate();
            System.out.println("[DB]    → 1 denda ditambahkan (TRX-002: Rp 125.000)");
        }

        System.out.println("[DB]  ✓ Data dummy selesai dimasukkan.");
    }
}
