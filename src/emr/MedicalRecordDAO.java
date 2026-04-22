package emr;

import db.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MedicalRecordDAO {
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