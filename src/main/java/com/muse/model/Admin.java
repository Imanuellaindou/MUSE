package com.muse.model;

/**
 * Model class untuk Admin
 * Sesuai Class Diagram proposal MUSE
 */
public class Admin {
    private int id;
    private String username;
    private String password;

    public Admin() {}

    public Admin(int id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }

    // Method sesuai class diagram
    public boolean login(String inputUsername, String inputPassword) {
        // Handled by AuthService
        return false;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
