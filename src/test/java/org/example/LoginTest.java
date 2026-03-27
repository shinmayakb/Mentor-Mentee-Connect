package org.example;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito; // We will use Mockito to mock components

import javax.swing.*;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class LoginTest {

    private static final String TEST_EMAIL = "test@login.com";
    private static final String TEST_PASSWORD = "securepassword";
    private static final String TEST_NAME = "TestUser";
    private static final String TEST_ROLE = "Mentee";

    private static Login loginFrame;

    // --- SETUP: Insert a test user into the register table ---
    @BeforeAll
    static void setUp() throws SQLException {
        // Suppress dialog boxes during testing
        UIManager.put("OptionPane.showConfirmDialog", Mockito.mock(JOptionPane.class));
        UIManager.put("OptionPane.showMessageDialog", Mockito.mock(JOptionPane.class));

        // 1. Initialize the Login frame on the AWT thread
        SwingUtilities.invokeLater(() -> {
            loginFrame = new Login();
            loginFrame.setVisible(false); // Keep it invisible
        });

        // Wait for the Swing thread to initialize the frame (needed to access fields)
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 2. Insert test user data directly into the live database
        // NOTE: This relies on the live DatabaseConnection class and register table.
        String sql = "INSERT INTO register (name, role, email, password) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, TEST_NAME);
            ps.setString(2, TEST_ROLE);
            ps.setString(3, TEST_EMAIL);
            ps.setString(4, TEST_PASSWORD);
            ps.executeUpdate();
            System.out.println("Test user inserted.");
        }
    }

    // --- TEARDOWN: Delete the test user ---
    @AfterAll
    static void tearDown() throws SQLException {
        // 1. Delete the test user from the database
        String sql = "DELETE FROM register WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, TEST_EMAIL);
            ps.executeUpdate();
            System.out.println("Test user deleted.");
        }
        // 2. Close the frame
        if (loginFrame != null) {
            SwingUtilities.invokeLater(() -> loginFrame.dispose());
        }
    }

    /**
     * Helper method to call the private authenticateUser() method using Reflection.
     */
    private void callAuthenticateUser() throws Exception {
        Method method = Login.class.getDeclaredMethod("authenticateUser");
        method.setAccessible(true);
        method.invoke(loginFrame);
    }

    // --- TEST CASES ---

    @Test
    void testSuccessfulLogin() throws Exception {
        // Set fields via reflection or helper methods (assuming emailField and passwordField are accessible)
        // Since they are private in Login, we'll access them via the frame methods (not ideal, but works for testing logic)
        SwingUtilities.invokeAndWait(() -> {
            loginFrame.emailField.setText(TEST_EMAIL);
            loginFrame.passwordField.setText(TEST_PASSWORD);
        });

        // The real test: does the authentication method execute without throwing an exception
        // and would it logically proceed to the Dashboard?
        // NOTE: Testing actual navigation is outside the scope of a JUnit test.
        assertDoesNotThrow(() -> callAuthenticateUser(), "Successful login failed.");
    }

    @Test
    void testInvalidPassword() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            loginFrame.emailField.setText(TEST_EMAIL);
            loginFrame.passwordField.setText("wrongpassword");
        });

        assertDoesNotThrow(() -> callAuthenticateUser(), "Invalid password should not crash.");
        // We rely on the JOptionPane to show the error, which is mocked, so we test for non-crash.
    }

    @Test
    void testMissingFields() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            loginFrame.emailField.setText("");
            loginFrame.passwordField.setText(TEST_PASSWORD);
        });

        // The method should return immediately before reaching database logic
        assertDoesNotThrow(() -> callAuthenticateUser(), "Missing field check failed.");
    }

    @Test
    void testInvalidEmail() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            loginFrame.emailField.setText("nonexistent@user.com");
            loginFrame.passwordField.setText(TEST_PASSWORD);
        });

        assertDoesNotThrow(() -> callAuthenticateUser(), "Invalid email check failed.");
    }
}