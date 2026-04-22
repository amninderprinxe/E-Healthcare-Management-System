package auth;

import db.DatabaseConnection;
import java.sql.*;

/**
 * Handles user login using PLAIN TEXT for testing bypass.
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
                
                // Direct comparison (Bypass BCrypt)
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

    public static void logout() {
        currentUser = null;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    // Helper to map Result Set
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
