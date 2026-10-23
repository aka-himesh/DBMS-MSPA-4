package com.pawcare.dao;

import com.pawcare.DBConnection;
import com.pawcare.model.GroomingAppointment;
import com.pawcare.model.VetAppointment;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object specifically for SQL Analytical Queries and Lab Reports.
 * Demonstrates aggregate functions (COUNT, SUM, AVG), GROUP BY, ORDER BY,
 * and date comparison expressions in MySQL.
 */
public class ReportDAO {

    /**
     * Report 1: Total counts of core registered entities.
     * Uses SQL COUNT(*) queries.
     */
    public int getTotalOwners() throws SQLException {
        return getSingleCount("SELECT COUNT(*) FROM Owners");
    }

    public int getTotalDogs() throws SQLException {
        return getSingleCount("SELECT COUNT(*) FROM Dogs");
    }

    public int getTotalVeterinarians() throws SQLException {
        return getSingleCount("SELECT COUNT(*) FROM Veterinarians");
    }

    public int getTotalGroomingServices() throws SQLException {
        return getSingleCount("SELECT COUNT(*) FROM GroomingServices");
    }

    private int getSingleCount(String sql) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Report 2: Today's Veterinary Appointments.
     * Uses SQL: WHERE appointment_date = CURRENT_DATE
     */
    public List<VetAppointment> getTodayVetAppointments() throws SQLException {
        List<VetAppointment> list = new ArrayList<>();
        String sql = """
            SELECT va.appointment_id, va.dog_id, va.vet_id, va.appointment_date, va.appointment_time,
                   va.reason, va.status, va.consultation_fee, va.created_at,
                   d.name AS dog_name, o.name AS owner_name, vt.name AS vet_name
            FROM VetAppointments va
            INNER JOIN Dogs d ON va.dog_id = d.dog_id
            INNER JOIN Owners o ON d.owner_id = o.owner_id
            INNER JOIN Veterinarians vt ON va.vet_id = vt.vet_id
            WHERE va.appointment_date = CURRENT_DATE
            ORDER BY va.appointment_time ASC
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                VetAppointment va = new VetAppointment(
                        rs.getInt("appointment_id"),
                        rs.getInt("dog_id"),
                        rs.getInt("vet_id"),
                        rs.getDate("appointment_date"),
                        rs.getTime("appointment_time"),
                        rs.getString("reason"),
                        rs.getString("status"),
                        rs.getBigDecimal("consultation_fee"),
                        rs.getTimestamp("created_at")
                );
                va.setDogName(rs.getString("dog_name"));
                va.setOwnerName(rs.getString("owner_name"));
                va.setVetName(rs.getString("vet_name"));
                list.add(va);
            }
        }
        return list;
    }

    /**
     * Report 3: Upcoming Veterinary & Grooming Appointments (from tomorrow onwards).
     * Uses SQL: WHERE appointment_date > CURRENT_DATE AND status = 'Scheduled'
     */
    public List<VetAppointment> getUpcomingVetAppointments() throws SQLException {
        List<VetAppointment> list = new ArrayList<>();
        String sql = """
            SELECT va.appointment_id, va.dog_id, va.vet_id, va.appointment_date, va.appointment_time,
                   va.reason, va.status, va.consultation_fee, va.created_at,
                   d.name AS dog_name, o.name AS owner_name, vt.name AS vet_name
            FROM VetAppointments va
            INNER JOIN Dogs d ON va.dog_id = d.dog_id
            INNER JOIN Owners o ON d.owner_id = o.owner_id
            INNER JOIN Veterinarians vt ON va.vet_id = vt.vet_id
            WHERE va.appointment_date > CURRENT_DATE AND va.status = 'Scheduled'
            ORDER BY va.appointment_date ASC, va.appointment_time ASC
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                VetAppointment va = new VetAppointment(
                        rs.getInt("appointment_id"),
                        rs.getInt("dog_id"),
                        rs.getInt("vet_id"),
                        rs.getDate("appointment_date"),
                        rs.getTime("appointment_time"),
                        rs.getString("reason"),
                        rs.getString("status"),
                        rs.getBigDecimal("consultation_fee"),
                        rs.getTimestamp("created_at")
                );
                va.setDogName(rs.getString("dog_name"));
                va.setOwnerName(rs.getString("owner_name"));
                va.setVetName(rs.getString("vet_name"));
                list.add(va);
            }
        }
        return list;
    }

    /**
     * Report 4: Appointment Status Breakdown (Completed vs Scheduled vs Cancelled).
     * Uses SQL GROUP BY and UNION ALL across both branches.
     */
    public List<String[]> getAppointmentStatusSummary() throws SQLException {
        List<String[]> rows = new ArrayList<>();
        String sql = """
            SELECT 'Veterinary' AS category, status, COUNT(*) AS total_count, COALESCE(SUM(consultation_fee), 0.00) AS total_amount
            FROM VetAppointments
            GROUP BY status
            UNION ALL
            SELECT 'Grooming' AS category, status, COUNT(*) AS total_count, COALESCE(SUM(price_at_booking), 0.00) AS total_amount
            FROM GroomingAppointments
            GROUP BY status
            ORDER BY category, status
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rows.add(new String[]{
                        rs.getString("category"),
                        rs.getString("status"),
                        String.valueOf(rs.getInt("total_count")),
                        String.format("₹%.2f", rs.getBigDecimal("total_amount"))
                });
            }
        }
        return rows;
    }

    /**
     * Report 5: Most Frequently Booked Grooming Services.
     * Uses SQL: GROUP BY service_name, COUNT(*), SUM(price_at_booking) ORDER BY booking_count DESC
     */
    public List<String[]> getMostBookedGroomingServices() throws SQLException {
        List<String[]> rows = new ArrayList<>();
        String sql = """
            SELECT gs.service_id, gs.service_name, gs.price,
                   COUNT(ga.booking_id) AS booking_count,
                   COALESCE(SUM(ga.price_at_booking), 0.00) AS revenue_generated
            FROM GroomingServices gs
            LEFT JOIN GroomingAppointments ga ON gs.service_id = ga.service_id
            GROUP BY gs.service_id, gs.service_name, gs.price
            ORDER BY booking_count DESC, revenue_generated DESC
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rows.add(new String[]{
                        String.valueOf(rs.getInt("service_id")),
                        rs.getString("service_name"),
                        String.format("₹%.2f", rs.getBigDecimal("price")),
                        String.valueOf(rs.getInt("booking_count")),
                        String.format("₹%.2f", rs.getBigDecimal("revenue_generated"))
                });
            }
        }
        return rows;
    }

    /**
     * Report 6: Total Revenue from all COMPLETED appointments (Veterinary + Grooming).
     * Uses SQL SUM() with status = 'Completed'.
     */
    public BigDecimal getTotalCompletedRevenue() throws SQLException {
        String sql = """
            SELECT
              (SELECT COALESCE(SUM(consultation_fee), 0.00) FROM VetAppointments WHERE status = 'Completed') +
              (SELECT COALESCE(SUM(price_at_booking), 0.00) FROM GroomingAppointments WHERE status = 'Completed') AS total_revenue
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getBigDecimal("total_revenue");
            }
        }
        return BigDecimal.ZERO;
    }
}
