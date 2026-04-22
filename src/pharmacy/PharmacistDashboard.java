package pharmacy;

import auth.User;
import emr.PrescriptionDAO;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/**
 * Pharmacist Dashboard – dispense pending prescriptions, manage stock.
 */
public class PharmacistDashboard {

    private final User user;
    private Stage stage;

    public PharmacistDashboard(User user) {
        this.user = user;
    }

    public void show() {
        stage = new Stage();
        stage.setTitle("Pharmacist Portal – " + user.getFullName());
        stage.setMaximized(true);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #fff8f0;");
        root.setTop(buildTopBar());
        root.setLeft(buildSidebar(root));
        root.setCenter(buildPendingPrescriptions());

        stage.setScene(new Scene(root, 1200, 700));
        stage.show();
    }

    private HBox buildTopBar() {
        HBox bar = new HBox();
        bar.setPadding(new Insets(12, 20, 12, 20));
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle("-fx-background-color: #8b4500; -fx-effect: dropshadow(gaussian,rgba(0,0,0,0.3),8,0,0,2);");

        Label logo = new Label("💊  Pharmacy Portal");
        logo.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold; -fx-font-family: 'Georgia';");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label info = new Label(user.getFullName() + " | PHARMACIST");
        info.setStyle("-fx-text-fill: #f0c89a; -fx-font-size: 12px;");

        Button logout = new Button("Logout");
        logout.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand;");
        logout.setOnAction(e -> { auth.AuthService.logout(); stage.close(); new auth.LoginWindow().start(new Stage()); });

        bar.getChildren().addAll(logo, spacer, info, new Label("   "), logout);
        return bar;
    }

    private VBox buildSidebar(BorderPane root) {
        VBox sb = new VBox(4);
        sb.setPrefWidth(220);
        sb.setStyle("-fx-background-color: #5c2d00; -fx-padding: 16 0;");
        addBtn(sb, "💊  Pending Prescriptions", () -> root.setCenter(buildPendingPrescriptions()));
        addBtn(sb, "📦  Medicine Stock",         () -> root.setCenter(buildStock()));
        addBtn(sb, "📋  Dispensed History",      () -> root.setCenter(buildHistory()));
        return sb;
    }

    private void addBtn(VBox sb, String label, Runnable r) {
        Button btn = new Button(label);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(10, 16, 10, 16));
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #f0c89a; -fx-font-size: 13px; -fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-text-fill: white; -fx-font-size: 13px; -fx-cursor: hand;"));
        btn.setOnMouseExited(e  -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #f0c89a; -fx-font-size: 13px; -fx-cursor: hand;"));
        btn.setOnAction(e -> r.run());
        sb.getChildren().add(btn);
    }

    private VBox buildPendingPrescriptions() {
        VBox box = new VBox(14);
        box.setPadding(new Insets(24));
        Label title = new Label("Pending Prescriptions");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #8b4500;");

        TableView<String[]> table = makeTable(new String[]{"ID","Patient","Doctor","Medicine","Dosage","Frequency","Prescribed"});
        Label status = new Label();

        try {
            PrescriptionDAO dao = new PrescriptionDAO();
            ObservableList<String[]> data = FXCollections.observableArrayList();
            for (var rx : dao.getPendingPrescriptions()) {
                data.add(new String[]{
                    String.valueOf(rx.getPrescriptionId()), rx.getPatientName(),
                    rx.getDoctorName(), rx.getMedicine(), rx.getDosage(),
                    rx.getFrequency(), String.valueOf(rx.getPrescribedDate())
                });
            }
            table.setItems(data);
        } catch (Exception e) { box.getChildren().add(new Label("Error: " + e.getMessage())); }

        Button dispenseBtn = new Button("✓  Mark Selected as Dispensed");
        dispenseBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 13px; -fx-padding: 9 18; -fx-background-radius: 8; -fx-cursor: hand;");
        dispenseBtn.setOnAction(e -> {
            String[] selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { status.setText("Select a prescription first."); return; }
            try {
                PrescriptionDAO dao = new PrescriptionDAO();
                dao.dispense(Integer.parseInt(selected[0]), user.getUserId());
                status.setStyle("-fx-text-fill: green;");
                status.setText("✓ Dispensed prescription #" + selected[0]);
                box.getChildren().clear();
                box.getChildren().addAll(title, table, dispenseBtn, status);
                box.getChildren().set(1, buildPendingPrescriptions().getChildren().get(1)); // refresh
            } catch (Exception ex) {
                status.setStyle("-fx-text-fill: red;");
                status.setText("Error: " + ex.getMessage());
            }
        });

        box.getChildren().addAll(title, table, dispenseBtn, status);
        VBox.setVgrow(table, Priority.ALWAYS);
        return box;
    }

    private VBox buildStock() {
        VBox box = new VBox(14);
        box.setPadding(new Insets(24));
        Label title = new Label("Medicine Stock");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #8b4500;");
        TableView<String[]> table = makeTable(new String[]{"ID","Name","Generic","Category","Stock Qty","Unit Price","Expiry"});
        try {
            java.sql.ResultSet rs = db.DatabaseConnection.getInstance().getConnection()
                .createStatement().executeQuery("SELECT * FROM medicines ORDER BY name");
            ObservableList<String[]> data = FXCollections.observableArrayList();
            while (rs.next()) {
                data.add(new String[]{
                    String.valueOf(rs.getInt("medicine_id")),
                    rs.getString("name"), rs.getString("generic_name"), rs.getString("category"),
                    String.valueOf(rs.getInt("stock_qty")),
                    String.format("₹%.2f", rs.getDouble("unit_price")),
                    String.valueOf(rs.getDate("expiry_date"))
                });
            }
            table.setItems(data);
        } catch (Exception e) { box.getChildren().add(new Label("Error: " + e.getMessage())); }
        box.getChildren().addAll(title, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        return box;
    }

    private VBox buildHistory() {
        VBox box = new VBox(14);
        box.setPadding(new Insets(24));
        box.getChildren().add(new Label("Dispensed history – coming soon."));
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
}
