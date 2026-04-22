# 🏥 E-Healthcare Management System

A full-stack Healthcare Management System built with **Java 17 + JavaFX**, **MySQL 8**, and an **HTML/CSS web portal**.

---

## 📁 Project Structure

```
ehealthcare/
├── pom.xml                          ← Maven build file
├── sql/
│   └── schema.sql                   ← Complete DB schema + seed data
├── src/
│   ├── auth/
│   │   ├── User.java                ← User model
│   │   ├── AuthService.java         ← Login / Register / BCrypt
│   │   └── LoginWindow.java         ← JavaFX Login UI
│   ├── db/
│   │   └── DatabaseConnection.java  ← Singleton JDBC connection
│   ├── patient/
│   │   ├── PatientDAO.java          ← Patient CRUD
│   │   └── PatientDashboard.java    ← Patient JavaFX UI
│   ├── doctor/
│   │   └── DoctorDashboard.java     ← Doctor JavaFX UI
│   ├── appointment/
│   │   └── AppointmentDAO.java      ← Book / Reschedule / Cancel
│   ├── emr/
│   │   └── EMRAndPrescriptionDAO.java ← Medical records + Rx
│   ├── billing/
│   │   └── BillingDAO.java          ← Bills + Invoice generator
│   ├── pharmacy/
│   │   └── PharmacistDashboard.java ← Pharmacist JavaFX UI
│   └── admin/
│       └── AdminDashboard.java      ← Admin JavaFX UI
└── web/
    └── patient-portal.html          ← HTML/CSS web portal
```

---

## 🛠️ Setup Instructions

### 1. Prerequisites

| Tool        | Version  |
|-------------|----------|
| Java JDK    | 17+      |
| JavaFX SDK  | 21+      |
| MySQL       | 8.0+     |
| Maven       | 3.8+     |

### 2. Database Setup

```bash
# Login to MySQL
mysql -u root -p

# Run the schema script
source /path/to/ehealthcare/sql/schema.sql;
```

This creates:
- Database `ehealthcare`
- All 8 tables with foreign key relationships
- Sample data: 2 doctors, 1 patient, 5 medicines

### 3. Configure Database Credentials

Edit `src/db/DatabaseConnection.java`:

```java
private static final String DB_URL  = "jdbc:mysql://localhost:3306/ehealthcare?useSSL=false&serverTimezone=UTC";
private static final String DB_USER = "root";
private static final String DB_PASS = "YOUR_PASSWORD";  // ← change this
```

### 4. Build & Run

```bash
# From project root
mvn clean install

# Run the application
mvn javafx:run
```

Or, to run the fat JAR:

```bash
java -jar target/ehealthcare-system-1.0.0-jar-with-dependencies.jar
```

---

## 🔑 Default Login Credentials

| Role        | Username   | Password   |
|-------------|------------|------------|
| Admin       | admin      | Admin@123  |
| Doctor      | dr_sharma  | Admin@123  |
| Doctor      | dr_patel   | Admin@123  |
| Patient     | patient01  | Admin@123  |
| Pharmacist  | pharma01   | Admin@123  |

> **Security Note**: All passwords are BCrypt-hashed in the database. Change default passwords before any production use.

---

## 🗄️ Database Schema

### Entity Relationships

```
users (1) ──── (1) patients
users (1) ──── (1) doctors
patients (1) ── (∞) appointments ──── (1) doctors
patients (1) ── (∞) medical_records ── (1) doctors
patients (1) ── (∞) prescriptions ──── (1) doctors
patients (1) ── (∞) billing
medical_records (1) ── (∞) prescriptions
```

### Tables Summary

| Table            | Key Columns                                    |
|------------------|------------------------------------------------|
| `users`          | user_id, username, password(BCrypt), role      |
| `patients`       | patient_id, user_id(FK), name, medical_history |
| `doctors`        | doctor_id, user_id(FK), specialization, fee    |
| `appointments`   | appointment_id, patient_id(FK), doctor_id(FK), status |
| `medical_records`| record_id, diagnosis, treatment, lab_reports   |
| `prescriptions`  | prescription_id, medicine, dosage, status      |
| `billing`        | bill_id, total_amount, payment_status          |
| `medicines`      | medicine_id, name, stock_qty, unit_price       |

---

## 🎨 Dashboard Overviews

### 🔴 Admin Dashboard
- Real-time stats: patient count, today's appointments, revenue
- Sidebar navigation: Patients, Doctors, Departments, Billing, Reports
- Color theme: Deep Navy (`#1a3c5e`)

### 🟢 Doctor Dashboard
- Today's appointments with patient details
- Full appointment history
- Medical records entry
- Prescription writing
- Color theme: Forest Green (`#1a5c2e`)

### 🟣 Patient Dashboard
- Home summary with stats cards
- Appointment booking form (doctor selection, date/time picker)
- My appointments, records, prescriptions, and bills
- Color theme: Royal Purple (`#5b2d8e`)

### 🟠 Pharmacist Dashboard
- Pending prescriptions queue
- One-click dispense with timestamp
- Medicine stock table (qty, price, expiry, reorder alerts)
- Color theme: Amber Brown (`#8b4500`)

---

## 🌐 Web Portal

Open `web/patient-portal.html` directly in a browser. Features:
- Responsive hero with animated CTA
- Stats bar (patients, doctors, departments, satisfaction)
- Services grid (6 service cards)
- Appointment booking form (connects to Java backend via REST in full deployment)
- Doctor profiles
- Footer with contact info

---

## 📦 Key Dependencies

| Dependency            | Purpose                          |
|-----------------------|----------------------------------|
| `javafx-controls`     | Desktop GUI components           |
| `mysql-connector-j`   | JDBC database connectivity       |
| `jbcrypt`             | Password hashing (BCrypt)        |
| `itextpdf`            | PDF invoice generation           |
| `junit-jupiter`       | Unit testing                     |

---

## 🚀 Extending the System

### Add REST API (Spring Boot)
Replace `AuthServlet` with Spring `@RestController`:
```java
@PostMapping("/api/login")
public ResponseEntity<User> login(@RequestBody LoginRequest req) {
    User user = AuthService.login(req.username(), req.password());
    return ResponseEntity.ok(user);
}
```

### Add Report Generation
Use JFreeChart for graphical reports:
```java
JFreeChart chart = ChartFactory.createBarChart(
    "Monthly Revenue", "Month", "₹ Amount", dataset);
```

### Add Email Notifications
Use JavaMail API:
```java
MimeMessage message = mailSender.createMimeMessage();
message.setRecipient(RecipientType.TO, new InternetAddress(patient.getEmail()));
```

---

## 📋 Checklist – Module Completion

- [x] User Authentication (BCrypt + role-based routing)
- [x] Patient Registration & Profile (DAO + JavaFX UI)
- [x] Doctor Profiles & Availability
- [x] Appointment Booking / Reschedule / Cancel
- [x] Electronic Medical Records (EMR)
- [x] Prescriptions (write + dispense)
- [x] Pharmacy Stock Management
- [x] Billing & Invoice Generation (text format)
- [x] Admin Dashboard with revenue stats
- [x] Web-based Patient Portal (HTML/CSS)
- [x] SQL Schema with seed data
- [ ] PDF Invoice export (iText – extend BillingDAO)
- [ ] REST API layer (add Spring Boot module)
- [ ] Email / SMS notifications
- [ ] Lab Report upload (file system integration)

---

*Built for academic/professional demonstration. Configure security settings before production deployment.*
