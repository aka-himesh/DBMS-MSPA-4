package com.pawcare.dao;

import com.pawcare.DBConnection;
import com.pawcare.model.Owner;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Owners table.
 * Encapsulates all CRUD operations using JDBC PreparedStatement.
 */
public class OwnerDAO {

    /**
     * Inserts a new owner into the database.
     * Uses RETURN_GENERATED_KEYS to retrieve auto-generated owner_id.
     */
    public int insert(Owner owner) throws SQLException {
        String sql = "INSERT INTO Owners (name, phone, email, address) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, owner.getName());
            ps.setString(2, owner.getPhone());
            ps.setString(3, owner.getEmail());
            ps.setString(4, owner.getAddress());

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        owner.setOwnerId(rs.getInt(1));
                        return owner.getOwnerId();
                    }
                }
            }
            return -1;
        }
    }

    /**
     * Retrieves all owners from the Owners table.
     */
    public List<Owner> findAll() throws SQLException {
        List<Owner> list = new ArrayList<>();
        String sql = "SELECT owner_id, name, phone, email, address, created_at FROM Owners ORDER BY owner_id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Owner o = new Owner(
                        rs.getInt("owner_id"),
                        rs.getString("name"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getString("address"),
                        rs.getTimestamp("created_at")
                );
                list.add(o);
            }
        }
        return list;
    }

    /**
     * Finds an owner by primary key (owner_id).
     */
    public Optional<Owner> findById(int ownerId) throws SQLException {
        String sql = "SELECT owner_id, name, phone, email, address, created_at FROM Owners WHERE owner_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, ownerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Owner o = new Owner(
                            rs.getInt("owner_id"),
                            rs.getString("name"),
                            rs.getString("phone"),
                            rs.getString("email"),
                            rs.getString("address"),
                            rs.getTimestamp("created_at")
                    );
                    return Optional.of(o);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Updates an owner's details.
     */
    public boolean update(Owner owner) throws SQLException {
        String sql = "UPDATE Owners SET name = ?, phone = ?, email = ?, address = ? WHERE owner_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, owner.getName());
            ps.setString(2, owner.getPhone());
            ps.setString(3, owner.getEmail());
            ps.setString(4, owner.getAddress());
            ps.setInt(5, owner.getOwnerId());

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Counts how many dogs are registered under an owner.
     * Used for pre-deletion safety checks.
     */
    public int countDogsByOwner(int ownerId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Dogs WHERE owner_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ownerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    /**
     * Deletes an owner record if safe.
     * Enforces foreign key integrity.
     */
    public boolean delete(int ownerId) throws SQLException {
        // Pre-check for dependent dog records
        int dogCount = countDogsByOwner(ownerId);
        if (dogCount > 0) {
            throw new SQLException(String.format(
                    "Cannot delete Owner ID %d: %d registered dog(s) exist. Please reassign or delete dogs first.",
                    ownerId, dogCount));
        }

        String sql = "DELETE FROM Owners WHERE owner_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ownerId);
            return ps.executeUpdate() > 0;
        }
    }
}
