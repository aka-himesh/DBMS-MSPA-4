package com.pawcare.dao;

import com.pawcare.DBConnection;
import com.pawcare.model.VetAppointment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for VetAppointments table.
 * Demonstrates complex multi-table SQL JOINs, date/time filtering,
 * scheduling conflict detection, and transactional status management.
 */
public class VetAppointmentDAO {

    /**
     * Checks if a veterinarian already has an active ('Scheduled') appointment at the same date and time.
     * Prevents overlapping double-bookings.
     */
    public boolean hasOverlappingAppointment(int vetId, Date date, Time time, int excludeApptId) throws SQLException {
        String sql = """
            SELECT COUNT(*) FROM VetAppointments
            WHERE vet_id = ?
              AND appointment_date = ?
              AND appointment_time = ?
              AND status = 'Scheduled'
              AND appointment_id <> ?
        """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, vetId);
            ps.setDate(2, date);
            ps.setTime(3, time);
            ps.setInt(4, excludeApptId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Books a new veterinary appointment after verifying no double-booking exists.
     * Uses JDBC transaction control.
     */
    public int insert(VetAppointment appt) throws SQLException {
        // Enforce scheduling conflict prevention
        if (hasOverlappingAppointment(appt.getVetId(), appt.getAppointmentDate(), appt.getAppointmentTime(), -1)) {
            throw new SQLException(String.format(
                    "Scheduling Conflict: Veterinarian ID %d already has a scheduled appointment on %s at %s.",
                    appt.getVetId(), appt.getAppointmentDate(), appt.getAppointmentTime()));
        }

        String sql = """
            INSERT INTO VetAppointments (dog_id, vet_id, appointment_date, appointment_time, reason, status, consultation_fee)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false); // Begin Transaction
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, appt.getDogId());
                ps.setInt(2, appt.getVetId());
                ps.setDate(3, appt.getAppointmentDate());
                ps.setTime(4, appt.getAppointmentTime());
                ps.setString(5, appt.getReason());
                ps.setString(6, appt.getStatus() != null ? appt.getStatus() : "Scheduled");
                ps.setBigDecimal(7, appt.getConsultationFee());

                int affected = ps.executeUpdate();
                if (affected > 0) {
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            appt.setAppointmentId(rs.getInt(1));
                            conn.commit(); // Commit Transaction
                            return appt.getAppointmentId();
                        }
                    }
                }
                conn.rollback();
                return -1;
            } catch (SQLException ex) {
                conn.rollback(); // Rollback on failure
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Base query for fetching appointments with Dog, Owner, and Veterinarian joined.
     */
    private static final String SELECT_JOIN_QUERY = """
        SELECT va.appointment_id, va.dog_id, va.vet_id, va.appointment_date, va.appointment_time,
               va.reason, va.status, va.consultation_fee, va.created_at,
               d.name AS dog_name,
               o.name AS owner_name,
               vt.name AS vet_name
        FROM VetAppointments va
        INNER JOIN Dogs d ON va.dog_id = d.dog_id
        INNER JOIN Owners o ON d.owner_id = o.owner_id
        INNER JOIN Veterinarians vt ON va.vet_id = vt.vet_id
    """;

    private VetAppointment mapResultSet(ResultSet rs) throws SQLException {
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
        return va;
    }

    /**
     * Retrieves all veterinary appointments ordered by date and time descending.
     */
    public List<VetAppointment> findAllWithDetails() throws SQLException {
        List<VetAppointment> list = new ArrayList<>();
        String sql = SELECT_JOIN_QUERY + " ORDER BY va.appointment_date DESC, va.appointment_time DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        }
        return list;
    }

    /**
     * Finds an appointment by ID with full joined details.
     */
    public Optional<VetAppointment> findByIdWithDetails(int apptId) throws SQLException {
        String sql = SELECT_JOIN_QUERY + " WHERE va.appointment_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, apptId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Searches appointments by Dog ID.
     */
    public List<VetAppointment> findByDogId(int dogId) throws SQLException {
        List<VetAppointment> list = new ArrayList<>();
        String sql = SELECT_JOIN_QUERY + " WHERE va.dog_id = ? ORDER BY va.appointment_date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dogId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        }
        return list;
    }

    /**
     * Searches appointments by Veterinarian ID.
     */
    public List<VetAppointment> findByVetId(int vetId) throws SQLException {
        List<VetAppointment> list = new ArrayList<>();
        String sql = SELECT_JOIN_QUERY + " WHERE va.vet_id = ? ORDER BY va.appointment_date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, vetId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        }
        return list;
    }

    /**
     * Searches appointments scheduled for a specific date.
     */
    public List<VetAppointment> findByDate(Date date) throws SQLException {
        List<VetAppointment> list = new ArrayList<>();
        String sql = SELECT_JOIN_QUERY + " WHERE va.appointment_date = ? ORDER BY va.appointment_time ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, date);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        }
        return list;
    }

    /**
     * Updates the status of an appointment ('Scheduled', 'Completed', 'Cancelled').
     */
    public boolean updateStatus(int appointmentId, String newStatus) throws SQLException {
        String sql = "UPDATE VetAppointments SET status = ? WHERE appointment_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, appointmentId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Reschedules an appointment to a new date and time with overlap check.
     */
    public boolean reschedule(int appointmentId, Date newDate, Time newTime) throws SQLException {
        Optional<VetAppointment> existing = findByIdWithDetails(appointmentId);
        if (existing.isEmpty()) {
            throw new SQLException("Appointment ID " + appointmentId + " not found.");
        }

        int vetId = existing.get().getVetId();
        if (hasOverlappingAppointment(vetId, newDate, newTime, appointmentId)) {
            throw new SQLException(String.format(
                    "Conflict: Veterinarian ID %d is already booked on %s at %s.",
                    vetId, newDate, newTime));
        }

        String sql = "UPDATE VetAppointments SET appointment_date = ?, appointment_time = ?, status = 'Scheduled' WHERE appointment_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, newDate);
            ps.setTime(2, newTime);
            ps.setInt(3, appointmentId);
            return ps.executeUpdate() > 0;
        }
    }
}
