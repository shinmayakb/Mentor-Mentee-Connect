package org.example;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class Register extends JFrame {

    // Removed the DB_HOST, DB_PORT, etc. constants.
    // This class will now rely solely on DatabaseConnection.getConnection().

    private JTextField nameField, emailField, phoneField, othersField;
    private JPasswordField passwordField;
    private JRadioButton maleRadio, femaleRadio;
    private JComboBox<String> qualificationCombo;
    private JCheckBox aiCB, mlCB, dataCB, cloudCB, promptCB, cyberCB, nosqlCB, sqlCB, softwareCB;
    private JCheckBox mentorCheck, menteeCheck;

    public Register() {
        setTitle("Mentor Connect - Register");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);

        JPanel contentWrapper = new JPanel(new BorderLayout());
        JPanel topBar = createTopBar();
        contentWrapper.add(topBar, BorderLayout.NORTH);

        JPanel formPanel = createFormPanel();
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        contentWrapper.add(scrollPane, BorderLayout.CENTER);
        add(contentWrapper);
    }

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(40, 40, 60));
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JButton backButton = new JButton("← Back to Login");
        backButton.setFont(new Font("Arial", Font.BOLD, 18));
        backButton.setBackground(new Color(150, 150, 255));
        backButton.setForeground(Color.WHITE);
        backButton.setFocusPainted(false);
        backButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        backButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        backButton.addActionListener(e -> {
            this.dispose();
            // Assuming Login class is defined and starts the application flow
            try {
                new Login().setVisible(true);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Login frame not available.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        topBar.add(backButton, BorderLayout.WEST);

        JLabel titleLabel = new JLabel("User Registration Form", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        topBar.add(titleLabel, BorderLayout.CENTER);
        topBar.add(new JPanel(){{setOpaque(false); setPreferredSize(new Dimension(180, 1));}}, BorderLayout.EAST);

        return topBar;
    }

    private JPanel createFormPanel() {
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                int w = getWidth();
                int h = getHeight();
                Color color1 = new Color(253, 79, 165);
                Color color2 = new Color(138, 43, 226);
                GradientPaint gp = new GradientPaint(0, 0, color1, w, h, color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Font labelFont = new Font("Arial", Font.BOLD, 22);
        Font fieldFont = new Font("Arial", Font.PLAIN, 20);

        int row = 0;

        // Role Selection
        gbc.gridx = 0; gbc.gridy = row++;
        mainPanel.add(createLabel("Register As:", labelFont), gbc);
        mentorCheck = new JCheckBox("Mentor");
        menteeCheck = new JCheckBox("Mentee");
        mentorCheck.setFont(fieldFont);
        menteeCheck.setFont(fieldFont);
        mentorCheck.setForeground(Color.WHITE);
        menteeCheck.setForeground(Color.WHITE);
        mentorCheck.setOpaque(false);
        menteeCheck.setOpaque(false);

        ButtonGroup roleGroup = new ButtonGroup();
        roleGroup.add(mentorCheck);
        roleGroup.add(menteeCheck);

        JPanel rolePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        rolePanel.setOpaque(false);
        rolePanel.add(mentorCheck);
        rolePanel.add(menteeCheck);
        gbc.gridx = 1; gbc.gridy = row-1; gbc.gridwidth = 3;
        mainPanel.add(rolePanel, gbc);
        gbc.gridwidth = 1;

        // Name, Password, Email fields
        gbc.gridx = 0; gbc.gridy = row++;
        mainPanel.add(createLabel("User Name:", labelFont), gbc);
        nameField = new JTextField(20);
        nameField.setFont(fieldFont);
        gbc.gridx = 1; gbc.gridy = row-1; gbc.gridwidth = 3;
        mainPanel.add(nameField, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0; gbc.gridy = row++;
        mainPanel.add(createLabel("Password:", labelFont), gbc);
        passwordField = new JPasswordField(20);
        passwordField.setFont(fieldFont);
        gbc.gridx = 1; gbc.gridy = row-1; gbc.gridwidth = 3;
        mainPanel.add(passwordField, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0; gbc.gridy = row++;
        mainPanel.add(createLabel("Email ID:", labelFont), gbc);
        emailField = new JTextField(20);
        emailField.setFont(fieldFont);
        emailField.setToolTipText("example@domain.com");
        gbc.gridx = 1; gbc.gridy = row-1; gbc.gridwidth = 3;
        mainPanel.add(emailField, gbc);
        gbc.gridwidth = 1;

        // Phone Number field (with constraints)
        gbc.gridx = 0; gbc.gridy = row++;
        mainPanel.add(createLabel("Phone No:", labelFont), gbc);
        phoneField = new JTextField(20);
        phoneField.setFont(fieldFont);

        // --- IMPLEMENTATION OF PHONE NUMBER CONSTRAINTS ---
        phoneField.setDocument(new PlainDocument() {
            @Override
            public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
                if (str == null) return;
                String currentText = getText(0, getLength());
                String resultingText = currentText.substring(0, offset) + str + currentText.substring(offset);

                if (resultingText.matches("\\d*") && resultingText.length() <= 10) {
                    super.insertString(offset, str, attr);
                } else if (!resultingText.matches("\\d*")) {
                    Toolkit.getDefaultToolkit().beep();
                }
            }
        });
        // --- END PHONE NUMBER CONSTRAINTS ---

        phoneField.setToolTipText("Enter exactly 10 digits");
        gbc.gridx = 1; gbc.gridy = row-1; gbc.gridwidth = 3;
        mainPanel.add(phoneField, gbc);
        gbc.gridwidth = 1;

        // Gender, Qualification, Expertise, Others fields
        gbc.gridx = 0; gbc.gridy = row++;
        mainPanel.add(createLabel("Gender:", labelFont), gbc);
        maleRadio = new JRadioButton("Male");
        femaleRadio = new JRadioButton("Female");

        maleRadio.setOpaque(false);
        femaleRadio.setOpaque(false);
        maleRadio.setFont(fieldFont);
        femaleRadio.setFont(fieldFont);
        maleRadio.setForeground(Color.WHITE);
        femaleRadio.setForeground(Color.WHITE);

        ButtonGroup genderGroup = new ButtonGroup();
        genderGroup.add(maleRadio);
        genderGroup.add(femaleRadio);

        JPanel genderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        genderPanel.setOpaque(false);
        genderPanel.add(maleRadio);
        genderPanel.add(femaleRadio);
        gbc.gridx = 1; gbc.gridy = row-1; gbc.gridwidth = 3;
        mainPanel.add(genderPanel, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0; gbc.gridy = row++;
        mainPanel.add(createLabel("Qualification:", labelFont), gbc);
        String[] qualifications = {"High School", "Diploma", "Bachelor", "Master", "PhD"};
        qualificationCombo = new JComboBox<>(qualifications);
        qualificationCombo.setFont(fieldFont);
        gbc.gridx = 1; gbc.gridy = row-1; gbc.gridwidth = 3;
        mainPanel.add(qualificationCombo, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0; gbc.gridy = row++;
        mainPanel.add(createLabel("Expertise:", labelFont), gbc);
        JPanel expertisePanel = new JPanel(new GridLayout(3, 3, 10, 10));
        expertisePanel.setOpaque(false);

        aiCB = new JCheckBox("AI"); mlCB = new JCheckBox("Machine Learning"); dataCB = new JCheckBox("Data Analysis");
        cloudCB = new JCheckBox("Cloud Computing"); promptCB = new JCheckBox("Prompt Engineering"); cyberCB = new JCheckBox("Cybersecurity");
        nosqlCB = new JCheckBox("No SQL"); sqlCB = new JCheckBox("SQL"); softwareCB = new JCheckBox("Software Engineering");

        JCheckBox[] checkboxes = {aiCB, mlCB, dataCB, cloudCB, promptCB, cyberCB, nosqlCB, sqlCB, softwareCB};
        for (JCheckBox cb : checkboxes) {
            cb.setFont(new Font("Arial", Font.PLAIN, 18));
            cb.setForeground(Color.WHITE);
            cb.setOpaque(false);
            expertisePanel.add(cb);
        }

        gbc.gridx = 1; gbc.gridy = row-1; gbc.gridwidth = 3;
        mainPanel.add(expertisePanel, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0; gbc.gridy = row++;
        mainPanel.add(createLabel("Others (Description):", labelFont), gbc);
        othersField = new JTextField(20);
        othersField.setFont(fieldFont);
        othersField.setToolTipText("Enter a brief description (maps to 'description' column).");
        gbc.gridx = 1; gbc.gridy = row-1; gbc.gridwidth = 3;
        mainPanel.add(othersField, gbc);
        gbc.gridwidth = 1;

        JButton registerBtn = new JButton("Register");
        registerBtn.setFont(new Font("Arial", Font.BOLD, 24));
        registerBtn.setBackground(new Color(50, 205, 50));
        registerBtn.setForeground(Color.WHITE);
        gbc.gridx = 1; gbc.gridy = row++; gbc.gridwidth = 2;
        mainPanel.add(registerBtn, gbc);

        registerBtn.addActionListener(e -> registerUser());

        return mainPanel;
    }

    private JLabel createLabel(String text, Font font) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(Color.WHITE);
        return label;
    }

    // --- Database Interaction Logic (Uses DatabaseConnection) ---
    private void registerUser() {
        // --- Validation ---
        String name = nameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();

        if ((!mentorCheck.isSelected() && !menteeCheck.isSelected()) ||
                name.isEmpty() || password.isEmpty() || email.isEmpty() || phone.isEmpty() ||
                (!maleRadio.isSelected() && !femaleRadio.isSelected())) {

            JOptionPane.showMessageDialog(null, "Please fill in all mandatory fields (Role, Name, Password, Email, Phone, Gender).", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (phone.length() != 10) {
            JOptionPane.showMessageDialog(null, "Phone number must be exactly 10 digits.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // --- Data Collection ---
        String role = mentorCheck.isSelected() ? "Mentor" : "Mentee";
        String gender = maleRadio.isSelected() ? "Male" : "Female";
        String qualification = (String) qualificationCombo.getSelectedItem();
        String description = othersField.getText().trim();

        StringBuilder expertise = new StringBuilder();
        JCheckBox[] checkboxes = {aiCB, mlCB, dataCB, cloudCB, promptCB, cyberCB, nosqlCB, sqlCB, softwareCB};
        for (JCheckBox cb : checkboxes) {
            if (cb.isSelected()) expertise.append(cb.getText()).append(", ");
        }
        String expertiseString = expertise.length() > 0 ? expertise.substring(0, expertise.length() - 2) : null;


        // --- Prepare SQL and Defaults ---
        String sql = "INSERT INTO register (role, name, password, email, phone, gender, qualification, expertise, description, status, schedule) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        String status = "Mentee".equals(role) ? "Active" : "Available";
        String schedule = null;

        // Mentee logic: Don't insert mentor-specific fields
        if ("Mentee".equals(role)) {
            expertiseString = null;
            description = null;
        }

        // Use try-with-resources for the connection, relying on the separate DatabaseConnection class
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, role);
            stmt.setString(2, name);
            // WARNING: You should hash the password before inserting!
            stmt.setString(3, password);
            stmt.setString(4, email);
            stmt.setString(5, phone);
            stmt.setString(6, gender);
            stmt.setString(7, qualification);

            // Set expertise (NULL if Mentee)
            stmt.setString(8, expertiseString);

            // Set description (NULL if Mentee)
            stmt.setString(9, description);

            // Set status
            stmt.setString(10, status);

            // Set schedule (NULL)
            stmt.setString(11, schedule);

            stmt.executeUpdate();

            JOptionPane.showMessageDialog(null, "Registration Successful! Please proceed to login.");

            // --- Field Reset ---
            nameField.setText("");
            passwordField.setText("");
            emailField.setText("");
            phoneField.setText("");
            othersField.setText("");
            qualificationCombo.setSelectedIndex(0);

            // Clear all selection groups
            ButtonGroup roleGroup = new ButtonGroup(); roleGroup.add(mentorCheck); roleGroup.add(menteeCheck); roleGroup.clearSelection();
            ButtonGroup genderGroup = new ButtonGroup(); genderGroup.add(maleRadio); genderGroup.add(femaleRadio); genderGroup.clearSelection();
            for (JCheckBox cb : checkboxes) { cb.setSelected(false); }

        } catch (SQLException ex) {
            // Error handling, checking for unique email violation
            if (ex.getSQLState().equals("23000") && ex.getMessage().contains("email")) {
                JOptionPane.showMessageDialog(null, "Registration Failed: This email is already registered.", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Register().setVisible(true));
    }
}