package com.pawcare.model;

import java.sql.Timestamp;

/**
 * Model class representing an Owner entity in the Owners table.
 */
public class Owner {
    private int ownerId;
    private String name;
    private String phone;
    private String email;
    private String address;
    private Timestamp createdAt;

    public Owner() {
    }

    public Owner(String name, String phone, String email, String address) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
    }

    public Owner(int ownerId, String name, String phone, String email, String address, Timestamp createdAt) {
        this.ownerId = ownerId;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.createdAt = createdAt;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return String.format("Owner [ID=%d, Name='%s', Phone='%s', Email='%s', Address='%s']",
                ownerId, name, phone, email, address != null ? address : "-");
    }
}
