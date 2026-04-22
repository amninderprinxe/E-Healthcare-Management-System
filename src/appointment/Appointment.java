package appointment;

import java.sql.Date;
import java.sql.Time;

public class Appointment {
    
    // ── Variables ──────────────────────────────────────────────
    private int    appointmentId, patientId, doctorId;
    private Date   appointmentDate;
    private Time   appointmentTime;
    private String reason, status, notes;
    private String patientName, doctorName;   // joined fields

    // ── Getters ────────────────────────────────────────────────
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

    // ── Setters ────────────────────────────────────────────────
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
