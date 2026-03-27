package org.example; // Ensure this matches your project's package

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FeedbackWindow extends JDialog {

    private final Dashboard parentDashboard;
    private final String menteeName;
    private final String mentorName;
    private int selectedRating = 0; // 0 means no rating selected

    private final JTextArea feedbackArea;
    private final List<JToggleButton> starButtons = new ArrayList<>();

    public FeedbackWindow(Dashboard parent, String menteeName, String mentorName) {
        super(parent, "Provide Feedback for " + mentorName, true);
        this.parentDashboard = parent;
        this.menteeName = menteeName;
        this.mentorName = mentorName;

        setSize(450, 400);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout(10, 10));

        // --- Custom Styling (Assuming ThemeManager) ---
        // getContentPane().setBackground(ThemeManager.BG_DARK_MAIN);

        // --- North Panel: Title and Mentor Name ---
        JPanel titlePanel = new JPanel();
        // titlePanel.setBackground(ThemeManager.BG_DARK_CARD);
        titlePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JLabel titleLabel = new JLabel("Rate Your Session with " + mentorName);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        // titleLabel.setForeground(ThemeManager.TEXT_LIGHT_PRIMARY);
        titlePanel.add(titleLabel);
        add(titlePanel, BorderLayout.NORTH);

        // --- Center Panel: Rating and Text Area ---
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        // centerPanel.setBackground(ThemeManager.BG_DARK_MAIN);

        // Rating Panel
        JPanel ratingPanel = createRatingPanel();
        centerPanel.add(ratingPanel, BorderLayout.NORTH);

        // Feedback Text Area
        feedbackArea = new JTextArea(5, 20);
        feedbackArea.setLineWrap(true);
        feedbackArea.setWrapStyleWord(true);
        feedbackArea.setBorder(BorderFactory.createTitledBorder("Written Feedback (Optional)"));
        JScrollPane scrollPane = new JScrollPane(feedbackArea);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // --- South Panel: Submit Button ---
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        // southPanel.setBackground(ThemeManager.BG_DARK_MAIN);
        JButton submitButton = new JButton("Submit Feedback");
        // parentDashboard.styleSearchButton(submitButton); // Reuse styling if available
        submitButton.addActionListener(e -> handleSubmit());
        southPanel.add(submitButton);
        add(southPanel, BorderLayout.SOUTH);
    }

    private JPanel createRatingPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        // panel.setBackground(ThemeManager.BG_DARK_MAIN);

        JLabel prompt = new JLabel("Your Rating:");
        // prompt.setForeground(ThemeManager.TEXT_LIGHT_PRIMARY);
        panel.add(prompt);

        ActionListener starListener = e -> {
            JToggleButton source = (JToggleButton) e.getSource();
            int rating = Integer.parseInt(source.getActionCommand());
            setSelectedRating(rating);
        };

        for (int i = 1; i <= 5; i++) {
            JToggleButton star = new JToggleButton("★");
            star.setActionCommand(String.valueOf(i));
            star.setFont(new Font("Arial", Font.BOLD, 24));
            star.setForeground(Color.GRAY); // Unselected color
            star.setBorderPainted(false);
            star.setFocusPainted(false);
            star.setContentAreaFilled(false);
            star.addActionListener(starListener);
            starButtons.add(star);
            panel.add(star);
        }
        return panel;
    }

    private void setSelectedRating(int rating) {
        this.selectedRating = rating;

        // Update the visual state of the stars
        for (int i = 0; i < starButtons.size(); i++) {
            JToggleButton star = starButtons.get(i);
            if (i < rating) {
                star.setForeground(new Color(255, 215, 0)); // Gold/Yellow for selected
                star.setSelected(true);
            } else {
                star.setForeground(Color.GRAY);
                star.setSelected(false);
            }
        }
    }

    private void handleSubmit() {
        if (selectedRating == 0) {
            JOptionPane.showMessageDialog(this, "Please select a star rating.", "Missing Rating", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (saveFeedbackToDatabase()) {
            JOptionPane.showMessageDialog(this,
                    "Thank you for your feedback! Rating " + selectedRating + "/5 submitted.",
                    "Submission Successful", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to save feedback due to a database error.", "Submission Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean saveFeedbackToDatabase() {
        // 1. Get IDs from names
        int menteeId = parentDashboard.getUserId(menteeName);
        int mentorId = parentDashboard.getUserId(mentorName);
        String feedbackText = feedbackArea.getText().trim();

        if (menteeId == -1 || mentorId == -1) {
            System.err.println("Feedback Save Error: Mentee or Mentor ID not found.");
            return false;
        }

        String sql = "INSERT INTO mentor_feedback (mentee_id, mentor_id, rating, feedback_text) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, menteeId);
            ps.setInt(2, mentorId);
            ps.setInt(3, selectedRating);

            // Handle empty feedback text by setting NULL or empty string
            if (feedbackText.isEmpty()) {
                ps.setNull(4, java.sql.Types.VARCHAR);
            } else {
                ps.setString(4, feedbackText);
            }

            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Database Error saving feedback: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}