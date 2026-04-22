package emr;
import java.sql.Date;
import db.DatabaseConnection;
import java.sql.*;
import java.util.*;

// ────────────────────────────────────────────────────────────────────────────
//  Medical Record Model
// ────────────────────────────────────────────────────────────────────────────
class MedicalRecord {
    private int    recordId, patientId, doctorId, appointmentId;
    private String diagnosis, symptoms, treatment, labReports;
    private Date   visitDate, followUpDate;
    private String patientName, doctorName;

    public int    getRecordId()      { return recordId;      }
    public int    getPatientId()     { return patientId;     }
    public int    getDoctorId()      { return doctorId;      }
    public int    getAppointmentId() { return appointmentId; }
    public String getDiagnosis()     { return diagnosis;     }
    public String getSymptoms()      { return symptoms;      }
    public String getTreatment()     { return treatment;     }
    public String getLabReports()    { return labReports;    }
    public Date   getVisitDate()     { return visitDate;     }
    public Date   getFollowUpDate()  { return followUpDate;  }
    public String getPatientName()   { return patientName;   }
    public String getDoctorName()    { return doctorName;    }

    public void setRecordId(int v)        { recordId = v;        }
    public void setPatientId(int v)       { patientId = v;       }
    public void setDoctorId(int v)        { doctorId = v;        }
    public void setAppointmentId(int v)   { appointmentId = v;   }
    public void setDiagnosis(String v)    { diagnosis = v;       }
    public void setSymptoms(String v)     { symptoms = v;        }
    public void setTreatment(String v)    { treatment = v;       }
    public void setLabReports(String v)   { labReports = v;      }
    public void setVisitDate(Date v)      { visitDate = v;       }
    public void setFollowUpDate(Date v)   { followUpDate = v;    }
    public void setPatientName(String v)  { patientName = v;     }
    public void setDoctorName(String v)   { doctorName = v;      }
}

// ────────────────────────────────────────────────────────────────────────────
//  Medical Record DAO
// ────────────────────────────────────────────────────────────────────────────
class MedicalRecordDAO {
    private Connection conn() { return DatabaseConnection.getInstance().getConnection(); }

    private static final String JOIN =
        "SELECT mr.*, p.name AS patient_name, d.name AS doctor_name " +
        "FROM medical_records mr " +
        "JOIN patients p ON mr.patient_id = p.patient_id " +
        "JOIN doctors  d ON mr.doctor_id  = d.doctor_id ";

    public int addRecord(MedicalRecord r) throws SQLException {
        String sql = "INSERT INTO medical_records (patient_id, doctor_id, appointment_id, diagnosis, " +
                     "symptoms, treatment, lab_reports, visit_date, follow_up_date) VALUES (?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, r.getPatientId());
            ps.setInt(2, r.getDoctorId());
            ps.setObject(3, r.getAppointmentId() > 0 ? r.getAppointmentId() : null);
            ps.setString(4, r.getDiagnosis());
            ps.setString(5, r.getSymptoms());
            ps.setString(6, r.getTreatment());
            ps.setString(7, r.getLabReports());
            ps.setDate(8, r.getVisitDate());
            ps.setDate(9, r.getFollowUpDate());
            ps.executeUpdate();
            ResultSet gk = ps.getGeneratedKeys();
            return gk.next() ? gk.getInt(1) : -1;
        }
    }

    public List<MedicalRecord> getByPatient(int patientId) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement(JOIN + "WHERE mr.patient_id = ? ORDER BY mr.visit_date DESC")) {
            ps.setInt(1, patientId);
            return mapList(ps.executeQuery());
        }
    }

    public List<MedicalRecord> getByDoctor(int doctorId) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement(JOIN + "WHERE mr.doctor_id = ? ORDER BY mr.visit_date DESC")) {
            ps.setInt(1, doctorId);
            return mapList(ps.executeQuery());
        }
    }

    private List<MedicalRecord> mapList(ResultSet rs) throws SQLException {
        List<MedicalRecord> list = new ArrayList<>();
        while (rs.next()) {
            MedicalRecord r = new MedicalRecord();
            r.setRecordId(rs.getInt("record_id"));
            r.setPatientId(rs.getInt("patient_id"));
            r.setDoctorId(rs.getInt("doctor_id"));
            r.setDiagnosis(rs.getString("diagnosis"));
            r.setSymptoms(rs.getString("symptoms"));
            r.setTreatment(rs.getString("treatment"));
            r.setLabReports(rs.getString("lab_reports"));
            r.setVisitDate(rs.getDate("visit_date"));
            r.setFollowUpDate(rs.getDate("follow_up_date"));
            try { r.setPatientName(rs.getString("patient_name")); } catch (SQLException ignored) {}
            try { r.setDoctorName(rs.getString("doctor_name"));   } catch (SQLException ignored) {}
            list.add(r);
        }
        return list;
    }
}

// ────────────────────────────────────────────────────────────────────────────
//  Prescription Model
// ────────────────────────────────────────────────────────────────────────────
class Prescription {
    private int    prescriptionId, patientId, doctorId, recordId;
    private String medicine, dosage, frequency, duration, instructions, status;
    private Date   prescribedDate, dispensedDate;
    private String patientName, doctorName;

    public int    getPrescriptionId()  { return prescriptionId;  }
    public int    getPatientId()       { return patientId;       }
    public int    getDoctorId()        { return doctorId;        }
    public int    getRecordId()        { return recordId;        }
    public String getMedicine()        { return medicine;        }
    public String getDosage()          { return dosage;          }
    public String getFrequency()       { return frequency;       }
    public String getDuration()        { return duration;        }
    public String getInstructions()    { return instructions;    }
    public String getStatus()          { return status;          }
    public Date   getPrescribedDate()  { return prescribedDate;  }
    public Date   getDispensedDate()   { return dispensedDate;   }
    public String getPatientName()     { return patientName;     }
    public String getDoctorName()      { return doctorName;      }

    public void setPrescriptionId(int v)     { prescriptionId = v;  }
    public void setPatientId(int v)          { patientId = v;       }
    public void setDoctorId(int v)           { doctorId = v;        }
    public void setRecordId(int v)           { recordId = v;        }
    public void setMedicine(String v)        { medicine = v;        }
    public void setDosage(String v)          { dosage = v;          }
    public void setFrequency(String v)       { frequency = v;       }
    public void setDuration(String v)        { duration = v;        }
    public void setInstructions(String v)    { instructions = v;    }
    public void setStatus(String v)          { status = v;          }
    public void setPrescribedDate(Date v)    { prescribedDate = v;  }
    public void setDispensedDate(Date v)     { dispensedDate = v;   }
    public void setPatientName(String v)     { patientName = v;     }
    public void setDoctorName(String v)      { doctorName = v;      }
}

// ────────────────────────────────────────────────────────────────────────────
//  Prescription DAO
// ────────────────────────────────────────────────────────────────────────────
public class PrescriptionDAO {
    private Connection conn() { return DatabaseConnection.getInstance().getConnection(); }

    private static final String JOIN =
        "SELECT pr.*, p.name AS patient_name, d.name AS doctor_name " +
        "FROM prescriptions pr " +
        "JOIN patients p ON pr.patient_id = p.patient_id " +
        "JOIN doctors  d ON pr.doctor_id  = d.doctor_id ";

    public int addPrescription(Prescription rx) throws SQLException {
        String sql = "INSERT INTO prescriptions (patient_id, doctor_id, record_id, medicine, dosage, " +
                     "frequency, duration, instructions, status, prescribed_date) VALUES (?,?,?,?,?,?,?,?,'PENDING',?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, rx.getPatientId());
            ps.setInt(2, rx.getDoctorId());
            ps.setObject(3, rx.getRecordId() > 0 ? rx.getRecordId() : null);
            ps.setString(4, rx.getMedicine());
            ps.setString(5, rx.getDosage());
            ps.setString(6, rx.getFrequency());
            ps.setString(7, rx.getDuration());
            ps.setString(8, rx.getInstructions());
            ps.setDate(9, rx.getPrescribedDate());
            ps.executeUpdate();
            ResultSet gk = ps.getGeneratedKeys();
            return gk.next() ? gk.getInt(1) : -1;
        }
    }

    public boolean dispense(int prescriptionId, int pharmacistUserId) throws SQLException {
        String sql = "UPDATE prescriptions SET status='DISPENSED', dispensed_date=CURDATE(), dispensed_by=? WHERE prescription_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, pharmacistUserId);
            ps.setInt(2, prescriptionId);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Prescription> getPendingPrescriptions() throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement(JOIN + "WHERE pr.status='PENDING' ORDER BY pr.prescribed_date")) {
            return mapList(ps.executeQuery());
        }
    }

    public List<Prescription> getByPatient(int patientId) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement(JOIN + "WHERE pr.patient_id=? ORDER BY pr.prescribed_date DESC")) {
            ps.setInt(1, patientId);
            return mapList(ps.executeQuery());
        }
    }

    private List<Prescription> mapList(ResultSet rs) throws SQLException {
        List<Prescription> list = new ArrayList<>();
        while (rs.next()) {
            Prescription rx = new Prescription();
            rx.setPrescriptionId(rs.getInt("prescription_id"));
            rx.setPatientId(rs.getInt("patient_id"));
            rx.setDoctorId(rs.getInt("doctor_id"));
            rx.setMedicine(rs.getString("medicine"));
            rx.setDosage(rs.getString("dosage"));
            rx.setFrequency(rs.getString("frequency"));
            rx.setDuration(rs.getString("duration"));
            rx.setInstructions(rs.getString("instructions"));
            rx.setStatus(rs.getString("status"));
            rx.setPrescribedDate(rs.getDate("prescribed_date"));
            rx.setDispensedDate(rs.getDate("dispensed_date"));
            try { rx.setPatientName(rs.getString("patient_name")); } catch (SQLException ignored) {}
            try { rx.setDoctorName(rs.getString("doctor_name"));   } catch (SQLException ignored) {}
            list.add(rx);
        }
        return list;
    }
}
