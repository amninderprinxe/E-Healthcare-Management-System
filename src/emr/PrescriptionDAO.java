package emr;

import db.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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