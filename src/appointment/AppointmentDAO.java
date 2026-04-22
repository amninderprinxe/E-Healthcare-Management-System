package appointment;

import db.DatabaseConnection;
import java.sql.*;
import java.util.*;

// ── Model ──────────────────────────────────────────────────────────────────────
class Appointment {
    private int    appointmentId, patientId, doctorId;
    private Date   appointmentDate;
    private Time   appointmentTime;
    private String reason, status, notes;
    private String patientName, doctorName;   // joined fields

    // Getters
    public int    getAppointmentId()   { return appointmentId;   }
    public int    getPatientId()       { return patientId;       }
    public int    getDoctorId()        { return doctorId;        }
    public Date   getAppointmentDate() { return appointmentDate; }
    public Time   getAppointmentTime() { return appointmentTime; }
    public String getReason()          { return reason;          }
    public String getStatus()          { return status;          }
    public String getNotes()           { return notes;           }
    public String getPatientName()     { return patientName;     }
    public String getDoctorName()      { return doctorName;      }

    // Setters
    public void setAppointmentId(int v)      { appointmentId = v;   }
    public void setPatientId(int v)          { patientId = v;       }
    public void setDoctorId(int v)           { doctorId = v;        }
    public void setAppointmentDate(Date v)   { appointmentDate = v; }
    public void setAppointmentTime(Time v)   { appointmentTime = v; }
    public void setReason(String v)          { reason = v;          }
    public void setStatus(String v)          { status = v;          }
    public void setNotes(String v)           { notes = v;           }
    public void setPatientName(String v)     { patientName = v;     }
    public void setDoctorName(String v)      { doctorName = v;      }
}

// ── DAO ────────────────────────────────────────────────────────────────────────
public class AppointmentDAO {

    private static final String JOIN_SQL =
        "SELECT a.*, p.name AS patient_name, d.name AS doctor_name " +
        "FROM appointments a " +
        "JOIN patients p ON a.patient_id = p.patient_id " +
        "JOIN doctors  d ON a.doctor_id  = d.doctor_id ";

    private Connection conn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    public int bookAppointment(Appointment a) throws SQLException {
        String sql = "INSERT INTO appointments (patient_id, doctor_id, appointment_date, " +
                     "appointment_time, reason, status) VALUES (?,?,?,?,?,'SCHEDULED')";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, a.getPatientId());
            ps.setInt(2, a.getDoctorId());
            ps.setDate(3, a.getAppointmentDate());
            ps.setTime(4, a.getAppointmentTime());
            ps.setString(5, a.getReason());
            ps.executeUpdate();
            ResultSet gk = ps.getGeneratedKeys();
            return gk.next() ? gk.getInt(1) : -1;
        }
    }

    public boolean updateStatus(int appointmentId, String status, String notes) throws SQLException {
        String sql = "UPDATE appointments SET status = ?, notes = ?, updated_at = NOW() WHERE appointment_id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, notes);
            ps.setInt(3, appointmentId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean cancelAppointment(int appointmentId) throws SQLException {
        return updateStatus(appointmentId, "CANCELLED", "Cancelled by user");
    }

    public boolean reschedule(int appointmentId, Date newDate, Time newTime) throws SQLException {
        String sql = "UPDATE appointments SET appointment_date=?, appointment_time=?, status='RESCHEDULED', updated_at=NOW() WHERE appointment_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setDate(1, newDate);
            ps.setTime(2, newTime);
            ps.setInt(3, appointmentId);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Appointment> getByPatient(int patientId) throws SQLException {
        return executeQuery(JOIN_SQL + "WHERE a.patient_id = ? ORDER BY a.appointment_date DESC, a.appointment_time", patientId);
    }

    public List<Appointment> getByDoctor(int doctorId) throws SQLException {
        return executeQuery(JOIN_SQL + "WHERE a.doctor_id = ? ORDER BY a.appointment_date, a.appointment_time", doctorId);
    }

    public List<Appointment> getByDoctorAndDate(int doctorId, Date date) throws SQLException {
        String sql = JOIN_SQL + "WHERE a.doctor_id = ? AND a.appointment_date = ? ORDER BY a.appointment_time";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            ps.setDate(2, date);
            ResultSet rs = ps.executeQuery();
            return mapList(rs);
        }
    }

    public List<Appointment> getAll() throws SQLException {
        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery(JOIN_SQL + "ORDER BY a.appointment_date DESC")) {
            return mapList(rs);
        }
    }

    public int getTodayCount() throws SQLException {
        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery(
                "SELECT COUNT(*) FROM appointments WHERE appointment_date = CURDATE()")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private List<Appointment> executeQuery(String sql, int param) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, param);
            return mapList(ps.executeQuery());
        }
    }

    private List<Appointment> mapList(ResultSet rs) throws SQLException {
        List<Appointment> list = new ArrayList<>();
        while (rs.next()) {
            Appointment a = new Appointment();
            a.setAppointmentId(rs.getInt("appointment_id"));
            a.setPatientId(rs.getInt("patient_id"));
            a.setDoctorId(rs.getInt("doctor_id"));
            a.setAppointmentDate(rs.getDate("appointment_date"));
            a.setAppointmentTime(rs.getTime("appointment_time"));
            a.setReason(rs.getString("reason"));
            a.setStatus(rs.getString("status"));
            a.setNotes(rs.getString("notes"));
            try { a.setPatientName(rs.getString("patient_name")); } catch (SQLException ignored) {}
            try { a.setDoctorName(rs.getString("doctor_name"));   } catch (SQLException ignored) {}
            list.add(a);
        }
        return list;
    }
}
