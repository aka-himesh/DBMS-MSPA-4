package com.pawcare.service;

import com.pawcare.dao.VeterinarianDAO;
import com.pawcare.model.Veterinarian;
import com.pawcare.util.ConsoleFormatter;
import com.pawcare.util.InputValidator;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service and UI controller for Veterinarian Management.
 */
public class VeterinarianService {

    private final VeterinarianDAO vetDAO = new VeterinarianDAO();

    public void handleMenu() {
        while (true) {
            System.out.println("\n-------------------------------------------");
            System.out.println("     PAWCARE: VETERINARIAN MANAGEMENT     ");
            System.out.println("-------------------------------------------");
            System.out.println("  1. Add New Veterinarian");
            System.out.println("  2. Display All Veterinarians");
            System.out.println("  3. Search Veterinarian by ID");
            System.out.println("  4. Update Veterinarian Details");
            System.out.println("  5. Delete Veterinarian (FK Protected)");
            System.out.println("  6. Return to Main Menu");
            System.out.println("-------------------------------------------");

            int choice = InputValidator.readInt("Enter your choice (1-6): ", 1, 6);
            switch (choice) {
                case 1 -> addVeterinarian();
                case 2 -> displayAllVeterinarians();
                case 3 -> searchVeterinarianById();
                case 4 -> updateVeterinarian();
                case 5 -> deleteVeterinarian();
                case 6 -> {
                    return;
                }
            }
        }
    }

    private void addVeterinarian() {
        System.out.println("\n--- [Add New Veterinarian] ---");
        String name = InputValidator.readString("Enter Doctor Name (e.g., Dr. Jane Doe): ");
        String spec = InputValidator.readString("Enter Specialization (e.g., Surgery, Orthopedics): ");
        String phone = InputValidator.readPhone("Enter Contact Phone: ");
        String email = InputValidator.readEmail("Enter Professional Email: ");

        Veterinarian vet = new Veterinarian(name, spec, phone, email);
        try {
            int newId = vetDAO.insert(vet);
            if (newId > 0) {
                System.out.printf("%n[SUCCESS] Veterinarian '%s' registered! Assigned Vet ID: %d%n", name, newId);
            } else {
                System.out.println("[FAILED] Could not register veterinarian.");
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Database operation failed: " + e.getMessage());
        }
    }

    public void displayAllVeterinarians() {
        try {
            List<Veterinarian> vets = vetDAO.findAll();
            String[] headers = {"Vet ID", "Doctor Name", "Specialization", "Phone", "Email"};
            List<String[]> rows = new ArrayList<>();
            for (Veterinarian v : vets) {
                rows.add(new String[]{
                        String.valueOf(v.getVetId()),
                        v.getName(),
                        v.getSpecialization(),
                        v.getPhone(),
                        v.getEmail()
                });
            }
            ConsoleFormatter.printTable("Registered Veterinarians", headers, rows);
        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to fetch veterinarians: " + e.getMessage());
        }
    }

    private void searchVeterinarianById() {
        System.out.println("\n--- [Search Veterinarian by ID] ---");
        int id = InputValidator.readPositiveInt("Enter Vet ID: ");
        try {
            Optional<Veterinarian> opt = vetDAO.findById(id);
            if (opt.isPresent()) {
                Veterinarian v = opt.get();
                String[] headers = {"Vet ID", "Doctor Name", "Specialization", "Phone", "Email", "Registered At"};
                List<String[]> rows = new ArrayList<>();
                rows.add(new String[]{
                        String.valueOf(v.getVetId()),
                        v.getName(),
                        v.getSpecialization(),
                        v.getPhone(),
                        v.getEmail(),
                        String.valueOf(v.getCreatedAt())
                });
                ConsoleFormatter.printTable("Veterinarian Profile", headers, rows);
            } else {
                System.out.printf("[INFO] No veterinarian found with ID %d.%n", id);
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Search failed: " + e.getMessage());
        }
    }

    private void updateVeterinarian() {
        System.out.println("\n--- [Update Veterinarian Information] ---");
        int id = InputValidator.readPositiveInt("Enter Vet ID to Update: ");
        try {
            Optional<Veterinarian> opt = vetDAO.findById(id);
            if (opt.isEmpty()) {
                System.out.printf("[INFO] Veterinarian with ID %d does not exist.%n", id);
                return;
            }

            Veterinarian v = opt.get();
            System.out.printf("Current: Name='%s', Specialization='%s', Phone='%s', Email='%s'%n",
                    v.getName(), v.getSpecialization(), v.getPhone(), v.getEmail());

            System.out.println("Enter updated details (press Enter to keep existing):");
            String newName = InputValidator.readOptionalString("New Doctor Name [" + v.getName() + "]: ");
            String newSpec = InputValidator.readOptionalString("New Specialization [" + v.getSpecialization() + "]: ");
            String newPhone = InputValidator.readOptionalString("New Phone [" + v.getPhone() + "]: ");
            String newEmail = InputValidator.readOptionalString("New Email [" + v.getEmail() + "]: ");

            if (newName != null) v.setName(newName);
            if (newSpec != null) v.setSpecialization(newSpec);
            if (newPhone != null) v.setPhone(newPhone);
            if (newEmail != null) v.setEmail(newEmail);

            if (vetDAO.update(v)) {
                System.out.println("[SUCCESS] Veterinarian details updated successfully.");
            } else {
                System.out.println("[FAILED] Unable to update veterinarian.");
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Update failed: " + e.getMessage());
        }
    }

    private void deleteVeterinarian() {
        System.out.println("\n--- [Delete Veterinarian Record] ---");
        int id = InputValidator.readPositiveInt("Enter Vet ID to Delete: ");
        if (!InputValidator.confirm("Are you sure you want to delete Veterinarian ID " + id + "?")) {
            System.out.println("[CANCELLED] Deletion cancelled.");
            return;
        }

        try {
            if (vetDAO.delete(id)) {
                System.out.printf("[SUCCESS] Veterinarian ID %d removed.%n", id);
            } else {
                System.out.printf("[INFO] Veterinarian ID %d was not found.%n", id);
            }
        } catch (SQLException e) {
            System.err.println("\n[REFERENTIAL CONSTRAINT] " + e.getMessage());
        }
    }
}
