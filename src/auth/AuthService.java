package auth;

import db.DatabaseConnection;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;

/**
 * Handles user login, logout, registration, and password management.
 * Uses BCrypt for password hashing.
 */
public class AuthService {

    private static User currentUser = null;

    // ── Login ──────────────────────────────────────────────────
    public static User login(String username, String plainPassword) throws Exception {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        String sql = "SELECT * FROM users WHERE username = ? AND is_active = TRUE";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password");
                if (BCrypt.checkpw(plainPassword, storedHash)) {
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

    // ── Logout ─────────────────────────────────────────────────
    public static void logout() {
        currentUser = null;
    }

    // ── Get current logged-in user ─────────────────────────────
    public static User getCurrentUser() {
        return currentUser;
    }

    // ── Register new user ──────────────────────────────────────
    public static boolean register(String username, String plainPassword,
                                   String role, String email,
                                   String fullName, String phone) throws Exception {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        String sql = "INSERT INTO users (username, password, role, email, full_name, phone) VALUES (?,?,?,?,?,?)";

        String hashed = BCrypt.hashpw(plainPassword, BCrypt.gensalt(10));

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, hashed);
            ps.setString(3, role);
            ps.setString(4, email);
            ps.setString(5, fullName);
            ps.setString(6, phone);
            int rows = ps.executeUpdate();
            return rows > 0;
        }
    }

    // ── Change password ────────────────────────────────────────
    public static boolean changePassword(int userId, String oldPassword, String newPassword) throws Exception {
        Connection conn = DatabaseConnection.getInstance().getConnection();

        // Verify old password
        String fetchSql = "SELECT password FROM users WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(fetchSql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String storedHash = rs.getString("password");
                if (!BCrypt.checkpw(oldPassword, storedHash)) {
                    throw new Exception("Old password is incorrect.");
                }
            }
        }

        // Update new password
        String updateSql = "UPDATE users SET password = ? WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
            ps.setString(1, BCrypt.hashpw(newPassword, BCrypt.gensalt(10)));
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    // ── Helper ─────────────────────────────────────────────────
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
