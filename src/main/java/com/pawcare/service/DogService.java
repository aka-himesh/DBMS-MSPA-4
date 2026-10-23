package com.pawcare.service;

import com.pawcare.dao.DogDAO;
import com.pawcare.dao.OwnerDAO;
import com.pawcare.model.Dog;
import com.pawcare.model.Owner;
import com.pawcare.util.ConsoleFormatter;
import com.pawcare.util.InputValidator;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service and UI controller for Dog Management.
 */
public class DogService {

    private final DogDAO dogDAO = new DogDAO();
    private final OwnerDAO ownerDAO = new OwnerDAO();

    public void handleMenu() {
        while (true) {
            System.out.println("\n-------------------------------------------");
            System.out.println("         PAWCARE: DOG MANAGEMENT          ");
            System.out.println("-------------------------------------------");
            System.out.println("  1. Register Dog (Under Existing Owner)");
            System.out.println("  2. Display All Dogs (with Owner JOIN)");
            System.out.println("  3. Search Dog by ID");
            System.out.println("  4. Update Dog Information");
            System.out.println("  5. Delete Dog (Protected)");
            System.out.println("  6. Return to Main Menu");
            System.out.println("-------------------------------------------");

            int choice = InputValidator.readInt("Enter your choice (1-6): ", 1, 6);
            switch (choice) {
                case 1 -> registerDog();
                case 2 -> displayAllDogs();
                case 3 -> searchDogById();
                case 4 -> updateDog();
                case 5 -> deleteDog();
                case 6 -> {
                    return;
                }
            }
        }
    }

    private void registerDog() {
        System.out.println("\n--- [Register Dog Under Existing Owner] ---");
        int ownerId = InputValidator.readPositiveInt("Enter Registered Owner ID: ");

        try {
            Optional<Owner> ownerOpt = ownerDAO.findById(ownerId);
            if (ownerOpt.isEmpty()) {
                System.out.printf("[ERROR] Owner ID %d does not exist. Please register the owner first.%n", ownerId);
                return;
            }
            System.out.printf("Registering pet for Owner: %s (%s)%n", ownerOpt.get().getName(), ownerOpt.get().getPhone());

            String name = InputValidator.readString("Enter Dog Name: ");
            String breed = InputValidator.readString("Enter Dog Breed: ");
            int age = InputValidator.readInt("Enter Dog Age in Years (0-35): ", 0, 35);
            String gender = InputValidator.readString("Enter Gender (Male/Female): ");
            String notes = InputValidator.readOptionalString("Enter Medical/Behavioral Notes (or press Enter): ");

            Dog dog = new Dog(ownerId, name, breed, age, gender, notes);
            int newId = dogDAO.insert(dog);
            if (newId > 0) {
                System.out.printf("%n[SUCCESS] Dog '%s' registered successfully! Assigned Dog ID: %d%n", name, newId);
            } else {
                System.out.println("[FAILED] Could not register dog.");
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Database operation failed: " + e.getMessage());
        }
    }

    public void displayAllDogs() {
        try {
            List<Dog> dogs = dogDAO.findAllWithOwners();
            String[] headers = {"Dog ID", "Name", "Breed", "Age", "Gender", "Owner Name", "Medical Notes"};
            List<String[]> rows = new ArrayList<>();
            for (Dog d : dogs) {
                rows.add(new String[]{
                        String.valueOf(d.getDogId()),
                        d.getName(),
                        d.getBreed(),
                        d.getAge() + " yrs",
                        d.getGender(),
                        d.getOwnerName() != null ? d.getOwnerName() : "ID:" + d.getOwnerId(),
                        d.getMedicalNotes() != null ? d.getMedicalNotes() : "None"
                });
            }
            ConsoleFormatter.printTable("Registered Dogs (with Owners)", headers, rows);
        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to fetch dogs: " + e.getMessage());
        }
    }

    private void searchDogById() {
        System.out.println("\n--- [Search Dog by ID] ---");
        int id = InputValidator.readPositiveInt("Enter Dog ID: ");
        try {
            Optional<Dog> opt = dogDAO.findById(id);
            if (opt.isPresent()) {
                Dog d = opt.get();
                String[] headers = {"Dog ID", "Name", "Breed", "Age", "Gender", "Owner", "Medical Notes", "Registered"};
                List<String[]> rows = new ArrayList<>();
                rows.add(new String[]{
                        String.valueOf(d.getDogId()),
                        d.getName(),
                        d.getBreed(),
                        String.valueOf(d.getAge()),
                        d.getGender(),
                        d.getOwnerName() != null ? d.getOwnerName() : "Owner #" + d.getOwnerId(),
                        d.getMedicalNotes() != null ? d.getMedicalNotes() : "None",
                        String.valueOf(d.getCreatedAt())
                });
                ConsoleFormatter.printTable("Dog Profile", headers, rows);
            } else {
                System.out.printf("[INFO] No dog found with ID %d.%n", id);
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Search failed: " + e.getMessage());
        }
    }

    private void updateDog() {
        System.out.println("\n--- [Update Dog Information] ---");
        int id = InputValidator.readPositiveInt("Enter Dog ID to Update: ");
        try {
            Optional<Dog> opt = dogDAO.findById(id);
            if (opt.isEmpty()) {
                System.out.printf("[INFO] Dog with ID %d does not exist.%n", id);
                return;
            }

            Dog d = opt.get();
            System.out.printf("Current: Name='%s', Breed='%s', Age=%d, Gender='%s'%n",
                    d.getName(), d.getBreed(), d.getAge(), d.getGender());

            System.out.println("Enter updated details (press Enter to keep existing):");
            String newName = InputValidator.readOptionalString("New Name [" + d.getName() + "]: ");
            String newBreed = InputValidator.readOptionalString("New Breed [" + d.getBreed() + "]: ");
            String ageInput = InputValidator.readOptionalString("New Age [" + d.getAge() + "]: ");
            String newGender = InputValidator.readOptionalString("New Gender [" + d.getGender() + "]: ");
            String newNotes = InputValidator.readOptionalString("New Notes [" + (d.getMedicalNotes() != null ? d.getMedicalNotes() : "") + "]: ");

            if (newName != null) d.setName(newName);
            if (newBreed != null) d.setBreed(newBreed);
            if (ageInput != null) {
                try {
                    int age = Integer.parseInt(ageInput);
                    if (age >= 0 && age <= 35) d.setAge(age);
                } catch (NumberFormatException ignored) {}
            }
            if (newGender != null) d.setGender(newGender);
            if (newNotes != null) d.setMedicalNotes(newNotes);

            if (dogDAO.update(d)) {
                System.out.println("[SUCCESS] Dog details updated successfully.");
            } else {
                System.out.println("[FAILED] Unable to update dog details.");
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Update failed: " + e.getMessage());
        }
    }

    private void deleteDog() {
        System.out.println("\n--- [Delete Dog Record] ---");
        int id = InputValidator.readPositiveInt("Enter Dog ID to Delete: ");
        if (!InputValidator.confirm("Are you sure you want to delete Dog ID " + id + "?")) {
            System.out.println("[CANCELLED] Deletion cancelled.");
            return;
        }

        try {
            if (dogDAO.delete(id)) {
                System.out.printf("[SUCCESS] Dog ID %d was successfully removed.%n", id);
            } else {
                System.out.printf("[INFO] Dog ID %d was not found.%n", id);
            }
        } catch (SQLException e) {
            System.err.println("\n[REFERENTIAL INTEGRITY] " + e.getMessage());
        }
    }
}
