package com.pawcare.dao;

import com.pawcare.DBConnection;
import com.pawcare.model.Veterinarian;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Veterinarians table.
 */
public class VeterinarianDAO {

    /**
     * Inserts a new veterinarian.
     */
    public int insert(Veterinarian vet) throws SQLException {
        String sql = "INSERT INTO Veterinarians (name, specialization, phone, email) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, vet.getName());
            ps.setString(2, vet.getSpecialization());
            ps.setString(3, vet.getPhone());
            ps.setString(4, vet.getEmail());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        vet.setVetId(rs.getInt(1));
                        return vet.getVetId();
                    }
                }
            }
            return -1;
        }
    }

    /**
     * Returns all registered veterinarians.
     */
    public List<Veterinarian> findAll() throws SQLException {
        List<Veterinarian> list = new ArrayList<>();
        String sql = "SELECT vet_id, name, specialization, phone, email, created_at FROM Veterinarians ORDER BY vet_id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Veterinarian v = new Veterinarian(
                        rs.getInt("vet_id"),
                        rs.getString("name"),
                        rs.getString("specialization"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getTimestamp("created_at")
                );
                list.add(v);
            }
        }
        return list;
    }

    /**
     * Finds a veterinarian by vet_id.
     */
    public Optional<Veterinarian> findById(int vetId) throws SQLException {
        String sql = "SELECT vet_id, name, specialization, phone, email, created_at FROM Veterinarians WHERE vet_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, vetId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Veterinarian v = new Veterinarian(
                            rs.getInt("vet_id"),
                            rs.getString("name"),
                            rs.getString("specialization"),
                            rs.getString("phone"),
                            rs.getString("email"),
                            rs.getTimestamp("created_at")
                    );
                    return Optional.of(v);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Updates veterinarian details.
     */
    public boolean update(Veterinarian vet) throws SQLException {
        String sql = "UPDATE Veterinarians SET name = ?, specialization = ?, phone = ?, email = ? WHERE vet_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, vet.getName());
            ps.setString(2, vet.getSpecialization());
            ps.setString(3, vet.getPhone());
            ps.setString(4, vet.getEmail());
            ps.setInt(5, vet.getVetId());

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Counts scheduled or completed appointments linked to this vet.
     */
    public int countAppointmentsByVet(int vetId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM VetAppointments WHERE vet_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, vetId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    /**
     * Deletes a veterinarian only if no appointments are linked.
     */
    public boolean delete(int vetId) throws SQLException {
        int apptCount = countAppointmentsByVet(vetId);
        if (apptCount > 0) {
            throw new SQLException(String.format(
                    "Cannot delete Veterinarian ID %d: %d appointment(s) exist in VetAppointments. Cannot delete due to foreign key integrity.",
                    vetId, apptCount));
        }

        String sql = "DELETE FROM Veterinarians WHERE vet_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, vetId);
            return ps.executeUpdate() > 0;
        }
    }
}
