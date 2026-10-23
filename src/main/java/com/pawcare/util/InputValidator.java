package com.pawcare.util;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Utility class for robust console input validation.
 * Prevents runtime exceptions from improper user input and validates business rules.
 */
public class InputValidator {

    private static final Scanner SCANNER = new Scanner(System.in);
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10,15}$");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter TIME_SEC_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    /**
     * Reads a non-empty string from the console.
     */
    public static String readString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = SCANNER.nextLine().trim();
            if (!input.isEmpty()) {
                return input;
            }
            System.out.println("  [!] Input cannot be blank. Please try again.");
        }
    }

    /**
     * Reads an optional string (can be empty / pressed Enter).
     */
    public static String readOptionalString(String prompt) {
        System.out.print(prompt);
        String input = SCANNER.nextLine().trim();
        return input.isEmpty() ? null : input;
    }

    /**
     * Reads an integer within [min, max].
     */
    public static int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String line = SCANNER.nextLine().trim();
            try {
                int value = Integer.parseInt(line);
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.printf("  [!] Please enter a number between %d and %d.%n", min, max);
            } catch (NumberFormatException e) {
                System.out.println("  [!] Invalid number format. Please enter an integer.");
            }
        }
    }

    /**
     * Reads a positive integer (>= 1).
     */
    public static int readPositiveInt(String prompt) {
        return readInt(prompt, 1, Integer.MAX_VALUE);
    }

    /**
     * Reads a valid email address.
     */
    public static String readEmail(String prompt) {
        while (true) {
            System.out.print(prompt);
            String email = SCANNER.nextLine().trim();
            if (EMAIL_PATTERN.matcher(email).matches()) {
                return email;
            }
            System.out.println("  [!] Invalid email format (e.g. name@domain.com). Please try again.");
        }
    }

    /**
     * Reads a valid phone number (10 to 15 digits).
     */
    public static String readPhone(String prompt) {
        while (true) {
            System.out.print(prompt);
            String phone = SCANNER.nextLine().trim();
            if (PHONE_PATTERN.matcher(phone).matches()) {
                return phone;
            }
            System.out.println("  [!] Invalid phone number. Must be between 10 and 15 digits.");
        }
    }

    /**
     * Reads a positive monetary amount / decimal.
     */
    public static BigDecimal readBigDecimal(String prompt, double min) {
        while (true) {
            System.out.print(prompt);
            String line = SCANNER.nextLine().trim();
            try {
                BigDecimal val = new BigDecimal(line);
                if (val.compareTo(BigDecimal.valueOf(min)) >= 0) {
                    return val;
                }
                System.out.printf("  [!] Amount must be at least %.2f.%n", min);
            } catch (NumberFormatException e) {
                System.out.println("  [!] Invalid decimal format (e.g. 500.00). Please try again.");
            }
        }
    }

    /**
     * Reads a date in YYYY-MM-DD format.
     *
     * @param allowPast if false, rejects dates prior to current date
     */
    public static Date readDate(String prompt, boolean allowPast) {
        while (true) {
            System.out.print(prompt + " (YYYY-MM-DD): ");
            String line = SCANNER.nextLine().trim();
            try {
                LocalDate localDate = LocalDate.parse(line, DATE_FORMATTER);
                if (!allowPast && localDate.isBefore(LocalDate.now())) {
                    System.out.println("  [!] Cannot schedule appointments in the past. Date must be today or future.");
                    continue;
                }
                return Date.valueOf(localDate);
            } catch (DateTimeParseException e) {
                System.out.println("  [!] Invalid date format. Use YYYY-MM-DD (e.g., 2026-11-15).");
            }
        }
    }

    /**
     * Reads a time in HH:mm or HH:mm:ss format.
     */
    public static Time readTime(String prompt) {
        while (true) {
            System.out.print(prompt + " (HH:mm, 24-hr): ");
            String line = SCANNER.nextLine().trim();
            try {
                LocalTime lt;
                if (line.length() == 5) {
                    lt = LocalTime.parse(line, TIME_FORMATTER);
                } else {
                    lt = LocalTime.parse(line, TIME_SEC_FORMATTER);
                }
                return Time.valueOf(lt);
            } catch (DateTimeParseException e) {
                System.out.println("  [!] Invalid time format. Use 24-hr format like 10:30 or 14:00.");
            }
        }
    }

    /**
     * Asks user for a Yes/No confirmation.
     */
    public static boolean confirm(String prompt) {
        System.out.print(prompt + " (y/N): ");
        String line = SCANNER.nextLine().trim().toLowerCase();
        return line.equals("y") || line.equals("yes");
    }
}
