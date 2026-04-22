package admin;

import auth.User;
import billing.BillingDAO;
import appointment.AppointmentDAO;
import patient.PatientDAO;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;

/**
 * Admin Dashboard – manages users, doctors, departments, and reports.
 */
public class AdminDashboard {

    private final User admin;
    private Stage stage;

    public AdminDashboard(User admin) {
        this.admin = admin;
    }

    public void show() {
        stage = new Stage();
        stage.setTitle("Admin Dashboard – E-Healthcare");
        stage.setMaximized(true);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f0f4f8;");

        // ── Sidebar ──────────────────────────────────────────
        VBox sidebar = buildSidebar();
        root.setLeft(sidebar);

        // ── Content area ─────────────────────────────────────
        StackPane content = new StackPane();
        content.setStyle("-fx-background-color: #f0f4f8; -fx-padding: 20;");
        content.getChildren().add(buildOverview());
        root.setCenter(content);

        // ── Top bar ──────────────────────────────────────────
        HBox topBar = buildTopBar();
        root.setTop(topBar);

        stage.setScene(new Scene(root, 1200, 700));
        stage.show();
    }

    private HBox buildTopBar() {
        HBox bar = new HBox();
        bar.setPadding(new Insets(12, 20, 12, 20));
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle("-fx-background-color: #1a3c5e; -fx-effect: dropshadow(gaussian,rgba(0,0,0,0.3),8,0,0,2);");

        Label logo = new Label("🏥  E-Healthcare Admin Panel");
        logo.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold; -fx-font-family: 'Georgia';");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label userInfo = new Label("👤 " + admin.getFullName() + "  |  ADMIN");
        userInfo.setStyle("-fx-text-fill: #a8c8e8; -fx-font-size: 12px;");

        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand;");
        logoutBtn.setOnAction(e -> {
            auth.AuthService.logout();
            stage.close();
            new auth.LoginWindow().start(new Stage());
        });

        bar.getChildren().addAll(logo, spacer, userInfo, new Label("   "), logoutBtn);
        return bar;
    }

    private VBox buildSidebar() {
        VBox sidebar = new VBox(4);
        sidebar.setPrefWidth(220);
        sidebar.setStyle("-fx-background-color: #1e2d3d; -fx-padding: 16 0;");

        String[] items = {"📊  Overview", "👥  Patients", "🩺  Doctors",
                          "🏢  Departments", "📅  Appointments",
                          "💊  Prescriptions", "💰  Billing", "📈  Reports",
                          "⚙️  Settings"};

        for (String item : items) {
            Button btn = new Button(item);
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setAlignment(Pos.CENTER_LEFT);
            btn.setPadding(new Insets(10, 16, 10, 16));
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #a0b4c8; -fx-font-size: 13px; -fx-cursor: hand;");
            btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: rgba(255,255,255,0.07); -fx-text-fill: white; -fx-font-size: 13px; -fx-cursor: hand;"));
            btn.setOnMouseExited(e  -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #a0b4c8; -fx-font-size: 13px; -fx-cursor: hand;"));
            sidebar.getChildren().add(btn);
        }
        return sidebar;
    }

    private VBox buildOverview() {
        VBox box = new VBox(20);
        box.setPadding(new Insets(10));

        Label title = new Label("Dashboard Overview");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1a3c5e; -fx-font-family: 'Georgia';");

        // ── Stats grid ────────────────────────────────────────
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(16);

        try {
            PatientDAO     pDao = new PatientDAO();
            AppointmentDAO aDao = new AppointmentDAO();
            BillingDAO     bDao = new BillingDAO();

            grid.add(statCard("Total Patients",      String.valueOf(pDao.getPatientCount()),         "#2980b9", "👥"), 0, 0);
            grid.add(statCard("Today's Appointments",String.valueOf(aDao.getTodayCount()),            "#27ae60", "📅"), 1, 0);
            grid.add(statCard("Monthly Revenue",     String.format("₹%.0f", bDao.getMonthlyRevenue()),"#e67e22","💰"), 2, 0);
            grid.add(statCard("Total Revenue",       String.format("₹%.0f", bDao.getTotalRevenue()), "#8e44ad", "📈"), 3, 0);
        } catch (Exception e) {
            grid.add(statCard("Total Patients",      "–", "#2980b9", "👥"), 0, 0);
            grid.add(statCard("Today's Appointments","–", "#27ae60", "📅"), 1, 0);
            grid.add(statCard("Monthly Revenue",     "–", "#e67e22", "💰"), 2, 0);
            grid.add(statCard("Total Revenue",       "–", "#8e44ad", "📈"), 3, 0);
        }

        // ── Quick actions ─────────────────────────────────────
        Label actTitle = new Label("Quick Actions");
        actTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        HBox actions = new HBox(12);
        actions.getChildren().addAll(
            actionBtn("➕  Add Doctor",     "#2980b9"),
            actionBtn("➕  Add Patient",    "#27ae60"),
            actionBtn("📊  Generate Report","#e67e22"),
            actionBtn("👁  View All Bills",  "#8e44ad")
        );

        box.getChildren().addAll(title, grid, actTitle, actions);
        return box;
    }

    private VBox statCard(String label, String value, String color, String icon) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(20));
        card.setPrefWidth(200);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                      "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.08),10,0,0,2);");

        Label ico = new Label(icon);
        ico.setStyle("-fx-font-size: 28px;");

        Label val = new Label(value);
        val.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");

        card.getChildren().addAll(ico, val, lbl);
        return card;
    }

    private Button actionBtn(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; " +
                     "-fx-font-size: 13px; -fx-padding: 10 18; -fx-background-radius: 8; -fx-cursor: hand;");
        return btn;
    }
}
