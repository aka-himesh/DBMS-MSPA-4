-- ============================================================================
-- PawCare: Sample Data Insertion Script
-- Populates pawcare_db with realistic initial records for demonstration
-- ============================================================================

USE pawcare_db;

-- Clear any existing rows in reverse dependency order
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE GroomingAppointments;
TRUNCATE TABLE GroomingServices;
TRUNCATE TABLE VetAppointments;
TRUNCATE TABLE Veterinarians;
TRUNCATE TABLE Dogs;
TRUNCATE TABLE Owners;
SET FOREIGN_KEY_CHECKS = 1;

-- ----------------------------------------------------------------------------
-- 1. Insert Pet Owners (5 Owners)
-- ----------------------------------------------------------------------------
INSERT INTO Owners (owner_id, name, phone, email, address) VALUES
(1, 'Aarav Sharma',     '9876543210', 'aarav.sharma@example.com',     '42 MG Road, Bangalore, Karnataka'),
(2, 'Priya Patel',      '9811223344', 'priya.patel@example.com',      '15 SV Road, Bandra, Mumbai'),
(3, 'Rohan Verma',      '9944556677', 'rohan.verma@example.com',      '78 Park Street, Kolkata, West Bengal'),
(4, 'Sneha Mukherjee',  '9722334455', 'sneha.mukherjee@example.com',  '102 Anna Salai, Chennai, Tamil Nadu'),
(5, 'Vikram Malhotra',  '9655443322', 'vikram.malhotra@example.com',  '25 Connaught Place, New Delhi');

-- ----------------------------------------------------------------------------
-- 2. Insert Dogs (7 Dogs across the 5 owners)
-- ----------------------------------------------------------------------------
INSERT INTO Dogs (dog_id, owner_id, name, breed, age, gender, medical_notes) VALUES
(1, 1, 'Max',      'Golden Retriever', 3, 'Male',   'Up to date on vaccinations; mild seasonal allergy.'),
(2, 1, 'Luna',     'Beagle',           2, 'Female', 'Sensitive stomach; requires hypoallergenic treats.'),
(3, 2, 'Rocky',    'German Shepherd',  4, 'Male',   'Hip dysplasia monitoring; no strenuous jumping.'),
(4, 3, 'Bella',    'Shih Tzu',         1, 'Female', 'Healthy puppy; needs regular coat brushing.'),
(5, 3, 'Bruno',    'Rottweiler',       5, 'Male',   'Routine tick & flea preventative applied monthly.'),
(6, 4, 'Coco',     'Poodle',           2, 'Female', 'Ear cleaning required due to moisture buildup.'),
(7, 5, 'Leo',      'Labrador',         6, 'Male',   'Arthritis in left hind leg; taking joint supplements.');

-- ----------------------------------------------------------------------------
-- 3. Insert Veterinarians (3 Veterinarians)
-- ----------------------------------------------------------------------------
INSERT INTO Veterinarians (vet_id, name, specialization, phone, email) VALUES
(1, 'Dr. Aditi Kulkarni', 'General Veterinary Medicine', '9820011223', 'dr.aditi@pawcare.com'),
(2, 'Dr. Rajesh Nair',    'Canine Orthopedics & Surgery', '9833445566', 'dr.rajesh@pawcare.com'),
(3, 'Dr. Meera Sen',      'Dermatology & Allergy Care',  '9844556677', 'dr.meera@pawcare.com');

-- ----------------------------------------------------------------------------
-- 4. Insert Grooming Services (5 Standard Services)
-- ----------------------------------------------------------------------------
INSERT INTO GroomingServices (service_id, service_name, description, price, duration_minutes) VALUES
(1, 'Bath & Blow Dry',     'Deep coat cleanse with organic shampoo, blow dry, and coat brushing.', 800.00,  45),
(2, 'Full Grooming',       'Bath, breed-specific haircut, blow dry, nail trimming, and ear cleaning.', 1800.00, 90),
(3, 'Nail Trimming',       'Safe clipping and smoothing of claws with styptic guard.', 350.00,  20),
(4, 'Ear & Teeth Hygiene', 'Delicate ear canal cleanse and enzymatic canine dental brushing.', 500.00,  30),
(5, 'De-Shedding Treatment','Specialized undercoat rake and deshedding wash to minimize shedding.', 1200.00, 60);

-- ----------------------------------------------------------------------------
-- 5. Insert Veterinary Appointments
-- Uses CURRENT_DATE offsets so reports (today, upcoming, past) are always populated.
-- ----------------------------------------------------------------------------
INSERT INTO VetAppointments (appointment_id, dog_id, vet_id, appointment_date, appointment_time, reason, status, consultation_fee) VALUES
-- Past Completed
(1, 1, 1, DATE_SUB(CURRENT_DATE, INTERVAL 5 DAY), '10:00:00', 'Annual vaccination & wellness checkup', 'Completed', 600.00),
(2, 3, 2, DATE_SUB(CURRENT_DATE, INTERVAL 3 DAY), '11:30:00', 'Hip joint pain evaluation and X-Ray',   'Completed', 1200.00),
(3, 2, 3, DATE_SUB(CURRENT_DATE, INTERVAL 2 DAY), '14:00:00', 'Skin rash diagnosis and topical balm',    'Completed', 750.00),
-- Today Scheduled
(4, 4, 1, CURRENT_DATE,                           '10:30:00', 'Routine puppy booster vaccination',      'Scheduled', 600.00),
(5, 7, 2, CURRENT_DATE,                           '15:00:00', 'Arthritis follow-up & pain medication',   'Scheduled', 900.00),
-- Upcoming Scheduled
(6, 6, 3, DATE_ADD(CURRENT_DATE, INTERVAL 2 DAY), '11:00:00', 'Ear inflammation checkup',               'Scheduled', 650.00),
(7, 5, 1, DATE_ADD(CURRENT_DATE, INTERVAL 4 DAY), '16:00:00', 'General health certificate for travel',  'Scheduled', 550.00),
-- Cancelled appointment
(8, 2, 1, DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY), '09:30:00', 'Mild vomiting inquiry (recovered)',      'Cancelled', 0.00);

-- ----------------------------------------------------------------------------
-- 6. Insert Grooming Appointments
-- ----------------------------------------------------------------------------
INSERT INTO GroomingAppointments (booking_id, dog_id, service_id, booking_date, booking_time, status, price_at_booking) VALUES
-- Past Completed
(1, 1, 2, DATE_SUB(CURRENT_DATE, INTERVAL 7 DAY), '10:00:00', 'Completed', 1800.00),
(2, 4, 1, DATE_SUB(CURRENT_DATE, INTERVAL 4 DAY), '14:00:00', 'Completed', 800.00),
(3, 6, 5, DATE_SUB(CURRENT_DATE, INTERVAL 2 DAY), '11:00:00', 'Completed', 1200.00),
-- Today Scheduled
(4, 2, 3, CURRENT_DATE,                           '11:30:00', 'Scheduled', 350.00),
(5, 7, 1, CURRENT_DATE,                           '16:30:00', 'Scheduled', 800.00),
-- Upcoming Scheduled
(6, 3, 4, DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), '10:00:00', 'Scheduled', 500.00),
(7, 5, 2, DATE_ADD(CURRENT_DATE, INTERVAL 3 DAY), '15:00:00', 'Scheduled', 1800.00),
-- Cancelled
(8, 4, 3, DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY), '12:00:00', 'Cancelled', 350.00);

-- Reset AUTO_INCREMENT counters to continue neatly after seed data
ALTER TABLE Owners AUTO_INCREMENT = 10;
ALTER TABLE Dogs AUTO_INCREMENT = 10;
ALTER TABLE Veterinarians AUTO_INCREMENT = 10;
ALTER TABLE GroomingServices AUTO_INCREMENT = 10;
ALTER TABLE VetAppointments AUTO_INCREMENT = 10;
ALTER TABLE GroomingAppointments AUTO_INCREMENT = 10;
