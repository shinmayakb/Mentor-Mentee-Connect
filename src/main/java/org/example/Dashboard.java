package org.example;

import javax.swing.*;
import javax.swing.Timer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;
import java.util.List;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Dashboard extends JFrame {

    // Removed all hardcoded DB connection strings. Relying on DatabaseConnection.

    private String loggedInUserName;
    private String loggedInUserRole;
    private JFrame loginFrame;

    private JPanel contentPanel;
    private JTextField searchField;

    private JButton notificationIcon;
    private JLabel notificationCountLabel;

    public void startSessionMonitoring(String menteeName) {
        System.out.println("🔥 Session monitoring started for partner: " + menteeName);
    }

    // --- ThemeManager ---
    private static class ThemeManager {
        public static final Color BG_DARK_MAIN = new Color(25, 29, 36);
        public static final Color BG_DARK_CARD = new Color(34, 40, 49);
        public static final Color BG_TOPBAR = new Color(34, 40, 49);

        public static final Color TEXT_LIGHT_PRIMARY = Color.WHITE;
        public static final Color TEXT_LIGHT_SECONDARY = new Color(170, 170, 170);
        public static final Color TEXT_AVAILABLE = new Color(0, 200, 100);
        public static final Color TEXT_BUSY = new Color(255, 70, 70);

        public static final Color ACCENT_PURPLE = new Color(130, 0, 255);
        public static final Color ACCENT_HOVER = new Color(170, 80, 255);
        public static final Color LOGOUT_RED = new Color(220, 50, 50);
        public static final Color LOGOUT_HOVER = new Color(255, 80, 80);
        public static final Color CARD_HOVER = new Color(45, 52, 64);
        public static final Color NOTIFICATION_ICON_COLOR = new Color(255, 100, 0);

        public static Color getIconBGColor(String domain) {
            return switch (domain) {
                case "Cybersecurity" -> new Color(0, 150, 200);
                case "Cloud Computing" -> new Color(255, 100, 0);
                case "Database Management" -> new Color(150, 0, 255);
                case "DevOps Engineering" -> new Color(50, 200, 50);
                case "Machine Learning" -> new Color(255, 50, 100);
                case "AI" -> new Color(255, 200, 0);
                case "Data Analysis" -> new Color(0, 100, 255);
                case "Frontend Development" -> new Color(200, 0, 255);
                case "Backend Development" -> new Color(0, 200, 200);
                default -> new Color(100, 150, 255);
            };
        }
    }
    // --- End ThemeManager ---

    // --- CONSTRUCTOR ---
    public Dashboard(String userName, String userRole, JFrame loginFrame) {
        this.loggedInUserName = userName;
        this.loggedInUserRole = userRole;
        this.loginFrame = loginFrame;

        setTitle("Mentor Connect Dashboard");
        setSize(1300, 850);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ignored) {
        }

        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(ThemeManager.BG_DARK_MAIN);
        add(mainContainer);

        JPanel topbar = createTopBar();
        mainContainer.add(topbar, BorderLayout.NORTH);

        contentPanel = new JPanel();
        contentPanel.setBackground(ThemeManager.BG_DARK_MAIN);
        contentPanel.setLayout(new GridLayout(0, 3, 30, 30));

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBackground(ThemeManager.BG_DARK_MAIN);
        scrollPane.getViewport().setBackground(ThemeManager.BG_DARK_MAIN);
        scrollPane.setBorder(null);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        mainContainer.add(scrollPane, BorderLayout.CENTER);

        // Initial load of mentors
        loadMentors(null);

        // Mentor-specific setup
        if ("Mentor".equalsIgnoreCase(loggedInUserRole)) {
            updateNotificationCount();
        }
    }

    // =========================================================================
    // TOP BAR METHODS
    // =========================================================================

    private JPanel createTopBar() {
        JPanel topbar = new JPanel(new BorderLayout());
        topbar.setBackground(ThemeManager.BG_TOPBAR);
        topbar.setBorder(BorderFactory.createEmptyBorder(15, 40, 15, 40));

        JLabel title = new JLabel("Mentor Connect");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(ThemeManager.TEXT_LIGHT_PRIMARY);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        rightPanel.setOpaque(false);

        // --- Search Components ---
        searchField = new JTextField(15);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setPreferredSize(new Dimension(searchField.getPreferredSize().width, 30));
        searchField.setBackground(new Color(50, 50, 70));
        searchField.setForeground(ThemeManager.TEXT_LIGHT_PRIMARY);
        searchField.setCaretColor(ThemeManager.TEXT_LIGHT_PRIMARY);
        searchField.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        JButton searchButton = new JButton("Search");
        styleSearchButton(searchButton);

        searchField.addActionListener(e -> loadMentors(searchField.getText().trim()));
        searchButton.addActionListener(e -> loadMentors(searchField.getText().trim()));

        rightPanel.add(searchField);
        rightPanel.add(searchButton);

        // --- Notification Icon (Mentor Only) ---
        if ("Mentor".equalsIgnoreCase(loggedInUserRole)) {
            notificationIcon = new JButton("🔔");
            notificationIcon.setFont(new Font("Segoe UI Emoji", Font.BOLD, 20));
            notificationIcon.setForeground(ThemeManager.TEXT_LIGHT_PRIMARY);
            notificationIcon.setBackground(ThemeManager.BG_TOPBAR);
            notificationIcon.setFocusPainted(false);
            notificationIcon.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            notificationIcon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            notificationCountLabel = new JLabel("0");
            notificationCountLabel.setForeground(Color.WHITE);
            notificationCountLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));

            JPanel notificationPanel = new JPanel(new BorderLayout());
            notificationPanel.setOpaque(false);
            notificationPanel.add(notificationIcon, BorderLayout.CENTER);

            // Badge styling
            notificationCountLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 8));
            notificationCountLabel.setHorizontalAlignment(SwingConstants.CENTER);
            notificationCountLabel.setVerticalAlignment(SwingConstants.TOP);
            notificationCountLabel.setOpaque(true);
            notificationCountLabel.setBackground(ThemeManager.NOTIFICATION_ICON_COLOR);
            notificationCountLabel.setPreferredSize(new Dimension(20, 20));

            JPanel countWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            countWrapper.setOpaque(false);
            countWrapper.add(notificationCountLabel);

            // Initially hide the badge
            countWrapper.setVisible(false);

            // Use OverlayLayout to stack icon and badge
            notificationPanel.setLayout(new OverlayLayout(notificationPanel));
            notificationPanel.add(countWrapper);
            notificationPanel.add(notificationIcon);

            notificationIcon.addActionListener(e -> showNotificationWindow());

            rightPanel.add(notificationPanel);

            JButton activeSessionsIcon = new JButton("🗓️"); // Calendar or Clock icon
            activeSessionsIcon.setFont(new Font("Segoe UI Emoji", Font.BOLD, 20));
            activeSessionsIcon.setForeground(ThemeManager.TEXT_LIGHT_PRIMARY);
            activeSessionsIcon.setBackground(ThemeManager.BG_TOPBAR);
            activeSessionsIcon.setFocusPainted(false);
            activeSessionsIcon.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            activeSessionsIcon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            activeSessionsIcon.addActionListener(e -> showMentorActiveSessionsWindow()); // NEW ACTION

            rightPanel.add(activeSessionsIcon);
            rightPanel.add(notificationPanel);
        }
        else if ("Mentee".equalsIgnoreCase(loggedInUserRole)) {
            // --- NEW: Mentee Status Icon (✅ / ❌ Badge) ---
            notificationIcon = new JButton("✉️"); // Envelope icon for requests
            notificationIcon.setFont(new Font("Segoe UI Emoji", Font.BOLD, 20));
            notificationIcon.setForeground(ThemeManager.TEXT_LIGHT_PRIMARY);
            notificationIcon.setBackground(ThemeManager.BG_TOPBAR);
            notificationIcon.setFocusPainted(false);
            notificationIcon.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            notificationIcon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            notificationCountLabel = new JLabel("0");
            notificationCountLabel.setForeground(Color.WHITE);
            notificationCountLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));

            JPanel notificationPanel = new JPanel(new BorderLayout());
            notificationPanel.setOpaque(false);
            notificationPanel.add(notificationIcon, BorderLayout.CENTER);

            // Badge styling
            notificationCountLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 8));
            notificationCountLabel.setHorizontalAlignment(SwingConstants.CENTER);
            notificationCountLabel.setVerticalAlignment(SwingConstants.TOP);
            notificationCountLabel.setOpaque(true);
            notificationCountLabel.setBackground(ThemeManager.NOTIFICATION_ICON_COLOR);
            notificationCountLabel.setPreferredSize(new Dimension(20, 20));

            JPanel countWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            countWrapper.setOpaque(false);
            countWrapper.add(notificationCountLabel);
            countWrapper.setVisible(false);

            notificationPanel.setLayout(new OverlayLayout(notificationPanel));
            notificationPanel.add(countWrapper);
            notificationPanel.add(notificationIcon);

            // NEW action: Open Mentee Status Window
            notificationIcon.addActionListener(e -> showMenteeStatusWindow());

            rightPanel.add(notificationPanel);

            // Initial call for mentee status count
            updateNotificationCount();
        }

        // --- User Greeting and Logout ---
        JLabel userGreeting = new JLabel("Hi, " + loggedInUserName);
        userGreeting.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        userGreeting.setForeground(ThemeManager.TEXT_LIGHT_PRIMARY);
        rightPanel.add(userGreeting);

        JLabel userIcon = new JLabel("👤");
        userIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        userIcon.setForeground(ThemeManager.TEXT_LIGHT_PRIMARY);
        rightPanel.add(userIcon);

        JButton logoutBtn = new JButton("Logout");
        styleLogoutButton(logoutBtn);
        logoutBtn.addActionListener(e -> logoutAction());
        rightPanel.add(logoutBtn);

        topbar.add(title, BorderLayout.WEST);
        topbar.add(rightPanel, BorderLayout.EAST);
        return topbar;
    }

    private void showMenteeStatusWindow() {
        // Call the external MenteeStatusWindow class:
        MenteeStatusWindow statusDialog = new MenteeStatusWindow(this, loggedInUserName);
        statusDialog.setVisible(true);
        updateNotificationCount();
    }

    private int getMenteeRequestCount() {
        if (!"Mentee".equalsIgnoreCase(loggedInUserRole)) return 0;

        String sql = "SELECT COUNT(*) FROM mentor_requests WHERE mentee_name = ? AND status = 'Pending'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, loggedInUserName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching mentee request count: " + e.getMessage());
        }
        return 0;
    }

    public void updateNotificationCount() {
        if (notificationCountLabel != null) {
            int count = 0;
            if ("Mentor".equalsIgnoreCase(loggedInUserRole)) {
                count = getUnreadNotificationCount(); // Existing method for mentor
            } else if ("Mentee".equalsIgnoreCase(loggedInUserRole)) {
                count = getMenteeRequestCount(); // New method for mentee
            }

            notificationCountLabel.setText(String.valueOf(count));
            notificationCountLabel.getParent().setVisible(count > 0);
            notificationCountLabel.getParent().revalidate();
            notificationCountLabel.getParent().repaint();
        }
    }


    // --- MenteeStatusWindow Inner Class ---
    public class MenteeStatusWindow extends JDialog {

        private final String menteeName;
        private final Dashboard dashboard;

        public MenteeStatusWindow(Dashboard parent, String menteeName) {
            super(parent, "My Request Status", true); // Modal dialog
            this.dashboard = parent;
            this.menteeName = menteeName;

            setSize(600, 450);
            setLocationRelativeTo(parent);
            setLayout(new BorderLayout());
            getContentPane().setBackground(ThemeManager.BG_DARK_MAIN);

            // Title Label
            JLabel titleLabel = new JLabel("Status of Requests Sent by " + menteeName, SwingConstants.CENTER);
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
            titleLabel.setForeground(ThemeManager.TEXT_LIGHT_PRIMARY);
            titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
            add(titleLabel, BorderLayout.NORTH);

            // Main content panel
            JPanel statusPanel = new JPanel();
            statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.Y_AXIS));
            statusPanel.setBackground(ThemeManager.BG_DARK_MAIN);
            JScrollPane scrollPane = new JScrollPane(statusPanel);
            scrollPane.setBorder(null);
            scrollPane.getViewport().setBackground(ThemeManager.BG_DARK_MAIN);
            add(scrollPane, BorderLayout.CENTER);

            loadMenteeRequests(statusPanel);

            // Close Button
            JButton closeButton = new JButton("Close");
            styleLogoutButton(closeButton); // Re-use the logout button style for a dark theme accent
            closeButton.addActionListener(e -> dispose());

            JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            bottomPanel.setBackground(ThemeManager.BG_DARK_MAIN);
            bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            bottomPanel.add(closeButton);
            add(bottomPanel, BorderLayout.SOUTH);
        }


        private void setupChatButton(JButton chatButton, String startTimeStr) {
            // Define the time window for starting the chat
            try {
                java.text.SimpleDateFormat timeFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                java.util.Date startTime = timeFormat.parse(startTimeStr);

                // Define the allowed start window (e.g., 5 minutes before to 10 minutes after)
                long startWindowMillis = startTime.getTime(); // When session is 'on time'
                long endWindowMillis = startTime.getTime() + (10 * 60 * 1000); // 10 minutes after start time

                // 1. Initial Check & Button Setup
                Timer timer = new Timer(60000, new ActionListener() { // Check every 60 seconds
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        long currentTimeMillis = System.currentTimeMillis();

                        if (currentTimeMillis >= startWindowMillis && currentTimeMillis <= endWindowMillis) {
                            // Current time is within the allowed 10-minute window
                            chatButton.setEnabled(true);
                            chatButton.setText("Start Chat");
                        } else if (currentTimeMillis > endWindowMillis) {
                            // Time has passed the 10-minute grace period
                            chatButton.setEnabled(false);
                            chatButton.setText("Session Expired");
                            ((Timer) e.getSource()).stop(); // Stop the timer once expired
                            // OPTIONAL: Update session status to 'Expired' in DB
                        } else {
                            // Session hasn't started yet
                            chatButton.setEnabled(false);
                            chatButton.setText("Upcoming");
                        }
                    }
                });

                // Run the initial check immediately
                timer.setInitialDelay(0);
                timer.start();

            } catch (java.text.ParseException e) {
                // Handle cases where time format is invalid or null
                chatButton.setEnabled(false);
                chatButton.setText("Time Invalid");
                System.err.println("Error parsing session time: " + startTimeStr);
            }
        }
        private void loadMenteeRequests(JPanel panel) {
            String sql = "SELECT mentor_name, status, request_date FROM mentor_requests WHERE mentee_name = ? ORDER BY request_date DESC";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, menteeName);
                try (ResultSet rs = ps.executeQuery()) {
                    boolean found = false;
                    while (rs.next()) {
                        found = true;
                        String mentor = rs.getString("mentor_name");
                        String status = rs.getString("status");
                        String date = rs.getTimestamp("request_date").toString().split("\\.")[0]; // Simple formatting

                        panel.add(createRequestStatusCard(mentor, status, date));
                        panel.add(Box.createVerticalStrut(10)); // Spacer
                    }

                    if (!found) {
                        JLabel noRequests = new JLabel("You have not sent any mentor requests yet.", SwingConstants.CENTER);
                        noRequests.setForeground(ThemeManager.TEXT_LIGHT_SECONDARY);
                        noRequests.setAlignmentX(Component.CENTER_ALIGNMENT);
                        panel.add(Box.createVerticalGlue()); // Push content to center
                        panel.add(noRequests);
                        panel.add(Box.createVerticalGlue());
                    }
                }
            } catch (SQLException e) {
                System.err.println("Error loading mentee requests: " + e.getMessage());
                JOptionPane.showMessageDialog(this, "Failed to load request status.", "DB Error", JOptionPane.ERROR_MESSAGE);
            }
            panel.revalidate();
            panel.repaint();
        }

        private JPanel createRequestStatusCard(String mentorName, String status, String date) {
            JPanel card = new JPanel(new BorderLayout(15, 0));
            card.setBackground(ThemeManager.BG_DARK_CARD);
            card.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
            card.setMaximumSize(new Dimension(550, 80)); // Constrain height

            // Mentor and Date Info
            JPanel infoPanel = new JPanel(new GridLayout(2, 1));
            infoPanel.setOpaque(false);

            JLabel mentorLabel = new JLabel("Mentor: " + mentorName);
            mentorLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            mentorLabel.setForeground(ThemeManager.TEXT_LIGHT_PRIMARY);

            JLabel dateLabel = new JLabel("Sent On: " + date);
            dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            dateLabel.setForeground(ThemeManager.TEXT_LIGHT_SECONDARY);

            infoPanel.add(mentorLabel);
            infoPanel.add(dateLabel);

            // Status Label
            JLabel statusLabel = new JLabel(status, SwingConstants.CENTER);
            statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));

            Color statusColor = ThemeManager.TEXT_LIGHT_SECONDARY; // Default grey
            if ("Pending".equalsIgnoreCase(status)) {
                statusColor = ThemeManager.NOTIFICATION_ICON_COLOR; // Orange
            } else if ("Accepted".equalsIgnoreCase(status)) {
                statusColor = ThemeManager.TEXT_AVAILABLE; // Green
                // Add a button to start chat for accepted status
                JButton chatButton = new JButton("Start Chat");
                styleSearchButton(chatButton); // Re-use style
                chatButton.addActionListener(e -> {

                    // Corrected call inside createRequestStatusCard:
                    dashboard.new ChatWindow(dashboard, menteeName, mentorName).setVisible(true);
                    dispose();
                });
                card.add(chatButton, BorderLayout.EAST);
            } else if ("Rejected".equalsIgnoreCase(status)) {
                statusColor = ThemeManager.TEXT_BUSY; // Red
            }

            statusLabel.setForeground(statusColor);

            card.add(infoPanel, BorderLayout.WEST);
            card.add(statusLabel, BorderLayout.CENTER);

            return card;
        }
    }

    private void styleSearchButton(JButton btn) {
        btn.setBackground(ThemeManager.ACCENT_PURPLE);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(6, 15, 6, 15));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(ThemeManager.ACCENT_HOVER);
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(ThemeManager.ACCENT_PURPLE);
            }
        });
    }

    private void styleLogoutButton(JButton btn) {
        btn.setBackground(ThemeManager.LOGOUT_RED);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(6, 15, 6, 15));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(ThemeManager.LOGOUT_HOVER);
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(ThemeManager.LOGOUT_RED);
            }
        });
    }

    private void logoutAction() {
        int result = JOptionPane.showConfirmDialog(this, "Are you sure you want to log out?", "Confirm Logout", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            this.dispose();
            if (loginFrame != null) {
                loginFrame.setVisible(true);
            } else {
                new Login().setVisible(true);
            }
        }
    }

    // =========================================================================
    // NOTIFICATION METHODS (USES DatabaseConnection)
    // =========================================================================

    private int getUnreadNotificationCount() {
        if (!"Mentor".equalsIgnoreCase(loggedInUserRole)) return 0;

        String sql = "SELECT COUNT(*) FROM mentor_requests WHERE mentor_name = ? AND is_read = FALSE AND status = 'Pending'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, loggedInUserName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching notification count: " + e.getMessage());
        }
        return 0;
    }

    /*public void updateNotificationCount() {
        if (notificationCountLabel != null && "Mentor".equalsIgnoreCase(loggedInUserRole)) {
            int count = getUnreadNotificationCount();
            notificationCountLabel.setText(String.valueOf(count));

            // Show/hide the badge wrapper
            notificationCountLabel.getParent().setVisible(count > 0);

            notificationCountLabel.getParent().revalidate();
            notificationCountLabel.getParent().repaint();
        }
    }*/

    private void showNotificationWindow() {
        // NOTE: NotificationWindow.java must be created and accept these parameters
        NotificationWindow notificationDialog = new NotificationWindow(this, loggedInUserName, loggedInUserRole);
        notificationDialog.setVisible(true);

        updateNotificationCount();
    }

    // Inside Dashboard.java (near showNotificationWindow)
    private void showMentorActiveSessionsWindow() {
        // Note: MentorActiveSessionsWindow.java must be created/defined
        MentorActiveSessionsWindow sessionsDialog = new MentorActiveSessionsWindow(this, loggedInUserName);
        sessionsDialog.setVisible(true);
    }

    // =========================================================================
    // MENTOR CARD METHODS (USES DatabaseConnection)
    // =========================================================================

    private JPanel createMentorCard(String name, String domain, String description, String status, String schedule) {
        JPanel card = new JPanel();
        card.setPreferredSize(new Dimension(400, 250));
        card.setBackground(ThemeManager.BG_DARK_CARD);
        card.setLayout(new BorderLayout(20, 15));
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel coreContent = new JPanel(new BorderLayout(0, 15));
        coreContent.setOpaque(false);

        // --- Top Section: Icon and Name/Domain ---
        JPanel topSection = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        topSection.setOpaque(false);

        Color iconBG = ThemeManager.getIconBGColor(domain);
        JLabel domainIcon = new JLabel("💻", SwingConstants.CENTER);

        JPanel iconCircle = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(iconBG);
                g2d.fillOval(0, 0, getWidth(), getHeight());
            }
        };
        iconCircle.setLayout(new GridBagLayout());
        iconCircle.setPreferredSize(new Dimension(55, 55));
        iconCircle.setOpaque(false);
        domainIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));
        domainIcon.setForeground(Color.WHITE);
        iconCircle.add(domainIcon);

        switch (domain) {
            case "Cybersecurity": domainIcon.setText("🔒"); break;
            case "Cloud Computing": domainIcon.setText("☁️"); break;
            case "Database Management": domainIcon.setText("💾"); break;
            case "DevOps Engineering": domainIcon.setText("⚙️"); break;
            case "Machine Learning": domainIcon.setText("🧠"); break;
            case "AI": domainIcon.setText("🤖"); break;
            case "Data Analysis": domainIcon.setText("📈"); break;
            case "Frontend Development": domainIcon.setText("🌐"); break;
            case "Backend Development": domainIcon.setText("🛠️"); break;
            default: domainIcon.setText("🧑‍💻"); break;
        }

        JPanel nameDomainPanel = new JPanel(new GridLayout(2, 1));
        nameDomainPanel.setOpaque(false);

        JLabel mentorNameLabel = new JLabel(name);
        mentorNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        mentorNameLabel.setForeground(ThemeManager.TEXT_LIGHT_PRIMARY);

        JLabel domainLabel = new JLabel(domain);
        domainLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        domainLabel.setForeground(ThemeManager.TEXT_LIGHT_SECONDARY);

        nameDomainPanel.add(mentorNameLabel);
        nameDomainPanel.add(domainLabel);

        topSection.add(iconCircle);
        topSection.add(nameDomainPanel);


        // --- Description ---
        JLabel descriptionLabel = new JLabel("<html><p style='width: 350px;'>" + description + "</p></html>");
        descriptionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        descriptionLabel.setForeground(ThemeManager.TEXT_LIGHT_SECONDARY);
        descriptionLabel.setVerticalAlignment(SwingConstants.TOP);
        descriptionLabel.setPreferredSize(new Dimension(350, 80));

        coreContent.add(topSection, BorderLayout.NORTH);
        coreContent.add(descriptionLabel, BorderLayout.CENTER);


        // --- Bottom Wrapper: Status and Action Button ---
        JPanel bottomWrapper = new JPanel(new BorderLayout(10, 0));
        bottomWrapper.setOpaque(false);

        JPanel statusSchedulePanel = new JPanel(new GridLayout(2, 1, 0, 5));
        statusSchedulePanel.setOpaque(false);

        JLabel statusLabel = new JLabel(status);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        statusLabel.setForeground("Available".equalsIgnoreCase(status) ? ThemeManager.TEXT_AVAILABLE : ThemeManager.TEXT_BUSY);

        JLabel scheduleLabel = new JLabel("Time: " + schedule);
        scheduleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        scheduleLabel.setForeground(ThemeManager.TEXT_LIGHT_SECONDARY);

        statusSchedulePanel.add(statusLabel);
        statusSchedulePanel.add(scheduleLabel);

        bottomWrapper.add(statusSchedulePanel, BorderLayout.WEST);

        // --- Action Button Panel (Role-based) ---
        JPanel actionButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionButtonPanel.setOpaque(false);

        if ("Mentee".equalsIgnoreCase(loggedInUserRole)) {
            JButton requestButton = new JButton("Send Request 🚀");
            styleRequestButton(requestButton);

            requestButton.addActionListener(e -> {
                sendRequest(mentorNameLabel.getText());
            });

            actionButtonPanel.add(requestButton);
        } else {
            // Mentor can only view profile/details here
            JButton placeholderButton = new JButton("View Profile");
            placeholderButton.setBackground(ThemeManager.ACCENT_PURPLE.darker());
            placeholderButton.setForeground(Color.WHITE);
            placeholderButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
            actionButtonPanel.add(placeholderButton);
        }

        bottomWrapper.add(actionButtonPanel, BorderLayout.EAST);

        // --- Card Interaction ---
        MouseAdapter detailClickAdapter = new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                card.setBackground(ThemeManager.CARD_HOVER);
            }
            public void mouseExited(MouseEvent e) {
                card.setBackground(ThemeManager.BG_DARK_CARD);
            }
            public void mouseClicked(MouseEvent e) {
                if (e.getSource() == card) {
                    JOptionPane.showMessageDialog(card,
                            "Mentor: " + name +
                                    "\nDomain: " + domain +
                                    "\nDescription: " + description +
                                    "\nStatus: " + status +
                                    "\nSchedule: " + schedule,
                            "Mentor Details",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }
        };

        card.addMouseListener(detailClickAdapter);
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        card.add(coreContent, BorderLayout.CENTER);
        card.add(bottomWrapper, BorderLayout.SOUTH);

        return card;
    }

    private void styleRequestButton(JButton btn) {
        btn.setBackground(ThemeManager.ACCENT_PURPLE);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(ThemeManager.ACCENT_HOVER);
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(ThemeManager.ACCENT_PURPLE);
            }
        });
    }

    private void sendRequest(String mentorName) {
        String insertSql = "INSERT INTO mentor_requests (mentee_name, mentor_name, status, is_read) VALUES (?, ?, 'Pending', FALSE)";
        String checkSql = "SELECT COUNT(*) FROM mentor_requests WHERE mentee_name = ? AND mentor_name = ? AND status = 'Pending'";
        boolean pendingExists = false;

        try (Connection conn = DatabaseConnection.getConnection()) {
            // 1. Check for existing pending request
            try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
                checkPs.setString(1, loggedInUserName);
                checkPs.setString(2, mentorName);
                try (ResultSet rs = checkPs.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        pendingExists = true;
                    }
                }
            }

            if (pendingExists) {
                JOptionPane.showMessageDialog(this,
                        "You already have a **Pending** request with " + mentorName + ".\nPlease wait for a response.",
                        "Request Already Sent",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                // 2. Insert the new request
                try (PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
                    insertPs.setString(1, loggedInUserName);
                    insertPs.setString(2, mentorName);
                    int rowsAffected = insertPs.executeUpdate();

                    if (rowsAffected > 0) {
                        JOptionPane.showMessageDialog(this,
                                "Request successfully sent to " + mentorName + "!",
                                "Request Sent",
                                JOptionPane.INFORMATION_MESSAGE);
                        updateNotificationCount(); // Update the mentee's notification badge
                    } else {
                        throw new SQLException("Database returned 0 rows affected.");
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Database Error during sendRequest: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Database Error: Could not send request.\nDetails: " + e.getMessage(),
                    "Request Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }


    // =========================================================================
    // DATA AND UTILITY METHODS (USES DatabaseConnection)
    // =========================================================================

    private void loadMentors(String query) {
        contentPanel.removeAll();
        List<MentorData> mentors = getMentorsFromDBWithFallback();

        if (query != null && !query.isEmpty()) {
            String q = query.toLowerCase();
            String[] tokens = q.split("\\s+");
            // Filtering logic to remove mentors that DON'T match ANY token in ANY field
            mentors.removeIf(m -> {
                String name = m.name.toLowerCase();
                String domain = m.domain.toLowerCase();
                String description = m.description.toLowerCase();
                String status = m.status.toLowerCase();

                boolean matchFound = false;
                for (String t : tokens) {
                    if (name.contains(t) || domain.contains(t) || description.contains(t) || status.contains(t)) {
                        matchFound = true;
                        break;
                    }
                }
                return !matchFound;
            });
        }

        if (mentors.isEmpty()) {
            JLabel msg = new JLabel("No mentors found matching your criteria.");
            msg.setFont(new Font("Segoe UI", Font.ITALIC, 18));
            msg.setForeground(ThemeManager.TEXT_LIGHT_SECONDARY);
            contentPanel.setLayout(new GridBagLayout());
            contentPanel.add(msg);
        } else {
            // Restore GridLayout if it was changed to GridBagLayout for the message
            contentPanel.setLayout(new GridLayout(0, 3, 30, 30));
            for (MentorData m : mentors)
                contentPanel.add(createMentorCard(m.name, m.domain, m.description, m.status, m.schedule));
        }
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private static class MentorData {
        String name;
        String domain;
        String description;
        String status;
        String schedule;

        public MentorData(String name, String domain, String description, String status, String schedule) {
            this.name = name;
            this.domain = domain;
            this.description = description;
            this.status = status;
            this.schedule = schedule;
        }
    }

    private List<MentorData> getMentorsFromDBWithFallback() {
        List<MentorData> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT name, expertise, description, status, schedule FROM register WHERE role='Mentor'");
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String expertise = rs.getString("expertise");
                if (expertise == null || expertise.isEmpty()) expertise = "General";

                // Use the first domain listed if multiple are present
                String domain = expertise.split(",")[0].trim();

                list.add(new MentorData(
                        rs.getString("name"),
                        domain,
                        rs.getString("description"),
                        rs.getString("status"),
                        rs.getString("schedule")
                ));
            }
        } catch (Exception e) {
            System.err.println("DB Connection failed. Loading DUMMY data. Error: " + e.getMessage());
            // Fallback to dummy data only if the connection fails
            return getDummyMentors();
        }

        // If the query was successful but returned no results, an empty list is returned.
        return list;
    }

    // This method remains for robust error handling during development/testing
    private List<MentorData> getDummyMentors() {
        List<MentorData> list = new ArrayList<>();
        list.add(new MentorData("Kavitha", "Cybersecurity", "Specializes in network security and ethical hacking and defense systems.", "Available", "9:00-12:40 AM"));
        list.add(new MentorData("Karthik", "Cloud Computing", "Expert in AWS, Azure, and Google Cloud infrastructure and migration strategies.", "Busy", "1:30-2:15 PM"));
        list.add(new MentorData("Lakshmi", "Database Management", "Proficient in SQL, NoSQL, and database design/optimization techniques.", "Available", "8:45-9:50 PM"));
        list.add(new MentorData("Rajesh", "DevOps Engineering", "Skilled in CI/CD, automation, and infrastructure as code using Terraform.", "Available", "9:00-12:40 AM"));
        list.add(new MentorData("Deepa", "Machine Learning", "Experienced in supervised/unsupervised learning and deep learning models.", "Busy", "1:30-2:15 PM"));
        list.add(new MentorData("Ramkumar", "AI", "Focuses on natural language processing, LLMs, and computer vision applications.", "Available", "8:45-9:50 PM"));
        list.add(new MentorData("Suresh", "Data Analysis", "Strong in statistical analysis, data visualization, and Python/R programming.", "Available", "9:00-12:40 AM"));
        list.add(new MentorData("Priya", "Frontend Development", "Specializes in React, Angular, and modern web UI/UX design and development.", "Busy", "1:30-2:15 PM"));
        list.add(new MentorData("Anand", "Backend Development", "Proficient in Java Spring Boot, Node.js, and designing scalable RESTful APIs.", "Available", "8:45-9:50 PM"));
        return list;
    }

    // --- ChatWindow Inner Class ---
    // Inside Dashboard.java

// Inside Dashboard.java

    public class ChatWindow extends JDialog {

        private final String menteeName;
        private final String mentorName;
        private final Dashboard dashboard;
        private JTextArea chatArea;

        // --- Core Chat Variables ---
        private javax.swing.Timer pollTimer;
        private int messageCount = 0;
        private final String sessionPartnerName;
        private final String currentUserName;
        private final int menteeId;
        private final int mentorId;
        private final int senderId;

        // NEW: Stores the ID of the last resource message for easy viewing
        private int lastResourceMessageId = -1;

        public ChatWindow(Dashboard parent, String menteeName, String mentorName) {
            super(parent, "Live Session: " + parent.loggedInUserName, true);
            this.dashboard = parent;
            this.menteeName = menteeName;
            this.mentorName = mentorName;

            // Session IDs: Essential for the bidirectional query
            this.menteeId = parent.getUserId(menteeName);
            this.mentorId = parent.getUserId(mentorName);
            this.senderId = parent.getUserId(parent.loggedInUserName);

            this.currentUserName = parent.loggedInUserName;
            // Determine who the other person in the chat is
            this.sessionPartnerName = parent.loggedInUserRole.equalsIgnoreCase("Mentor") ? menteeName : mentorName;

            setSize(700, 600);
            setLocationRelativeTo(parent);
            setLayout(new BorderLayout());
            // Assuming ThemeManager exists
            // getContentPane().setBackground(ThemeManager.BG_DARK_MAIN);

            // --- Chat Display Area ---
            chatArea = new JTextArea();
            chatArea.setEditable(false);
            // ... [Chat Area Setup] ...
            chatArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            chatArea.setLineWrap(true);
            chatArea.setWrapStyleWord(true);
            JScrollPane scrollPane = new JScrollPane(chatArea);
            scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            add(scrollPane, BorderLayout.CENTER);

            // Initial Message and Load History
            chatArea.append("--- Live Session with " + sessionPartnerName + " is OPEN ---\n");
            loadNewMessages(); // Loads initial/existing messages

            // --- Polling Timer Setup (2 seconds) ---
            pollTimer = new javax.swing.Timer(2000, e -> loadNewMessages());
            pollTimer.start();

            // --- Window Closing Logic: SESSION ENDING ---
            addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    pollTimer.stop();
                    System.out.println("Chat polling stopped for " + currentUserName);

                    // Trigger the session end process: Clear chat, open feedback (for mentee), update status
                    dashboard.handleSessionEnd(menteeName, mentorName);

                    dispose();
                }
            });

            // --- Input Panel ---
            JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
            // inputPanel.setBackground(ThemeManager.BG_TOPBAR); // Use a darker color
            inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            JTextField messageField = new JTextField();
            messageField.setFont(new Font("Segoe UI", Font.PLAIN, 14));

            JButton sendButton = new JButton("Send");
            dashboard.styleSearchButton(sendButton);

            // --- RESOURCE SHARING BUTTON ---
            JButton resourceButton = new JButton("📎 Share Resource");
            dashboard.styleSearchButton(resourceButton);
            resourceButton.addActionListener(e -> handleResourceSharing());

            // --- RESOURCE VIEWING BUTTON ---
            JButton viewResourceButton = new JButton("▶ View Last Resource");
            dashboard.styleSearchButton(viewResourceButton);
            viewResourceButton.addActionListener(e -> handleViewLastResource());


            // Logic for sending message
            ActionListener sendAction = e -> {
                String message = messageField.getText().trim();
                if (!message.isEmpty()) {
                    sendMessage(message, false); // Not a resource
                    messageField.setText("");
                }
            };

            sendButton.addActionListener(sendAction);
            messageField.addActionListener(sendAction);

            // Adjusted layout to include the View Resource Button
            JPanel resourceControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            resourceControls.add(resourceButton);
            resourceControls.add(viewResourceButton);

            inputPanel.add(resourceControls, BorderLayout.WEST);
            inputPanel.add(messageField, BorderLayout.CENTER);
            inputPanel.add(sendButton, BorderLayout.EAST);

            add(inputPanel, BorderLayout.SOUTH);
        }

        // --- MESSAGE SENDING LOGIC (TEXT ONLY) ---
        private void sendMessage(String message, boolean isResource) {
            // Note: This SQL is for TEXT messages only (resource_data defaults to NULL)
            String sql = "INSERT INTO chat_messages (mentee_id, mentor_id, sender_id, message_content, is_resource) " +
                    "VALUES (?, ?, ?, ?, ?)";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, menteeId);
                ps.setInt(2, mentorId);
                ps.setInt(3, senderId);
                ps.setString(4, message);
                ps.setBoolean(5, isResource);

                ps.executeUpdate();
                loadNewMessages();

            } catch (SQLException e) {
                System.err.println("Error saving chat message to database: " + e.getMessage());
            }
        }

        // --- NEW: MESSAGE SENDING LOGIC (WITH BLOB RESOURCE) ---
        private void sendMessageWithResource(String message, java.io.File file) {
            String sql = "INSERT INTO chat_messages (mentee_id, mentor_id, sender_id, message_content, is_resource, resource_data) " +
                    "VALUES (?, ?, ?, ?, TRUE, ?)";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql);
                 java.io.FileInputStream fis = new java.io.FileInputStream(file)) {

                ps.setInt(1, menteeId);
                ps.setInt(2, mentorId);
                ps.setInt(3, senderId);
                ps.setString(4, message);
                ps.setBinaryStream(5, fis, (int) file.length()); // Set the BLOB data

                ps.executeUpdate();
                loadNewMessages();

            } catch (SQLException e) {
                System.err.println("Error saving resource to database: " + e.getMessage());
                JOptionPane.showMessageDialog(this, "Database error saving resource.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (java.io.IOException e) {
                System.err.println("Error reading file stream: " + e.getMessage());
            }
        }

        // --- RESOURCE SHARING IMPLEMENTATION (UPDATED) ---
        private void handleResourceSharing() {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select Image or PDF to Share");
            int result = fileChooser.showOpenDialog(this);

            if (result == JFileChooser.APPROVE_OPTION) {
                java.io.File selectedFile = fileChooser.getSelectedFile();

                try {
                    long fileSize = selectedFile.length();
                    // Check file size (e.g., limit to 5MB)
                    if (fileSize > 5 * 1024 * 1024) {
                        JOptionPane.showMessageDialog(this, "File is too large (max 5MB).", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // Check file type (Image or PDF)
                    String mimeType = java.nio.file.Files.probeContentType(selectedFile.toPath());
                    if (mimeType == null || (!mimeType.startsWith("image/") && !mimeType.equals("application/pdf"))) {
                        JOptionPane.showMessageDialog(this, "Only Images and PDFs are supported.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // Message format: FILE_RESOURCE:name:type
                    String message = "FILE_RESOURCE:" + selectedFile.getName() + ":" + mimeType;

                    sendMessageWithResource(message, selectedFile);

                } catch (java.io.IOException ex) {
                    JOptionPane.showMessageDialog(this, "Error reading file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        // --- MESSAGE RETRIEVAL LOGIC (UPDATED to get ID and is_resource) ---
        private void loadNewMessages() {
            // FIX: SELECT the ID and is_resource column
            String sql = "SELECT id, sender_id, message_content, is_resource FROM chat_messages " +
                    "WHERE (mentee_id = ? AND mentor_id = ?) " +
                    "OR (mentee_id = ? AND mentor_id = ?) " +
                    "ORDER BY timestamp ASC";

            java.util.List<String> messages = new java.util.ArrayList<>();

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                // Parameters for Bidirectional Query
                ps.setInt(1, menteeId);
                ps.setInt(2, mentorId);
                ps.setInt(3, mentorId);
                ps.setInt(4, menteeId);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String senderName = dashboard.getUserName(rs.getInt("sender_id"));
                        String content = rs.getString("message_content");
                        boolean isResource = rs.getBoolean("is_resource");
                        int messageId = rs.getInt("id");

                        String displayMessage;

                        if (isResource) {
                            // Extract file name for display
                            String[] parts = content.split(":", 3);
                            String fileName = parts.length > 1 ? parts[1] : "Unknown File";
                            displayMessage = senderName + ": 📎 Shared Resource: " + fileName;

                            // Update the last resource ID for the 'View Last Resource' button
                            lastResourceMessageId = messageId;

                        } else {
                            displayMessage = senderName + ": " + content;
                        }
                        messages.add(displayMessage);
                    }
                }
            } catch (SQLException e) {
                System.err.println("Error polling chat history: " + e.getMessage());
                return;
            }

            // --- Synchronization Logic (Same as before) ---
            if (messages.size() > messageCount) {
                for (int i = messageCount; i < messages.size(); i++) {
                    chatArea.append(messages.get(i) + "\n");
                }
                messageCount = messages.size();
                chatArea.setCaretPosition(chatArea.getDocument().getLength());
            }
        }

        // --- NEW: View Last Resource Logic ---
        private void handleViewLastResource() {
            if (lastResourceMessageId == -1) {
                JOptionPane.showMessageDialog(this, "No resources shared in this session yet.", "View Resource", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // 1. Fetch the BLOB data and content using the stored message ID
            String sql = "SELECT resource_data, message_content FROM chat_messages WHERE id = ?";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, lastResourceMessageId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        byte[] fileBytes = rs.getBytes("resource_data");
                        String content = rs.getString("message_content");

                        // Extract file name and MIME type from content: FILE_RESOURCE:name:type
                        String[] parts = content.split(":", 3);
                        String fileName = parts.length > 1 ? parts[1] : "viewable_file";
                        String mimeType = parts.length > 2 ? parts[2] : "application/octet-stream";

                        // 2. Save the bytes to a temporary local file
                        // Use a safe file extension derived from the MIME type
                        String extension = mimeType.substring(mimeType.lastIndexOf('/') + 1);
                        if (extension.contains(";")) extension = extension.substring(0, extension.indexOf(";"));

                        java.io.File tempFile = java.io.File.createTempFile("temp_resource_", "." + extension);
                        tempFile.deleteOnExit(); // Ensure the file is cleaned up on exit

                        try (java.io.FileOutputStream fos = new java.io.FileOutputStream(tempFile)) {
                            fos.write(fileBytes);
                        }

                        // 3. Open the file using the system's default viewer
                        if (java.awt.Desktop.isDesktopSupported() && tempFile.exists()) {
                            java.awt.Desktop.getDesktop().open(tempFile);
                        } else {
                            JOptionPane.showMessageDialog(this, "Could not open file. Desktop is not supported.", "View Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            } catch (SQLException | java.io.IOException e) {
                System.err.println("Error viewing resource: " + e.getMessage());
                JOptionPane.showMessageDialog(this, "Error processing file view.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
// --- End ChatWindow Inner Class ---

    public void handleSessionEnd(String menteeName, String mentorName) {

        String sql = "UPDATE mentor_requests SET status = ?, session_end = ? " +
                "WHERE mentee_name = ? AND mentor_name = ? AND status IN ('Accepted', 'Active')";

        // Format the current time
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String currentTime = LocalDateTime.now().format(dtf);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "Completed");
            ps.setString(2, currentTime);
            ps.setString(3, menteeName);
            ps.setString(4, mentorName);

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Session between " + menteeName + " and " + mentorName + " marked as Completed.");
            } else {
                // If rowsAffected is 0, the session was likely already completed by the partner. Not an error.
                System.out.println("Session status already completed or not found as 'Active/Accepted'.");
            }

            // 2. Open the FeedbackWindow ONLY if the current user is the MENTEE
            if (loggedInUserRole.equalsIgnoreCase("Mentee")) {

                // --- Your Instantiation ---
                new FeedbackWindow(this, menteeName, mentorName).setVisible(true);

                // Remove the old placeholder dialog
                System.out.println("Mentee is calling the FeedbackWindow to provide feedback for " + mentorName);
            }

        } catch (SQLException e) {
            System.err.println("Database error ending session: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Database error: Could not end session status.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public int getUserId(String userName) {

        String sql = "SELECT id FROM register WHERE name = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, userName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            // Updated error message to reflect the correct table name
            System.err.println("Database Error fetching user ID for " + userName + ": " + e.getMessage());
        }
        return -1;
    }

    public String getUserName(int userId) {

        String sql = "SELECT name FROM register WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // FIX: Retrieve the name using the correct column name 'name'
                    return rs.getString("name");
                }
            }
        } catch (SQLException e) {
            System.err.println("Database Error fetching user name for ID " + userId + ": " + e.getMessage());
        }
        return "Unknown User";
    }

    // Inside Dashboard.java (New or updated method)

    public void clearSessionChatHistory(String menteeName, String mentorName) {
        // 1. Get IDs from names
        int menteeId = getUserId(menteeName);
        int mentorId = getUserId(mentorName);

        if (menteeId == -1 || mentorId == -1) {
            System.err.println("Cannot clear chat history: Invalid User IDs.");
            return;
        }

        // 2. Delete query using IDs
        String sql = "DELETE FROM chat_messages WHERE mentee_id = ? AND mentor_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, menteeId);
            ps.setInt(2, mentorId);

            // Ensure you also delete the reverse combination, if roles could be swapped in the DB
            // For simplicity, we stick to the mentee_id/mentor_id pattern defined above.

            int rowsAffected = ps.executeUpdate();
            System.out.println("Cleaned up " + rowsAffected + " old chat messages for session: " + menteeName + " - " + mentorName);

        } catch (SQLException e) {
            System.err.println("Error clearing chat history: " + e.getMessage());
        }
    }

    // Inside Dashboard.java (as a new public class or inner class)
    public class MentorActiveSessionsWindow extends JDialog {

        private final String mentorName;
        private final Dashboard parentDashboard;

        public MentorActiveSessionsWindow(Dashboard parent, String mentorName) {
            super(parent, "My Active Sessions", true);
            this.parentDashboard = parent;
            this.mentorName = mentorName;

            setSize(600, 450);
            setLocationRelativeTo(parent);
            setLayout(new BorderLayout());
            getContentPane().setBackground(ThemeManager.BG_DARK_MAIN);

            // Title setup
            JLabel titleLabel = new JLabel("Sessions with Accepted Requests", SwingConstants.CENTER);
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
            titleLabel.setForeground(ThemeManager.TEXT_LIGHT_PRIMARY);
            titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
            add(titleLabel, BorderLayout.NORTH);

            JPanel sessionsPanel = new JPanel();
            sessionsPanel.setLayout(new BoxLayout(sessionsPanel, BoxLayout.Y_AXIS));
            sessionsPanel.setBackground(ThemeManager.BG_DARK_MAIN);
            JScrollPane scrollPane = new JScrollPane(sessionsPanel);
            scrollPane.setBorder(null);
            scrollPane.getViewport().setBackground(ThemeManager.BG_DARK_MAIN);
            add(scrollPane, BorderLayout.CENTER);

            loadActiveSessions(sessionsPanel);

            // Close button
            JButton closeButton = new JButton("Close");
            // Reuse a style method like styleLogoutButton or styleSearchButton from Dashboard
            // Assuming styleLogoutButton is accessible or copied here
            closeButton.setBackground(ThemeManager.LOGOUT_RED);
            closeButton.setForeground(Color.WHITE);
            closeButton.addActionListener(e -> dispose());

            JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            bottomPanel.setBackground(ThemeManager.BG_DARK_MAIN);
            bottomPanel.add(closeButton);
            add(bottomPanel, BorderLayout.SOUTH);
        }

        private void loadActiveSessions(JPanel panel) {
            // Query to get ACCEPTED requests for the current mentor
            String sql = "SELECT mentee_name, session_start, session_end, request_date FROM mentor_requests WHERE mentor_name = ? AND status = 'Accepted' ORDER BY request_date DESC";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, mentorName);
                try (ResultSet rs = ps.executeQuery()) {
                    boolean found = false;
                    while (rs.next()) {
                        found = true;
                        String mentee = rs.getString("mentee_name");
                        String date = rs.getTimestamp("request_date").toString().split("\\.")[0];
                        String startTime = rs.getString("session_start");
                        String endTime = rs.getString("session_end");

                        panel.add(createSessionCard(mentee, date, startTime, endTime));
                        panel.add(Box.createVerticalStrut(10));
                    }

                    if (!found) {
                        JLabel noSessions = new JLabel("You have no accepted sessions scheduled yet.", SwingConstants.CENTER);
                        noSessions.setForeground(ThemeManager.TEXT_LIGHT_SECONDARY);
                        noSessions.setAlignmentX(Component.CENTER_ALIGNMENT);
                        panel.add(Box.createVerticalGlue());
                        panel.add(noSessions);
                        panel.add(Box.createVerticalGlue());
                    }
                }
            } catch (SQLException e) {
                System.err.println("Error loading active mentor sessions: " + e.getMessage());
                JOptionPane.showMessageDialog(this, "Failed to load active sessions.", "DB Error", JOptionPane.ERROR_MESSAGE);
            }
            panel.revalidate();
            panel.repaint();
        }

        private JPanel createSessionCard(String menteeName, String date, String startTime, String endTime) {
            JPanel card = new JPanel(new BorderLayout(15, 0));
            card.setBackground(ThemeManager.BG_DARK_CARD);
            card.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
            card.setMaximumSize(new Dimension(550, 90));

            // Mentee and Date Info
            JPanel infoPanel = new JPanel(new GridLayout(3, 1));
            infoPanel.setOpaque(false);

            JLabel menteeLabel = new JLabel("Mentee: " + menteeName);
            menteeLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            menteeLabel.setForeground(ThemeManager.TEXT_LIGHT_PRIMARY);

            JLabel scheduleLabel = new JLabel("Time: " + startTime + " - " + endTime);
            scheduleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            scheduleLabel.setForeground(ThemeManager.TEXT_AVAILABLE);

            JLabel dateLabel = new JLabel("Accepted On: " + date);
            dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            dateLabel.setForeground(ThemeManager.TEXT_LIGHT_SECONDARY);

            infoPanel.add(menteeLabel);
            infoPanel.add(scheduleLabel);
            infoPanel.add(dateLabel);

            // Start Chat Button
            JButton chatButton = new JButton("Start Chat");
            chatButton.setBackground(ThemeManager.ACCENT_PURPLE);
            chatButton.setForeground(Color.WHITE);
            chatButton.setFont(new Font("Segoe UI", Font.BOLD, 14));

            chatButton.addActionListener(e -> {
                // Mentor starts the chat (Mentor is loggedInUserName)
                parentDashboard.new ChatWindow(parentDashboard, mentorName, menteeName).setVisible(true);
                dispose();
            });

            card.add(infoPanel, BorderLayout.WEST);
            card.add(chatButton, BorderLayout.EAST);

            return card;
        }
    }
    // --- Main method (for quick testing) ---
    public static void main(String[] args) {
        // Run as a Mentee to see mentor cards and the 'Send Request' button
        // To test as a Mentor, change the name to a registered mentor (e.g., "Kavitha") and the role to "Mentor"
        SwingUtilities.invokeLater(() -> new Dashboard("TestMentee", "Mentee", null).setVisible(true));
    }
}
