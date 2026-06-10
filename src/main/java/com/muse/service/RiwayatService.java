package service;

import com.muse.model.Transaksi;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

/**
 * Service layer untuk Riwayat Transaksi
 * Termasuk fitur export CSV
 */

public class RiwayatService {
    private final TransaksiDAO transaksiDAO;

    public RiwayatService() {
        this.transaksiDAO = new TransaksiDAO();
    }

    public List<Transaksi> getRiwayat(String keyword, String status,
                                      LocalDate dari, LocalDate sampai) {
        return transaksiDAO.findWithFilter(keyword, status, dari, sampai);
    }

    /**
     * Export data riwayat ke format CSV
     */
    public boolean exportCSV(List<Transaksi> data, String filePath) {
        try (FileWriter fw = new FileWriter(filePath)) {
            fw.write("ID,Pelanggan,Alat Musik,Tgl Pinjam,Tgl Kembali,Tgl Aktual,Denda,Status\n");
            for (Transaksi t : data) {
                fw.write(String.format("TRX-%03d,%s,%s,%s,%s,%s,%.0f,%s\n",
                        t.getId(),
                        t.getNamaPelanggan(),
                        t.getNamaAlat(),
                        t.getTanggalPinjam(),
                        t.getTanggalKembali(),
                        t.getTanggalAktual() != null ? t.getTanggalAktual() : "-",
                        t.getJumlahDenda(),
                        t.getStatus()
                ));
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
