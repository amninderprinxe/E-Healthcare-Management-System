package patient;

import auth.User;
import appointment.*;
import billing.*;
import emr.*;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.List;

/**
 * Patient Dashboard – book appointments, view prescriptions, billing.
 */
public class PatientDashboard {

    private final User user;
    private int patientId = -1;
    private Stage stage;

    public PatientDashboard(User user) {
        this.user = user;
    }

    public void show() {
        resolvePatientId();
        stage = new Stage();
        stage.setTitle("Patient Portal – " + user.getFullName());
        stage.setMaximized(true);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f0fb;");
        root.setTop(buildTopBar());
        root.setLeft(buildSidebar(root));
        root.setCenter(buildHome());

        stage.setScene(new Scene(root, 1200, 700));
        stage.show();
    }

    private HBox buildTopBar() {
        HBox bar = new HBox();
        bar.setPadding(new Insets(12, 20, 12, 20));
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle("-fx-background-color: #5b2d8e; -fx-effect: dropshadow(gaussian,rgba(0,0,0,0.3),8,0,0,2);");

        Label logo = new Label("🏥  Patient Portal");
        logo.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold; -fx-font-family: 'Georgia';");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label info = new Label("Welcome, " + user.getFullName());
        info.setStyle("-fx-text-fill: #d4b8f0; -fx-font-size: 13px;");

        Button logout = new Button("Logout");
        logout.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand;");
        logout.setOnAction(e -> { auth.AuthService.logout(); stage.close(); new auth.LoginWindow().start(new Stage()); });

        bar.getChildren().addAll(logo, spacer, info, new Label("   "), logout);
        return bar;
    }

    private VBox buildSidebar(BorderPane root) {
        VBox sb = new VBox(4);
        sb.setPrefWidth(220);
        sb.setStyle("-fx-background-color: #3b1a6e; -fx-padding: 16 0;");

        addBtn(sb, "🏠  Home",              () -> root.setCenter(buildHome()));
        addBtn(sb, "📅  Book Appointment",  () -> root.setCenter(buildBookAppointment()));
        addBtn(sb, "📋  My Appointments",   () -> root.setCenter(buildMyAppointments()));
        addBtn(sb, "📁  Medical Records",   () -> root.setCenter(buildMedicalRecords()));
        addBtn(sb, "💊  Prescriptions",     () -> root.setCenter(buildPrescriptions()));
        addBtn(sb, "💰  Billing",           () -> root.setCenter(buildBilling()));
        addBtn(sb, "👤  My Profile",        () -> root.setCenter(buildProfile()));
        return sb;
    }

    private void addBtn(VBox sb, String label, Runnable action) {
        Button btn = new Button(label);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(10, 16, 10, 16));
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #c8a8e8; -fx-font-size: 13px; -fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-text-fill: white; -fx-font-size: 13px; -fx-cursor: hand;"));
        btn.setOnMouseExited(e  -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #c8a8e8; -fx-font-size: 13px; -fx-cursor: hand;"));
        btn.setOnAction(e -> action.run());
        sb.getChildren().add(btn);
    }

    private VBox buildHome() {
        VBox box = new VBox(16);
        box.setPadding(new Insets(24));

        Label title = new Label("My Health Summary");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #5b2d8e;");

        HBox cards = new HBox(16);
        try {
            AppointmentDAO aDao = new AppointmentDAO();
            BillingDAO     bDao = new BillingDAO();
            PrescriptionDAO rxDao = new PrescriptionDAO();

            int apptCount = patientId > 0 ? aDao.getByPatient(patientId).size() : 0;
            int rxCount   = patientId > 0 ? rxDao.getByPatient(patientId).size() : 0;
            double pending = patientId > 0
                ? bDao.getByPatient(patientId).stream()
                      .filter(b -> "PENDING".equals(b.getPaymentStatus()))
                      .mapToDouble(b -> b.getTotalAmount() - b.getPaidAmount())
                      .sum() : 0;

            cards.getChildren().addAll(
                statCard("Total Appointments", String.valueOf(apptCount), "#2980b9", "📅"),
                statCard("Prescriptions",      String.valueOf(rxCount),   "#27ae60", "💊"),
                statCard("Pending Bills",      String.format("₹%.0f", pending), "#e67e22", "💰")
            );
        } catch (Exception e) {
            cards.getChildren().add(new Label("Could not load stats: " + e.getMessage()));
        }

        box.getChildren().addAll(title, cards);
        return box;
    }

    private VBox buildBookAppointment() {
        VBox box = new VBox(14);
        box.setPadding(new Insets(24));
        Label title = new Label("Book an Appointment");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #5b2d8e;");

        GridPane form = new GridPane();
        form.setHgap(14); form.setVgap(12);
        form.setMaxWidth(500);

        // Doctor selection
        ComboBox<String> doctorCb = new ComboBox<>();
        try {
            java.sql.Connection conn = db.DatabaseConnection.getInstance().getConnection();
            java.sql.ResultSet rs = conn.createStatement().executeQuery("SELECT doctor_id, name, specialization FROM doctors");
            while (rs.next()) doctorCb.getItems().add(rs.getInt("doctor_id") + " – " + rs.getString("name") + " (" + rs.getString("specialization") + ")");
        } catch (Exception ignored) {}

        DatePicker datePicker = new DatePicker();
        TextField timeTf = new TextField();
        timeTf.setPromptText("HH:MM (e.g. 10:30)");
        TextArea reasonTa = new TextArea();
        reasonTa.setPromptText("Reason for visit");
        reasonTa.setPrefRowCount(3);

        form.add(new Label("Doctor:"),     0, 0); form.add(doctorCb,  1, 0);
        form.add(new Label("Date:"),       0, 1); form.add(datePicker,1, 1);
        form.add(new Label("Time:"),       0, 2); form.add(timeTf,    1, 2);
        form.add(new Label("Reason:"),     0, 3); form.add(reasonTa,  1, 3);

        Label statusLbl = new Label();
        Button book = new Button("Book Appointment");
        book.setStyle("-fx-background-color: #5b2d8e; -fx-text-fill: white; -fx-font-size: 13px; -fx-padding: 9 20; -fx-background-radius: 8; -fx-cursor: hand;");
        book.setOnAction(e -> {
            try {
                String docStr = doctorCb.getValue();
                if (docStr == null || datePicker.getValue() == null || timeTf.getText().isEmpty()) {
                    statusLbl.setStyle("-fx-text-fill: red;");
                    statusLbl.setText("Please fill all required fields.");
                    return;
                }
                int docId = Integer.parseInt(docStr.split(" – ")[0]);
                AppointmentDAO dao = new AppointmentDAO();
                appointment.Appointment appt = new appointment.Appointment();
                appt.setPatientId(patientId);
                appt.setDoctorId(docId);
                appt.setAppointmentDate(java.sql.Date.valueOf(datePicker.getValue()));
                appt.setAppointmentTime(java.sql.Time.valueOf(timeTf.getText() + ":00"));
                appt.setReason(reasonTa.getText());
                int id = dao.bookAppointment(appt);
                statusLbl.setStyle("-fx-text-fill: green;");
                statusLbl.setText("✓ Appointment booked! ID: " + id);
            } catch (Exception ex) {
                statusLbl.setStyle("-fx-text-fill: red;");
                statusLbl.setText("Error: " + ex.getMessage());
            }
        });

        box.getChildren().addAll(title, form, book, statusLbl);
        return box;
    }

    private VBox buildMyAppointments() {
        VBox box = new VBox(14);
        box.setPadding(new Insets(24));
        Label title = new Label("My Appointments");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #5b2d8e;");

        TableView<String[]> table = makeTable(new String[]{"ID","Doctor","Date","Time","Reason","Status"});
        try {
            AppointmentDAO dao = new AppointmentDAO();
            ObservableList<String[]> data = FXCollections.observableArrayList();
            for (var a : dao.getByPatient(patientId)) {
                data.add(new String[]{
                    String.valueOf(a.getAppointmentId()), a.getDoctorName(),
                    String.valueOf(a.getAppointmentDate()), String.valueOf(a.getAppointmentTime()),
                    a.getReason(), a.getStatus()
                });
            }
            table.setItems(data);
        } catch (Exception e) { box.getChildren().add(new Label("Error: " + e.getMessage())); }

        box.getChildren().addAll(title, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        return box;
    }

    private VBox buildMedicalRecords() {
        VBox box = new VBox(14);
        box.setPadding(new Insets(24));
        Label title = new Label("Medical Records");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #5b2d8e;");
        TableView<String[]> table = makeTable(new String[]{"ID","Doctor","Visit Date","Diagnosis","Treatment","Follow-up"});
        try {
            MedicalRecordDAO dao = new MedicalRecordDAO();
            ObservableList<String[]> data = FXCollections.observableArrayList();
            for (var r : dao.getByPatient(patientId)) {
                data.add(new String[]{
                    String.valueOf(r.getRecordId()), r.getDoctorName(),
                    String.valueOf(r.getVisitDate()), r.getDiagnosis(),
                    r.getTreatment(), String.valueOf(r.getFollowUpDate())
                });
            }
            table.setItems(data);
        } catch (Exception e) { box.getChildren().add(new Label("Error: " + e.getMessage())); }
        box.getChildren().addAll(title, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        return box;
    }

    private VBox buildPrescriptions() {
        VBox box = new VBox(14);
        box.setPadding(new Insets(24));
        Label title = new Label("My Prescriptions");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #5b2d8e;");
        TableView<String[]> table = makeTable(new String[]{"ID","Doctor","Medicine","Dosage","Frequency","Duration","Status"});
        try {
            PrescriptionDAO dao = new PrescriptionDAO();
            ObservableList<String[]> data = FXCollections.observableArrayList();
            for (var rx : dao.getByPatient(patientId)) {
                data.add(new String[]{
                    String.valueOf(rx.getPrescriptionId()), rx.getDoctorName(),
                    rx.getMedicine(), rx.getDosage(), rx.getFrequency(), rx.getDuration(), rx.getStatus()
                });
            }
            table.setItems(data);
        } catch (Exception e) { box.getChildren().add(new Label("Error: " + e.getMessage())); }
        box.getChildren().addAll(title, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        return box;
    }

    private VBox buildBilling() {
        VBox box = new VBox(14);
        box.setPadding(new Insets(24));
        Label title = new Label("My Bills");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #5b2d8e;");
        TableView<String[]> table = makeTable(new String[]{"Bill ID","Date","Consult Fee","Medicine","Total","Paid","Status"});
        try {
            BillingDAO dao = new BillingDAO();
            ObservableList<String[]> data = FXCollections.observableArrayList();
            for (var b : dao.getByPatient(patientId)) {
                data.add(new String[]{
                    String.valueOf(b.getBillId()), String.valueOf(b.getBillDate()),
                    String.format("₹%.0f", b.getConsultationFee()),
                    String.format("₹%.0f", b.getMedicineCost()),
                    String.format("₹%.0f", b.getTotalAmount()),
                    String.format("₹%.0f", b.getPaidAmount()),
                    b.getPaymentStatus()
                });
            }
            table.setItems(data);
        } catch (Exception e) { box.getChildren().add(new Label("Error: " + e.getMessage())); }
        box.getChildren().addAll(title, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        return box;
    }

    private VBox buildProfile() {
        VBox box = new VBox(14);
        box.setPadding(new Insets(24));
        Label title = new Label("My Profile");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #5b2d8e;");
        GridPane g = new GridPane();
        g.setHgap(16); g.setVgap(10);
        g.add(new Label("Name:"),  0,0); g.add(new Label(user.getFullName()), 1, 0);
        g.add(new Label("Email:"), 0,1); g.add(new Label(user.getEmail()),    1, 1);
        g.add(new Label("Phone:"), 0,2); g.add(new Label(user.getPhone()),    1, 2);
        box.getChildren().addAll(title, g);
        return box;
    }

    @SuppressWarnings("unchecked")
    private TableView<String[]> makeTable(String[] cols) {
        TableView<String[]> table = new TableView<>();
        for (int i = 0; i < cols.length; i++) {
            final int idx = i;
            TableColumn<String[], String> col = new TableColumn<>(cols[i]);
            col.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue()[idx]));
            col.setPrefWidth(130);
            table.getColumns().add(col);
        }
        return table;
    }

    private VBox statCard(String label, String value, String color, String icon) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(18));
        card.setPrefWidth(180);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian,rgba(0,0,0,0.08),10,0,0,2);");
        Label ico = new Label(icon);   ico.setStyle("-fx-font-size: 24px;");
        Label val = new Label(value);  val.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        Label lbl = new Label(label);  lbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d;");
        card.getChildren().addAll(ico, val, lbl);
        return card;
    }

    private void resolvePatientId() {
        try {
            java.sql.PreparedStatement ps = db.DatabaseConnection.getInstance().getConnection()
                .prepareStatement("SELECT patient_id FROM patients WHERE user_id = ?");
            ps.setInt(1, user.getUserId());
            java.sql.ResultSet rs = ps.executeQuery();
            if (rs.next()) patientId = rs.getInt("patient_id");
        } catch (Exception e) { System.err.println("Cannot resolve patient_id: " + e.getMessage()); }
    }
}
