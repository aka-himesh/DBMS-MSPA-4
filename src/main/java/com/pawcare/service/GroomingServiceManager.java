package com.pawcare.service;

import com.pawcare.dao.GroomingServiceDAO;
import com.pawcare.model.GroomingService;
import com.pawcare.util.ConsoleFormatter;
import com.pawcare.util.InputValidator;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service and UI controller for Grooming Catalog Management.
 */
public class GroomingServiceManager {

    private final GroomingServiceDAO serviceDAO = new GroomingServiceDAO();

    public void handleMenu() {
        while (true) {
            System.out.println("\n-------------------------------------------");
            System.out.println("     PAWCARE: GROOMING SERVICES CATALOG    ");
            System.out.println("-------------------------------------------");
            System.out.println("  1. Add Grooming Service Package");
            System.out.println("  2. Display All Available Services");
            System.out.println("  3. Update Service Details & Price");
            System.out.println("  4. Delete Grooming Service (Protected)");
            System.out.println("  5. Return to Main Menu");
            System.out.println("-------------------------------------------");

            int choice = InputValidator.readInt("Enter your choice (1-5): ", 1, 5);
            switch (choice) {
                case 1 -> addService();
                case 2 -> displayAllServices();
                case 3 -> updateService();
                case 4 -> deleteService();
                case 5 -> {
                    return;
                }
            }
        }
    }

    private void addService() {
        System.out.println("\n--- [Add Grooming Service Package] ---");
        String name = InputValidator.readString("Enter Service Name (e.g., Bath & Blow Dry, Full Grooming): ");
        String desc = InputValidator.readString("Enter Service Description: ");
        BigDecimal price = InputValidator.readBigDecimal("Enter Price (₹): ", 0.00);
        int duration = InputValidator.readInt("Enter Duration in Minutes (1-300): ", 1, 300);

        GroomingService s = new GroomingService(name, desc, price, duration);
        try {
            int newId = serviceDAO.insert(s);
            if (newId > 0) {
                System.out.printf("%n[SUCCESS] Grooming Service '%s' added! Assigned Service ID: %d%n", name, newId);
            } else {
                System.out.println("[FAILED] Unable to add grooming service.");
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Database operation failed: " + e.getMessage());
        }
    }

    public void displayAllServices() {
        try {
            List<GroomingService> list = serviceDAO.findAll();
            String[] headers = {"Service ID", "Service Name", "Price (₹)", "Duration", "Description"};
            List<String[]> rows = new ArrayList<>();
            for (GroomingService s : list) {
                rows.add(new String[]{
                        String.valueOf(s.getServiceId()),
                        s.getServiceName(),
                        String.format("₹%.2f", s.getPrice()),
                        s.getDurationMinutes() + " mins",
                        s.getDescription() != null ? s.getDescription() : "-"
                });
            }
            ConsoleFormatter.printTable("Available Grooming Services", headers, rows);
        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to list grooming services: " + e.getMessage());
        }
    }

    private void updateService() {
        System.out.println("\n--- [Update Grooming Service] ---");
        int id = InputValidator.readPositiveInt("Enter Service ID to Update: ");
        try {
            Optional<GroomingService> opt = serviceDAO.findById(id);
            if (opt.isEmpty()) {
                System.out.printf("[INFO] Grooming service with ID %d does not exist.%n", id);
                return;
            }

            GroomingService s = opt.get();
            System.out.printf("Current: Name='%s', Price=₹%.2f, Duration=%d mins%n",
                    s.getServiceName(), s.getPrice(), s.getDurationMinutes());

            System.out.println("Enter updated details (press Enter to keep existing):");
            String newName = InputValidator.readOptionalString("New Service Name [" + s.getServiceName() + "]: ");
            String newDesc = InputValidator.readOptionalString("New Description [" + (s.getDescription() != null ? s.getDescription() : "") + "]: ");
            String priceStr = InputValidator.readOptionalString("New Price [" + s.getPrice() + "]: ");
            String durStr = InputValidator.readOptionalString("New Duration in Mins [" + s.getDurationMinutes() + "]: ");

            if (newName != null) s.setServiceName(newName);
            if (newDesc != null) s.setDescription(newDesc);
            if (priceStr != null) {
                try {
                    s.setPrice(new BigDecimal(priceStr));
                } catch (NumberFormatException ignored) {}
            }
            if (durStr != null) {
                try {
                    s.setDurationMinutes(Integer.parseInt(durStr));
                } catch (NumberFormatException ignored) {}
            }

            if (serviceDAO.update(s)) {
                System.out.println("[SUCCESS] Grooming service updated successfully.");
            } else {
                System.out.println("[FAILED] Unable to update grooming service.");
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Update failed: " + e.getMessage());
        }
    }

    private void deleteService() {
        System.out.println("\n--- [Delete Grooming Service] ---");
        int id = InputValidator.readPositiveInt("Enter Service ID to Delete: ");
        if (!InputValidator.confirm("Are you sure you want to delete Grooming Service ID " + id + "?")) {
            System.out.println("[CANCELLED] Deletion cancelled.");
            return;
        }

        try {
            if (serviceDAO.delete(id)) {
                System.out.printf("[SUCCESS] Grooming service ID %d removed.%n", id);
            } else {
                System.out.printf("[INFO] Service ID %d was not found.%n", id);
            }
        } catch (SQLException e) {
            System.err.println("\n[REFERENTIAL INTEGRITY] " + e.getMessage());
        }
    }
}
