package com.muse.service;

import com.muse.dao.AdminDAO;
import com.muse.model.Admin;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Service untuk autentikasi Admin
 * Sesuai Business Logic Layer proposal
 */
public class AuthService {

    private static AuthService instance;
    private Admin adminLogin; // session saat ini
    private final AdminDAO adminDAO;

    private AuthService() {
        this.adminDAO = new AdminDAO();
    }

    public static AuthService getInstance() {
        if (instance == null) instance = new AuthService();
        return instance;
    }

    /**
     * Validasi username & password
     */
    public boolean login(String username, String password) {
        Admin admin = adminDAO.findByUsername(username);
        if (admin == null) return false;

        boolean valid = BCrypt.checkpw(password, admin.getPassword());
        if (valid) {
            this.adminLogin = admin;
        }
        return valid;
    }

    public void logout() {
        this.adminLogin = null;
    }

    public Admin getAdminLogin() {
        return adminLogin;
    }

    public boolean isLoggedIn() {
        return adminLogin != null;
    }
}
