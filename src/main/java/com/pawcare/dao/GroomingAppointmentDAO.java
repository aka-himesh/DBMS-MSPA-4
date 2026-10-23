package com.pawcare.dao;

import com.pawcare.DBConnection;
import com.pawcare.model.GroomingAppointment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for GroomingAppointments table.
 * Demonstrates 4-table SQL JOINs, transaction management, snapshotting prices at booking time,
 * and slot collision prevention.
 */
public class GroomingAppointmentDAO {

    /**
     * Checks if a dog or grooming salon slot is already booked for the exact date and time.
     */
    public boolean hasOverlappingBooking(Date date, Time time, int excludeBookingId) throws SQLException {
        String sql = """
            SELECT COUNT(*) FROM GroomingAppointments
            WHERE booking_date = ?
              AND booking_time = ?
              AND status = 'Scheduled'
              AND booking_id <> ?
        """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, date);
            ps.setTime(2, time);
            ps.setInt(3, excludeBookingId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Books a grooming appointment.
     * Snapshots the service's current price as price_at_booking inside a transaction.
     */
    public int insert(GroomingAppointment booking) throws SQLException {
        if (hasOverlappingBooking(booking.getBookingDate(), booking.getBookingTime(), -1)) {
            throw new SQLException(String.format(
                    "Scheduling Conflict: A grooming session is already scheduled on %s at %s.",
                    booking.getBookingDate(), booking.getBookingTime()));
        }

        String fetchPriceSql = "SELECT price FROM GroomingServices WHERE service_id = ?";
        String insertSql = """
            INSERT INTO GroomingAppointments (dog_id, service_id, booking_date, booking_time, status, price_at_booking)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false); // Begin Transaction
            try {
                // 1. Fetch current price if not preset
                if (booking.getPriceAtBooking() == null) {
                    try (PreparedStatement psPrice = conn.prepareStatement(fetchPriceSql)) {
                        psPrice.setInt(1, booking.getServiceId());
                        try (ResultSet rs = psPrice.executeQuery()) {
                            if (rs.next()) {
                                booking.setPriceAtBooking(rs.getBigDecimal("price"));
                            } else {
                                throw new SQLException("Grooming service ID " + booking.getServiceId() + " not found.");
                            }
                        }
                    }
                }

                // 2. Insert booking record
                try (PreparedStatement psInsert = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                    psInsert.setInt(1, booking.getDogId());
                    psInsert.setInt(2, booking.getServiceId());
                    psInsert.setDate(3, booking.getBookingDate());
                    psInsert.setTime(4, booking.getBookingTime());
                    psInsert.setString(5, booking.getStatus() != null ? booking.getStatus() : "Scheduled");
                    psInsert.setBigDecimal(6, booking.getPriceAtBooking());

                    int affected = psInsert.executeUpdate();
                    if (affected > 0) {
                        try (ResultSet rs = psInsert.getGeneratedKeys()) {
                            if (rs.next()) {
                                booking.setBookingId(rs.getInt(1));
                                conn.commit(); // Commit Transaction
                                return booking.getBookingId();
                            }
                        }
                    }
                }
                conn.rollback();
                return -1;
            } catch (SQLException ex) {
                conn.rollback(); // Rollback on error
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private static final String SELECT_JOIN_QUERY = """
        SELECT ga.booking_id, ga.dog_id, ga.service_id, ga.booking_date, ga.booking_time,
               ga.status, ga.price_at_booking, ga.created_at,
               d.name AS dog_name,
               o.name AS owner_name,
               gs.service_name
        FROM GroomingAppointments ga
        INNER JOIN Dogs d ON ga.dog_id = d.dog_id
        INNER JOIN Owners o ON d.owner_id = o.owner_id
        INNER JOIN GroomingServices gs ON ga.service_id = gs.service_id
    """;

    private GroomingAppointment mapResultSet(ResultSet rs) throws SQLException {
        GroomingAppointment ga = new GroomingAppointment(
                rs.getInt("booking_id"),
                rs.getInt("dog_id"),
                rs.getInt("service_id"),
                rs.getDate("booking_date"),
                rs.getTime("booking_time"),
                rs.getString("status"),
                rs.getBigDecimal("price_at_booking"),
                rs.getTimestamp("created_at")
        );
        ga.setDogName(rs.getString("dog_name"));
        ga.setOwnerName(rs.getString("owner_name"));
        ga.setServiceName(rs.getString("service_name"));
        return ga;
    }

    /**
     * Lists all grooming appointments with Dog, Owner, and Service joined.
     */
    public List<GroomingAppointment> findAllWithDetails() throws SQLException {
        List<GroomingAppointment> list = new ArrayList<>();
        String sql = SELECT_JOIN_QUERY + " ORDER BY ga.booking_date DESC, ga.booking_time DESC";

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
     * Finds a single grooming appointment by booking_id.
     */
    public Optional<GroomingAppointment> findByIdWithDetails(int bookingId) throws SQLException {
        String sql = SELECT_JOIN_QUERY + " WHERE ga.booking_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Searches grooming appointments by booking date.
     */
    public List<GroomingAppointment> findByDate(Date date) throws SQLException {
        List<GroomingAppointment> list = new ArrayList<>();
        String sql = SELECT_JOIN_QUERY + " WHERE ga.booking_date = ? ORDER BY ga.booking_time ASC";

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
     * Updates the status of a grooming appointment ('Scheduled', 'Completed', 'Cancelled').
     */
    public boolean updateStatus(int bookingId, String newStatus) throws SQLException {
        String sql = "UPDATE GroomingAppointments SET status = ? WHERE booking_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, bookingId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Reschedules a grooming appointment to a new date and time.
     */
    public boolean reschedule(int bookingId, Date newDate, Time newTime) throws SQLException {
        Optional<GroomingAppointment> existing = findByIdWithDetails(bookingId);
        if (existing.isEmpty()) {
            throw new SQLException("Grooming Booking ID " + bookingId + " not found.");
        }

        if (hasOverlappingBooking(newDate, newTime, bookingId)) {
            throw new SQLException(String.format(
                    "Conflict: A grooming appointment is already scheduled on %s at %s.",
                    newDate, newTime));
        }

        String sql = "UPDATE GroomingAppointments SET booking_date = ?, booking_time = ?, status = 'Scheduled' WHERE booking_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, newDate);
            ps.setTime(2, newTime);
            ps.setInt(3, bookingId);
            return ps.executeUpdate() > 0;
        }
    }
}
