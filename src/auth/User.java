package auth;

/**
 * Model class representing a registered user in the system.
 */
public class User {
    private int userId;
    private String username;
    private String role;
    private String email;
    private String fullName;
    private String phone;
    private boolean isActive;

    public User(int userId, String username, String role, String email, String fullName, String phone, boolean isActive) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.email = email;
        this.fullName = fullName;
        this.phone = phone;
        this.isActive = isActive;
    }

    // ── Getters ────────────────────────────────────────────────
    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
    public String getPhone() { return phone; }
    public boolean isActive() { return isActive; }
    
    // ── Setters ────────────────────────────────────────────────
    public void setUserId(int userId) { this.userId = userId; }
    public void setUsername(String username) { this.username = username; }
    public void setRole(String role) { this.role = role; }
    public void setEmail(String email) { this.email = email; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setActive(boolean active) { isActive = active; }
}