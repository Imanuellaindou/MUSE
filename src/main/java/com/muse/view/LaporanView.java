package com.muse.view;

import com.muse.model.Transaksi;
import com.muse.service.LaporanService;
import com.muse.util.CurrencyFormatter;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PrinterJob;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Halaman Laporan Penyewaan
 * Admin pilih bulan & mode (bulanan/mingguan).
 * Kartu: total transaksi, total pendapatan, total denda.
 * Bar chart jumlah transaksi per minggu.
 * Tombol Cetak.
 */
public class LaporanView {

    private VBox root;
    private VBox reportContent;
    private ComboBox<String> cbBulan;
    private ComboBox<Integer> cbTahun;
    private ComboBox<String> cbMode;

    private Label lblTotalTrx, lblTotalPendapatan, lblTotalDenda;
    private BarChart<String, Number> barChart;

    private final LaporanService laporanService = new LaporanService();

    public LaporanView() {
        build();
    }

    private void build() {
        root = new VBox(16);
        root.setPadding(new Insets(0));
        VBox.setVgrow(root, Priority.ALWAYS);

        // Toolbar laporan
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setStyle("-fx-background-color: white; -fx-padding: 12; -fx-background-radius: 6;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 6, 0, 0, 1);");

        Label lblPeriode = new Label("Periode:");
        lblPeriode.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");

        cbBulan = new ComboBox<>();
        cbBulan.getItems().addAll(
            "Januari", "Februari", "Maret", "April", "Mei", "Juni",
            "Juli", "Agustus", "September", "Oktober", "November", "Desember"
        );
        // Set bulan saat ini (1=Jan, getValue() dari getMonthValue() sudah 1-based)
        int bulanSekarang = LocalDate.now().getMonthValue(); // 1-12
        String[] namaBulan = {"Januari","Februari","Maret","April","Mei","Juni",
                              "Juli","Agustus","September","Oktober","November","Desember"};
        cbBulan.setValue(namaBulan[bulanSekarang - 1]);
        cbBulan.getStyleClass().add("combo-box");
        cbBulan.setPrefWidth(130);

        cbTahun = new ComboBox<>();
        int tahunNow = LocalDate.now().getYear();
        for (int y = tahunNow; y >= tahunNow - 5; y--) cbTahun.getItems().add(y);
        cbTahun.setValue(tahunNow);
        cbTahun.getStyleClass().add("combo-box");
        cbTahun.setPrefWidth(90);

        cbMode = new ComboBox<>();
        cbMode.getItems().addAll("Bulanan", "Mingguan");
        cbMode.setValue("Bulanan");
        cbMode.getStyleClass().add("combo-box");
        cbMode.setPrefWidth(110);

        Button btnTampilkan = new Button("Tampilkan");
        btnTampilkan.getStyleClass().add("btn-primary");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnCetak = new Button("⎙ Cetak");
        btnCetak.getStyleClass().add("btn-secondary");
        btnCetak.setOnAction(e -> cetakLaporan());

        toolbar.getChildren().addAll(lblPeriode, cbBulan, cbTahun, cbMode, btnTampilkan, spacer, btnCetak);

        // Report content area
        reportContent = new VBox(16);
        VBox.setVgrow(reportContent, Priority.ALWAYS);

        // Kartu-kartu ringkasan
        HBox cardsRow = new HBox(16);

        lblTotalTrx = new Label("—");
        lblTotalTrx.getStyleClass().addAll("stat-number", "stat-number-blue");
        VBox cardTrx = buildReportCard(lblTotalTrx, "Total Transaksi");

        lblTotalPendapatan = new Label("—");
        lblTotalPendapatan.getStyleClass().addAll("stat-number", "stat-number-green");
        lblTotalPendapatan.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #16A34A;");
        VBox cardPendapatan = buildReportCard(lblTotalPendapatan, "Total Pendapatan");

        lblTotalDenda = new Label("—");
        lblTotalDenda.getStyleClass().addAll("stat-number", "stat-number-red");
        lblTotalDenda.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #DC2626;");
        VBox cardDenda = buildReportCard(lblTotalDenda, "Total Denda");

        HBox.setHgrow(cardTrx, Priority.ALWAYS);
        HBox.setHgrow(cardPendapatan, Priority.ALWAYS);
        HBox.setHgrow(cardDenda, Priority.ALWAYS);
        cardsRow.getChildren().addAll(cardTrx, cardPendapatan, cardDenda);

        // Bar chart
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Minggu");
        yAxis.setLabel("Jumlah Transaksi");
        barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Grafik Transaksi per Minggu (BarChart)");
        barChart.setAnimated(false);
        barChart.getStyleClass().add("bar-chart");
        barChart.setPrefHeight(280);
        VBox.setVgrow(barChart, Priority.ALWAYS);

        VBox chartBox = new VBox(8);
        chartBox.getStyleClass().add("chart-container");
        Label chartTitle = new Label("Grafik Transaksi per Minggu (BarChart)");
        chartTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #374151;");
        Label chartNote = new Label("* Grafik menggunakan komponen BarChart dari JavaFX Charts API");
        chartNote.setStyle("-fx-font-size: 10px; -fx-text-fill: #9CA3AF; -fx-padding: 4 0 0 0;");
        chartBox.getChildren().addAll(chartTitle, barChart, chartNote);

        reportContent.getChildren().addAll(cardsRow, chartBox);

        // Title laporan
        Label titleLaporan = new Label("Laporan Penyewaan");
        titleLaporan.getStyleClass().add("page-title");

        // Events
        btnTampilkan.setOnAction(e -> tampilkanLaporan());

        root.getChildren().addAll(titleLaporan, toolbar, reportContent);

        // Auto tampilkan bulan ini
        tampilkanLaporan();
    }

    private VBox buildReportCard(Label numLabel, String text) {
        VBox card = new VBox(4);
        card.getStyleClass().add("stat-card");
        card.setAlignment(Pos.CENTER);
        Label lbl = new Label(text);
        lbl.getStyleClass().add("stat-label");
        card.getChildren().addAll(numLabel, lbl);
        return card;
    }

    private void tampilkanLaporan() {
        int bulanIdx = cbBulan.getSelectionModel().getSelectedIndex() + 1;
        if (bulanIdx <= 0) bulanIdx = LocalDate.now().getMonthValue(); // fallback
        int tahun = cbTahun.getValue();

        List<Transaksi> data = laporanService.getTransaksiByBulan(tahun, bulanIdx);

        // Update kartu
        int totalTrx = laporanService.getTotalTransaksi(data);
        double totalPendapatan = laporanService.getTotalPendapatan(data);
        double totalDenda = laporanService.getTotalDenda(tahun, bulanIdx);

        lblTotalTrx.setText(String.valueOf(totalTrx));
        lblTotalPendapatan.setText(CurrencyFormatter.formatSimple(totalPendapatan));
        lblTotalDenda.setText(CurrencyFormatter.formatSimple(totalDenda));

        // Update chart
        barChart.getData().clear();
        int[] perMinggu = laporanService.getTransaksiPerMinggu(data);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Transaksi");
        series.getData().add(new XYChart.Data<>("Minggu 1", perMinggu[0]));
        series.getData().add(new XYChart.Data<>("Minggu 2", perMinggu[1]));
        series.getData().add(new XYChart.Data<>("Minggu 3", perMinggu[2]));
        series.getData().add(new XYChart.Data<>("Minggu 4", perMinggu[3]));
        barChart.getData().add(series);
    }

    private void cetakLaporan() {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null) {
            boolean proceed = job.showPrintDialog(root.getScene().getWindow());
            if (proceed) {
                boolean printed = job.printPage(reportContent);
                if (printed) job.endJob();
                else new Alert(Alert.AlertType.ERROR, "Gagal mencetak laporan.").showAndWait();
            }
        } else {
            new Alert(Alert.AlertType.WARNING, "Printer tidak tersedia.").showAndWait();
        }
    }

    public VBox getRoot() { return root; }
}
