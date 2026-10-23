package com.pawcare.model;

import java.sql.Timestamp;

/**
 * Model class representing a Veterinarian entity in the Veterinarians table.
 */
public class Veterinarian {
    private int vetId;
    private String name;
    private String specialization;
    private String phone;
    private String email;
    private Timestamp createdAt;

    public Veterinarian() {
    }

    public Veterinarian(String name, String specialization, String phone, String email) {
        this.name = name;
        this.specialization = specialization;
        this.phone = phone;
        this.email = email;
    }

    public Veterinarian(int vetId, String name, String specialization, String phone, String email, Timestamp createdAt) {
        this.vetId = vetId;
        this.name = name;
        this.specialization = specialization;
        this.phone = phone;
        this.email = email;
        this.createdAt = createdAt;
    }

    public int getVetId() {
        return vetId;
    }

    public void setVetId(int vetId) {
        this.vetId = vetId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return String.format("Veterinarian [ID=%d, Name='%s', Specialization='%s', Phone='%s', Email='%s']",
                vetId, name, specialization, phone, email);
    }
}
