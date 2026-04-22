package patient;

import db.DatabaseConnection;
import java.sql.*;
import java.util.*;

// ── Model ──────────────────────────────────────────────────────────────────────
class Patient {
    private int    patientId, userId, age;
    private String name, gender, bloodGroup, contact, address, emergencyContact, medicalHistory, allergies;
    private java.sql.Date dob;

    // Getters
    public int    getPatientId()       { return patientId;       }
    public int    getUserId()          { return userId;          }
    public String getName()            { return name;            }
    public int    getAge()             { return age;             }
    public String getGender()          { return gender;          }
    public String getBloodGroup()      { return bloodGroup;      }
    public String getContact()         { return contact;         }
    public String getAddress()         { return address;         }
    public String getEmergencyContact(){ return emergencyContact;}
    public String getMedicalHistory()  { return medicalHistory;  }
    public String getAllergies()        { return allergies;       }
    public java.sql.Date getDob()      { return dob;             }

    // Setters
    public void setPatientId(int v)        { patientId = v;        }
    public void setUserId(int v)           { userId = v;           }
    public void setName(String v)          { name = v;             }
    public void setAge(int v)              { age = v;              }
    public void setGender(String v)        { gender = v;           }
    public void setBloodGroup(String v)    { bloodGroup = v;       }
    public void setContact(String v)       { contact = v;          }
    public void setAddress(String v)       { address = v;          }
    public void setEmergencyContact(String v){ emergencyContact = v;}
    public void setMedicalHistory(String v){ medicalHistory = v;   }
    public void setAllergies(String v)     { allergies = v;        }
    public void setDob(java.sql.Date v)    { dob = v;              }
}

// ── DAO ────────────────────────────────────────────────────────────────────────
public class PatientDAO {

    private Connection conn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    public boolean registerPatient(Patient p) throws SQLException {
        String sql = "INSERT INTO patients (user_id, name, date_of_birth, age, gender, blood_group, " +
                     "contact, address, emergency_contact, medical_history, allergies) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, p.getUserId());
            ps.setString(2, p.getName());
            ps.setDate(3, p.getDob());
            ps.setInt(4, p.getAge());
            ps.setString(5, p.getGender());
            ps.setString(6, p.getBloodGroup());
            ps.setString(7, p.getContact());
            ps.setString(8, p.getAddress());
            ps.setString(9, p.getEmergencyContact());
            ps.setString(10, p.getMedicalHistory());
            ps.setString(11, p.getAllergies());
            return ps.executeUpdate() > 0;
        }
    }
    
    // Dashboard di 'Add Patient' quick action layi eh method chahida hai
    public boolean addPatient(String name, int age, String phone) throws SQLException {
        Patient p = new Patient();
        p.setName(name);
        p.setAge(age);
        p.setContact(phone);
        
        // Eh default values add karo taaki Database error na deve
        p.setUserId(0); // Ya koi valid default user ID
        p.setDob(new java.sql.Date(System.currentTimeMillis())); // Aaj di date default
        p.setGender("O");
        p.setBloodGroup("N/A");
        
        return registerPatient(p);
    }


    public Patient getPatientByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM patients WHERE user_id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        }
        return null;
    }

    public Patient getPatientById(int patientId) throws SQLException {
        String sql = "SELECT * FROM patients WHERE patient_id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, patientId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        }
        return null;
    }

    public List<Patient> getAllPatients() throws SQLException {
        List<Patient> list = new ArrayList<>();
        String sql = "SELECT * FROM patients ORDER BY name";
        try (Statement st = conn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public boolean updatePatient(Patient p) throws SQLException {
        String sql = "UPDATE patients SET name=?, date_of_birth=?, age=?, gender=?, blood_group=?, " +
                     "contact=?, address=?, emergency_contact=?, medical_history=?, allergies=? WHERE patient_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, p.getName());
            ps.setDate(2, p.getDob());
            ps.setInt(3, p.getAge());
            ps.setString(4, p.getGender());
            ps.setString(5, p.getBloodGroup());
            ps.setString(6, p.getContact());
            ps.setString(7, p.getAddress());
            ps.setString(8, p.getEmergencyContact());
            ps.setString(9, p.getMedicalHistory());
            ps.setString(10, p.getAllergies());
            ps.setInt(11, p.getPatientId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deletePatient(int patientId) throws SQLException {
        String sql = "DELETE FROM patients WHERE patient_id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, patientId);
            return ps.executeUpdate() > 0;
        }
    }

    public int getPatientCount() throws SQLException {
        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM patients")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private Patient map(ResultSet rs) throws SQLException {
        Patient p = new Patient();
        p.setPatientId(rs.getInt("patient_id"));
        p.setUserId(rs.getInt("user_id"));
        p.setName(rs.getString("name"));
        p.setDob(rs.getDate("date_of_birth"));
        p.setAge(rs.getInt("age"));
        p.setGender(rs.getString("gender"));
        p.setBloodGroup(rs.getString("blood_group"));
        p.setContact(rs.getString("contact"));
        p.setAddress(rs.getString("address"));
        p.setEmergencyContact(rs.getString("emergency_contact"));
        p.setMedicalHistory(rs.getString("medical_history"));
        p.setAllergies(rs.getString("allergies"));
        return p;
    }
}
