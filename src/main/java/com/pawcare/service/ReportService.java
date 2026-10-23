package com.pawcare.service;

import com.pawcare.dao.ReportDAO;
import com.pawcare.model.VetAppointment;
import com.pawcare.util.ConsoleFormatter;
import com.pawcare.util.InputValidator;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service and UI controller for DBMS Reports & Analytical Queries.
 */
public class ReportService {

    private final ReportDAO reportDAO = new ReportDAO();

    public void handleMenu() {
        while (true) {
            System.out.println("\n-------------------------------------------");
            System.out.println("     PAWCARE: DBMS REPORTS & ANALYTICS     ");
            System.out.println("-------------------------------------------");
            System.out.println("  1. System Summary (Total Counts)");
            System.out.println("  2. Today's Veterinary Appointments");
            System.out.println("  3. Upcoming Appointments (Tomorrow Onwards)");
            System.out.println("  4. Status Breakdown (Completed/Scheduled/Cancelled)");
            System.out.println("  5. Most Booked Grooming Services (GROUP BY)");
            System.out.println("  6. Total Revenue from Completed Sessions");
            System.out.println("  7. Run All Reports (Exam Demo Mode)");
            System.out.println("  8. Return to Main Menu");
            System.out.println("-------------------------------------------");

            int choice = InputValidator.readInt("Enter report option (1-8): ", 1, 8);
            switch (choice) {
                case 1 -> showSystemSummary();
                case 2 -> showTodayAppointments();
                case 3 -> showUpcomingAppointments();
                case 4 -> showStatusBreakdown();
                case 5 -> showPopularServices();
                case 6 -> showTotalRevenue();
                case 7 -> runAllReports();
                case 8 -> {
                    return;
                }
            }
        }
    }

    public void showSystemSummary() {
        try {
            int owners = reportDAO.getTotalOwners();
            int dogs = reportDAO.getTotalDogs();
            int vets = reportDAO.getTotalVeterinarians();
            int services = reportDAO.getTotalGroomingServices();

            String[] headers = {"Metric", "Registered Count"};
            List<String[]> rows = List.of(
                    new String[]{"Total Pet Owners", String.valueOf(owners)},
                    new String[]{"Total Registered Dogs", String.valueOf(dogs)},
                    new String[]{"Total Active Veterinarians", String.valueOf(vets)},
                    new String[]{"Available Grooming Services", String.valueOf(services)}
            );
            ConsoleFormatter.printTable("PawCare System Statistics", headers, rows);
        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to fetch system summary: " + e.getMessage());
        }
    }

    public void showTodayAppointments() {
        try {
            List<VetAppointment> list = reportDAO.getTodayVetAppointments();
            renderVetTable("Today's Veterinary Appointments (CURRENT_DATE)", list);
        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to fetch today's appointments: " + e.getMessage());
        }
    }

    public void showUpcomingAppointments() {
        try {
            List<VetAppointment> list = reportDAO.getUpcomingVetAppointments();
            renderVetTable("Upcoming Scheduled Appointments", list);
        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to fetch upcoming appointments: " + e.getMessage());
        }
    }

    public void showStatusBreakdown() {
        try {
            List<String[]> rows = reportDAO.getAppointmentStatusSummary();
            String[] headers = {"Department", "Status", "Total Appointments", "Total Fee / Price"};
            ConsoleFormatter.printTable("Appointments Breakdown by Status (GROUP BY)", headers, rows);
        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to fetch status summary: " + e.getMessage());
        }
    }

    public void showPopularServices() {
        try {
            List<String[]> rows = reportDAO.getMostBookedGroomingServices();
            String[] headers = {"Service ID", "Service Name", "Base Price", "Times Booked", "Revenue Generated"};
            ConsoleFormatter.printTable("Most Frequently Booked Grooming Services", headers, rows);
        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to fetch popular services: " + e.getMessage());
        }
    }

    public void showTotalRevenue() {
        try {
            BigDecimal rev = reportDAO.getTotalCompletedRevenue();
            System.out.println("\n+------------------------------------------------------+");
            System.out.printf("|  TOTAL REALIZED REVENUE (Completed Sessions): ₹%-8.2f|%n", rev);
            System.out.println("+------------------------------------------------------+");
        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to calculate revenue: " + e.getMessage());
        }
    }

    private void runAllReports() {
        System.out.println("\n========== EXECUTING FULL ANALYTICAL REPORT SUITE ==========");
        showSystemSummary();
        showTodayAppointments();
        showUpcomingAppointments();
        showStatusBreakdown();
        showPopularServices();
        showTotalRevenue();
        System.out.println("========== REPORT GENERATION COMPLETE ==========");
    }

    private void renderVetTable(String title, List<VetAppointment> list) {
        String[] headers = {"Appt ID", "Dog Name", "Owner Name", "Doctor Name", "Date", "Time", "Status", "Reason"};
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
                    va.getReason()
            });
        }
        ConsoleFormatter.printTable(title, headers, rows);
    }
}
