package com.pawcare.dao;

import com.pawcare.DBConnection;
import com.pawcare.model.Dog;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Dogs table.
 * Demonstrates relational queries, SQL JOINs with Owners, and referential integrity checks.
 */
public class DogDAO {

    /**
     * Inserts a new Dog record into the database.
     */
    public int insert(Dog dog) throws SQLException {
        String sql = "INSERT INTO Dogs (owner_id, name, breed, age, gender, medical_notes) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, dog.getOwnerId());
            ps.setString(2, dog.getName());
            ps.setString(3, dog.getBreed());
            ps.setInt(4, dog.getAge());
            ps.setString(5, dog.getGender());
            ps.setString(6, dog.getMedicalNotes());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        dog.setDogId(rs.getInt(1));
                        return dog.getDogId();
                    }
                }
            }
            return -1;
        }
    }

    /**
     * Retrieves all dogs with their respective owner names using an INNER JOIN.
     * Demonstrates: SELECT d.*, o.name FROM Dogs d INNER JOIN Owners o ON d.owner_id = o.owner_id
     */
    public List<Dog> findAllWithOwners() throws SQLException {
        List<Dog> list = new ArrayList<>();
        String sql = """
            SELECT d.dog_id, d.owner_id, d.name, d.breed, d.age, d.gender, d.medical_notes, d.created_at,
                   o.name AS owner_name
            FROM Dogs d
            INNER JOIN Owners o ON d.owner_id = o.owner_id
            ORDER BY d.dog_id ASC
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Dog d = new Dog(
                        rs.getInt("dog_id"),
                        rs.getInt("owner_id"),
                        rs.getString("name"),
                        rs.getString("breed"),
                        rs.getInt("age"),
                        rs.getString("gender"),
                        rs.getString("medical_notes"),
                        rs.getTimestamp("created_at")
                );
                d.setOwnerName(rs.getString("owner_name"));
                list.add(d);
            }
        }
        return list;
    }

    /**
     * Finds a single dog by ID with owner name joined.
     */
    public Optional<Dog> findById(int dogId) throws SQLException {
        String sql = """
            SELECT d.dog_id, d.owner_id, d.name, d.breed, d.age, d.gender, d.medical_notes, d.created_at,
                   o.name AS owner_name
            FROM Dogs d
            INNER JOIN Owners o ON d.owner_id = o.owner_id
            WHERE d.dog_id = ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, dogId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Dog d = new Dog(
                            rs.getInt("dog_id"),
                            rs.getInt("owner_id"),
                            rs.getString("name"),
                            rs.getString("breed"),
                            rs.getInt("age"),
                            rs.getString("gender"),
                            rs.getString("medical_notes"),
                            rs.getTimestamp("created_at")
                    );
                    d.setOwnerName(rs.getString("owner_name"));
                    return Optional.of(d);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Finds all dogs belonging to a particular owner ID.
     */
    public List<Dog> findByOwnerId(int ownerId) throws SQLException {
        List<Dog> list = new ArrayList<>();
        String sql = "SELECT dog_id, owner_id, name, breed, age, gender, medical_notes, created_at FROM Dogs WHERE owner_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, ownerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Dog d = new Dog(
                            rs.getInt("dog_id"),
                            rs.getInt("owner_id"),
                            rs.getString("name"),
                            rs.getString("breed"),
                            rs.getInt("age"),
                            rs.getString("gender"),
                            rs.getString("medical_notes"),
                            rs.getTimestamp("created_at")
                    );
                    list.add(d);
                }
            }
        }
        return list;
    }

    /**
     * Updates an existing dog record.
     */
    public boolean update(Dog dog) throws SQLException {
        String sql = "UPDATE Dogs SET name = ?, breed = ?, age = ?, gender = ?, medical_notes = ? WHERE dog_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, dog.getName());
            ps.setString(2, dog.getBreed());
            ps.setInt(3, dog.getAge());
            ps.setString(4, dog.getGender());
            ps.setString(5, dog.getMedicalNotes());
            ps.setInt(6, dog.getDogId());

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Counts active or past appointments for a dog across both vet and grooming tables.
     */
    public int countAppointmentsForDog(int dogId) throws SQLException {
        String sql = """
            SELECT (SELECT COUNT(*) FROM VetAppointments WHERE dog_id = ?) +
                   (SELECT COUNT(*) FROM GroomingAppointments WHERE dog_id = ?) AS total
        """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dogId);
            ps.setInt(2, dogId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        }
        return 0;
    }

    /**
     * Deletes a dog if safe. Prevents accidental loss of appointment history.
     */
    public boolean delete(int dogId) throws SQLException {
        int apptCount = countAppointmentsForDog(dogId);
        if (apptCount > 0) {
            throw new SQLException(String.format(
                    "Cannot delete Dog ID %d: %d appointment/grooming record(s) exist. Historical medical logs cannot be deleted.",
                    dogId, apptCount));
        }

        String sql = "DELETE FROM Dogs WHERE dog_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dogId);
            return ps.executeUpdate() > 0;
        }
    }
}
