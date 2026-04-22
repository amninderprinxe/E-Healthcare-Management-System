package emr;

import java.sql.Date;

public class MedicalRecord {
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