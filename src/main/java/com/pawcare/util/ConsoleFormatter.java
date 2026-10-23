package com.pawcare.util;

import java.util.List;

/**
 * Utility for formatting tabular output on the terminal.
 * Gives the console interface a clean, professional appearance.
 */
public class ConsoleFormatter {

    /**
     * Prints a beautiful ASCII table given headers and rows of string data.
     */
    public static void printTable(String title, String[] headers, List<String[]> rows) {
        if (headers == null || headers.length == 0) {
            return;
        }

        int colCount = headers.length;
        int[] colWidths = new int[colCount];

        // Header widths
        for (int i = 0; i < colCount; i++) {
            colWidths[i] = headers[i].length();
        }

        // Row cell widths
        if (rows != null) {
            for (String[] row : rows) {
                for (int i = 0; i < colCount && i < row.length; i++) {
                    if (row[i] != null && row[i].length() > colWidths[i]) {
                        colWidths[i] = Math.min(row[i].length(), 40); // Cap max column width to prevent overflow
                    }
                }
            }
        }

        // Total width calculation
        int totalWidth = 1; // leading border
        for (int w : colWidths) {
            totalWidth += (w + 3); // space + content + space + '|'
        }

        // Print Title Header
        System.out.println();
        if (title != null && !title.isEmpty()) {
            System.out.println("  ==> " + title.toUpperCase());
        }

        // Horizontal separator line
        printDivider(colWidths);

        // Header Row
        System.out.print("|");
        for (int i = 0; i < colCount; i++) {
            System.out.printf(" %-" + colWidths[i] + "s |", headers[i]);
        }
        System.out.println();

        printDivider(colWidths);

        // Data Rows
        if (rows == null || rows.isEmpty()) {
            System.out.println("| " + padRight("No records found.", totalWidth - 4) + " |");
        } else {
            for (String[] row : rows) {
                System.out.print("|");
                for (int i = 0; i < colCount; i++) {
                    String val = (i < row.length && row[i] != null) ? row[i] : "";
                    if (val.length() > colWidths[i]) {
                        val = val.substring(0, colWidths[i] - 3) + "...";
                    }
                    System.out.printf(" %-" + colWidths[i] + "s |", val);
                }
                System.out.println();
            }
        }

        printDivider(colWidths);
        System.out.printf("  Total records: %d%n%n", (rows != null ? rows.size() : 0));
    }

    private static void printDivider(int[] colWidths) {
        System.out.print("+");
        for (int w : colWidths) {
            for (int i = 0; i < w + 2; i++) {
                System.out.print("-");
            }
            System.out.print("+");
        }
        System.out.println();
    }

    private static String padRight(String s, int n) {
        return String.format("%-" + n + "s", s);
    }
}
