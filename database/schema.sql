-- ============================================================================
-- PawCare: Dog Grooming and Veterinary Appointment Management System
-- DBMS Mini-Project / Practical 10
-- Relational Database Schema (MySQL 8.0+)
-- ============================================================================

DROP DATABASE IF EXISTS pawcare_db;
CREATE DATABASE pawcare_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE pawcare_db;

-- ----------------------------------------------------------------------------
-- Table 1: Owners
-- Stores pet owners' personal and contact information.
-- ----------------------------------------------------------------------------
CREATE TABLE Owners (
    owner_id    INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    phone       VARCHAR(15)  NOT NULL,
    email       VARCHAR(100) NOT NULL UNIQUE,
    address     VARCHAR(255),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_owner_phone CHECK (LENGTH(phone) >= 10)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- Table 2: Dogs
-- Stores dog records linked to their registered pet owner.
-- Foreign Key: owner_id -> Owners(owner_id) [ON DELETE RESTRICT]
-- ----------------------------------------------------------------------------
CREATE TABLE Dogs (
    dog_id        INT AUTO_INCREMENT PRIMARY KEY,
    owner_id      INT NOT NULL,
    name          VARCHAR(100) NOT NULL,
    breed         VARCHAR(100),
    age           INT,
    gender        VARCHAR(20),
    medical_notes TEXT,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_dogs_owner
        FOREIGN KEY (owner_id) REFERENCES Owners(owner_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT chk_dog_age CHECK (age >= 0 AND age <= 35)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- Table 3: Veterinarians
-- Stores veterinarian professional and contact information.
-- ----------------------------------------------------------------------------
CREATE TABLE Veterinarians (
    vet_id         INT AUTO_INCREMENT PRIMARY KEY,
    name           VARCHAR(100) NOT NULL,
    specialization VARCHAR(100) NOT NULL,
    phone          VARCHAR(15)  NOT NULL,
    email          VARCHAR(100) NOT NULL UNIQUE,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- Table 4: VetAppointments
-- Records clinical consultations between a dog and a veterinarian.
-- Foreign Keys: dog_id -> Dogs(dog_id), vet_id -> Veterinarians(vet_id)
-- ----------------------------------------------------------------------------
CREATE TABLE VetAppointments (
    appointment_id   INT AUTO_INCREMENT PRIMARY KEY,
    dog_id           INT NOT NULL,
    vet_id           INT NOT NULL,
    appointment_date DATE NOT NULL,
    appointment_time TIME NOT NULL,
    reason           VARCHAR(255) NOT NULL,
    status           VARCHAR(20)  NOT NULL DEFAULT 'Scheduled',
    consultation_fee DECIMAL(10,2) NOT NULL DEFAULT 500.00,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_vetapp_dog
        FOREIGN KEY (dog_id) REFERENCES Dogs(dog_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_vetapp_vet
        FOREIGN KEY (vet_id) REFERENCES Veterinarians(vet_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT chk_vetapp_status
        CHECK (status IN ('Scheduled', 'Completed', 'Cancelled'))
) ENGINE=InnoDB;

-- Index to optimize conflict checking and calendar lookups
CREATE INDEX idx_vet_datetime ON VetAppointments(vet_id, appointment_date, appointment_time);

-- ----------------------------------------------------------------------------
-- Table 5: GroomingServices
-- Catalog of available grooming services, standard prices, and duration.
-- ----------------------------------------------------------------------------
CREATE TABLE GroomingServices (
    service_id       INT AUTO_INCREMENT PRIMARY KEY,
    service_name     VARCHAR(100) NOT NULL UNIQUE,
    description      TEXT,
    price            DECIMAL(10,2) NOT NULL,
    duration_minutes INT NOT NULL,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_service_price CHECK (price >= 0),
    CONSTRAINT chk_service_duration CHECK (duration_minutes > 0)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- Table 6: GroomingAppointments
-- Records grooming sessions booked for dogs.
-- Foreign Keys: dog_id -> Dogs(dog_id), service_id -> GroomingServices(service_id)
-- ----------------------------------------------------------------------------
CREATE TABLE GroomingAppointments (
    booking_id       INT AUTO_INCREMENT PRIMARY KEY,
    dog_id           INT NOT NULL,
    service_id       INT NOT NULL,
    booking_date     DATE NOT NULL,
    booking_time     TIME NOT NULL,
    status           VARCHAR(20) NOT NULL DEFAULT 'Scheduled',
    price_at_booking DECIMAL(10,2) NOT NULL,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_groomapp_dog
        FOREIGN KEY (dog_id) REFERENCES Dogs(dog_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_groomapp_service
        FOREIGN KEY (service_id) REFERENCES GroomingServices(service_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT chk_groomapp_status
        CHECK (status IN ('Scheduled', 'Completed', 'Cancelled'))
) ENGINE=InnoDB;

-- Index to optimize grooming schedule conflict lookups
CREATE INDEX idx_groom_datetime ON GroomingAppointments(booking_date, booking_time);
