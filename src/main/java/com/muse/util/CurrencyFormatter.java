package com.muse.util;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Utility untuk format mata uang Rupiah
 */
public class CurrencyFormatter {

    private static final Locale LOCALE_ID = Locale.forLanguageTag("id-ID");
    private static final NumberFormat FORMAT = NumberFormat.getCurrencyInstance(LOCALE_ID);

    public static String format(double amount) {
        return FORMAT.format(amount);
    }

    public static String formatSimple(double amount) {
        return "Rp " + String.format("%,.0f", amount);
    }
}
