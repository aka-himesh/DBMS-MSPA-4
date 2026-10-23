package com.pawcare.service;

import com.pawcare.dao.DogDAO;
import com.pawcare.dao.GroomingAppointmentDAO;
import com.pawcare.dao.GroomingServiceDAO;
import com.pawcare.model.Dog;
import com.pawcare.model.GroomingAppointment;
import com.pawcare.model.GroomingService;
import com.pawcare.util.ConsoleFormatter;
import com.pawcare.util.InputValidator;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service and UI controller for Grooming Appointment Bookings.
 */
public class GroomingAppointmentService {

    private final GroomingAppointmentDAO bookingDAO = new GroomingAppointmentDAO();
    private final DogDAO dogDAO = new DogDAO();
    private final GroomingServiceDAO serviceDAO = new GroomingServiceDAO();

    public void handleMenu() {
        while (true) {
            System.out.println("\n-------------------------------------------");
            System.out.println("   PAWCARE: GROOMING APPOINTMENT SYSTEM    ");
            System.out.println("-------------------------------------------");
            System.out.println("  1. Book Dog Grooming Session");
            System.out.println("  2. Display All Grooming Bookings (with JOINs)");
            System.out.println("  3. Search Bookings by Date");
            System.out.println("  4. Update Booking Status (Scheduled/Completed/Cancelled)");
            System.out.println("  5. Reschedule Grooming Session");
            System.out.println("  6. Cancel Grooming Booking");
            System.out.println("  7. Return to Main Menu");
            System.out.println("-------------------------------------------");

            int choice = InputValidator.readInt("Enter your choice (1-7): ", 1, 7);
            switch (choice) {
                case 1 -> bookSession();
                case 2 -> displayAllBookings();
                case 3 -> searchByDate();
                case 4 -> updateStatus();
                case 5 -> rescheduleBooking();
                case 6 -> cancelBooking();
                case 7 -> {
                    return;
                }
            }
        }
    }

    private void bookSession() {
        System.out.println("\n--- [Book Dog Grooming Session] ---");
        int dogId = InputValidator.readPositiveInt("Enter Dog ID: ");
        int serviceId = InputValidator.readPositiveInt("Enter Grooming Service ID: ");

        try {
            Optional<Dog> dogOpt = dogDAO.findById(dogId);
            if (dogOpt.isEmpty()) {
                System.out.printf("[ERROR] Dog ID %d does not exist.%n", dogId);
                return;
            }

            Optional<GroomingService> sOpt = serviceDAO.findById(serviceId);
            if (sOpt.isEmpty()) {
                System.out.printf("[ERROR] Grooming Service ID %d does not exist.%n", serviceId);
                return;
            }

            GroomingService gs = sOpt.get();
            System.out.printf("Selected: '%s' for Dog '%s' | Standard Price: ₹%.2f (Est. %d mins)%n",
                    gs.getServiceName(), dogOpt.get().getName(), gs.getPrice(), gs.getDurationMinutes());

            Date date = InputValidator.readDate("Enter Grooming Date", false);
            Time time = InputValidator.readTime("Enter Grooming Slot Time");

            GroomingAppointment ga = new GroomingAppointment(dogId, serviceId, date, time, "Scheduled", gs.getPrice());
            int newId = bookingDAO.insert(ga);
            if (newId > 0) {
                System.out.printf("%n[SUCCESS] Grooming appointment booked successfully! Booking ID: %d%n", newId);
            } else {
                System.out.println("[FAILED] Could not book session.");
            }
        } catch (SQLException e) {
            System.err.println("\n[BOOKING ERROR] " + e.getMessage());
        }
    }

    public void displayAllBookings() {
        try {
            List<GroomingAppointment> list = bookingDAO.findAllWithDetails();
            renderTable("All Grooming Bookings", list);
        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to fetch bookings: " + e.getMessage());
        }
    }

    private void searchByDate() {
        System.out.println("\n--- [Search Bookings by Date] ---");
        Date date = InputValidator.readDate("Enter Date to Search", true);
        try {
            List<GroomingAppointment> list = bookingDAO.findByDate(date);
            renderTable("Grooming Sessions on " + date, list);
        } catch (SQLException e) {
            System.err.println("[ERROR] Search failed: " + e.getMessage());
        }
    }

    private void updateStatus() {
        System.out.println("\n--- [Update Grooming Booking Status] ---");
        int id = InputValidator.readPositiveInt("Enter Booking ID: ");
        try {
            Optional<GroomingAppointment> opt = bookingDAO.findByIdWithDetails(id);
            if (opt.isEmpty()) {
                System.out.printf("[INFO] Booking ID %d does not exist.%n", id);
                return;
            }

            System.out.printf("Current Status: %s (Dog: %s, Service: %s, Date: %s)%n",
                    opt.get().getStatus(), opt.get().getDogName(), opt.get().getServiceName(), opt.get().getBookingDate());

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

            if (bookingDAO.updateStatus(id, newStatus)) {
                System.out.printf("[SUCCESS] Booking ID %d status changed to '%s'.%n", id, newStatus);
            } else {
                System.out.println("[FAILED] Status update failed.");
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Could not update status: " + e.getMessage());
        }
    }

    private void rescheduleBooking() {
        System.out.println("\n--- [Reschedule Grooming Session] ---");
        int id = InputValidator.readPositiveInt("Enter Booking ID to Reschedule: ");
        try {
            Optional<GroomingAppointment> opt = bookingDAO.findByIdWithDetails(id);
            if (opt.isEmpty()) {
                System.out.printf("[INFO] Booking ID %d does not exist.%n", id);
                return;
            }

            Date newDate = InputValidator.readDate("Enter New Booking Date", false);
            Time newTime = InputValidator.readTime("Enter New Time Slot");

            if (bookingDAO.reschedule(id, newDate, newTime)) {
                System.out.printf("[SUCCESS] Booking ID %d rescheduled to %s at %s.%n", id, newDate, newTime);
            } else {
                System.out.println("[FAILED] Rescheduling failed.");
            }
        } catch (SQLException e) {
            System.err.println("\n[RESCHEDULE ERROR] " + e.getMessage());
        }
    }

    private void cancelBooking() {
        System.out.println("\n--- [Cancel Grooming Booking] ---");
        int id = InputValidator.readPositiveInt("Enter Booking ID to Cancel: ");
        if (!InputValidator.confirm("Are you sure you want to cancel Booking ID " + id + "?")) {
            System.out.println("[CANCELLED] Action aborted.");
            return;
        }

        try {
            if (bookingDAO.updateStatus(id, "Cancelled")) {
                System.out.printf("[SUCCESS] Grooming Booking ID %d marked as 'Cancelled'.%n", id);
            } else {
                System.out.printf("[INFO] Booking ID %d was not found.%n", id);
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Cancellation failed: " + e.getMessage());
        }
    }

    private void renderTable(String title, List<GroomingAppointment> list) {
        String[] headers = {"Booking ID", "Dog Name", "Owner Name", "Service Package", "Date", "Time", "Status", "Price Paid"};
        List<String[]> rows = new ArrayList<>();
        for (GroomingAppointment ga : list) {
            rows.add(new String[]{
                    String.valueOf(ga.getBookingId()),
                    ga.getDogName() != null ? ga.getDogName() : "ID:" + ga.getDogId(),
                    ga.getOwnerName() != null ? ga.getOwnerName() : "-",
                    ga.getServiceName() != null ? ga.getServiceName() : "ID:" + ga.getServiceId(),
                    String.valueOf(ga.getBookingDate()),
                    String.valueOf(ga.getBookingTime()),
                    ga.getStatus(),
                    String.format("₹%.2f", ga.getPriceAtBooking())
            });
        }
        ConsoleFormatter.printTable(title, headers, rows);
    }
}
