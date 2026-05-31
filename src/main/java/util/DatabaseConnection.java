package util;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * ============================================================
 * DatabaseConnection — Singleton Connection ke SQLite via JDBC
 * ============================================================
 *
 * ALUR KONEKSI:
 * 1. MainApp.start() dipanggil saat aplikasi berjalan
 * 2. DatabaseInitializer.initialize() memanggil getConnection()
 * 3. getConnection() membuka file muse.db di folder kerja aplikasi
 * 4. Jika muse.db belum ada, SQLite otomatis membuatnya
 * 5. Connection disimpan sebagai singleton (satu koneksi dipakai semua DAO)
 * 6. Setiap DAO memanggil getConnection() untuk execute SQL
 * 7. MainApp.stop() memanggil closeConnection() saat app ditutup
 *
 * LOKASI FILE DATABASE:
 * - Development (IntelliJ): [root-project]/muse/muse.db
 * - JAR: folder yang sama dengan file .jar
 *
 * JDBC URL FORMAT:
 * jdbc:sqlite:[path-ke-file]
 * jdbc:sqlite:muse.db  → path relatif dari working directory
 */

public class DatabaseConnection {
    // Nama file database SQLite
    private static final String DB_NAME = "muse.db";

    // JDBC URL — SQLite membuat file jika belum ada
    private static final String URL = "jdbc:sqlite:" + DB_NAME;

    // Singleton connection instance
    private static Connection connection = null;

    // Private constructor — tidak boleh di-instantiate
    private DatabaseConnection() {}

    /**
     * Mendapatkan koneksi database (singleton pattern).
     *
     * Alur:
     * 1. Cek apakah connection sudah ada dan belum tertutup
     * 2. Jika belum ada / sudah tertutup → buat koneksi baru
     * 3. Set PRAGMA untuk performa dan integritas data
     * 4. Return connection
     *
     * @return Connection ke SQLite database
     * @throws SQLException jika gagal membuka koneksi
     */
    public static synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                // Load SQLite JDBC driver (auto di Java 11+, eksplisit untuk kejelasan)
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException e) {
                throw new SQLException("SQLite JDBC driver tidak ditemukan. " +
                        "Pastikan dependency sqlite-jdbc ada di pom.xml", e);
            }

            // Buka / buat file database
            connection = DriverManager.getConnection(URL);

            // Konfigurasi SQLite
            try (var stmt = connection.createStatement()) {
                // Aktifkan Foreign Key constraint (default OFF di SQLite)
                stmt.execute("PRAGMA foreign_keys = ON");

                // WAL mode: lebih cepat untuk baca/tulis bersamaan
                stmt.execute("PRAGMA journal_mode = WAL");

                // Sinkronisasi normal: balance antara kecepatan dan keamanan
                stmt.execute("PRAGMA synchronous = NORMAL");

                // Cache size: 10MB untuk performa query
                stmt.execute("PRAGMA cache_size = 10000");
            }

            // Auto-commit ON: setiap SQL langsung tersimpan
            connection.setAutoCommit(true);

            System.out.println("[DB] Koneksi berhasil → " + DB_NAME);
        }
        return connection;
    }

    /**
     * Menutup koneksi database.
     * Dipanggil dari MainApp.stop() saat aplikasi ditutup.
     */
    public static void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                    System.out.println("[DB] Koneksi ditutup.");
                }
            } catch (SQLException e) {
                System.err.println("[DB] Gagal menutup koneksi: " + e.getMessage());
            } finally {
                connection = null;
            }
        }
    }

    /**
     * Cek apakah koneksi sedang aktif.
     * Berguna untuk debugging.
     */
    public static boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Mendapatkan path database yang sedang digunakan.
     */
    public static String getDatabasePath() {
        return System.getProperty("user.dir") + "/" + DB_NAME;
    }
}
