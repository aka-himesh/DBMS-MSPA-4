package com.pawcare.dao;

import com.pawcare.DBConnection;
import com.pawcare.model.GroomingService;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for GroomingServices catalog.
 */
public class GroomingServiceDAO {

    /**
     * Inserts a new grooming service package.
     */
    public int insert(GroomingService service) throws SQLException {
        String sql = "INSERT INTO GroomingServices (service_name, description, price, duration_minutes) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, service.getServiceName());
            ps.setString(2, service.getDescription());
            ps.setBigDecimal(3, service.getPrice());
            ps.setInt(4, service.getDurationMinutes());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        service.setServiceId(rs.getInt(1));
                        return service.getServiceId();
                    }
                }
            }
            return -1;
        }
    }

    /**
     * Lists all available grooming services.
     */
    public List<GroomingService> findAll() throws SQLException {
        List<GroomingService> list = new ArrayList<>();
        String sql = "SELECT service_id, service_name, description, price, duration_minutes, created_at FROM GroomingServices ORDER BY service_id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                GroomingService s = new GroomingService(
                        rs.getInt("service_id"),
                        rs.getString("service_name"),
                        rs.getString("description"),
                        rs.getBigDecimal("price"),
                        rs.getInt("duration_minutes"),
                        rs.getTimestamp("created_at")
                );
                list.add(s);
            }
        }
        return list;
    }

    /**
     * Finds a grooming service by its service_id.
     */
    public Optional<GroomingService> findById(int serviceId) throws SQLException {
        String sql = "SELECT service_id, service_name, description, price, duration_minutes, created_at FROM GroomingServices WHERE service_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, serviceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    GroomingService s = new GroomingService(
                            rs.getInt("service_id"),
                            rs.getString("service_name"),
                            rs.getString("description"),
                            rs.getBigDecimal("price"),
                            rs.getInt("duration_minutes"),
                            rs.getTimestamp("created_at")
                    );
                    return Optional.of(s);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Updates an existing grooming service's details and price.
     */
    public boolean update(GroomingService service) throws SQLException {
        String sql = "UPDATE GroomingServices SET service_name = ?, description = ?, price = ?, duration_minutes = ? WHERE service_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, service.getServiceName());
            ps.setString(2, service.getDescription());
            ps.setBigDecimal(3, service.getPrice());
            ps.setInt(4, service.getDurationMinutes());
            ps.setInt(5, service.getServiceId());

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Counts how many appointment bookings reference this service.
     */
    public int countBookingsForService(int serviceId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM GroomingAppointments WHERE service_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, serviceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    /**
     * Deletes a grooming service only if it is not referenced by existing bookings.
     */
    public boolean delete(int serviceId) throws SQLException {
        int bookingCount = countBookingsForService(serviceId);
        if (bookingCount > 0) {
            throw new SQLException(String.format(
                    "Cannot delete Grooming Service ID %d: %d historical booking(s) reference this service. Delete restricted by foreign key.",
                    serviceId, bookingCount));
        }

        String sql = "DELETE FROM GroomingServices WHERE service_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, serviceId);
            return ps.executeUpdate() > 0;
        }
    }
}
