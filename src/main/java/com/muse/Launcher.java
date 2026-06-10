package com.muse;

/**
 * Launcher class — entry point yang WAJIB ada untuk fat JAR JavaFX.
 *
 * Kenapa perlu class ini?
 * JavaFX 11+ membutuhkan module system. Jika Main class extends Application
 * di-load langsung dari classpath (bukan module-path), JVM akan throw:
 * "Error: JavaFX runtime components are missing"
 *
 * Solusi standar: buat class Launcher terpisah yang TIDAK extends Application,
 * lalu panggil MainApp.main() dari sini.
 * Class inilah yang dijadikan Main-Class di MANIFEST.MF.
 */
public class Launcher {
    public static void main(String[] args) {
        MainApp.main(args);
    }
}
