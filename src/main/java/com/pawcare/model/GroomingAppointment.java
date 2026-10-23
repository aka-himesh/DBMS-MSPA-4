package com.pawcare.model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

/**
 * Model class representing a booked grooming session in GroomingAppointments.
 */
public class GroomingAppointment {
    private int bookingId;
    private int dogId;
    private int serviceId;
    private Date bookingDate;
    private Time bookingTime;
    private String status; // 'Scheduled', 'Completed', 'Cancelled'
    private BigDecimal priceAtBooking;
    private Timestamp createdAt;

    // Display fields populated via SQL JOINs
    private String dogName;
    private String ownerName;
    private String serviceName;

    public GroomingAppointment() {
    }

    public GroomingAppointment(int dogId, int serviceId, Date bookingDate, Time bookingTime,
                               String status, BigDecimal priceAtBooking) {
        this.dogId = dogId;
        this.serviceId = serviceId;
        this.bookingDate = bookingDate;
        this.bookingTime = bookingTime;
        this.status = status;
        this.priceAtBooking = priceAtBooking;
    }

    public GroomingAppointment(int bookingId, int dogId, int serviceId, Date bookingDate,
                               Time bookingTime, String status, BigDecimal priceAtBooking, Timestamp createdAt) {
        this.bookingId = bookingId;
        this.dogId = dogId;
        this.serviceId = serviceId;
        this.bookingDate = bookingDate;
        this.bookingTime = bookingTime;
        this.status = status;
        this.priceAtBooking = priceAtBooking;
        this.createdAt = createdAt;
    }

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public int getDogId() {
        return dogId;
    }

    public void setDogId(int dogId) {
        this.dogId = dogId;
    }

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public Date getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(Date bookingDate) {
        this.bookingDate = bookingDate;
    }

    public Time getBookingTime() {
        return bookingTime;
    }

    public void setBookingTime(Time bookingTime) {
        this.bookingTime = bookingTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getPriceAtBooking() {
        return priceAtBooking;
    }

    public void setPriceAtBooking(BigDecimal priceAtBooking) {
        this.priceAtBooking = priceAtBooking;
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

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public String toString() {
        return String.format("GroomingAppointment [ID=%d, Dog=%s, Owner=%s, Service=%s, Date=%s %s, Status=%s, Price=₹%.2f]",
                bookingId,
                (dogName != null ? dogName : "Dog#" + dogId),
                (ownerName != null ? ownerName : "-"),
                (serviceName != null ? serviceName : "Service#" + serviceId),
                bookingDate, bookingTime, status, priceAtBooking);
    }
}
