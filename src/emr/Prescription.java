package emr;

import java.sql.Date;

public class Prescription {
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