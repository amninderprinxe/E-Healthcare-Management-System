-- ============================================================
--  E-Healthcare Management System – Database Schema
--  MySQL 8.0+
-- ============================================================

CREATE DATABASE IF NOT EXISTS ehealthcare;
USE ehealthcare;

-- ─────────────────────────────────────────────
--  1. USERS  (authentication & roles)
-- ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS users (
    user_id     INT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,          -- BCrypt hash
    role        ENUM('ADMIN','DOCTOR','PATIENT','PHARMACIST') NOT NULL,
    email       VARCHAR(100) UNIQUE,
    full_name   VARCHAR(100),
    phone       VARCHAR(20),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active   BOOLEAN   DEFAULT TRUE
);

-- ─────────────────────────────────────────────
--  2. PATIENTS
-- ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS patients (
    patient_id      INT AUTO_INCREMENT PRIMARY KEY,
    user_id         INT NOT NULL,
    name            VARCHAR(100) NOT NULL,
    date_of_birth   DATE,
    age             INT,
    gender          ENUM('Male','Female','Other'),
    blood_group     VARCHAR(5),
    contact         VARCHAR(20),
    address         TEXT,
    emergency_contact VARCHAR(20),
    medical_history TEXT,
    allergies       TEXT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- ─────────────────────────────────────────────
--  3. DOCTORS
-- ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS doctors (
    doctor_id       INT AUTO_INCREMENT PRIMARY KEY,
    user_id         INT NOT NULL,
    name            VARCHAR(100) NOT NULL,
    specialization  VARCHAR(100),
    department      VARCHAR(100),
    qualification   VARCHAR(200),
    experience_yrs  INT DEFAULT 0,
    license_number  VARCHAR(50) UNIQUE,
    consultation_fee DECIMAL(10,2) DEFAULT 0.00,
    availability    TEXT,                         -- JSON string of schedule
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- ─────────────────────────────────────────────
--  4. DEPARTMENTS
-- ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS departments (
    dept_id     INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    head_doctor INT,
    FOREIGN KEY (head_doctor) REFERENCES doctors(doctor_id) ON DELETE SET NULL
);

-- ─────────────────────────────────────────────
--  5. APPOINTMENTS
-- ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS appointments (
    appointment_id  INT AUTO_INCREMENT PRIMARY KEY,
    patient_id      INT NOT NULL,
    doctor_id       INT NOT NULL,
    appointment_date DATE NOT NULL,
    appointment_time TIME NOT NULL,
    reason          TEXT,
    status          ENUM('SCHEDULED','COMPLETED','CANCELLED','RESCHEDULED') DEFAULT 'SCHEDULED',
    notes           TEXT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id)  REFERENCES doctors(doctor_id)  ON DELETE CASCADE
);

-- ─────────────────────────────────────────────
--  6. ELECTRONIC MEDICAL RECORDS (EMR)
-- ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS medical_records (
    record_id       INT AUTO_INCREMENT PRIMARY KEY,
    patient_id      INT NOT NULL,
    doctor_id       INT NOT NULL,
    appointment_id  INT,
    diagnosis       TEXT,
    symptoms        TEXT,
    treatment       TEXT,
    lab_reports     TEXT,
    visit_date      DATE NOT NULL,
    follow_up_date  DATE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id)     REFERENCES patients(patient_id)         ON DELETE CASCADE,
    FOREIGN KEY (doctor_id)      REFERENCES doctors(doctor_id)           ON DELETE CASCADE,
    FOREIGN KEY (appointment_id) REFERENCES appointments(appointment_id) ON DELETE SET NULL
);

-- ─────────────────────────────────────────────
--  7. PRESCRIPTIONS
-- ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS prescriptions (
    prescription_id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id      INT NOT NULL,
    doctor_id       INT NOT NULL,
    record_id       INT,
    medicine        VARCHAR(200) NOT NULL,
    dosage          VARCHAR(100),
    frequency       VARCHAR(100),
    duration        VARCHAR(100),
    instructions    TEXT,
    status          ENUM('PENDING','DISPENSED','CANCELLED') DEFAULT 'PENDING',
    prescribed_date DATE NOT NULL,
    dispensed_date  DATE,
    dispensed_by    INT,                          -- pharmacist user_id
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id)  REFERENCES doctors(doctor_id)  ON DELETE CASCADE,
    FOREIGN KEY (record_id)  REFERENCES medical_records(record_id) ON DELETE SET NULL
);

-- ─────────────────────────────────────────────
--  8. MEDICINES / PHARMACY STOCK
-- ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS medicines (
    medicine_id     INT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(200) NOT NULL,
    generic_name    VARCHAR(200),
    category        VARCHAR(100),
    manufacturer    VARCHAR(200),
    unit_price      DECIMAL(10,2) NOT NULL,
    stock_qty       INT DEFAULT 0,
    expiry_date     DATE,
    reorder_level   INT DEFAULT 10,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ─────────────────────────────────────────────
--  9. BILLING
-- ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS billing (
    bill_id         INT AUTO_INCREMENT PRIMARY KEY,
    patient_id      INT NOT NULL,
    appointment_id  INT,
    consultation_fee DECIMAL(10,2) DEFAULT 0.00,
    medicine_cost   DECIMAL(10,2) DEFAULT 0.00,
    lab_cost        DECIMAL(10,2) DEFAULT 0.00,
    other_charges   DECIMAL(10,2) DEFAULT 0.00,
    discount        DECIMAL(10,2) DEFAULT 0.00,
    total_amount    DECIMAL(10,2) NOT NULL,
    paid_amount     DECIMAL(10,2) DEFAULT 0.00,
    payment_status  ENUM('PENDING','PAID','PARTIAL','CANCELLED') DEFAULT 'PENDING',
    payment_method  ENUM('CASH','CARD','UPI','INSURANCE','ONLINE') DEFAULT 'CASH',
    bill_date       DATE NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id)     REFERENCES patients(patient_id)         ON DELETE CASCADE,
    FOREIGN KEY (appointment_id) REFERENCES appointments(appointment_id) ON DELETE SET NULL
);

-- ─────────────────────────────────────────────
--  SAMPLE DATA
-- ─────────────────────────────────────────────
-- Admin user (password: Admin@123 – BCrypt)
INSERT INTO users (username, password, role, email, full_name, phone) VALUES
('admin',     '$2a$10$xJwL5v5Wflt68lBOsIJBvu3bGT8KXPX8ACxeU1e8Z4X1g3Lh1sJNm', 'ADMIN',      'admin@ehealth.com',        'System Admin',      '9000000001'),
('dr_sharma', '$2a$10$xJwL5v5Wflt68lBOsIJBvu3bGT8KXPX8ACxeU1e8Z4X1g3Lh1sJNm', 'DOCTOR',     'sharma@ehealth.com',       'Dr. Ravi Sharma',   '9000000002'),
('dr_patel',  '$2a$10$xJwL5v5Wflt68lBOsIJBvu3bGT8KXPX8ACxeU1e8Z4X1g3Lh1sJNm', 'DOCTOR',     'patel@ehealth.com',        'Dr. Priya Patel',   '9000000003'),
('patient01', '$2a$10$xJwL5v5Wflt68lBOsIJBvu3bGT8KXPX8ACxeU1e8Z4X1g3Lh1sJNm', 'PATIENT',    'patient01@gmail.com',      'Amit Verma',        '9111111111'),
('pharma01',  '$2a$10$xJwL5v5Wflt68lBOsIJBvu3bGT8KXPX8ACxeU1e8Z4X1g3Lh1sJNm', 'PHARMACIST', 'pharma01@ehealth.com',     'Neha Gupta',        '9222222222');

INSERT INTO doctors (user_id, name, specialization, department, qualification, experience_yrs, license_number, consultation_fee, availability) VALUES
(2, 'Dr. Ravi Sharma', 'Cardiology',   'Cardiology',      'MD, DM Cardiology',   15, 'MCI-2009-CRD', 800.00, '{"Mon":"09:00-17:00","Wed":"09:00-17:00","Fri":"09:00-13:00"}'),
(3, 'Dr. Priya Patel', 'Pediatrics',   'Pediatrics',      'MD, DCH Pediatrics',  10, 'MCI-2014-PED', 600.00, '{"Tue":"09:00-17:00","Thu":"09:00-17:00","Sat":"09:00-13:00"}');

INSERT INTO patients (user_id, name, date_of_birth, age, gender, blood_group, contact, medical_history) VALUES
(4, 'Amit Verma', '1990-05-14', 35, 'Male', 'B+', '9111111111', 'Hypertension since 2018');

INSERT INTO departments (name, description, head_doctor) VALUES
('Cardiology', 'Heart & Cardiovascular care', 1),
('Pediatrics', 'Child healthcare', 2),
('General Medicine', 'Primary care & OPD', NULL),
('Orthopedics', 'Bone & Joint care', NULL),
('Pharmacy', 'Medicine dispensary', NULL);

INSERT INTO medicines (name, generic_name, category, manufacturer, unit_price, stock_qty, expiry_date) VALUES
('Paracetamol 500mg', 'Acetaminophen',   'Analgesic',      'Sun Pharma',    2.50,  500, '2026-12-31'),
('Amoxicillin 250mg', 'Amoxicillin',     'Antibiotic',     'Cipla',         8.00,  300, '2026-06-30'),
('Metformin 500mg',   'Metformin HCl',   'Antidiabetic',   'Zydus',         3.00,  400, '2027-03-31'),
('Amlodipine 5mg',    'Amlodipine',      'Antihypertensive','Dr. Reddys',   5.50,  250, '2027-01-15'),
('Atorvastatin 10mg', 'Atorvastatin',    'Statin',         'Lupin',         12.00, 200, '2026-09-30');
