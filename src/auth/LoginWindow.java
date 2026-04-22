package auth;

import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.scene.effect.DropShadow;

import admin.AdminDashboard;
import doctor.DoctorDashboard;
import patient.PatientDashboard;
import pharmacy.PharmacistDashboard;

/**
 * JavaFX Login Window – entry point of the application.
 * Run this class via Main.java.
 */
public class LoginWindow extends Application {

    private TextField     usernameField;
    private PasswordField passwordField;
    private Label         statusLabel;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("E-Healthcare Management System – Login");
        primaryStage.setResizable(false);

        // ── Root layout ────────────────────────────────────────
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #0d1b2a, #1b4f72);");

        // ── Card ───────────────────────────────────────────────
        VBox card = new VBox(18);
        card.setPadding(new Insets(40, 50, 40, 50));
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(420);
        card.setStyle(
            "-fx-background-color: rgba(255,255,255,0.07);" +
            "-fx-background-radius: 16;" +
            "-fx-border-color: rgba(255,255,255,0.15);" +
            "-fx-border-radius: 16;" +
            "-fx-border-width: 1;"
        );
        DropShadow shadow = new DropShadow(30, Color.rgb(0,0,0,0.5));
        card.setEffect(shadow);

        // ── Logo / Title ───────────────────────────────────────
        Label icon  = new Label("🏥");
        icon.setStyle("-fx-font-size: 48px;");

        Label title = new Label("E-Healthcare");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: white; -fx-font-family: 'Georgia';");

        Label sub = new Label("Management System");
        sub.setStyle("-fx-font-size: 13px; -fx-text-fill: #a0b4c8;");

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: rgba(255,255,255,0.2);");

        // ── Fields ────────────────────────────────────────────
        usernameField = styledTextField("Username");
        passwordField = styledPasswordField("Password");

        // ── Role hint ─────────────────────────────────────────
        Label hint = new Label("Default credentials  |  admin / Admin@123");
        hint.setStyle("-fx-font-size: 10px; -fx-text-fill: #7f9ab0;");

        // ── Login button ──────────────────────────────────────
        Button loginBtn = new Button("Sign In");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setStyle(
            "-fx-background-color: #2980b9;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 10 0;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;"
        );
        loginBtn.setOnMouseEntered(e -> loginBtn.setStyle(
            "-fx-background-color: #1a6fa0; -fx-text-fill: white; -fx-font-size: 14px;" +
            "-fx-font-weight: bold; -fx-padding: 10 0; -fx-background-radius: 8; -fx-cursor: hand;"));
        loginBtn.setOnMouseExited(e  -> loginBtn.setStyle(
            "-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-size: 14px;" +
            "-fx-font-weight: bold; -fx-padding: 10 0; -fx-background-radius: 8; -fx-cursor: hand;"));
        loginBtn.setOnAction(e -> handleLogin(primaryStage));

        // Enter key triggers login
        passwordField.setOnAction(e -> handleLogin(primaryStage));

        // ── Status label ──────────────────────────────────────
        statusLabel = new Label("");
        statusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px;");
        statusLabel.setWrapText(true);

        card.getChildren().addAll(icon, title, sub, sep,
                                  fieldLabel("Username"), usernameField,
                                  fieldLabel("Password"), passwordField,
                                  hint, loginBtn, statusLabel);

        root.setCenter(card);
        BorderPane.setAlignment(card, Pos.CENTER);

        // ── Footer ────────────────────────────────────────────
        Label footer = new Label("© 2025 E-Healthcare System. All rights reserved.");
        footer.setStyle("-fx-text-fill: #4a6278; -fx-font-size: 10px;");
        root.setBottom(footer);
        BorderPane.setAlignment(footer, Pos.CENTER);
        BorderPane.setMargin(footer, new Insets(0, 0, 15, 0));

        Scene scene = new Scene(root, 500, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // ── Login handler ──────────────────────────────────────────
    private void handleLogin(Stage stage) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("⚠ Please enter both username and password.");
            return;
        }

        try {
            User user = AuthService.login(username, password);
            statusLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-size: 12px;");
            statusLabel.setText("✓ Welcome, " + user.getFullName() + "! Redirecting…");

            // Small delay then open dashboard
            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(
                javafx.util.Duration.seconds(0.8));
            pause.setOnFinished(e -> openDashboard(user, stage));
            pause.play();

        } catch (Exception ex) {
            statusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px;");
            statusLabel.setText("✗ " + ex.getMessage());
        }
    }

    private void openDashboard(User user, Stage currentStage) {
        currentStage.close();
        try {
            switch (user.getRole()) {
                case "ADMIN"      -> new AdminDashboard(user).show();
                case "DOCTOR"     -> new DoctorDashboard(user).show();
                case "PATIENT"    -> new PatientDashboard(user).show();
                case "PHARMACIST" -> new PharmacistDashboard(user).show();
                default           -> showError("Unknown role: " + user.getRole());
            }
        } catch (Exception e) {
            showError("Failed to open dashboard: " + e.getMessage());
        }
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.showAndWait();
    }

    // ── UI helpers ─────────────────────────────────────────────
    private Label fieldLabel(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-text-fill: #a0b4c8; -fx-font-size: 12px;");
        VBox.setMargin(lbl, new Insets(-8, 0, -4, 2));
        return lbl;
    }

    private TextField styledTextField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setStyle(fieldStyle());
        return tf;
    }

    private PasswordField styledPasswordField(String prompt) {
        PasswordField pf = new PasswordField();
        pf.setPromptText(prompt);
        pf.setStyle(fieldStyle());
        return pf;
    }

    private String fieldStyle() {
        return "-fx-background-color: rgba(255,255,255,0.1);" +
               "-fx-text-fill: white;" +
               "-fx-prompt-text-fill: #5a7a90;" +
               "-fx-border-color: rgba(255,255,255,0.2);" +
               "-fx-border-radius: 6;" +
               "-fx-background-radius: 6;" +
               "-fx-padding: 9 12;" +
               "-fx-font-size: 13px;";
    }

    public static void main(String[] args) {
        launch(args);
    }
}
