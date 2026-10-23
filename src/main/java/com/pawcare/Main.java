package com.pawcare;

import com.pawcare.service.*;
import com.pawcare.util.InputValidator;

/**
 * Main application entry point for PawCare Management System.
 * Presents a clear, menu-driven CLI interface to demonstrate
 * Java, MySQL, and JDBC capabilities for the DBMS practical examination.
 */
public class Main {

    private static final OwnerService ownerService = new OwnerService();
    private static final DogService dogService = new DogService();
    private static final VeterinarianService vetService = new VeterinarianService();
    private static final VetAppointmentService vetApptService = new VetAppointmentService();
    private static final GroomingServiceManager groomingServiceManager = new GroomingServiceManager();
    private static final GroomingAppointmentService groomingApptService = new GroomingAppointmentService();
    private static final ReportService reportService = new ReportService();

    public static void main(String[] args) {
        printBanner();

        // Optional connectivity test check on startup
        System.out.println("Checking database connectivity...");
        boolean connected = DBConnection.testConnection();
        if (!connected) {
            System.out.println("\n[NOTICE] Unable to reach MySQL Server with current credentials.");
            System.out.println("Please ensure MySQL is running, schema is created, and db.properties has the right password.");
            System.out.println("You can still navigate menus or test connections from the menu.\n");
        } else {
            System.out.println("[SUCCESS] PawCare is ready for live demonstration!\n");
        }

        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = InputValidator.readInt("Enter your selection (0-8): ", 0, 8);

            switch (choice) {
                case 1 -> ownerService.handleMenu();
                case 2 -> dogService.handleMenu();
                case 3 -> vetService.handleMenu();
                case 4 -> vetApptService.handleMenu();
                case 5 -> groomingServiceManager.handleMenu();
                case 6 -> groomingApptService.handleMenu();
                case 7 -> reportService.handleMenu();
                case 8 -> {
                    System.out.println("\nThank you for using PawCare Management System.");
                    System.out.println("Exiting application. Goodbye!\n");
                    running = false;
                }
                case 0 -> {
                    DBConnection.reloadConfig();
                    DBConnection.testConnection();
                }
            }
        }
    }

    private static void printBanner() {
        System.out.println("""
            ======================================================================
               __   ___   _    _  ___   _   ___  ___ 
              / /  / _ \\ | |  | |/   \\ / | | _ \\| __|
             / /__| (_) || |/\\| || - | | | |   /| _| 
             \\____/\\___/  \\_\\/\\_/|_|_| |_| |_|_\\|___|
              
              Dog Grooming and Veterinary Appointment Management System
              DBMS Practical Examination Mini-Project (Java + MySQL + JDBC)
            ======================================================================
        """);
    }

    private static void printMainMenu() {
        System.out.println("\n========== PAWCARE MANAGEMENT SYSTEM ==========");
        System.out.println("  1. Owner Management");
        System.out.println("  2. Dog Management");
        System.out.println("  3. Veterinarian Management");
        System.out.println("  4. Veterinary Appointments");
        System.out.println("  5. Grooming Services");
        System.out.println("  6. Grooming Appointments");
        System.out.println("  7. Reports");
        System.out.println("  8. Exit");
        System.out.println("  ---------------------------------------------");
        System.out.println("  0. Test / Reconnect MySQL JDBC Connection");
        System.out.println("===============================================");
    }
}
