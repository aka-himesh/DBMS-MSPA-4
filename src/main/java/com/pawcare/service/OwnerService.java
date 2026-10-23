package com.pawcare.service;

import com.pawcare.dao.OwnerDAO;
import com.pawcare.model.Owner;
import com.pawcare.util.ConsoleFormatter;
import com.pawcare.util.InputValidator;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service and UI controller for Owner Management.
 */
public class OwnerService {

    private final OwnerDAO ownerDAO = new OwnerDAO();

    public void handleMenu() {
        while (true) {
            System.out.println("\n-------------------------------------------");
            System.out.println("        PAWCARE: OWNER MANAGEMENT         ");
            System.out.println("-------------------------------------------");
            System.out.println("  1. Register New Pet Owner");
            System.out.println("  2. Display All Owners");
            System.out.println("  3. Search Owner by ID");
            System.out.println("  4. Update Owner Information");
            System.out.println("  5. Delete Owner (FK Protected)");
            System.out.println("  6. Return to Main Menu");
            System.out.println("-------------------------------------------");

            int choice = InputValidator.readInt("Enter your choice (1-6): ", 1, 6);
            switch (choice) {
                case 1 -> registerOwner();
                case 2 -> displayAllOwners();
                case 3 -> searchOwnerById();
                case 4 -> updateOwner();
                case 5 -> deleteOwner();
                case 6 -> {
                    return;
                }
            }
        }
    }

    private void registerOwner() {
        System.out.println("\n--- [Register New Pet Owner] ---");
        String name = InputValidator.readString("Enter Owner Full Name: ");
        String phone = InputValidator.readPhone("Enter Contact Phone (10-15 digits): ");
        String email = InputValidator.readEmail("Enter Email Address: ");
        String address = InputValidator.readOptionalString("Enter Address (or press Enter to skip): ");

        Owner owner = new Owner(name, phone, email, address);
        try {
            int newId = ownerDAO.insert(owner);
            if (newId > 0) {
                System.out.printf("%n[SUCCESS] Pet owner registered successfully! Assigned Owner ID: %d%n", newId);
            } else {
                System.out.println("%n[FAILED] Unable to register owner.");
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Database operation failed: " + e.getMessage());
        }
    }

    public void displayAllOwners() {
        try {
            List<Owner> owners = ownerDAO.findAll();
            String[] headers = {"Owner ID", "Name", "Phone", "Email", "Address"};
            List<String[]> rows = new ArrayList<>();
            for (Owner o : owners) {
                rows.add(new String[]{
                        String.valueOf(o.getOwnerId()),
                        o.getName(),
                        o.getPhone(),
                        o.getEmail(),
                        o.getAddress() != null ? o.getAddress() : "-"
                });
            }
            ConsoleFormatter.printTable("Registered Pet Owners", headers, rows);
        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to fetch owners: " + e.getMessage());
        }
    }

    private void searchOwnerById() {
        System.out.println("\n--- [Search Owner by ID] ---");
        int id = InputValidator.readPositiveInt("Enter Owner ID: ");
        try {
            Optional<Owner> opt = ownerDAO.findById(id);
            if (opt.isPresent()) {
                Owner o = opt.get();
                String[] headers = {"Owner ID", "Name", "Phone", "Email", "Address", "Registered Date"};
                List<String[]> rows = new ArrayList<>();
                rows.add(new String[]{
                        String.valueOf(o.getOwnerId()),
                        o.getName(),
                        o.getPhone(),
                        o.getEmail(),
                        o.getAddress() != null ? o.getAddress() : "-",
                        String.valueOf(o.getCreatedAt())
                });
                ConsoleFormatter.printTable("Owner Details", headers, rows);
            } else {
                System.out.printf("[INFO] No pet owner found with ID %d.%n", id);
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Search failed: " + e.getMessage());
        }
    }

    private void updateOwner() {
        System.out.println("\n--- [Update Owner Information] ---");
        int id = InputValidator.readPositiveInt("Enter Owner ID to Update: ");
        try {
            Optional<Owner> opt = ownerDAO.findById(id);
            if (opt.isEmpty()) {
                System.out.printf("[INFO] Owner with ID %d does not exist.%n", id);
                return;
            }

            Owner existing = opt.get();
            System.out.printf("Current Data: %s | %s | %s | %s%n",
                    existing.getName(), existing.getPhone(), existing.getEmail(), existing.getAddress());

            System.out.println("Enter updated details (press Enter to keep current value):");
            String newName = InputValidator.readOptionalString("New Name [" + existing.getName() + "]: ");
            String newPhone = InputValidator.readOptionalString("New Phone [" + existing.getPhone() + "]: ");
            String newEmail = InputValidator.readOptionalString("New Email [" + existing.getEmail() + "]: ");
            String newAddress = InputValidator.readOptionalString("New Address [" + (existing.getAddress() != null ? existing.getAddress() : "-") + "]: ");

            if (newName != null) existing.setName(newName);
            if (newPhone != null) existing.setPhone(newPhone);
            if (newEmail != null) existing.setEmail(newEmail);
            if (newAddress != null) existing.setAddress(newAddress);

            if (ownerDAO.update(existing)) {
                System.out.println("[SUCCESS] Owner details updated successfully.");
            } else {
                System.out.println("[FAILED] Could not update owner details.");
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Update failed: " + e.getMessage());
        }
    }

    private void deleteOwner() {
        System.out.println("\n--- [Delete Pet Owner] ---");
        int id = InputValidator.readPositiveInt("Enter Owner ID to Delete: ");
        if (!InputValidator.confirm("Are you sure you want to permanently delete Owner ID " + id + "?")) {
            System.out.println("[CANCELLED] Deletion cancelled.");
            return;
        }

        try {
            if (ownerDAO.delete(id)) {
                System.out.printf("[SUCCESS] Owner ID %d has been removed.%n", id);
            } else {
                System.out.printf("[INFO] Owner ID %d was not found.%n", id);
            }
        } catch (SQLException e) {
            System.err.println("\n[FOREIGN KEY RESTRICTION] " + e.getMessage());
            System.err.println("Tip: Relational integrity prevents deleting an owner while their registered dogs exist in the database.");
        }
    }
}
