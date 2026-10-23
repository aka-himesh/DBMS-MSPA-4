package com.pawcare.model;

import java.sql.Timestamp;

/**
 * Model class representing a Dog entity in the Dogs table.
 */
public class Dog {
    private int dogId;
    private int ownerId;
    private String name;
    private String breed;
    private int age;
    private String gender;
    private String medicalNotes;
    private Timestamp createdAt;

    // Additional display field populated via SQL JOINs
    private String ownerName;

    public Dog() {
    }

    public Dog(int ownerId, String name, String breed, int age, String gender, String medicalNotes) {
        this.ownerId = ownerId;
        this.name = name;
        this.breed = breed;
        this.age = age;
        this.gender = gender;
        this.medicalNotes = medicalNotes;
    }

    public Dog(int dogId, int ownerId, String name, String breed, int age, String gender, String medicalNotes, Timestamp createdAt) {
        this.dogId = dogId;
        this.ownerId = ownerId;
        this.name = name;
        this.breed = breed;
        this.age = age;
        this.gender = gender;
        this.medicalNotes = medicalNotes;
        this.createdAt = createdAt;
    }

    public int getDogId() {
        return dogId;
    }

    public void setDogId(int dogId) {
        this.dogId = dogId;
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

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getMedicalNotes() {
        return medicalNotes;
    }

    public void setMedicalNotes(String medicalNotes) {
        this.medicalNotes = medicalNotes;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    @Override
    public String toString() {
        return String.format("Dog [ID=%d, Name='%s', Breed='%s', Age=%d, Gender='%s', Owner='%s']",
                dogId, name, breed, age, gender, (ownerName != null ? ownerName : "ID:" + ownerId));
    }
}
