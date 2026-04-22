package billing;

import db.DatabaseConnection;
import java.sql.*;
import java.util.*;

// ── DAO ────────────────────────────────────────────────────────────────────────
public class BillingDAO {

    private Connection conn() { return DatabaseConnection.getInstance().getConnection(); }

    public int createBill(Bill b) throws SQLException {
        double total = b.getConsultationFee() + b.getMedicineCost() +
                       b.getLabCost() + b.getOtherCharges() - b.getDiscount();
        b.setTotalAmount(total);

        String sql = "INSERT INTO billing (patient_id, appointment_id, consultation_fee, medicine_cost, " +
                     "lab_cost, other_charges, discount, total_amount, paid_amount, payment_status, " +
                     "payment_method, bill_date) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, b.getPatientId());
            ps.setObject(2, b.getAppointmentId() > 0 ? b.getAppointmentId() : null);
            ps.setDouble(3, b.getConsultationFee());
            ps.setDouble(4, b.getMedicineCost());
            ps.setDouble(5, b.getLabCost());
            ps.setDouble(6, b.getOtherCharges());
            ps.setDouble(7, b.getDiscount());
            ps.setDouble(8, b.getTotalAmount());
            ps.setDouble(9, b.getPaidAmount());
            ps.setString(10, b.getPaymentStatus() != null ? b.getPaymentStatus() : "PENDING");
            ps.setString(11, b.getPaymentMethod()  != null ? b.getPaymentMethod()  : "CASH");
            ps.setDate(12, b.getBillDate());
            ps.executeUpdate();
            ResultSet gk = ps.getGeneratedKeys();
            return gk.next() ? gk.getInt(1) : -1;
        }
    }

    public boolean markPaid(int billId, double amount, String method) throws SQLException {
        String sql = "UPDATE billing SET paid_amount=?, payment_method=?, " +
                     "payment_status=IF(paid_amount>=total_amount,'PAID','PARTIAL') WHERE bill_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setDouble(1, amount);
            ps.setString(2, method);
            ps.setInt(3, billId);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Bill> getByPatient(int patientId) throws SQLException {
        String sql = "SELECT b.*, p.name AS patient_name FROM billing b " +
                     "JOIN patients p ON b.patient_id = p.patient_id " +
                     "WHERE b.patient_id = ? ORDER BY b.bill_date DESC";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, patientId);
            return mapList(ps.executeQuery());
        }
    }

    public List<Bill> getAll() throws SQLException {
        String sql = "SELECT b.*, p.name AS patient_name FROM billing b " +
                     "JOIN patients p ON b.patient_id = p.patient_id ORDER BY b.bill_date DESC";
        try (Statement st = conn().createStatement()) {
            return mapList(st.executeQuery(sql));
        }
    }

    public double getTotalRevenue() throws SQLException {
        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery("SELECT SUM(paid_amount) FROM billing WHERE payment_status IN ('PAID','PARTIAL')")) {
            return rs.next() ? rs.getDouble(1) : 0.0;
        }
    }

    public double getMonthlyRevenue() throws SQLException {
        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery(
                "SELECT SUM(paid_amount) FROM billing WHERE payment_status IN ('PAID','PARTIAL') " +
                "AND MONTH(bill_date)=MONTH(NOW()) AND YEAR(bill_date)=YEAR(NOW())")) {
            return rs.next() ? rs.getDouble(1) : 0.0;
        }
    }

    /**
     * Generates a formatted plain-text invoice string for printing or display.
     */
    public String generateInvoice(int billId) throws SQLException {
        String sql = "SELECT b.*, p.name AS patient_name, p.contact FROM billing b " +
                     "JOIN patients p ON b.patient_id = p.patient_id WHERE b.bill_id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, billId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return "Bill not found.";

            StringBuilder sb = new StringBuilder();
            sb.append("=======================================================\n");
            sb.append("          E-HEALTHCARE MANAGEMENT SYSTEM\n");
            sb.append("                   INVOICE\n");
            sb.append("=======================================================\n");
            sb.append(String.format("Invoice #:    BILL-%05d%n", rs.getInt("bill_id")));
            sb.append(String.format("Date:         %s%n", rs.getDate("bill_date")));
            sb.append(String.format("Patient:      %s%n", rs.getString("patient_name")));
            sb.append(String.format("Contact:      %s%n", rs.getString("contact")));
            sb.append("-------------------------------------------------------\n");
            sb.append(String.format("%-30s %10s%n", "Consultation Fee",
                    String.format("₹%.2f", rs.getDouble("consultation_fee"))));
            sb.append(String.format("%-30s %10s%n", "Medicine Cost",
                    String.format("₹%.2f", rs.getDouble("medicine_cost"))));
            sb.append(String.format("%-30s %10s%n", "Lab / Investigation",
                    String.format("₹%.2f", rs.getDouble("lab_cost"))));
            sb.append(String.format("%-30s %10s%n", "Other Charges",
                    String.format("₹%.2f", rs.getDouble("other_charges"))));
            sb.append(String.format("%-30s %10s%n", "Discount",
                    String.format("-₹%.2f", rs.getDouble("discount"))));
            sb.append("-------------------------------------------------------\n");
            sb.append(String.format("%-30s %10s%n", "TOTAL AMOUNT",
                    String.format("₹%.2f", rs.getDouble("total_amount"))));
            sb.append(String.format("%-30s %10s%n", "Paid Amount",
                    String.format("₹%.2f", rs.getDouble("paid_amount"))));
            sb.append(String.format("%-30s %10s%n", "Balance Due",
                    String.format("₹%.2f",
                        rs.getDouble("total_amount") - rs.getDouble("paid_amount"))));
            sb.append("-------------------------------------------------------\n");
            sb.append(String.format("Payment Method: %s%n", rs.getString("payment_method")));
            sb.append(String.format("Status:         %s%n", rs.getString("payment_status")));
            sb.append("=======================================================\n");
            sb.append("        Thank you for choosing E-Healthcare!\n");
            sb.append("=======================================================\n");
            return sb.toString();
        }
    }

    private List<Bill> mapList(ResultSet rs) throws SQLException {
        List<Bill> list = new ArrayList<>();
        while (rs.next()) {
            Bill b = new Bill();
            b.setBillId(rs.getInt("bill_id"));
            b.setPatientId(rs.getInt("patient_id"));
            b.setConsultationFee(rs.getDouble("consultation_fee"));
            b.setMedicineCost(rs.getDouble("medicine_cost"));
            b.setLabCost(rs.getDouble("lab_cost"));
            b.setOtherCharges(rs.getDouble("other_charges"));
            b.setDiscount(rs.getDouble("discount"));
            b.setTotalAmount(rs.getDouble("total_amount"));
            b.setPaidAmount(rs.getDouble("paid_amount"));
            b.setPaymentStatus(rs.getString("payment_status"));
            b.setPaymentMethod(rs.getString("payment_method"));
            b.setBillDate(rs.getDate("bill_date"));
            try { b.setPatientName(rs.getString("patient_name")); } catch (SQLException ignored) {}
            list.add(b);
        }
        return list;
    }
}
