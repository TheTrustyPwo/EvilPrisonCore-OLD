package me.pwo.evilprisoncore.utils;

import java.math.BigDecimal;

public class Utils {
    public static double round(double value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }

    public static String formatNumber(double number, int type) {
        switch (type) {
            case 1:
                // Comma
                return String.format("%,.2f", number);
            case 2:
                // Unit

        }
    }
}
