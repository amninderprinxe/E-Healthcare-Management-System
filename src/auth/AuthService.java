package auth;

import db.DatabaseConnection;
import java.sql.*;

/**
 * Handles user login and registration with Bypass Hashing.
 */
public class AuthService {

    private static User currentUser = null;

    // ── Login (Bypass Hashing) ──────────────────────────────────
    public static User login(String username, String plainPassword) throws Exception {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        String sql = "SELECT * FROM users WHERE username = ? AND is_active = TRUE";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("password");
                
                if (plainPassword.equals(storedPassword)) {
                    User user = mapResultSet(rs);
                    currentUser = user;
                    return user;
                } else {
                    throw new Exception("Invalid username or password.");
                }
            } else {
                throw new Exception("User not found or account disabled.");
            }
        }
    }

    // ── Register Method (Jo miss ho gaya si) ─────────────────────
    public static boolean register(String username, String password, String role, 
                                   String email, String fullName, String phone) throws Exception {
        
        Connection conn = DatabaseConnection.getInstance().getConnection();
        String sql = "INSERT INTO users (username, password, role, email, full_name, phone, is_active) " +
                     "VALUES (?, ?, ?, ?, ?, ?, TRUE)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password); // Saving as plain text for now (Bypass)
            ps.setString(3, role);
            ps.setString(4, email);
            ps.setString(5, fullName);
            ps.setString(6, phone);

            int rows = ps.executeUpdate();
            return rows > 0;
        }
    }

    public static void logout() {
        currentUser = null;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    private static User mapResultSet(ResultSet rs) throws SQLException {
        return new User(
            rs.getInt("user_id"),
            rs.getString("username"),
            rs.getString("role"),
            rs.getString("email"),
            rs.getString("full_name"),
            rs.getString("phone"),
            rs.getBoolean("is_active")
        );
    }
}
