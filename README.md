# PawCare — Dog Grooming and Veterinary Appointment Management System

A menu-driven desktop database application built using **Java, MySQL, and JDBC** for college DBMS practical examination demonstration.

---

## 1. Project Overview

**PawCare** provides a centralized, relational database management platform for veterinary clinics and pet grooming salons. It enforces relational integrity, prevents scheduling double-bookings, tracks service fees, and generates analytical business reports.

### Key Architectural Highlights
- **Direct JDBC Implementation**: Uses `Connection`, `PreparedStatement`, `ResultSet`, and explicit transaction management (`setAutoCommit(false)`, `commit()`, `rollback()`) without bulky ORM abstractions.
- **Strict Referential Integrity**: Primary keys, foreign keys with `ON DELETE RESTRICT`, check constraints, and unique constraints.
- **Relational SQL Queries**: Extensive multi-table `INNER JOIN` and `LEFT JOIN` queries linking up to 4 tables.
- **Zero-Crash Console Experience**: Defensive input validation for dates, times, emails, and numeric inputs with tabular ASCII rendering.
- **Flexible Execution**: Can be compiled and run with standard `javac`/`java` using the included `lib/` JAR, or built with Apache Maven.

---

## 2. Directory Structure

```
DBMS-MSPA-4/
├── pom.xml                               # Maven project descriptor
├── README.md                             # Complete setup & execution guide
├── PRACTICAL_REPORT.md                   # College DBMS Practical 10 lab report
├── .gitignore                            # Excludes credentials and binaries
├── compile.bat                           # 1-click Windows compilation script
├── run.bat                               # 1-click Windows execution script
├── setup_database.bat                    # Interactive MySQL DB setup script
├── db.properties                         # Local active DB credentials (gitignored)
├── config/
│   └── db.properties.example             # Credential configuration template
├── lib/
│   └── mysql-connector-j-9.2.0.jar       # Standalone MySQL JDBC driver JAR
├── database/
│   ├── schema.sql                        # DDL table creation script with constraints
│   └── sample_data.sql                   # DML seed data (owners, dogs, vets, bookings)
└── src/
    └── main/
        └── java/
            └── com/
                └── pawcare/
                    ├── Main.java         # Console entry point & main menu
                    ├── DBConnection.java # Reusable JDBC connection manager
                    ├── model/            # Entity models
                    │   ├── Owner.java
                    │   ├── Dog.java
                    │   ├── Veterinarian.java
                    │   ├── VetAppointment.java
                    │   ├── GroomingService.java
                    │   └── GroomingAppointment.java
                    ├── dao/              # JDBC Data Access Objects
                    │   ├── OwnerDAO.java
                    │   ├── DogDAO.java
                    │   ├── VeterinarianDAO.java
                    │   ├── VetAppointmentDAO.java
                    │   ├── GroomingServiceDAO.java
                    │   ├── GroomingAppointmentDAO.java
                    │   └── ReportDAO.java
                    ├── service/          # Business logic & Submenu controllers
                    │   ├── OwnerService.java
                    │   ├── DogService.java
                    │   ├── VeterinarianService.java
                    │   ├── VetAppointmentService.java
                    │   ├── GroomingServiceManager.java
                    │   ├── GroomingAppointmentService.java
                    │   └── ReportService.java
                    └── util/             # Utility classes
                        ├── InputValidator.java
                        └── ConsoleFormatter.java
```

---

## 3. Prerequisites

1. **Java Development Kit (JDK)**: JDK 17, 21, or 25.
   - Verify by running in PowerShell / Command Prompt:
     ```bash
     javac -version
     java -version
     ```
2. **MySQL Server 8.0+**:
   - Ensure the `MySQL80` service is running in Windows Services (`services.msc`).
3. **MySQL Workbench** or `mysql` CLI client.

---

## 4. Database Setup Instructions

### Method A: Automated Setup (Recommended)
Run the automated batch script:
```cmd
setup_database.bat
```
The script will prompt for your MySQL root password, automatically create `pawcare_db`, execute `database/schema.sql`, populate `database/sample_data.sql`, and configure `db.properties`.

### Method B: Manual MySQL CLI Setup
Open Command Prompt or PowerShell:
```cmd
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p < database\schema.sql
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p < database\sample_data.sql
```

### Method C: MySQL Workbench
1. Open MySQL Workbench and connect to your local MySQL connection.
2. Go to **File -> Open SQL Script...** and select `database/schema.sql`.
3. Click the **Execute (lightning bolt)** button to create the database and tables.
4. Go to **File -> Open SQL Script...** and select `database/sample_data.sql`.
5. Click **Execute** to seed initial records.

---

## 5. Configuring Database Credentials

Open `db.properties` in the project root directory (or copy from `config/db.properties.example`):

```properties
db.url=jdbc:mysql://localhost:3306/pawcare_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.user=root
db.password=YOUR_LOCAL_MYSQL_PASSWORD
```

> **Note**: Replace `YOUR_LOCAL_MYSQL_PASSWORD` with the password configured during your MySQL Server installation. Alternatively, you can set the environment variables `DB_USER` and `DB_PASSWORD`.

---

## 6. How to Compile and Run

### Option 1: Using Batch Scripts (No Maven Required)
- **Compile**: Double-click `compile.bat` or run:
  ```cmd
  compile.bat
  ```
- **Run**: Double-click `run.bat` or run:
  ```cmd
  run.bat
  ```

### Option 2: Using Standard CLI Commands
```cmd
# 1. Compile all Java classes into bin/
javac -cp ".;lib\mysql-connector-j-9.2.0.jar" -d bin src\main\java\com\pawcare\model\*.java src\main\java\com\pawcare\util\*.java src\main\java\com\pawcare\dao\*.java src\main\java\com\pawcare\service\*.java src\main\java\com\pawcare\*.java

# 2. Run Main application
java -cp "bin;lib\mysql-connector-j-9.2.0.jar;." com.pawcare.Main
```

### Option 3: Using Apache Maven (If Installed)
```bash
mvn clean compile
mvn exec:java -Dexec.mainClass="com.pawcare.Main"
```

---

## 7. Application Features & Menu Structure

```
========== PAWCARE MANAGEMENT SYSTEM ==========
  1. Owner Management
     ├── Register New Pet Owner
     ├── Display All Owners
     ├── Search Owner by ID
     ├── Update Owner Information
     └── Delete Owner (FK Protected)
  2. Dog Management
     ├── Register Dog (Under Existing Owner)
     ├── Display All Dogs (with Owner JOIN)
     ├── Search Dog by ID
     ├── Update Dog Information
     └── Delete Dog (Protected)
  3. Veterinarian Management
     ├── Add New Veterinarian
     ├── Display All Veterinarians
     ├── Search Veterinarian by ID
     ├── Update Veterinarian Details
     └── Delete Veterinarian (FK Protected)
  4. Veterinary Appointments
     ├── Book Veterinary Appointment (Conflict & Past-date Checked)
     ├── Display All Appointments (Multi-Table JOIN)
     ├── Search Appointments (by ID, Dog, Vet, or Date)
     ├── Update Status (Scheduled / Completed / Cancelled)
     ├── Reschedule Appointment
     └── Cancel Appointment
  5. Grooming Services
     ├── Add Grooming Service Package
     ├── Display Available Services
     ├── Update Service Details & Price
     └── Delete Grooming Service (FK Protected)
  6. Grooming Appointments
     ├── Book Dog Grooming Session (Snapshots Price)
     ├── Display All Grooming Bookings (with JOINs)
     ├── Search Bookings by Date
     ├── Update Booking Status
     ├── Reschedule Grooming Session
     └── Cancel Grooming Booking
  7. Reports
     ├── System Summary (Total Counts)
     ├── Today's Veterinary Appointments
     ├── Upcoming Appointments
     ├── Status Breakdown Summary
     ├── Most Frequently Booked Grooming Services (GROUP BY)
     ├── Total Realized Revenue (Completed Sessions)
     └── Run All Reports (Exam Demo Mode)
  8. Exit
  ---------------------------------------------
  0. Test / Reconnect MySQL JDBC Connection
```

---

## 8. Troubleshooting Common JDBC Connection Errors

1. **`ERROR 1045 (28000): Access denied for user 'root'@'localhost'`**
   - **Cause**: Incorrect MySQL root password specified in `db.properties`.
   - **Solution**: Open `db.properties` and verify `db.password`. If you forgot the root password, reset it via MySQL Workbench or MySQL Installer.

2. **`Communications link failure / Connection refused`**
   - **Cause**: MySQL Server service is stopped or port 3306 is blocked.
   - **Solution**: Open Windows Services (`services.msc`), find `MySQL80`, and click **Start**.

3. **`ClassNotFoundException: com.mysql.cj.jdbc.Driver`**
   - **Cause**: MySQL Connector/J JAR is missing from the classpath.
   - **Solution**: Ensure `lib/mysql-connector-j-9.2.0.jar` exists and `-cp "bin;lib\mysql-connector-j-9.2.0.jar;."` is supplied when executing `java`.

4. **`Unknown database 'pawcare_db'`**
   - **Cause**: Schema has not been created yet.
   - **Solution**: Execute `database/schema.sql` using `setup_database.bat` or MySQL Workbench.
