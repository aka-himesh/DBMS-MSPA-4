package com.pawcare.model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

/**
 * Model class representing an appointment with a veterinarian.
 */
public class VetAppointment {
    private int appointmentId;
    private int dogId;
    private int vetId;
    private Date appointmentDate;
    private Time appointmentTime;
    private String reason;
    private String status; // 'Scheduled', 'Completed', 'Cancelled'
    private BigDecimal consultationFee;
    private Timestamp createdAt;

    // Display fields populated via SQL JOINs
    private String dogName;
    private String ownerName;
    private String vetName;

    public VetAppointment() {
    }

    public VetAppointment(int dogId, int vetId, Date appointmentDate, Time appointmentTime,
                          String reason, String status, BigDecimal consultationFee) {
        this.dogId = dogId;
        this.vetId = vetId;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.reason = reason;
        this.status = status;
        this.consultationFee = consultationFee;
    }

    public VetAppointment(int appointmentId, int dogId, int vetId, Date appointmentDate,
                          Time appointmentTime, String reason, String status,
                          BigDecimal consultationFee, Timestamp createdAt) {
        this.appointmentId = appointmentId;
        this.dogId = dogId;
        this.vetId = vetId;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.reason = reason;
        this.status = status;
        this.consultationFee = consultationFee;
        this.createdAt = createdAt;
    }

    public int getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
    }

    public int getDogId() {
        return dogId;
    }

    public void setDogId(int dogId) {
        this.dogId = dogId;
    }

    public int getVetId() {
        return vetId;
    }

    public void setVetId(int vetId) {
        this.vetId = vetId;
    }

    public Date getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(Date appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public Time getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(Time appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(BigDecimal consultationFee) {
        this.consultationFee = consultationFee;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getDogName() {
        return dogName;
    }

    public void setDogName(String dogName) {
        this.dogName = dogName;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getVetName() {
        return vetName;
    }

    public void setVetName(String vetName) {
        this.vetName = vetName;
    }

    @Override
    public String toString() {
        return String.format("VetAppointment [ID=%d, Dog=%s, Owner=%s, Vet=%s, Date=%s %s, Status=%s, Fee=₹%.2f]",
                appointmentId,
                (dogName != null ? dogName : "Dog#" + dogId),
                (ownerName != null ? ownerName : "-"),
                (vetName != null ? vetName : "Vet#" + vetId),
                appointmentDate, appointmentTime, status, consultationFee);
    }
}
