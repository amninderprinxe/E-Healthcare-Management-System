package doctor;

import java.util.List;

import appointment.AppointmentDAO;
import auth.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Doctor Dashboard – view appointments, update EMR, write prescriptions.
 */
public class DoctorDashboard {

    private final User doctor;
    private Stage stage;

    // Resolved doctor_id from doctors table
    private int doctorId = -1;

    public DoctorDashboard(User doctor) {
        this.doctor = doctor;
    }

    public void show() {
        resolveDoctorId();
        stage = new Stage();
        stage.setTitle("Doctor Dashboard – " + doctor.getFullName());
        stage.setMaximized(true);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f4f9f4;");
        root.setTop(buildTopBar());
        root.setLeft(buildSidebar(root));
        root.setCenter(buildTodayAppointments());

        stage.setScene(new Scene(root, 1200, 700));
        stage.show();
    }

    private HBox buildTopBar() {
        HBox bar = new HBox();
        bar.setPadding(new Insets(12, 20, 12, 20));
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle("-fx-background-color: #1a5c2e; -fx-effect: dropshadow(gaussian,rgba(0,0,0,0.3),8,0,0,2);");

        Label logo = new Label("🩺  Doctor Portal");
        logo.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold; -fx-font-family: 'Georgia';");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label info = new Label("Dr. " + doctor.getFullName());
        info.setStyle("-fx-text-fill: #a8e6c0; -fx-font-size: 13px;");

        Button logout = new Button("Logout");
        logout.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand;");
        logout.setOnAction(e -> { auth.AuthService.logout(); stage.close(); new auth.LoginWindow().start(new Stage()); });

        bar.getChildren().addAll(logo, spacer, info, new Label("   "), logout);
        return bar;
    }

    private VBox buildSidebar(BorderPane root) {
        VBox sb = new VBox(4);
        sb.setPrefWidth(220);
        sb.setStyle("-fx-background-color: #1e3a28; -fx-padding: 16 0;");

        addSidebarBtn(sb, "📅  Today's Appointments", () -> root.setCenter(buildTodayAppointments()));
        addSidebarBtn(sb, "📋  All Appointments",      () -> root.setCenter(buildAllAppointments()));
        addSidebarBtn(sb, "📁  Medical Records",       () -> root.setCenter(buildMedicalRecords()));
        addSidebarBtn(sb, "💊  Prescriptions",         () -> root.setCenter(buildPrescriptions()));
        addSidebarBtn(sb, "👤  My Profile",            () -> root.setCenter(buildProfile()));
        return sb;
    }

    private void addSidebarBtn(VBox sb, String label, Runnable action) {
        Button btn = new Button(label);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(10, 16, 10, 16));
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #a0c8a8; -fx-font-size: 13px; -fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: rgba(255,255,255,0.07); -fx-text-fill: white; -fx-font-size: 13px; -fx-cursor: hand;"));
        btn.setOnMouseExited(e  -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #a0c8a8; -fx-font-size: 13px; -fx-cursor: hand;"));
        btn.setOnAction(e -> action.run());
        sb.getChildren().add(btn);
    }

    private VBox buildTodayAppointments() {
        VBox box = new VBox(14);
        box.setPadding(new Insets(20));

        Label title = new Label("Today's Appointments");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1a5c2e;");

        TableView<String[]> table = appointmentTable();

        try {
            AppointmentDAO dao = new AppointmentDAO();
            java.sql.Date today = new java.sql.Date(System.currentTimeMillis());
            List<appointment.Appointment> appts = dao.getByDoctorAndDate(doctorId, today);
            ObservableList<String[]> data = FXCollections.observableArrayList();
            for (var a : appts) {
                data.add(new String[]{
                    String.valueOf(a.getAppointmentId()),
                    a.getPatientName(),
                    a.getAppointmentTime() != null ? a.getAppointmentTime().toString() : "",
                    a.getReason(),
                    a.getStatus()
                });
            }
            table.setItems(data);
        } catch (Exception e) {
            box.getChildren().add(new Label("Error loading: " + e.getMessage()));
        }

        box.getChildren().addAll(title, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        return box;
    }

    private VBox buildAllAppointments() {
        VBox box = new VBox(14);
        box.setPadding(new Insets(20));
        Label title = new Label("All Appointments");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1a5c2e;");
        TableView<String[]> table = appointmentTable();

        try {
            AppointmentDAO dao = new AppointmentDAO();
            List<appointment.Appointment> appts = dao.getByDoctor(doctorId);
            ObservableList<String[]> data = FXCollections.observableArrayList();
            for (var a : appts) {
                data.add(new String[]{
                    String.valueOf(a.getAppointmentId()),
                    a.getPatientName(),
                    a.getAppointmentDate() != null ? a.getAppointmentDate().toString() : "",
                    a.getReason(),
                    a.getStatus()
                });
            }
            table.setItems(data);
        } catch (Exception e) {
            box.getChildren().add(new Label("Error: " + e.getMessage()));
        }

        box.getChildren().addAll(title, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        return box;
    }

    private VBox buildMedicalRecords() {
        VBox box = new VBox(14);
        box.setPadding(new Insets(20));
        Label title = new Label("Medical Records (my patients)");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1a5c2e;");
        box.getChildren().add(title);
        box.getChildren().add(new Label("Select an appointment and click 'Add Record' to create EMR entries."));
        return box;
    }

    private VBox buildPrescriptions() {
        VBox box = new VBox(14);
        box.setPadding(new Insets(20));
        Label title = new Label("Prescriptions Written");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1a5c2e;");
        box.getChildren().add(title);
        return box;
    }

    private VBox buildProfile() {
        VBox box = new VBox(14);
        box.setPadding(new Insets(20));
        Label title = new Label("My Profile");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1a5c2e;");

        GridPane grid = new GridPane();
        grid.setHgap(16); grid.setVgap(10);
        grid.add(new Label("Name:"),  0, 0); grid.add(new Label(doctor.getFullName()), 1, 0);
        grid.add(new Label("Email:"), 0, 1); grid.add(new Label(doctor.getEmail()),    1, 1);
        grid.add(new Label("Phone:"), 0, 2); grid.add(new Label(doctor.getPhone()),    1, 2);
        grid.add(new Label("Role:"),  0, 3); grid.add(new Label(doctor.getRole()),     1, 3);

        box.getChildren().addAll(title, grid);
        return box;
    }

    
    private TableView<String[]> appointmentTable() {
        TableView<String[]> table = new TableView<>();
        String[] cols = {"ID", "Patient", "Date/Time", "Reason", "Status"};
        for (int i = 0; i < cols.length; i++) {
            final int idx = i;
            TableColumn<String[], String> col = new TableColumn<>(cols[i]);
            col.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue()[idx]));
            col.setPrefWidth(i == 3 ? 200 : 120);
            table.getColumns().add(col);
        }
        table.setStyle("-fx-background-color: white; -fx-background-radius: 8;");
        return table;
    }

    private void resolveDoctorId() {
        try {
            java.sql.Connection conn = db.DatabaseConnection.getInstance().getConnection();
            java.sql.PreparedStatement ps = conn.prepareStatement("SELECT doctor_id FROM doctors WHERE user_id = ?");
            ps.setInt(1, doctor.getUserId());
            java.sql.ResultSet rs = ps.executeQuery();
            if (rs.next()) doctorId = rs.getInt("doctor_id");
        } catch (Exception e) {
            System.err.println("Could not resolve doctor_id: " + e.getMessage());
        }
    }
}
