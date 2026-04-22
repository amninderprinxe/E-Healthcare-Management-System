package billing;

import java.sql.Date;

public class Bill {
    
    // ── Variables ──────────────────────────────────────────────
    private int    billId, patientId, appointmentId;
    private double consultationFee, medicineCost, labCost, otherCharges, discount, totalAmount, paidAmount;
    private String paymentStatus, paymentMethod, patientName;
    private Date   billDate;

    // ── Getters ────────────────────────────────────────────────
    public int    getBillId()           { return billId;          }
    public int    getPatientId()        { return patientId;       }
    public int    getAppointmentId()    { return appointmentId;   }
    public double getConsultationFee()  { return consultationFee; }
    public double getMedicineCost()     { return medicineCost;    }
    public double getLabCost()          { return labCost;         }
    public double getOtherCharges()     { return otherCharges;    }
    public double getDiscount()         { return discount;        }
    public double getTotalAmount()      { return totalAmount;     }
    public double getPaidAmount()       { return paidAmount;      }
    public String getPaymentStatus()    { return paymentStatus;   }
    public String getPaymentMethod()    { return paymentMethod;   }
    public String getPatientName()      { return patientName;     }
    public Date   getBillDate()         { return billDate;        }

    // ── Setters ────────────────────────────────────────────────
    public void setBillId(int v)           { billId = v;          }
    public void setPatientId(int v)        { patientId = v;       }
    public void setAppointmentId(int v)    { appointmentId = v;   }
    public void setConsultationFee(double v){ consultationFee = v;}
    public void setMedicineCost(double v)  { medicineCost = v;    }
    public void setLabCost(double v)       { labCost = v;         }
    public void setOtherCharges(double v)  { otherCharges = v;    }
    public void setDiscount(double v)      { discount = v;        }
    public void setTotalAmount(double v)   { totalAmount = v;     }
    public void setPaidAmount(double v)    { paidAmount = v;      }
    public void setPaymentStatus(String v) { paymentStatus = v;   }
    public void setPaymentMethod(String v) { paymentMethod = v;   }
    public void setPatientName(String v)   { patientName = v;     }
    public void setBillDate(Date v)        { billDate = v;        }
}
