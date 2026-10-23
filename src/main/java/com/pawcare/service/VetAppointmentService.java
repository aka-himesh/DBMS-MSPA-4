package com.pawcare.service;

import com.pawcare.dao.DogDAO;
import com.pawcare.dao.VetAppointmentDAO;
import com.pawcare.dao.VeterinarianDAO;
import com.pawcare.model.Dog;
import com.pawcare.model.VetAppointment;
import com.pawcare.model.Veterinarian;
import com.pawcare.util.ConsoleFormatter;
import com.pawcare.util.InputValidator;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service and UI controller for Veterinary Appointment Management.
 */
public class VetAppointmentService {

    private final VetAppointmentDAO apptDAO = new VetAppointmentDAO();
    private final DogDAO dogDAO = new DogDAO();
    private final VeterinarianDAO vetDAO = new VeterinarianDAO();

    public void handleMenu() {
        while (true) {
            System.out.println("\n-------------------------------------------");
            System.out.println("     PAWCARE: VET APPOINTMENT SYSTEM       ");
            System.out.println("-------------------------------------------");
            System.out.println("  1. Book Veterinary Appointment");
            System.out.println("  2. Display All Appointments (Multi-Table JOIN)");
            System.out.println("  3. Search Appointments (by ID, Dog, Vet, or Date)");
            System.out.println("  4. Update Appointment Status (Scheduled/Completed/Cancelled)");
            System.out.println("  5. Reschedule Appointment");
            System.out.println("  6. Cancel Appointment");
            System.out.println("  7. Return to Main Menu");
            System.out.println("-------------------------------------------");

            int choice = InputValidator.readInt("Enter your choice (1-7): ", 1, 7);
            switch (choice) {
                case 1 -> bookAppointment();
                case 2 -> displayAllAppointments();
                case 3 -> searchAppointments();
                case 4 -> updateStatus();
                case 5 -> rescheduleAppointment();
                case 6 -> cancelAppointment();
                case 7 -> {
                    return;
                }
            }
        }
    }

    private void bookAppointment() {
        System.out.println("\n--- [Book Veterinary Appointment] ---");
        int dogId = InputValidator.readPositiveInt("Enter Dog ID: ");
        int vetId = InputValidator.readPositiveInt("Enter Veterinarian ID: ");

        try {
            Optional<Dog> dogOpt = dogDAO.findById(dogId);
            if (dogOpt.isEmpty()) {
                System.out.printf("[ERROR] Dog ID %d does not exist.%n", dogId);
                return;
            }

            Optional<Veterinarian> vetOpt = vetDAO.findById(vetId);
            if (vetOpt.isEmpty()) {
                System.out.printf("[ERROR] Veterinarian ID %d does not exist.%n", vetId);
                return;
            }

            System.out.printf("Scheduling appointment for Dog '%s' with %s%n",
                    dogOpt.get().getName(), vetOpt.get().getName());

            Date date = InputValidator.readDate("Enter Appointment Date", false);
            Time time = InputValidator.readTime("Enter Appointment Time");
            String reason = InputValidator.readString("Enter Reason for Visit: ");
            BigDecimal fee = InputValidator.readBigDecimal("Enter Consultation Fee (₹)", 0.00);

            VetAppointment appt = new VetAppointment(dogId, vetId, date, time, reason, "Scheduled", fee);
            int newId = apptDAO.insert(appt);
            if (newId > 0) {
                System.out.printf("%n[SUCCESS] Veterinary appointment booked successfully! Appointment ID: %d%n", newId);
            } else {
                System.out.println("[FAILED] Could not book appointment.");
            }
        } catch (SQLException e) {
            System.err.println("\n[BOOKING ERROR] " + e.getMessage());
        }
    }

    public void displayAllAppointments() {
        try {
            List<VetAppointment> list = apptDAO.findAllWithDetails();
            renderTable("All Veterinary Appointments", list);
        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to fetch appointments: " + e.getMessage());
        }
    }

    private void searchAppointments() {
        System.out.println("\n--- [Search Appointments] ---");
        System.out.println("  1. Search by Appointment ID");
        System.out.println("  2. Search by Dog ID");
        System.out.println("  3. Search by Veterinarian ID");
        System.out.println("  4. Search by Appointment Date");
        int subChoice = InputValidator.readInt("Select filter (1-4): ", 1, 4);

        try {
            switch (subChoice) {
                case 1 -> {
                    int id = InputValidator.readPositiveInt("Enter Appointment ID: ");
                    Optional<VetAppointment> opt = apptDAO.findByIdWithDetails(id);
                    if (opt.isPresent()) {
                        renderTable("Appointment Details", List.of(opt.get()));
                    } else {
                        System.out.printf("[INFO] No appointment found with ID %d.%n", id);
                    }
                }
                case 2 -> {
                    int dogId = InputValidator.readPositiveInt("Enter Dog ID: ");
                    List<VetAppointment> list = apptDAO.findByDogId(dogId);
                    renderTable("Appointments for Dog #" + dogId, list);
                }
                case 3 -> {
                    int vetId = InputValidator.readPositiveInt("Enter Veterinarian ID: ");
                    List<VetAppointment> list = apptDAO.findByVetId(vetId);
                    renderTable("Appointments for Vet #" + vetId, list);
                }
                case 4 -> {
                    Date date = InputValidator.readDate("Enter Appointment Date", true);
                    List<VetAppointment> list = apptDAO.findByDate(date);
                    renderTable("Appointments on " + date, list);
                }
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Search failed: " + e.getMessage());
        }
    }

    private void updateStatus() {
        System.out.println("\n--- [Update Appointment Status] ---");
        int id = InputValidator.readPositiveInt("Enter Appointment ID: ");
        try {
            Optional<VetAppointment> opt = apptDAO.findByIdWithDetails(id);
            if (opt.isEmpty()) {
                System.out.printf("[INFO] Appointment ID %d does not exist.%n", id);
                return;
            }

            System.out.printf("Current Status: %s (Dog: %s, Vet: %s, Date: %s)%n",
                    opt.get().getStatus(), opt.get().getDogName(), opt.get().getVetName(), opt.get().getAppointmentDate());

            System.out.println("Select New Status:");
            System.out.println("  1. Scheduled");
            System.out.println("  2. Completed");
            System.out.println("  3. Cancelled");
            int sc = InputValidator.readInt("Enter choice (1-3): ", 1, 3);
            String newStatus = switch (sc) {
                case 1 -> "Scheduled";
                case 2 -> "Completed";
                case 3 -> "Cancelled";
                default -> "Scheduled";
            };

            if (apptDAO.updateStatus(id, newStatus)) {
                System.out.printf("[SUCCESS] Appointment ID %d status changed to '%s'.%n", id, newStatus);
            } else {
                System.out.println("[FAILED] Status update failed.");
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Could not update status: " + e.getMessage());
        }
    }

    private void rescheduleAppointment() {
        System.out.println("\n--- [Reschedule Appointment] ---");
        int id = InputValidator.readPositiveInt("Enter Appointment ID to Reschedule: ");
        try {
            Optional<VetAppointment> opt = apptDAO.findByIdWithDetails(id);
            if (opt.isEmpty()) {
                System.out.printf("[INFO] Appointment ID %d does not exist.%n", id);
                return;
            }

            Date newDate = InputValidator.readDate("Enter New Date", false);
            Time newTime = InputValidator.readTime("Enter New Time");

            if (apptDAO.reschedule(id, newDate, newTime)) {
                System.out.printf("[SUCCESS] Appointment ID %d rescheduled to %s at %s.%n", id, newDate, newTime);
            } else {
                System.out.println("[FAILED] Rescheduling failed.");
            }
        } catch (SQLException e) {
            System.err.println("\n[RESCHEDULE ERROR] " + e.getMessage());
        }
    }

    private void cancelAppointment() {
        System.out.println("\n--- [Cancel Appointment] ---");
        int id = InputValidator.readPositiveInt("Enter Appointment ID to Cancel: ");
        if (!InputValidator.confirm("Are you sure you want to cancel Appointment ID " + id + "?")) {
            System.out.println("[CANCELLED] Action aborted.");
            return;
        }

        try {
            if (apptDAO.updateStatus(id, "Cancelled")) {
                System.out.printf("[SUCCESS] Appointment ID %d marked as 'Cancelled'.%n", id);
            } else {
                System.out.printf("[INFO] Appointment ID %d was not found.%n", id);
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Cancellation failed: " + e.getMessage());
        }
    }

    private void renderTable(String title, List<VetAppointment> list) {
        String[] headers = {"Appt ID", "Dog Name", "Owner Name", "Doctor Name", "Date", "Time", "Status", "Reason", "Fee"};
        List<String[]> rows = new ArrayList<>();
        for (VetAppointment va : list) {
            rows.add(new String[]{
                    String.valueOf(va.getAppointmentId()),
                    va.getDogName() != null ? va.getDogName() : "ID:" + va.getDogId(),
                    va.getOwnerName() != null ? va.getOwnerName() : "-",
                    va.getVetName() != null ? va.getVetName() : "ID:" + va.getVetId(),
                    String.valueOf(va.getAppointmentDate()),
                    String.valueOf(va.getAppointmentTime()),
                    va.getStatus(),
                    va.getReason(),
                    String.format("₹%.2f", va.getConsultationFee())
            });
        }
        ConsoleFormatter.printTable(title, headers, rows);
    }
}
