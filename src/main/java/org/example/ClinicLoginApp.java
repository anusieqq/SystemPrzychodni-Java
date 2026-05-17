package org.example;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class ClinicLoginApp {

    private JFrame frame;
    private JPanel mainPanel;
    private CardLayout cardLayout;
    private User currentUser;
    private DataManager dataManager;
    private Map<String, JPanel> panels = new HashMap<>();
    private String userType = "";
    private List<AvailableAppointment> availableAppointments = new ArrayList<>();
    private List<AvailableAppointment> availableLaboratoryTests = new ArrayList<>();
    private List<AvailableAppointment> availableProcedures = new ArrayList<>();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ClinicLoginApp().createGUI());
    }

    private void createGUI() {
        dataManager = new DataManager();
        dataManager.loadAllData();

        frame = new JFrame("System Opieki Zdrowotnej");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1300, 900);
        frame.setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.add(roleSelectionPanel(), "RoleSelection");
        mainPanel.add(loginPanel(), "Login");

        frame.add(mainPanel);
        cardLayout.show(mainPanel, "RoleSelection");
        frame.setVisible(true);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (dataManager != null) {
                dataManager.saveAllData();
            }
        }));
    }

    // ==================== METODY POMOCNICZE ====================

    private JPanel createHeaderPanel(String title, String backTarget) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(70, 130, 200));
        header.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JButton backBtn = new JButton("◀ Powrót");
        backBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        backBtn.setBackground(new Color(100, 150, 200));
        backBtn.setForeground(Color.WHITE);
        backBtn.setFocusPainted(false);
        backBtn.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        backBtn.addActionListener(e -> {
            if (backTarget.equals("Logout")) {
                logout();
            } else if (backTarget.equals("RoleSelection")) {
                userType = "";
                cardLayout.show(mainPanel, "RoleSelection");
            } else {
                cardLayout.show(mainPanel, backTarget);
            }
        });

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        header.add(backBtn, BorderLayout.WEST);
        header.add(titleLabel, BorderLayout.CENTER);

        JPanel emptyPanel = new JPanel();
        emptyPanel.setOpaque(false);
        emptyPanel.setPreferredSize(new Dimension(80, 40));
        header.add(emptyPanel, BorderLayout.EAST);

        return header;
    }

    private JPanel createStyledPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));
        return panel;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.darker());
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }

    private JPanel createSearchPanel(String placeholder, JPanel targetPanel, Consumer<String> searchCallback) {
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel searchIcon = new JLabel("🔍 ");
        searchIcon.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JTextField searchField = new JTextField();
        searchField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        searchField.setToolTipText(placeholder);

        searchField.setText(placeholder);
        searchField.setForeground(Color.GRAY);
        searchField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (searchField.getText().equals(placeholder)) {
                    searchField.setText("");
                    searchField.setForeground(Color.BLACK);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setText(placeholder);
                    searchField.setForeground(Color.GRAY);
                }
            }
        });

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { searchCallback.accept(searchField.getText()); }
            @Override public void removeUpdate(DocumentEvent e) { searchCallback.accept(searchField.getText()); }
            @Override public void changedUpdate(DocumentEvent e) { searchCallback.accept(searchField.getText()); }
        });

        JButton clearBtn = new JButton("✖");
        clearBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        clearBtn.setBackground(new Color(200, 200, 200));
        clearBtn.setForeground(Color.BLACK);
        clearBtn.setFocusPainted(false);
        clearBtn.addActionListener(e -> {
            searchField.setText(placeholder);
            searchField.setForeground(Color.GRAY);
            searchCallback.accept("");
        });

        searchPanel.add(searchIcon, BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(clearBtn, BorderLayout.EAST);

        return searchPanel;
    }

    private boolean validatePesel(String pesel) {
        if (pesel == null || !pesel.matches("\\d{11}")) {
            return false;
        }
        // Sprawdzenie sumy kontrolnej PESEL
        int[] weights = {1, 3, 7, 9, 1, 3, 7, 9, 1, 3};
        int sum = 0;
        for (int i = 0; i < 10; i++) {
            sum += Integer.parseInt(pesel.substring(i, i + 1)) * weights[i];
        }
        int checksum = (10 - (sum % 10)) % 10;
        return checksum == Integer.parseInt(pesel.substring(10, 11));
    }

    private boolean validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) return true;
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return Pattern.matches(emailRegex, email);
    }

    private boolean validatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) return true;
        String phoneRegex = "^[0-9\\-\\+]{9,15}$";
        return Pattern.matches(phoneRegex, phone);
    }

    private JPanel roleSelectionPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(240, 248, 255));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 200), 2),
                BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));

        GridBagConstraints cardGbc = new GridBagConstraints();
        cardGbc.insets = new Insets(15, 15, 15, 15);

        JLabel title = new JLabel("System Opieki Zdrowotnej");
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setForeground(new Color(70, 130, 200));
        cardGbc.gridx = 0;
        cardGbc.gridy = 0;
        cardGbc.gridwidth = 2;
        card.add(title, cardGbc);

        JButton patientBtn = createStyledButton("👤 Pacjent", new Color(70, 130, 200));
        JButton doctorBtn = createStyledButton("👨‍⚕️ Lekarz", new Color(70, 130, 200));
        JButton receptionistBtn = createStyledButton("📋 Recepcjonista", new Color(70, 130, 200));
        JButton adminBtn = createStyledButton("🔧 Administrator", new Color(70, 130, 200));

        patientBtn.addActionListener(e -> {
            userType = "PATIENT";
            cardLayout.show(mainPanel, "Login");
        });

        doctorBtn.addActionListener(e -> {
            userType = "DOCTOR";
            cardLayout.show(mainPanel, "Login");
        });

        receptionistBtn.addActionListener(e -> {
            userType = "RECEPTIONIST";
            cardLayout.show(mainPanel, "Login");
        });

        adminBtn.addActionListener(e -> {
            userType = "ADMIN";
            cardLayout.show(mainPanel, "Login");
        });

        cardGbc.gridy = 1;
        cardGbc.gridwidth = 2;
        card.add(patientBtn, cardGbc);
        cardGbc.gridy = 2;
        card.add(doctorBtn, cardGbc);
        cardGbc.gridy = 3;
        card.add(receptionistBtn, cardGbc);
        cardGbc.gridy = 4;
        card.add(adminBtn, cardGbc);

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(card, gbc);

        return panel;
    }

    private JPanel loginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(240, 248, 255));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JPanel loginCard = new JPanel(new GridBagLayout());
        loginCard.setBackground(Color.WHITE);
        loginCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 200), 2),
                BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));

        GridBagConstraints cardGbc = new GridBagConstraints();
        cardGbc.insets = new Insets(10, 15, 10, 15);

        String titleText = "";
        if (userType.equals("PATIENT")) titleText = "Panel Pacjenta - Logowanie";
        else if (userType.equals("RECEPTIONIST")) titleText = "Panel Recepcjonisty - Logowanie";
        else if (userType.equals("DOCTOR")) titleText = "Panel Lekarza - Logowanie";
        else if (userType.equals("ADMIN")) titleText = "Panel Administratora - Logowanie";

        JLabel titleLabel = new JLabel(titleText);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(new Color(70, 130, 200));
        cardGbc.gridx = 0;
        cardGbc.gridy = 0;
        cardGbc.gridwidth = 2;
        loginCard.add(titleLabel, cardGbc);

        JLabel userLabel = new JLabel("Login:");
        userLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        cardGbc.gridy = 1;
        cardGbc.gridwidth = 1;
        loginCard.add(userLabel, cardGbc);

        JTextField userField = new JTextField(15);
        userField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        cardGbc.gridx = 1;
        loginCard.add(userField, cardGbc);

        JLabel passLabel = new JLabel("Hasło:");
        cardGbc.gridx = 0;
        cardGbc.gridy = 2;
        loginCard.add(passLabel, cardGbc);

        JPasswordField passField = new JPasswordField(15);
        passField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        cardGbc.gridx = 1;
        loginCard.add(passField, cardGbc);

        JButton loginBtn = createStyledButton("Zaloguj się", new Color(70, 130, 200));
        cardGbc.gridx = 0;
        cardGbc.gridy = 3;
        cardGbc.gridwidth = 2;
        loginCard.add(loginBtn, cardGbc);

        JButton backBtn = new JButton("◀ Powrót do wyboru roli");
        backBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        backBtn.setBackground(new Color(150, 150, 150));
        backBtn.setForeground(Color.WHITE);
        backBtn.setFocusPainted(false);
        cardGbc.gridy = 4;
        loginCard.add(backBtn, cardGbc);

        // Panel z przykładowymi kontami - UKRYTY
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(new Color(255, 255, 200));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Konta testowe"));
        infoPanel.setVisible(false); // UKRYCIE PANELU Z HASŁAMI

        cardGbc.gridy = 5;
        loginCard.add(infoPanel, cardGbc);

        ActionListener loginAction = e -> {
            String login = userField.getText();
            String password = new String(passField.getPassword());

            if (userType.equals("PATIENT")) {
                Patient patient = dataManager.getPatient(login);
                if (patient != null && patient.getPassword().equals(password)) {
                    currentUser = patient;
                    initializePanelsAfterLogin();
                    refreshDashboard();
                    cardLayout.show(mainPanel, "Dashboard");
                } else {
                    JOptionPane.showMessageDialog(frame, "Nieprawidłowy login lub hasło!", "Błąd", JOptionPane.ERROR_MESSAGE);
                }
            } else if (userType.equals("RECEPTIONIST")) {
                Receptionist receptionist = dataManager.getReceptionist(login);
                if (receptionist != null && receptionist.getPassword().equals(password)) {
                    currentUser = receptionist;
                    initializeReceptionistPanels();
                    refreshReceptionistDashboard();
                    cardLayout.show(mainPanel, "ReceptionistDashboard");
                } else {
                    JOptionPane.showMessageDialog(frame, "Nieprawidłowy login lub hasło!", "Błąd", JOptionPane.ERROR_MESSAGE);
                }
            } else if (userType.equals("DOCTOR")) {
                Doctor doctor = dataManager.getDoctor(login);
                if (doctor != null && doctor.getPassword().equals(password)) {
                    currentUser = doctor;
                    initializeDoctorPanels();
                    refreshDoctorDashboard();
                    cardLayout.show(mainPanel, "DoctorDashboard");
                } else {
                    JOptionPane.showMessageDialog(frame, "Nieprawidłowy login lub hasło!", "Błąd", JOptionPane.ERROR_MESSAGE);
                }
            } else if (userType.equals("ADMIN")) {
                Admin admin = dataManager.getAdmin(login);
                if (admin != null && admin.getPassword().equals(password)) {
                    currentUser = admin;
                    initializeAdminPanels();
                    refreshAdminDashboard();
                    cardLayout.show(mainPanel, "AdminDashboard");
                } else {
                    JOptionPane.showMessageDialog(frame, "Nieprawidłowy login lub hasło!", "Błąd", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        userField.addActionListener(loginAction);
        passField.addActionListener(loginAction);
        loginBtn.addActionListener(loginAction);

        backBtn.addActionListener(e -> {
            userType = "";
            cardLayout.show(mainPanel, "RoleSelection");
        });

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(loginCard, gbc);

        return panel;
    }

    // ==================== INICJALIZACJA PANELI ====================

    private void initializeAvailableAppointments() {
        availableAppointments.clear();
        availableLaboratoryTests.clear();
        availableProcedures.clear();

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        LocalDate today = LocalDate.now();

        String[] nfzHours = {"09:00", "10:00", "11:00", "12:00", "14:00"};
        String[] privateHours = {"08:00", "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00"};

        for (int i = 1; i <= 21; i++) {
            LocalDate date = today.plusDays(i);
            if (date.getDayOfWeek().getValue() >= 6) continue;

            String dateStr = date.format(dateFormatter);

            for (Doctor doctor : dataManager.getDoctors()) {
                for (String hour : nfzHours) {
                    availableAppointments.add(new AvailableAppointment(
                            doctor.getId(), doctor.getName(), doctor.getSpecialization(),
                            dateStr, hour, true, "VISIT", "NFZ"
                    ));
                }
                for (String hour : privateHours) {
                    availableAppointments.add(new AvailableAppointment(
                            doctor.getId(), doctor.getName(), doctor.getSpecialization(),
                            dateStr, hour, true, "VISIT", "PRIVATE"
                    ));
                }
            }

            String[] labTests = {"Pobranie krwi", "EKG", "USG", "Holter EKG", "RTG klatki piersiowej"};
            for (String test : labTests) {
                for (String hour : nfzHours) {
                    availableLaboratoryTests.add(new AvailableAppointment(
                            0, test, "Badanie laboratoryjne", dateStr, hour, true, "LAB", "NFZ"
                    ));
                }
                for (String hour : privateHours) {
                    availableLaboratoryTests.add(new AvailableAppointment(
                            0, test, "Badanie laboratoryjne", dateStr, hour, true, "LAB", "PRIVATE"
                    ));
                }
            }

            String[] procedures = {"Zastrzyk domięśniowy", "Szczepienie", "Usunięcie szwów", "Opracowanie rany", "Iniekcja dostawowa"};
            for (String proc : procedures) {
                for (String hour : nfzHours) {
                    availableProcedures.add(new AvailableAppointment(
                            0, proc, "Zabieg ambulatoryjny", dateStr, hour, true, "PROC", "NFZ"
                    ));
                }
                for (String hour : privateHours) {
                    availableProcedures.add(new AvailableAppointment(
                            0, proc, "Zabieg ambulatoryjny", dateStr, hour, true, "PROC", "PRIVATE"
                    ));
                }
            }
        }

        markOccupiedAppointments();
    }

    private void markOccupiedAppointments() {
        for (Patient patient : dataManager.getAllPatients().values()) {
            for (Visit visit : patient.getRegisteredVisits()) {
                String[] dateTimeParts = visit.getDateTime().split(" ");
                if (dateTimeParts.length >= 2) {
                    String visitDate = dateTimeParts[0];
                    String visitHour = dateTimeParts[1];
                    String paymentCode = visit.getPaymentStatus().equals("NFZ") ? "NFZ" : "PRIVATE";

                    if (visit.getType().equals("VISIT")) {
                        for (AvailableAppointment app : availableAppointments) {
                            if (app.getDoctorName().equals(visit.getDoctorName()) &&
                                    app.getDate().equals(visitDate) &&
                                    app.getHour().equals(visitHour) &&
                                    app.getPaymentType().equals(paymentCode)) {
                                app.setAvailable(false);
                                break;
                            }
                        }
                    } else if (visit.getType().equals("LAB")) {
                        for (AvailableAppointment app : availableLaboratoryTests) {
                            if (app.getDoctorName().equals(visit.getDoctorName()) &&
                                    app.getDate().equals(visitDate) &&
                                    app.getHour().equals(visitHour) &&
                                    app.getPaymentType().equals(paymentCode)) {
                                app.setAvailable(false);
                                break;
                            }
                        }
                    } else if (visit.getType().equals("PROC")) {
                        for (AvailableAppointment app : availableProcedures) {
                            if (app.getDoctorName().equals(visit.getDoctorName()) &&
                                    app.getDate().equals(visitDate) &&
                                    app.getHour().equals(visitHour) &&
                                    app.getPaymentType().equals(paymentCode)) {
                                app.setAvailable(false);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    private void initializePanelsAfterLogin() {
        if (panels.isEmpty()) {
            initializeAvailableAppointments();
            panels.put("Dashboard", patientDashboard());
            panels.put("VisitMenu", visitMenuPanel());
            panels.put("RegisterService", registerServicePanel());
            panels.put("RescheduleVisit", rescheduleVisitPanel());
            panels.put("CancelVisit", cancelVisitPanel());
            panels.put("Prescriptions", prescriptionsPanel());
            panels.put("History", historyPanel());
            panels.put("Referrals", referralsPanel());
            panels.put("DoctorsList", doctorsListPanel());
            panels.put("SickLeave", sickLeavePanel());
            panels.put("TestResults", testResultsPanel());

            for (Map.Entry<String, JPanel> entry : panels.entrySet()) {
                mainPanel.add(entry.getValue(), entry.getKey());
            }
        }
    }

    private void initializeReceptionistPanels() {
        if (panels.isEmpty()) {
            initializeAvailableAppointments();
            panels.put("ReceptionistDashboard", receptionistDashboard());
            panels.put("ManagePatients", managePatientsPanel());
            panels.put("RegisterPatient", registerPatientPanel());
            panels.put("ReceptionistRegisterService", receptionistRegisterServicePanel());
            panels.put("ReceptionistRescheduleVisit", receptionistRescheduleVisitPanel());
            panels.put("ReceptionistCancelVisit", receptionistCancelVisitPanel());
            panels.put("AllVisitsList", allVisitsListPanel());

            for (Map.Entry<String, JPanel> entry : panels.entrySet()) {
                mainPanel.add(entry.getValue(), entry.getKey());
            }
        }
    }

    private void initializeDoctorPanels() {
        if (panels.isEmpty()) {
            initializeAvailableAppointments();
            panels.put("DoctorDashboard", doctorDashboard());
            panels.put("DoctorMyVisits", doctorMyVisitsPanel());
            panels.put("DoctorCompletedVisits", doctorCompletedVisitsPanel());
            panels.put("DoctorAddResults", doctorAddResultsPanel());
            panels.put("DoctorAddPrescription", doctorAddPrescriptionPanel());
            panels.put("DoctorAddReferral", doctorAddReferralPanel());
            panels.put("DoctorAddSickLeave", doctorAddSickLeavePanel());

            for (Map.Entry<String, JPanel> entry : panels.entrySet()) {
                mainPanel.add(entry.getValue(), entry.getKey());
            }
        }
    }

    private void initializeAdminPanels() {
        if (panels.isEmpty()) {
            panels.put("AdminDashboard", adminDashboard());
            panels.put("AdminManageDoctors", adminManageDoctorsPanel());
            panels.put("AdminManageReceptionists", adminManageReceptionistsPanel());
            panels.put("AdminManagePatients", adminManagePatientsPanel());
            panels.put("AdminManageSchedule", adminManageSchedulePanel());

            for (Map.Entry<String, JPanel> entry : panels.entrySet()) {
                mainPanel.add(entry.getValue(), entry.getKey());
            }
        }
    }

    // ==================== PANEL LEKARZA ====================

    private JPanel doctorDashboard() {
        JPanel panel = createStyledPanel("Panel Lekarza");
        panel.add(createHeaderPanel("Panel Lekarza - Dashboard", "Logout"), BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel welcomeLabel = new JLabel("Witaj, " + currentUser.getName() + "!");
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        welcomeLabel.setForeground(new Color(70, 130, 200));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        contentPanel.add(welcomeLabel, gbc);

        String[][] buttons = {
                {"📋 Moje wizyty (aktywne)", "DoctorMyVisits"},
                {"✅ Zakończone wizyty", "DoctorCompletedVisits"},
                {"📊 Dodaj wyniki badań", "DoctorAddResults"},
                {"💊 Wystaw receptę", "DoctorAddPrescription"},
                {"📄 Wystaw skierowanie", "DoctorAddReferral"},
                {"📋 Wystaw zwolnienie", "DoctorAddSickLeave"}
        };

        int row = 1;
        for (String[] btn : buttons) {
            JButton button = createStyledButton(btn[0], new Color(70, 130, 200));
            button.setPreferredSize(new Dimension(300, 50));
            button.addActionListener(e -> {
                refreshDoctorPanel(btn[1]);
                cardLayout.show(mainPanel, btn[1]);
            });
            gbc.gridy = row;
            contentPanel.add(button, gbc);
            row++;
        }

        panel.add(contentPanel, BorderLayout.CENTER);

        JPanel statsPanel = createDoctorStatsPanel();
        panel.add(statsPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createDoctorStatsPanel() {
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Statystyki"),
                BorderFactory.createEmptyBorder(10, 15, 15, 15)
        ));
        statsPanel.setBackground(new Color(248, 248, 255));
        statsPanel.setPreferredSize(new Dimension(300, 400));

        Doctor doctor = (Doctor) currentUser;
        int activeVisits = 0;
        int completedVisits = 0;

        for (Patient patient : dataManager.getAllPatients().values()) {
            for (Visit visit : patient.getRegisteredVisits()) {
                if (visit.getDoctorName().equals(doctor.getName())) {
                    activeVisits++;
                }
            }
            for (Visit visit : patient.getHistory()) {
                if (visit.getDoctorName().equals(doctor.getName())) {
                    completedVisits++;
                }
            }
        }

        statsPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        statsPanel.add(new JLabel("👨‍⚕️ " + doctor.getName()));
        statsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        statsPanel.add(new JLabel("🔬 Specjalizacja: " + doctor.getSpecialization()));
        statsPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        statsPanel.add(new JLabel("📋 Aktywne wizyty: " + activeVisits));
        statsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        statsPanel.add(new JLabel("✅ Zakończone wizyty: " + completedVisits));
        statsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        statsPanel.add(new JLabel("⏰ Godziny przyjęć: " + doctor.getHours()));

        return statsPanel;
    }

    private JPanel doctorMyVisitsPanel() {
        JPanel panel = createStyledPanel("Moje wizyty");
        panel.add(createHeaderPanel("Moje wizyty (aktywne)", "DoctorDashboard"), BorderLayout.NORTH);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        Doctor doctor = (Doctor) currentUser;
        List<Patient> patientsWithVisits = new ArrayList<>();
        Map<Patient, List<Visit>> patientVisitsMap = new HashMap<>();

        for (Patient patient : dataManager.getAllPatients().values()) {
            List<Visit> visits = new ArrayList<>();
            for (Visit visit : patient.getRegisteredVisits()) {
                if (visit.getDoctorName().equals(doctor.getName())) {
                    visits.add(visit);
                }
            }
            if (!visits.isEmpty()) {
                patientsWithVisits.add(patient);
                patientVisitsMap.put(patient, visits);
            }
        }

        for (Patient patient : patientsWithVisits) {
            JPanel patientCard = createDoctorPatientCard(patient, patientVisitsMap.get(patient), true);
            contentPanel.add(patientCard);
            contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        if (patientsWithVisits.isEmpty()) {
            JLabel emptyLabel = new JLabel("Brak aktywnych wizyt");
            emptyLabel.setFont(new Font("SansSerif", Font.ITALIC, 14));
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            contentPanel.add(emptyLabel);
        }

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel doctorCompletedVisitsPanel() {
        JPanel panel = createStyledPanel("Zakończone wizyty");
        panel.add(createHeaderPanel("Zakończone wizyty", "DoctorDashboard"), BorderLayout.NORTH);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        Doctor doctor = (Doctor) currentUser;
        List<Patient> patientsWithVisits = new ArrayList<>();
        Map<Patient, List<Visit>> patientVisitsMap = new HashMap<>();

        for (Patient patient : dataManager.getAllPatients().values()) {
            List<Visit> visits = new ArrayList<>();
            for (Visit visit : patient.getHistory()) {
                if (visit.getDoctorName().equals(doctor.getName())) {
                    visits.add(visit);
                }
            }
            if (!visits.isEmpty()) {
                patientsWithVisits.add(patient);
                patientVisitsMap.put(patient, visits);
            }
        }

        for (Patient patient : patientsWithVisits) {
            JPanel patientCard = createDoctorPatientCard(patient, patientVisitsMap.get(patient), false);
            contentPanel.add(patientCard);
            contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        if (patientsWithVisits.isEmpty()) {
            JLabel emptyLabel = new JLabel("Brak zakończonych wizyt");
            emptyLabel.setFont(new Font("SansSerif", Font.ITALIC, 14));
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            contentPanel.add(emptyLabel);
        }

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createDoctorPatientCard(Patient patient, List<Visit> visits, boolean isActive) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        card.setBackground(new Color(248, 248, 255));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        infoPanel.add(new JLabel("👤 " + patient.getName()));
        infoPanel.add(new JLabel("📧 Login: " + patient.getLogin()));
        infoPanel.add(new JLabel("🆔 PESEL: " + patient.getPesel()));

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonsPanel.setOpaque(false);

        JButton detailsBtn = new JButton("📋 Szczegóły pacjenta");
        detailsBtn.setBackground(new Color(70, 130, 200));
        detailsBtn.setForeground(Color.WHITE);
        detailsBtn.setFocusPainted(false);
        detailsBtn.addActionListener(e -> showPatientDetails(patient));

        buttonsPanel.add(detailsBtn);

        card.add(infoPanel, BorderLayout.CENTER);
        card.add(buttonsPanel, BorderLayout.EAST);

        JPanel visitsPanel = new JPanel();
        visitsPanel.setLayout(new BoxLayout(visitsPanel, BoxLayout.Y_AXIS));
        visitsPanel.setOpaque(false);
        visitsPanel.setBorder(BorderFactory.createTitledBorder(isActive ? "Aktywne wizyty" : "Zakończone wizyty"));

        for (Visit visit : visits) {
            JPanel visitPanel = createDoctorVisitPanel(visit, patient, isActive);
            visitsPanel.add(visitPanel);
            visitsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        }

        card.add(visitsPanel, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createDoctorVisitPanel(Visit visit, Patient patient, boolean isActive) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        panel.setBackground(Color.WHITE);

        String icon = visit.getType().equals("LAB") ? "🔬" : (visit.getType().equals("PROC") ? "💉" : "👨‍⚕️");
        JLabel infoLabel = new JLabel(icon + " " + visit.getDateTime() + " - " + visit.getSpecialization());

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonsPanel.setOpaque(false);

        if (isActive) {
            JButton completeBtn = new JButton("✅ Zakończ wizytę");
            completeBtn.setBackground(new Color(0, 150, 0));
            completeBtn.setForeground(Color.WHITE);
            completeBtn.setFocusPainted(false);
            completeBtn.addActionListener(e -> completeVisit(visit, patient));

            JButton noteBtn = new JButton("📝 Dodaj notatkę");
            noteBtn.setBackground(new Color(70, 130, 200));
            noteBtn.setForeground(Color.WHITE);
            noteBtn.setFocusPainted(false);
            noteBtn.addActionListener(e -> addVisitNote(visit));

            buttonsPanel.add(completeBtn);
            buttonsPanel.add(noteBtn);
        } else if (visit.getNotes() != null && !visit.getNotes().isEmpty()) {
            JLabel noteLabel = new JLabel("📝 " + visit.getNotes());
            noteLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
            panel.add(noteLabel, BorderLayout.SOUTH);
        }

        panel.add(infoLabel, BorderLayout.CENTER);
        panel.add(buttonsPanel, BorderLayout.EAST);

        return panel;
    }

    private void completeVisit(Visit visit, Patient patient) {
        String diagnosis = JOptionPane.showInputDialog(frame, "Podaj diagnozę:");
        if (diagnosis != null && !diagnosis.trim().isEmpty()) {
            visit.setStatus("Zakończona");
            visit.setNotes(diagnosis);
            patient.getRegisteredVisits().remove(visit);
            patient.addToHistory(visit);
            dataManager.saveAllData();
            JOptionPane.showMessageDialog(frame, "Wizyta zakończona pomyślnie!");
            refreshDoctorPanel("DoctorMyVisits");
            refreshDoctorPanel("DoctorCompletedVisits");
            cardLayout.show(mainPanel, "DoctorMyVisits");
        } else if (diagnosis != null) {
            JOptionPane.showMessageDialog(frame, "Diagnoza nie może być pusta!", "Błąd", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addVisitNote(Visit visit) {
        String note = JOptionPane.showInputDialog(frame, "Dodaj notatkę do wizyty:");
        if (note != null && !note.trim().isEmpty()) {
            visit.setNotes(note);
            dataManager.saveAllData();
            JOptionPane.showMessageDialog(frame, "Notatka dodana!");
            refreshDoctorPanel("DoctorMyVisits");
        }
    }

    private void showPatientDetails(Patient patient) {
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel nameLabel = new JLabel("👤 " + patient.getName());
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        detailsPanel.add(nameLabel);
        detailsPanel.add(new JLabel("📧 Login: " + patient.getLogin()));
        detailsPanel.add(new JLabel("🆔 PESEL: " + patient.getPesel()));
        detailsPanel.add(new JLabel("📍 Adres: " + patient.getAddress()));
        detailsPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        JLabel historyLabel = new JLabel("📋 Poprzednie wizyty:");
        historyLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        historyLabel.setForeground(new Color(70, 130, 200));
        detailsPanel.add(historyLabel);

        if (patient.getHistory().isEmpty()) {
            detailsPanel.add(new JLabel("  Brak poprzednich wizyt"));
        } else {
            for (Visit visit : patient.getHistory()) {
                JPanel visitPanel = new JPanel();
                visitPanel.setLayout(new BoxLayout(visitPanel, BoxLayout.Y_AXIS));
                visitPanel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                        BorderFactory.createEmptyBorder(5, 10, 5, 10)
                ));
                visitPanel.setBackground(new Color(250, 250, 250));
                visitPanel.add(new JLabel("  📅 " + visit.getDateTime() + " - " + visit.getDoctorName()));
                visitPanel.add(new JLabel("  📋 " + visit.getSpecialization()));
                if (visit.getNotes() != null && !visit.getNotes().isEmpty()) {
                    visitPanel.add(new JLabel("  📝 " + visit.getNotes()));
                }
                detailsPanel.add(visitPanel);
                detailsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            }
        }
        detailsPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        JLabel prescriptionsLabel = new JLabel("💊 Wypisane leki:");
        prescriptionsLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        prescriptionsLabel.setForeground(new Color(70, 130, 200));
        detailsPanel.add(prescriptionsLabel);

        if (patient.getPrescriptions().isEmpty()) {
            detailsPanel.add(new JLabel("  Brak wypisanych leków"));
        } else {
            for (Prescription pres : patient.getPrescriptions()) {
                detailsPanel.add(new JLabel("  📋 " + pres.getDate() + " - " + pres.getMedicines() + " (dr " + pres.getDoctor() + ")"));
            }
        }
        detailsPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        JLabel resultsLabel = new JLabel("📊 Wyniki badań:");
        resultsLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        resultsLabel.setForeground(new Color(70, 130, 200));
        detailsPanel.add(resultsLabel);

        if (patient.getTestResults().isEmpty()) {
            detailsPanel.add(new JLabel("  Brak wyników badań"));
        } else {
            for (TestResult result : patient.getTestResults()) {
                JPanel resultPanel = new JPanel();
                resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
                resultPanel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                        BorderFactory.createEmptyBorder(5, 10, 5, 10)
                ));
                resultPanel.setBackground(new Color(250, 250, 250));
                resultPanel.add(new JLabel("  🔬 " + result.getTestName() + " (" + result.getDate() + ")"));
                resultPanel.add(new JLabel("  📊 Wynik: " + result.getResult()));
                resultPanel.add(new JLabel("  👨‍⚕️ Lekarz: " + result.getDoctor()));
                detailsPanel.add(resultPanel);
                detailsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            }
        }

        JScrollPane scrollPane = new JScrollPane(detailsPanel);
        scrollPane.setPreferredSize(new Dimension(600, 500));

        JOptionPane.showMessageDialog(frame, scrollPane, "Szczegóły pacjenta: " + patient.getName(),
                JOptionPane.INFORMATION_MESSAGE);
    }

    private JPanel doctorAddResultsPanel() {
        JPanel panel = createStyledPanel("Dodaj wyniki badań");
        panel.add(createHeaderPanel("Dodaj wyniki badań", "DoctorDashboard"), BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Pacjent:"), gbc);

        JComboBox<String> patientCombo = new JComboBox<>();
        for (Patient p : dataManager.getAllPatients().values()) {
            patientCombo.addItem(p.getLogin() + " - " + p.getName());
        }
        patientCombo.setPreferredSize(new Dimension(300, 30));

        JPanel searchPanel = createSearchPanel("Szukaj pacjenta po imieniu lub nazwisku...", formPanel,
                searchText -> filterPatientsCombo(patientCombo, searchText));

        gbc.gridx = 1;
        gbc.gridwidth = 1;
        formPanel.add(searchPanel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        formPanel.add(new JLabel("Wybierz pacjenta:"), gbc);
        gbc.gridx = 1;
        formPanel.add(patientCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Nazwa badania:"), gbc);

        JTextField testNameField = new JTextField(20);
        gbc.gridx = 1;
        formPanel.add(testNameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Wynik:"), gbc);

        JTextArea resultArea = new JTextArea(5, 20);
        JScrollPane resultScroll = new JScrollPane(resultArea);
        gbc.gridx = 1;
        formPanel.add(resultScroll, gbc);

        JButton addBtn = createStyledButton("Dodaj wynik", new Color(70, 130, 200));
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        formPanel.add(addBtn, gbc);

        addBtn.addActionListener(e -> {
            String selected = (String) patientCombo.getSelectedItem();
            String testName = testNameField.getText().trim();
            String result = resultArea.getText().trim();

            if (selected == null || testName.isEmpty() || result.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Wszystkie pola są wymagane!", "Błąd", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String login = selected.split(" - ")[0];
            Patient patient = dataManager.getPatient(login);
            Doctor doctor = (Doctor) currentUser;

            TestResult testResult = new TestResult(testName,
                    LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                    result, doctor.getName());

            patient.addTestResult(testResult);
            dataManager.saveAllData();

            JOptionPane.showMessageDialog(panel, "Wynik badania dodany pomyślnie!");
            testNameField.setText("");
            resultArea.setText("");
        });

        panel.add(formPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel doctorAddPrescriptionPanel() {
        JPanel panel = createStyledPanel("Wystaw receptę");
        panel.add(createHeaderPanel("Wystaw receptę", "DoctorDashboard"), BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Pacjent:"), gbc);

        JComboBox<String> patientCombo = new JComboBox<>();
        for (Patient p : dataManager.getAllPatients().values()) {
            patientCombo.addItem(p.getLogin() + " - " + p.getName());
        }
        patientCombo.setPreferredSize(new Dimension(300, 30));

        JPanel searchPanel = createSearchPanel("Szukaj pacjenta po imieniu lub nazwisku...", formPanel,
                searchText -> filterPatientsCombo(patientCombo, searchText));

        gbc.gridx = 1;
        formPanel.add(searchPanel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Wybierz pacjenta:"), gbc);
        gbc.gridx = 1;
        formPanel.add(patientCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Leki:"), gbc);

        JTextArea medicinesArea = new JTextArea(5, 20);
        JScrollPane medicinesScroll = new JScrollPane(medicinesArea);
        gbc.gridx = 1;
        formPanel.add(medicinesScroll, gbc);

        JButton addBtn = createStyledButton("Wystaw receptę", new Color(70, 130, 200));
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        formPanel.add(addBtn, gbc);

        addBtn.addActionListener(e -> {
            String selected = (String) patientCombo.getSelectedItem();
            String medicines = medicinesArea.getText().trim();

            if (selected == null || medicines.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Wszystkie pola są wymagane!", "Błąd", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String login = selected.split(" - ")[0];
            Patient patient = dataManager.getPatient(login);
            Doctor doctor = (Doctor) currentUser;

            String prescriptionNumber = "RX-" + System.currentTimeMillis();
            Prescription prescription = new Prescription(prescriptionNumber, medicines,
                    LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")), doctor.getName());

            patient.addPrescription(prescription);
            dataManager.saveAllData();

            JOptionPane.showMessageDialog(panel, "Recepta wystawiona pomyślnie! Numer: " + prescriptionNumber);
            medicinesArea.setText("");
        });

        panel.add(formPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel doctorAddReferralPanel() {
        JPanel panel = createStyledPanel("Wystaw skierowanie");
        panel.add(createHeaderPanel("Wystaw skierowanie", "DoctorDashboard"), BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Pacjent:"), gbc);

        JComboBox<String> patientCombo = new JComboBox<>();
        for (Patient p : dataManager.getAllPatients().values()) {
            patientCombo.addItem(p.getLogin() + " - " + p.getName());
        }
        patientCombo.setPreferredSize(new Dimension(300, 30));

        JPanel searchPanel = createSearchPanel("Szukaj pacjenta po imieniu lub nazwisku...", formPanel,
                searchText -> filterPatientsCombo(patientCombo, searchText));

        gbc.gridx = 1;
        formPanel.add(searchPanel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Wybierz pacjenta:"), gbc);
        gbc.gridx = 1;
        formPanel.add(patientCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Rodzaj skierowania:"), gbc);

        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"Badanie krwi", "EKG", "USG", "RTG", "Konsultacja specjalistyczna"});
        typeCombo.setPreferredSize(new Dimension(200, 30));
        gbc.gridx = 1;
        formPanel.add(typeCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Ważne do:"), gbc);

        JSpinner validUntilSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(validUntilSpinner, "dd.MM.yyyy");
        validUntilSpinner.setEditor(dateEditor);
        validUntilSpinner.setValue(new Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000));
        gbc.gridx = 1;
        formPanel.add(validUntilSpinner, gbc);

        JButton addBtn = createStyledButton("Wystaw skierowanie", new Color(70, 130, 200));
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        formPanel.add(addBtn, gbc);

        addBtn.addActionListener(e -> {
            String selected = (String) patientCombo.getSelectedItem();

            if (selected == null) {
                JOptionPane.showMessageDialog(panel, "Wybierz pacjenta!", "Błąd", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String login = selected.split(" - ")[0];
            Patient patient = dataManager.getPatient(login);
            Doctor doctor = (Doctor) currentUser;

            String referralNumber = "SKI-" + System.currentTimeMillis();
            String validUntil = new java.text.SimpleDateFormat("dd.MM.yyyy").format(validUntilSpinner.getValue());
            Referral referral = new Referral(referralNumber, (String) typeCombo.getSelectedItem(),
                    validUntil, doctor.getName());

            patient.addReferral(referral);
            dataManager.saveAllData();

            JOptionPane.showMessageDialog(panel, "Skierowanie wystawione pomyślnie! Numer: " + referralNumber);
        });

        panel.add(formPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel doctorAddSickLeavePanel() {
        JPanel panel = createStyledPanel("Wystaw zwolnienie");
        panel.add(createHeaderPanel("Wystaw zwolnienie lekarskie", "DoctorDashboard"), BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Pacjent:"), gbc);

        JComboBox<String> patientCombo = new JComboBox<>();
        for (Patient p : dataManager.getAllPatients().values()) {
            patientCombo.addItem(p.getLogin() + " - " + p.getName());
        }
        patientCombo.setPreferredSize(new Dimension(300, 30));

        JPanel searchPanel = createSearchPanel("Szukaj pacjenta po imieniu lub nazwisku...", formPanel,
                searchText -> filterPatientsCombo(patientCombo, searchText));

        gbc.gridx = 1;
        formPanel.add(searchPanel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Wybierz pacjenta:"), gbc);
        gbc.gridx = 1;
        formPanel.add(patientCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Data rozpoczęcia:"), gbc);

        JSpinner startDateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor startEditor = new JSpinner.DateEditor(startDateSpinner, "dd.MM.yyyy");
        startDateSpinner.setEditor(startEditor);
        startDateSpinner.setValue(new Date());
        gbc.gridx = 1;
        formPanel.add(startDateSpinner, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Data zakończenia:"), gbc);

        JSpinner endDateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor endEditor = new JSpinner.DateEditor(endDateSpinner, "dd.MM.yyyy");
        endDateSpinner.setEditor(endEditor);
        endDateSpinner.setValue(new Date(System.currentTimeMillis() + 14L * 24 * 60 * 60 * 1000));
        gbc.gridx = 1;
        formPanel.add(endDateSpinner, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(new JLabel("Kod choroby:"), gbc);

        JComboBox<String> codeCombo = new JComboBox<>(new String[]{"A", "B", "C", "D", "E"});
        codeCombo.setPreferredSize(new Dimension(100, 30));
        gbc.gridx = 1;
        formPanel.add(codeCombo, gbc);

        JButton addBtn = createStyledButton("Wystaw zwolnienie", new Color(70, 130, 200));
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        formPanel.add(addBtn, gbc);

        addBtn.addActionListener(e -> {
            String selected = (String) patientCombo.getSelectedItem();

            if (selected == null) {
                JOptionPane.showMessageDialog(panel, "Wybierz pacjenta!", "Błąd", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String login = selected.split(" - ")[0];
            Patient patient = dataManager.getPatient(login);
            Doctor doctor = (Doctor) currentUser;

            String startDate = new java.text.SimpleDateFormat("dd.MM.yyyy").format(startDateSpinner.getValue());
            String endDate = new java.text.SimpleDateFormat("dd.MM.yyyy").format(endDateSpinner.getValue());

            SickLeave sickLeave = new SickLeave(startDate, endDate, doctor.getName(), (String) codeCombo.getSelectedItem());
            patient.addSickLeave(sickLeave);
            dataManager.saveAllData();

            JOptionPane.showMessageDialog(panel, "Zwolnienie wystawione pomyślnie!");
        });

        panel.add(formPanel, BorderLayout.CENTER);
        return panel;
    }

    private void filterPatientsCombo(JComboBox<String> comboBox, String searchText) {
        String searchLower = searchText.toLowerCase().trim();
        comboBox.removeAllItems();
        for (Patient p : dataManager.getAllPatients().values()) {
            if (searchLower.isEmpty() || p.getName().toLowerCase().contains(searchLower) ||
                    p.getLogin().toLowerCase().contains(searchLower)) {
                comboBox.addItem(p.getLogin() + " - " + p.getName());
            }
        }
    }

    // ==================== PANEL ADMINISTRATORA ====================

    private JPanel adminDashboard() {
        JPanel panel = createStyledPanel("Panel Administratora");
        panel.add(createHeaderPanel("Panel Administratora - Dashboard", "Logout"), BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel welcomeLabel = new JLabel("Witaj, " + currentUser.getName() + "!");
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        welcomeLabel.setForeground(new Color(70, 130, 200));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        contentPanel.add(welcomeLabel, gbc);

        String[][] buttons = {
                {"👨‍⚕️ Zarządzaj lekarzami", "AdminManageDoctors"},
                {"📋 Zarządzaj recepcjonistami", "AdminManageReceptionists"},
                {"👥 Zarządzaj pacjentami", "AdminManagePatients"},
                {"📅 Zarządzaj harmonogramem", "AdminManageSchedule"},
                {"📊 Statystyki systemu", "AdminStatistics"}
        };

        int row = 1;
        for (String[] btn : buttons) {
            JButton button = createStyledButton(btn[0], new Color(70, 130, 200));
            button.setPreferredSize(new Dimension(320, 50));
            if (btn[1].equals("AdminStatistics")) {
                button.addActionListener(e -> showAdminStatistics());
            } else {
                button.addActionListener(e -> {
                    refreshAdminPanel(btn[1]);
                    cardLayout.show(mainPanel, btn[1]);
                });
            }
            gbc.gridy = row;
            contentPanel.add(button, gbc);
            row++;
        }

        panel.add(contentPanel, BorderLayout.CENTER);

        JPanel statsPanel = createAdminStatsPanel();
        panel.add(statsPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createAdminStatsPanel() {
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Statystyki systemu"),
                BorderFactory.createEmptyBorder(10, 15, 15, 15)
        ));
        statsPanel.setBackground(new Color(248, 248, 255));
        statsPanel.setPreferredSize(new Dimension(300, 400));

        int patientCount = dataManager.getAllPatients().size();
        int doctorCount = dataManager.getDoctors().size();
        int receptionistCount = dataManager.getReceptionists().size();
        int totalVisits = 0;
        int activeVisits = 0;

        for (Patient p : dataManager.getAllPatients().values()) {
            totalVisits += p.getHistory().size();
            activeVisits += p.getRegisteredVisits().size();
        }

        statsPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        statsPanel.add(new JLabel("👥 Pacjenci: " + patientCount));
        statsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        statsPanel.add(new JLabel("👨‍⚕️ Lekarze: " + doctorCount));
        statsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        statsPanel.add(new JLabel("📋 Recepcjoniści: " + receptionistCount));
        statsPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        statsPanel.add(new JLabel("📅 Zakończone wizyty: " + totalVisits));
        statsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        statsPanel.add(new JLabel("⏰ Aktywne wizyty: " + activeVisits));

        return statsPanel;
    }

    private void showAdminStatistics() {
        StringBuilder sb = new StringBuilder();
        sb.append("STATYSTYKI SYSTEMU\n\n");
        sb.append("Liczba pacjentów: ").append(dataManager.getAllPatients().size()).append("\n");
        sb.append("Liczba lekarzy: ").append(dataManager.getDoctors().size()).append("\n");
        sb.append("Liczba recepcjonistów: ").append(dataManager.getReceptionists().size()).append("\n\n");

        int totalVisits = 0;
        int activeVisits = 0;
        for (Patient p : dataManager.getAllPatients().values()) {
            totalVisits += p.getHistory().size();
            activeVisits += p.getRegisteredVisits().size();
        }
        sb.append("Zakończone wizyty: ").append(totalVisits).append("\n");
        sb.append("Aktywne wizyty: ").append(activeVisits).append("\n\n");

        sb.append("LEKARZE:\n");
        for (Doctor d : dataManager.getDoctors()) {
            sb.append("- ").append(d.getName()).append(" (").append(d.getSpecialization()).append(")\n");
        }

        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 400));
        JOptionPane.showMessageDialog(frame, scrollPane, "Statystyki systemu", JOptionPane.INFORMATION_MESSAGE);
    }

    private JPanel adminManageDoctorsPanel() {

        JPanel panel = createStyledPanel("Zarządzanie lekarzami");
        panel.add(
                createHeaderPanel("Zarządzanie lekarzami", "AdminDashboard"),
                BorderLayout.NORTH
        );

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // =========================
        // ADD BUTTON
        // =========================
        JButton addBtn = createStyledButton("➕ Dodaj nowego lekarza", new Color(0, 150, 0));
        addBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        addBtn.setMaximumSize(new Dimension(250, 40));
        addBtn.addActionListener(e -> showAddDoctorDialog());

        contentPanel.add(addBtn);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // =========================
        // 🔎 SEARCH FIELD
        // =========================
        JTextField searchField = new JTextField();
        searchField.setMaximumSize(new Dimension(300, 28));
        searchField.setToolTipText("Szukaj lekarza (imię, nazwisko, specjalizacja)");

        contentPanel.add(searchField);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // =========================
        // REFRESH LIST
        // =========================
        Runnable refreshList = () -> {

            // usuń stare karty (ale zostaw button + search)
            contentPanel.removeAll();
            contentPanel.add(addBtn);
            contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));
            contentPanel.add(searchField);
            contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));

            String q = searchField.getText().trim().toLowerCase();

            int count = 0;

            for (Doctor doctor : dataManager.getDoctors()) {

                String data = (
                        doctor.getName() + " " +
                                doctor.getSpecialization()
                ).toLowerCase();

                if (q.isEmpty() || data.contains(q)) {

                    JPanel doctorCard = createAdminDoctorCard(doctor);

                    contentPanel.add(doctorCard);
                    contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));

                    count++;
                }
            }

            if (count == 0) {

                JLabel empty = new JLabel("Brak lekarzy dla podanego filtra");
                empty.setFont(new Font("SansSerif", Font.ITALIC, 14));
                empty.setAlignmentX(Component.CENTER_ALIGNMENT);

                contentPanel.add(empty);
            }

            contentPanel.revalidate();
            contentPanel.repaint();
        };

        // =========================
        // LIVE SEARCH
        // =========================
        searchField.getDocument().addDocumentListener(new SimpleDocListener(refreshList));

        // =========================
        // INITIAL LOAD
        // =========================
        refreshList.run();

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }
    private JPanel createAdminDoctorCard(Doctor doctor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        card.setBackground(new Color(248, 248, 255));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        infoPanel.add(new JLabel("👨‍⚕️ " + doctor.getName()));
        infoPanel.add(new JLabel("📧 Login: " + doctor.getLogin()));
        infoPanel.add(new JLabel("🔬 Specjalizacja: " + doctor.getSpecialization()));
        infoPanel.add(new JLabel("⏰ Godziny: " + doctor.getHours()));

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonsPanel.setOpaque(false);

        JButton editBtn = new JButton("✏️ Edytuj");
        editBtn.setBackground(new Color(70, 130, 200));
        editBtn.setForeground(Color.WHITE);
        editBtn.setFocusPainted(false);
        editBtn.addActionListener(e -> showEditDoctorDialog(doctor));

        JButton deleteBtn = new JButton("🗑️ Usuń");
        deleteBtn.setBackground(new Color(200, 0, 0));
        deleteBtn.setForeground(Color.WHITE);
        deleteBtn.setFocusPainted(false);
        deleteBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(frame,
                    "Czy na pewno chcesz usunąć lekarza " + doctor.getName() + "?",
                    "Potwierdzenie", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                dataManager.removeDoctor(doctor);
                dataManager.saveAllData();
                refreshAdminPanel("AdminManageDoctors");
                cardLayout.show(mainPanel, "AdminManageDoctors");
            }
        });

        buttonsPanel.add(editBtn);
        buttonsPanel.add(deleteBtn);

        card.add(infoPanel, BorderLayout.CENTER);
        card.add(buttonsPanel, BorderLayout.EAST);

        return card;
    }

    private void showAddDoctorDialog() {
        JDialog dialog = new JDialog(frame, "Dodaj nowego lekarza", true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JTextField loginField = new JTextField(20);
        JPasswordField passwordField = new JPasswordField(20);
        JTextField nameField = new JTextField(20);
        JTextField specField = new JTextField(20);
        JTextField hoursField = new JTextField(20);
        JTextField emailField = new JTextField(20);
        JTextField phoneField = new JTextField(20);

        gbc.gridx = 0;
        gbc.gridy = 0;
        dialog.add(new JLabel("Login:"), gbc);
        gbc.gridx = 1;
        dialog.add(loginField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        dialog.add(new JLabel("Hasło:"), gbc);
        gbc.gridx = 1;
        dialog.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        dialog.add(new JLabel("Imię i nazwisko:"), gbc);
        gbc.gridx = 1;
        dialog.add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        dialog.add(new JLabel("Specjalizacja:"), gbc);
        gbc.gridx = 1;
        dialog.add(specField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        dialog.add(new JLabel("Godziny pracy:"), gbc);
        gbc.gridx = 1;
        dialog.add(hoursField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        dialog.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        dialog.add(emailField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        dialog.add(new JLabel("Telefon:"), gbc);
        gbc.gridx = 1;
        dialog.add(phoneField, gbc);

        JButton saveBtn = createStyledButton("Zapisz", new Color(0, 150, 0));
        saveBtn.addActionListener(e -> {
            String login = loginField.getText().trim();
            String password = new String(passwordField.getPassword());
            String name = nameField.getText().trim();
            String spec = specField.getText().trim();
            String hours = hoursField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();

            if (login.isEmpty() || password.isEmpty() || name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Login, hasło i imię są wymagane!", "Błąd", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!validateEmail(email)) {
                JOptionPane.showMessageDialog(dialog, "Nieprawidłowy format email!", "Błąd", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!validatePhone(phone)) {
                JOptionPane.showMessageDialog(dialog, "Nieprawidłowy numer telefonu!", "Błąd", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int newId = dataManager.getDoctors().size() + 1;
            Doctor doctor = new Doctor(newId, name, spec, hours, email, phone);
            doctor.setLogin(login);
            doctor.setPassword(password);
            dataManager.addDoctor(doctor);
            dataManager.saveAllData();

            JOptionPane.showMessageDialog(dialog, "Lekarz dodany pomyślnie!");
            dialog.dispose();
            refreshAdminPanel("AdminManageDoctors");
            cardLayout.show(mainPanel, "AdminManageDoctors");
        });

        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        dialog.add(saveBtn, gbc);

        dialog.pack();
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }

    private void showEditDoctorDialog(Doctor doctor) {
        JDialog dialog = new JDialog(frame, "Edytuj lekarza", true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JTextField nameField = new JTextField(doctor.getName(), 20);
        JTextField specField = new JTextField(doctor.getSpecialization(), 20);
        JTextField hoursField = new JTextField(doctor.getHours(), 20);
        JTextField emailField = new JTextField(doctor.getEmail(), 20);
        JTextField phoneField = new JTextField(doctor.getPhone(), 20);

        gbc.gridx = 0;
        gbc.gridy = 0;
        dialog.add(new JLabel("Imię i nazwisko:"), gbc);
        gbc.gridx = 1;
        dialog.add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        dialog.add(new JLabel("Specjalizacja:"), gbc);
        gbc.gridx = 1;
        dialog.add(specField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        dialog.add(new JLabel("Godziny pracy:"), gbc);
        gbc.gridx = 1;
        dialog.add(hoursField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        dialog.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        dialog.add(emailField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        dialog.add(new JLabel("Telefon:"), gbc);
        gbc.gridx = 1;
        dialog.add(phoneField, gbc);

        JButton saveBtn = createStyledButton("Zapisz", new Color(0, 150, 0));
        saveBtn.addActionListener(e -> {
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();

            if (!validateEmail(email)) {
                JOptionPane.showMessageDialog(dialog, "Nieprawidłowy format email!", "Błąd", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!validatePhone(phone)) {
                JOptionPane.showMessageDialog(dialog, "Nieprawidłowy numer telefonu!", "Błąd", JOptionPane.ERROR_MESSAGE);
                return;
            }

            doctor.setName(nameField.getText().trim());
            doctor.setSpecialization(specField.getText().trim());
            doctor.setHours(hoursField.getText().trim());
            doctor.setEmail(email);
            doctor.setPhone(phone);

            dataManager.saveAllData();
            JOptionPane.showMessageDialog(dialog, "Dane zaktualizowane!");
            dialog.dispose();
            refreshAdminPanel("AdminManageDoctors");
            cardLayout.show(mainPanel, "AdminManageDoctors");
        });

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        dialog.add(saveBtn, gbc);

        dialog.pack();
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }

    private JPanel adminManageReceptionistsPanel() {

        JPanel panel = createStyledPanel("Zarządzanie recepcjonistami");
        panel.add(
                createHeaderPanel("Zarządzanie recepcjonistami", "AdminDashboard"),
                BorderLayout.NORTH
        );

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // =========================
        // ADD BUTTON
        // =========================
        JButton addBtn = createStyledButton(
                "➕ Dodaj nowego recepcjonistę",
                new Color(0, 150, 0)
        );

        addBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        addBtn.setMaximumSize(new Dimension(250, 40));
        addBtn.addActionListener(e -> showAddReceptionistDialog());

        contentPanel.add(addBtn);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // =========================
        // 🔎 SEARCH FIELD
        // =========================
        JTextField searchField = new JTextField();
        searchField.setMaximumSize(new Dimension(300, 28));
        searchField.setToolTipText("Szukaj recepcjonisty (login, imię, nazwisko)");

        contentPanel.add(searchField);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // =========================
        // REFRESH LIST
        // =========================
        Runnable refreshList = () -> {

            contentPanel.removeAll();
            contentPanel.add(addBtn);
            contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));
            contentPanel.add(searchField);
            contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));

            String q = searchField.getText().trim().toLowerCase();

            int count = 0;

            for (Receptionist receptionist : dataManager.getReceptionists().values()) {

                String data = (
                        receptionist.getLogin() + " " +
                                receptionist.getName()
                ).toLowerCase();

                if (q.isEmpty() || data.contains(q)) {

                    JPanel recCard = createAdminReceptionistCard(receptionist);

                    contentPanel.add(recCard);
                    contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));

                    count++;
                }
            }

            if (count == 0) {

                JLabel empty = new JLabel("Brak recepcjonistów dla podanego filtra");
                empty.setFont(new Font("SansSerif", Font.ITALIC, 14));
                empty.setAlignmentX(Component.CENTER_ALIGNMENT);

                contentPanel.add(empty);
            }

            contentPanel.revalidate();
            contentPanel.repaint();
        };

        // =========================
        // LIVE SEARCH
        // =========================
        searchField.getDocument().addDocumentListener(new SimpleDocListener(refreshList));

        // =========================
        // INITIAL LOAD
        // =========================
        refreshList.run();

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createAdminReceptionistCard(Receptionist receptionist) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        card.setBackground(new Color(248, 248, 255));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        infoPanel.add(new JLabel("📋 " + receptionist.getName()));
        infoPanel.add(new JLabel("📧 Login: " + receptionist.getLogin()));
        infoPanel.add(new JLabel("🆔 ID: " + receptionist.getEmployeeId()));

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonsPanel.setOpaque(false);

        JButton editBtn = new JButton("✏️ Edytuj");
        editBtn.setBackground(new Color(70, 130, 200));
        editBtn.setForeground(Color.WHITE);
        editBtn.setFocusPainted(false);
        editBtn.addActionListener(e -> showEditReceptionistDialog(receptionist));

        JButton deleteBtn = new JButton("🗑️ Usuń");
        deleteBtn.setBackground(new Color(200, 0, 0));
        deleteBtn.setForeground(Color.WHITE);
        deleteBtn.setFocusPainted(false);
        deleteBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(frame,
                    "Czy na pewno chcesz usunąć recepcjonistę " + receptionist.getName() + "?",
                    "Potwierdzenie", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                dataManager.removeReceptionist(receptionist.getLogin());
                dataManager.saveAllData();
                refreshAdminPanel("AdminManageReceptionists");
                cardLayout.show(mainPanel, "AdminManageReceptionists");
            }
        });

        buttonsPanel.add(editBtn);
        buttonsPanel.add(deleteBtn);

        card.add(infoPanel, BorderLayout.CENTER);
        card.add(buttonsPanel, BorderLayout.EAST);

        return card;
    }

    private void showEditReceptionistDialog(Receptionist receptionist) {
        JDialog dialog = new JDialog(frame, "Edytuj recepcjonistę", true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JTextField nameField = new JTextField(receptionist.getName(), 20);
        JTextField employeeIdField = new JTextField(receptionist.getEmployeeId(), 20);
        JPasswordField passwordField = new JPasswordField(20);

        gbc.gridx = 0;
        gbc.gridy = 0;
        dialog.add(new JLabel("Imię i nazwisko:"), gbc);
        gbc.gridx = 1;
        dialog.add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        dialog.add(new JLabel("ID pracownika:"), gbc);
        gbc.gridx = 1;
        dialog.add(employeeIdField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        dialog.add(new JLabel("Nowe hasło (pozostaw puste aby nie zmieniać):"), gbc);
        gbc.gridx = 1;
        dialog.add(passwordField, gbc);

        JButton saveBtn = createStyledButton("Zapisz", new Color(0, 150, 0));
        saveBtn.addActionListener(e -> {
            String newPassword = new String(passwordField.getPassword());
            if (!newPassword.isEmpty()) {
                receptionist.setPassword(newPassword);
            }
            receptionist.setName(nameField.getText().trim());
            receptionist.setEmployeeId(employeeIdField.getText().trim());

            dataManager.saveAllData();
            JOptionPane.showMessageDialog(dialog, "Dane zaktualizowane!");
            dialog.dispose();
            refreshAdminPanel("AdminManageReceptionists");
            cardLayout.show(mainPanel, "AdminManageReceptionists");
        });

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        dialog.add(saveBtn, gbc);

        dialog.pack();
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }

    private void showAddReceptionistDialog() {
        JDialog dialog = new JDialog(frame, "Dodaj nowego recepcjonistę", true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JTextField loginField = new JTextField(20);
        JPasswordField passwordField = new JPasswordField(20);
        JTextField nameField = new JTextField(20);
        JTextField employeeIdField = new JTextField(20);

        gbc.gridx = 0;
        gbc.gridy = 0;
        dialog.add(new JLabel("Login:"), gbc);
        gbc.gridx = 1;
        dialog.add(loginField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        dialog.add(new JLabel("Hasło:"), gbc);
        gbc.gridx = 1;
        dialog.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        dialog.add(new JLabel("Imię i nazwisko:"), gbc);
        gbc.gridx = 1;
        dialog.add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        dialog.add(new JLabel("ID pracownika:"), gbc);
        gbc.gridx = 1;
        dialog.add(employeeIdField, gbc);

        JButton saveBtn = createStyledButton("Zapisz", new Color(0, 150, 0));
        saveBtn.addActionListener(e -> {
            String login = loginField.getText().trim();
            String password = new String(passwordField.getPassword());
            String name = nameField.getText().trim();
            String employeeId = employeeIdField.getText().trim();

            if (login.isEmpty() || password.isEmpty() || name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Login, hasło i imię są wymagane!", "Błąd", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Receptionist receptionist = new Receptionist(login, password, name, employeeId);
            dataManager.addReceptionist(receptionist);
            dataManager.saveAllData();

            JOptionPane.showMessageDialog(dialog, "Recepcjonista dodany pomyślnie!");
            dialog.dispose();
            refreshAdminPanel("AdminManageReceptionists");
            cardLayout.show(mainPanel, "AdminManageReceptionists");
        });

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        dialog.add(saveBtn, gbc);

        dialog.pack();
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }

    private JPanel adminManagePatientsPanel() {

        JPanel panel = createStyledPanel("Zarządzanie pacjentami");
        panel.setLayout(new BorderLayout());

        panel.add(
                createHeaderPanel("Zarządzanie pacjentami", "AdminDashboard"),
                BorderLayout.NORTH
        );

        // ===== GŁÓWNY PANEL =====
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // ===== PRZYCISK DODAWANIA =====
        JButton addBtn = createStyledButton(
                "➕ Dodaj nowego pacjenta",
                new Color(0, 150, 0)
        );

        addBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        addBtn.setMaximumSize(new Dimension(250, 40));

        addBtn.addActionListener(e -> showAdminAddPatientDialog());

        contentPanel.add(addBtn);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // ===== PANEL WYSZUKIWANIA =====
        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JTextField searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(300, 35));

        JButton searchBtn = createStyledButton(
                "Szukaj",
                new Color(0, 120, 215)
        );

        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchBtn, BorderLayout.EAST);

        contentPanel.add(searchPanel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // ===== PANEL Z LISTĄ PACJENTÓW =====
        JPanel patientsListPanel = new JPanel();
        patientsListPanel.setLayout(new BoxLayout(patientsListPanel, BoxLayout.Y_AXIS));
        patientsListPanel.setBackground(Color.WHITE);

        // Funkcja odświeżania listy
        Runnable refreshPatients = () -> {

            patientsListPanel.removeAll();

            String searchText = searchField.getText().trim().toLowerCase();

            for (Patient patient : dataManager.getAllPatients().values()) {

                boolean matches = searchText.isEmpty()

                        || patient.getName().toLowerCase().contains(searchText)

                        || patient.getPesel().contains(searchText);

                if (matches) {

                    JPanel patientCard = createAdminPatientCard(patient);

                    patientsListPanel.add(patientCard);
                    patientsListPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                }
            }

            patientsListPanel.revalidate();
            patientsListPanel.repaint();
        };

        // Początkowe załadowanie
        refreshPatients.run();

        // Wyszukiwanie po kliknięciu
        searchBtn.addActionListener(e -> refreshPatients.run());

        // Wyszukiwanie podczas pisania
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {

            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                refreshPatients.run();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                refreshPatients.run();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                refreshPatients.run();
            }
        });

        contentPanel.add(patientsListPanel);

        // ===== SCROLL =====
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }
    private void showAdminAddPatientDialog() {
        JDialog dialog = new JDialog(frame, "Dodaj nowego pacjenta", true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JTextField loginField = new JTextField(20);
        JPasswordField passwordField = new JPasswordField(20);
        JTextField nameField = new JTextField(20);
        JTextField peselField = new JTextField(20);
        JTextField addressField = new JTextField(20);

        gbc.gridx = 0;
        gbc.gridy = 0;
        dialog.add(new JLabel("Login:"), gbc);
        gbc.gridx = 1;
        dialog.add(loginField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        dialog.add(new JLabel("Hasło:"), gbc);
        gbc.gridx = 1;
        dialog.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        dialog.add(new JLabel("Imię i nazwisko:"), gbc);
        gbc.gridx = 1;
        dialog.add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        dialog.add(new JLabel("PESEL (11 cyfr):"), gbc);
        gbc.gridx = 1;
        dialog.add(peselField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        dialog.add(new JLabel("Adres:"), gbc);
        gbc.gridx = 1;
        dialog.add(addressField, gbc);

        JButton saveBtn = createStyledButton("Zapisz", new Color(0, 150, 0));
        saveBtn.addActionListener(e -> {
            String login = loginField.getText().trim();
            String password = new String(passwordField.getPassword());
            String name = nameField.getText().trim();
            String pesel = peselField.getText().trim();
            String address = addressField.getText().trim();

            if (login.isEmpty() || password.isEmpty() || name.isEmpty() || pesel.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Wszystkie pola są wymagane!", "Błąd", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!validatePesel(pesel)) {
                JOptionPane.showMessageDialog(dialog, "Nieprawidłowy numer PESEL! Musi zawierać 11 cyfr i poprawną sumę kontrolną.", "Błąd", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (dataManager.getPatient(login) != null) {
                JOptionPane.showMessageDialog(dialog, "Pacjent z takim loginem już istnieje!", "Błąd", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Patient newPatient = new Patient(login, password, name, pesel, address);
            dataManager.addPatient(newPatient);
            dataManager.saveAllData();

            JOptionPane.showMessageDialog(dialog, "Pacjent został pomyślnie dodany!");
            dialog.dispose();
            refreshAdminPanel("AdminManagePatients");
            cardLayout.show(mainPanel, "AdminManagePatients");
        });

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        dialog.add(saveBtn, gbc);

        dialog.pack();
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }

    private JPanel createAdminPatientCard(Patient patient) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        card.setBackground(new Color(248, 248, 255));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        infoPanel.add(new JLabel("👤 " + patient.getName()));
        infoPanel.add(new JLabel("📧 Login: " + patient.getLogin()));
        infoPanel.add(new JLabel("🆔 PESEL: " + patient.getPesel()));
        infoPanel.add(new JLabel("📍 Adres: " + patient.getAddress()));

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonsPanel.setOpaque(false);

        JButton editBtn = new JButton("✏️ Edytuj");
        editBtn.setBackground(new Color(70, 130, 200));
        editBtn.setForeground(Color.WHITE);
        editBtn.setFocusPainted(false);
        editBtn.addActionListener(e -> showEditPatientDialog(patient));

        JButton deleteBtn = new JButton("🗑️ Usuń");
        deleteBtn.setBackground(new Color(200, 0, 0));
        deleteBtn.setForeground(Color.WHITE);
        deleteBtn.setFocusPainted(false);
        deleteBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(frame,
                    "Czy na pewno chcesz usunąć pacjenta " + patient.getName() + "?\n" +
                            "Spowoduje to również usunięcie wszystkich wizyt i dokumentów!",
                    "Potwierdzenie", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                dataManager.removePatient(patient.getLogin());
                dataManager.saveAllData();
                refreshAdminPanel("AdminManagePatients");
                cardLayout.show(mainPanel, "AdminManagePatients");
            }
        });

        buttonsPanel.add(editBtn);
        buttonsPanel.add(deleteBtn);

        card.add(infoPanel, BorderLayout.CENTER);
        card.add(buttonsPanel, BorderLayout.EAST);

        return card;
    }

    private void showEditPatientDialog(Patient patient) {
        JDialog dialog = new JDialog(frame, "Edytuj pacjenta", true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JTextField nameField = new JTextField(patient.getName(), 20);
        JTextField peselField = new JTextField(patient.getPesel(), 20);
        JTextField addressField = new JTextField(patient.getAddress(), 20);
        JPasswordField passwordField = new JPasswordField(20);

        gbc.gridx = 0;
        gbc.gridy = 0;
        dialog.add(new JLabel("Imię i nazwisko:"), gbc);
        gbc.gridx = 1;
        dialog.add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        dialog.add(new JLabel("PESEL (11 cyfr):"), gbc);
        gbc.gridx = 1;
        dialog.add(peselField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        dialog.add(new JLabel("Adres:"), gbc);
        gbc.gridx = 1;
        dialog.add(addressField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        dialog.add(new JLabel("Nowe hasło (pozostaw puste aby nie zmieniać):"), gbc);
        gbc.gridx = 1;
        dialog.add(passwordField, gbc);

        JButton saveBtn = createStyledButton("Zapisz", new Color(0, 150, 0));
        saveBtn.addActionListener(e -> {
            String pesel = peselField.getText().trim();
            if (!validatePesel(pesel)) {
                JOptionPane.showMessageDialog(dialog, "Nieprawidłowy numer PESEL! Musi zawierać 11 cyfr i poprawną sumę kontrolną.", "Błąd", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String newPassword = new String(passwordField.getPassword());
            if (!newPassword.isEmpty()) {
                patient.setPassword(newPassword);
            }
            patient.setName(nameField.getText().trim());
            patient.setPesel(pesel);
            patient.setAddress(addressField.getText().trim());

            dataManager.saveAllData();
            JOptionPane.showMessageDialog(dialog, "Dane zaktualizowane!");
            dialog.dispose();
            refreshAdminPanel("AdminManagePatients");
            cardLayout.show(mainPanel, "AdminManagePatients");
        });

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        dialog.add(saveBtn, gbc);

        dialog.pack();
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }

    private JPanel adminManageSchedulePanel() {
        JPanel panel = createStyledPanel("Zarządzanie harmonogramem");
        panel.add(createHeaderPanel("Zarządzanie harmonogramem", "AdminDashboard"), BorderLayout.NORTH);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JPanel schedulePanel = new JPanel(new GridBagLayout());
        schedulePanel.setBorder(BorderFactory.createTitledBorder("Ustawienia harmonogramu"));
        schedulePanel.setBackground(new Color(248, 248, 255));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        gbc.gridx = 0;
        gbc.gridy = 0;
        schedulePanel.add(new JLabel("Godziny NFZ (oddzielone przecinkami):"), gbc);
        JTextField nfzHoursField = new JTextField("09:00,10:00,11:00,12:00,14:00", 30);
        gbc.gridx = 1;
        schedulePanel.add(nfzHoursField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        schedulePanel.add(new JLabel("Godziny prywatne (oddzielone przecinkami):"), gbc);
        JTextField privateHoursField = new JTextField("08:00,09:00,10:00,11:00,12:00,13:00,14:00,15:00,16:00,17:00", 30);
        gbc.gridx = 1;
        schedulePanel.add(privateHoursField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        schedulePanel.add(new JLabel("Dni robocze (1-7, gdzie 1=poniedziałek):"), gbc);
        JTextField workDaysField = new JTextField("1,2,3,4,5", 30);
        gbc.gridx = 1;
        schedulePanel.add(workDaysField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        schedulePanel.add(new JLabel("Liczba dni do przodu:"), gbc);
        JSpinner daysSpinner = new JSpinner(new SpinnerNumberModel(21, 7, 90, 1));
        gbc.gridx = 1;
        schedulePanel.add(daysSpinner, gbc);

        contentPanel.add(schedulePanel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        JButton saveBtn = createStyledButton("💾 Zapisz ustawienia i odśwież harmonogram", new Color(0, 150, 0));
        saveBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        saveBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(panel, "Ustawienia zapisane! Harmonogram zostanie odświeżony.");
            initializeAvailableAppointments();
        });
        contentPanel.add(saveBtn);

        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        JButton refreshBtn = createStyledButton("🔄 Odśwież harmonogram", new Color(70, 130, 200));
        refreshBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        refreshBtn.addActionListener(e -> {
            initializeAvailableAppointments();
            JOptionPane.showMessageDialog(panel, "Harmonogram został odświeżony!");
        });
        contentPanel.add(refreshBtn);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    // ==================== PANELE RECEPCJONISTY ====================

    private JPanel receptionistDashboard() {
        JPanel panel = createStyledPanel("Panel Recepcjonisty");
        panel.add(createHeaderPanel("Panel Recepcjonisty - Dashboard", "Logout"), BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel welcomeLabel = new JLabel("Witaj, " + currentUser.getName() + "!");
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        welcomeLabel.setForeground(new Color(70, 130, 200));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        contentPanel.add(welcomeLabel, gbc);

        String[][] buttons = {
                {"👥 Zarządzaj pacjentami", "ManagePatients"},
                {"📝 Zarejestruj nowego pacjenta", "RegisterPatient"},
                {"📅 Rejestruj wizytę/badanie/zabieg", "ReceptionistRegisterService"},
                {"🔄 Przełóż wizytę", "ReceptionistRescheduleVisit"},
                {"❌ Odwołaj wizytę", "ReceptionistCancelVisit"},
                {"📋 Lista wszystkich wizyt", "AllVisitsList"}
        };

        int row = 1;
        for (String[] btn : buttons) {
            JButton button = createStyledButton(btn[0], new Color(70, 130, 200));
            button.setPreferredSize(new Dimension(320, 50));
            button.addActionListener(e -> {
                refreshReceptionistPanel(btn[1]);
                cardLayout.show(mainPanel, btn[1]);
            });
            gbc.gridy = row;
            contentPanel.add(button, gbc);
            row++;
        }

        panel.add(contentPanel, BorderLayout.CENTER);

        JPanel statsPanel = createReceptionistStatsPanel();
        panel.add(statsPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createReceptionistStatsPanel() {
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Statystyki"),
                BorderFactory.createEmptyBorder(10, 15, 15, 15)
        ));
        statsPanel.setBackground(new Color(248, 248, 255));
        statsPanel.setPreferredSize(new Dimension(300, 400));

        int patientCount = dataManager.getAllPatients().size();
        int activeVisits = 0;
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        int todayVisits = 0;

        for (Patient p : dataManager.getAllPatients().values()) {
            for (Visit v : p.getRegisteredVisits()) {
                activeVisits++;
                if (v.getDateTime().startsWith(today)) {
                    todayVisits++;
                }
            }
        }

        statsPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        statsPanel.add(new JLabel("📊 Liczba pacjentów: " + patientCount));
        statsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        statsPanel.add(new JLabel("📅 Aktywne wizyty: " + activeVisits));
        statsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        statsPanel.add(new JLabel("⏰ Wizyty dzisiaj: " + todayVisits));
        statsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        statsPanel.add(new JLabel("👨‍⚕️ Liczba lekarzy: " + dataManager.getDoctors().size()));

        return statsPanel;
    }

    private JPanel managePatientsPanel() {

        JPanel panel = createStyledPanel("Zarządzanie pacjentami");
        panel.setLayout(new BorderLayout());

        panel.add(
                createHeaderPanel("Zarządzanie pacjentami", "ReceptionistDashboard"),
                BorderLayout.NORTH
        );

        // ===== GŁÓWNY PANEL =====
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // ===== PANEL WYSZUKIWANIA =====
        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JTextField searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(300, 35));

        JButton searchBtn = createStyledButton(
                "Szukaj",
                new Color(0, 120, 215)
        );

        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchBtn, BorderLayout.EAST);

        contentPanel.add(searchPanel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // ===== PANEL Z LISTĄ PACJENTÓW =====
        JPanel patientsListPanel = new JPanel();
        patientsListPanel.setLayout(new BoxLayout(patientsListPanel, BoxLayout.Y_AXIS));
        patientsListPanel.setBackground(Color.WHITE);

        // ===== FUNKCJA ODŚWIEŻANIA =====
        Runnable refreshPatients = () -> {

            patientsListPanel.removeAll();

            String searchText = searchField.getText().trim().toLowerCase();

            for (Patient patient : dataManager.getAllPatients().values()) {

                boolean matches = searchText.isEmpty()

                        || patient.getName().toLowerCase().contains(searchText)

                        || patient.getPesel().contains(searchText);

                if (matches) {

                    JPanel patientCard = createReceptionistPatientCard(patient);

                    patientsListPanel.add(patientCard);
                    patientsListPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                }
            }

            patientsListPanel.revalidate();
            patientsListPanel.repaint();
        };

        // ===== PIERWSZE ZAŁADOWANIE =====
        refreshPatients.run();

        // ===== WYSZUKIWANIE PO KLIKNIĘCIU =====
        searchBtn.addActionListener(e -> refreshPatients.run());

        // ===== WYSZUKIWANIE PODCZAS PISANIA =====
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {

            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                refreshPatients.run();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                refreshPatients.run();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                refreshPatients.run();
            }
        });

        contentPanel.add(patientsListPanel);

        // ===== SCROLL =====
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createReceptionistPatientCard(Patient patient) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        card.setBackground(new Color(248, 248, 255));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        infoPanel.add(new JLabel("👤 " + patient.getName()));
        infoPanel.add(new JLabel("📧 Login: " + patient.getLogin()));
        infoPanel.add(new JLabel("🆔 PESEL: " + patient.getPesel()));
        infoPanel.add(new JLabel("📍 Adres: " + patient.getAddress()));
        infoPanel.add(new JLabel("📋 Aktywne wizyty: " + patient.getRegisteredVisits().size()));

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonsPanel.setOpaque(false);

        JButton viewVisitsBtn = new JButton("📋 Pokaż wizyty");
        viewVisitsBtn.setBackground(new Color(70, 130, 200));
        viewVisitsBtn.setForeground(Color.WHITE);
        viewVisitsBtn.setFocusPainted(false);
        viewVisitsBtn.addActionListener(e -> showPatientVisits(patient));

        JButton addVisitBtn = new JButton("➕ Dodaj wizytę");
        addVisitBtn.setBackground(new Color(0, 150, 0));
        addVisitBtn.setForeground(Color.WHITE);
        addVisitBtn.setFocusPainted(false);
        addVisitBtn.addActionListener(e -> showRegisterForPatient(patient));

        buttonsPanel.add(viewVisitsBtn);
        buttonsPanel.add(addVisitBtn);

        card.add(infoPanel, BorderLayout.CENTER);
        card.add(buttonsPanel, BorderLayout.EAST);

        return card;
    }

    private JPanel registerPatientPanel() {

        JPanel panel = createStyledPanel("Rejestracja nowego pacjenta");
        panel.setLayout(new BorderLayout());

        panel.add(
                createHeaderPanel("Rejestracja nowego pacjenta", "ReceptionistDashboard"),
                BorderLayout.NORTH
        );

        // Formularz
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);

        // Ładniejsza ramka
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));

        // Rozmiar formularza
        formPanel.setPreferredSize(new Dimension(500, 360));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // ===== LOGIN =====
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Login:"), gbc);

        JTextField loginField = new JTextField(20);

        gbc.gridx = 1;
        formPanel.add(loginField, gbc);

        // ===== HASŁO =====
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Hasło:"), gbc);

        JPasswordField passwordField = new JPasswordField(20);

        gbc.gridx = 1;
        formPanel.add(passwordField, gbc);

        // ===== IMIĘ I NAZWISKO =====
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Imię i nazwisko:"), gbc);

        JTextField nameField = new JTextField(20);

        gbc.gridx = 1;
        formPanel.add(nameField, gbc);

        // ===== PESEL =====
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("PESEL (11 cyfr):"), gbc);

        JTextField peselField = new JTextField(20);

        gbc.gridx = 1;
        formPanel.add(peselField, gbc);

        // ===== ADRES =====
        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(new JLabel("Adres:"), gbc);

        JTextField addressField = new JTextField(20);

        gbc.gridx = 1;
        formPanel.add(addressField, gbc);

        // ===== PRZYCISK =====
        JButton registerBtn = createStyledButton(
                "Zarejestruj pacjenta",
                new Color(0, 150, 0)
        );

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;

        formPanel.add(registerBtn, gbc);

        // ===== AKCJA PRZYCISKU =====
        registerBtn.addActionListener(e -> {

            String login = loginField.getText().trim();
            String password = new String(passwordField.getPassword());
            String name = nameField.getText().trim();
            String pesel = peselField.getText().trim();
            String address = addressField.getText().trim();

            if (login.isEmpty() ||
                    password.isEmpty() ||
                    name.isEmpty() ||
                    pesel.isEmpty()) {

                JOptionPane.showMessageDialog(
                        panel,
                        "Wszystkie pola są wymagane!",
                        "Błąd",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            if (!validatePesel(pesel)) {

                JOptionPane.showMessageDialog(
                        panel,
                        "Nieprawidłowy numer PESEL! Musi zawierać 11 cyfr i poprawną sumę kontrolną.",
                        "Błąd",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            if (dataManager.getPatient(login) != null) {

                JOptionPane.showMessageDialog(
                        panel,
                        "Pacjent z takim loginem już istnieje!",
                        "Błąd",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            Patient newPatient = new Patient(
                    login,
                    password,
                    name,
                    pesel,
                    address
            );

            dataManager.addPatient(newPatient);
            dataManager.saveAllData();

            JOptionPane.showMessageDialog(
                    panel,
                    "Pacjent został pomyślnie zarejestrowany!"
            );

            // Czyszczenie pól
            loginField.setText("");
            passwordField.setText("");
            nameField.setText("");
            peselField.setText("");
            addressField.setText("");
        });

        // ===== PANEL CENTRUJĄCY =====
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setBackground(new Color(245, 247, 250));

        centerWrapper.add(formPanel);

        panel.add(centerWrapper, BorderLayout.CENTER);

        return panel;
    }

    private JPanel receptionistRegisterServicePanel() {

        JPanel panel = createStyledPanel("Rejestracja usługi");
        panel.setLayout(new BorderLayout());

        panel.add(
                createHeaderPanel("Rejestracja na wizytę/badanie/zabieg", "ReceptionistDashboard"),
                BorderLayout.NORTH
        );

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // =========================
        // 🔎 SEARCH PACJENT
        // =========================
        gbc.gridx = 0;
        gbc.gridy = 0;
        contentPanel.add(new JLabel("Szukaj pacjenta:"), gbc);

        JTextField patientSearchField = new JTextField();
        gbc.gridx = 1;
        contentPanel.add(patientSearchField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        contentPanel.add(new JLabel("Pacjent:"), gbc);

        JComboBox<String> patientCombo = new JComboBox<>();
        patientCombo.setPreferredSize(new Dimension(300, 30));
        gbc.gridx = 1;
        contentPanel.add(patientCombo, gbc);

        // =========================
        // 🔎 SEARCH LEKARZ / USŁUGA
        // =========================
        gbc.gridx = 0;
        gbc.gridy = 2;
        contentPanel.add(new JLabel("Szukaj usługi/lekarza:"), gbc);

        JTextField serviceSearchField = new JTextField();
        gbc.gridx = 1;
        contentPanel.add(serviceSearchField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        contentPanel.add(new JLabel("Rodzaj usługi:"), gbc);

        JComboBox<String> serviceTypeCombo =
                new JComboBox<>(new String[]{
                        "Wizyta lekarska",
                        "Badanie laboratoryjne",
                        "Zabieg ambulatoryjny"
                });
        gbc.gridx = 1;
        contentPanel.add(serviceTypeCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        contentPanel.add(new JLabel("Wybierz:"), gbc);

        JComboBox<String> serviceCombo = new JComboBox<>();
        gbc.gridx = 1;
        contentPanel.add(serviceCombo, gbc);

        // =========================
        // PAYMENT
        // =========================
        gbc.gridx = 0;
        gbc.gridy = 5;
        contentPanel.add(new JLabel("Forma płatności:"), gbc);

        JComboBox<String> paymentCombo =
                new JComboBox<>(new String[]{"NFZ (refundowana)", "Płatna prywatnie"});
        gbc.gridx = 1;
        contentPanel.add(paymentCombo, gbc);

        // =========================
        // TERMINY
        // =========================
        JPanel appointmentsPanel = new JPanel();
        appointmentsPanel.setLayout(new BoxLayout(appointmentsPanel, BoxLayout.Y_AXIS));
        appointmentsPanel.setBackground(Color.WHITE);
        appointmentsPanel.setBorder(BorderFactory.createTitledBorder("Dostępne terminy"));

        JScrollPane scrollPane = new JScrollPane(appointmentsPanel);
        scrollPane.setPreferredSize(new Dimension(600, 300));

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        contentPanel.add(scrollPane, gbc);

        // =========================
        // REFRESH APPOINTMENTS
        // =========================
        Runnable refreshAppointments = () -> {

            appointmentsPanel.removeAll();

            String selectedType = (String) serviceTypeCombo.getSelectedItem();
            String selected = (String) serviceCombo.getSelectedItem();
            String paymentType = (String) paymentCombo.getSelectedItem();
            String paymentCode = paymentType.equals("NFZ (refundowana)") ? "NFZ" : "PRIVATE";

            if (selected == null) return;

            Map<String, List<AvailableAppointment>> grouped = new LinkedHashMap<>();

            if (selectedType.equals("Wizyta lekarska")) {

                String doctorName = selected.split(" \\(")[0];

                for (AvailableAppointment app : availableAppointments) {
                    if (app.getDoctorName().equals(doctorName)
                            && app.isAvailable()
                            && app.getPaymentType().equals(paymentCode)) {

                        grouped.computeIfAbsent(app.getDate(), k -> new ArrayList<>()).add(app);
                    }
                }

            } else if (selectedType.equals("Badanie laboratoryjne")) {

                for (AvailableAppointment app : availableLaboratoryTests) {
                    if (app.getDoctorName().equals(selected)
                            && app.isAvailable()
                            && app.getPaymentType().equals(paymentCode)) {

                        grouped.computeIfAbsent(app.getDate(), k -> new ArrayList<>()).add(app);
                    }
                }

            } else if (selectedType.equals("Zabieg ambulatoryjny")) {

                for (AvailableAppointment app : availableProcedures) {
                    if (app.getDoctorName().equals(selected)
                            && app.isAvailable()
                            && app.getPaymentType().equals(paymentCode)) {

                        grouped.computeIfAbsent(app.getDate(), k -> new ArrayList<>()).add(app);
                    }
                }
            }

            if (grouped.isEmpty()) {
                appointmentsPanel.add(new JLabel("Brak dostępnych terminów"));
            } else {

                for (var entry : grouped.entrySet()) {

                    JPanel dayPanel = new JPanel();
                    dayPanel.setLayout(new BoxLayout(dayPanel, BoxLayout.Y_AXIS));
                    dayPanel.setBorder(BorderFactory.createTitledBorder(entry.getKey()));
                    dayPanel.setBackground(Color.WHITE);

                    JPanel hours = new JPanel(new FlowLayout(FlowLayout.LEFT));

                    for (AvailableAppointment app : entry.getValue()) {

                        JButton btn = new JButton(app.getHour());
                        btn.setBackground(new Color(70, 130, 200));
                        btn.setForeground(Color.WHITE);

                        btn.addActionListener(ev -> {
                            panel.putClientProperty("selectedDate", app.getDate());
                            panel.putClientProperty("selectedHour", app.getHour());
                            panel.putClientProperty("selectedServiceName", app.getDoctorName());
                            panel.putClientProperty("selectedSpecialization", app.getSpecialization());
                            panel.putClientProperty("selectedVisitType", app.getType());
                            panel.putClientProperty("selectedPaymentType", paymentCode);

                            JOptionPane.showMessageDialog(panel,
                                    "Wybrano: " + app.getDate() + " " + app.getHour());
                        });

                        hours.add(btn);
                    }

                    dayPanel.add(hours);
                    appointmentsPanel.add(dayPanel);
                    appointmentsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                }
            }

            appointmentsPanel.revalidate();
            appointmentsPanel.repaint();
        };

        // =========================
        // 🔎 REFRESH PACJENTÓW (SEARCH)
        // =========================
        Runnable refreshPatients = () -> {

            patientCombo.removeAllItems();

            String q = patientSearchField.getText().trim().toLowerCase();

            for (Patient p : dataManager.getAllPatients().values()) {

                String full = (p.getLogin() + " " + p.getName() + " " + p.getPesel()).toLowerCase();

                if (q.isEmpty() || full.contains(q)) {
                    patientCombo.addItem(p.getLogin() + " - " + p.getName());
                }
            }
        };

        // =========================
        // 🔎 REFRESH LEKARZY / USŁUG
        // =========================
        Runnable refreshServices = () -> {

            serviceCombo.removeAllItems();

            String type = (String) serviceTypeCombo.getSelectedItem();
            String q = serviceSearchField.getText().trim().toLowerCase();

            if (type.equals("Wizyta lekarska")) {

                for (Doctor d : dataManager.getDoctors()) {

                    String full = (d.getName() + " " + d.getSpecialization()).toLowerCase();

                    if (q.isEmpty() || full.contains(q)) {
                        serviceCombo.addItem(d.getName() + " (" + d.getSpecialization() + ")");
                    }
                }

            } else if (type.equals("Badanie laboratoryjne")) {

                String[] lab = {"Pobranie krwi", "EKG", "USG", "Holter EKG", "RTG klatki piersiowej"};

                for (String s : lab) {
                    if (q.isEmpty() || s.toLowerCase().contains(q)) {
                        serviceCombo.addItem(s);
                    }
                }

            } else {

                String[] proc = {"Zastrzyk domięśniowy", "Szczepienie", "Usunięcie szwów", "Opracowanie rany"};

                for (String s : proc) {
                    if (q.isEmpty() || s.toLowerCase().contains(q)) {
                        serviceCombo.addItem(s);
                    }
                }
            }
        };

        // =========================
        // LISTENERS
        // =========================
        patientSearchField.getDocument().addDocumentListener(new SimpleDocListener(refreshPatients));
        serviceSearchField.getDocument().addDocumentListener(new SimpleDocListener(refreshServices));

        serviceTypeCombo.addActionListener(e -> {
            refreshServices.run();
            refreshAppointments.run();
        });

        serviceCombo.addActionListener(e -> refreshAppointments.run());
        paymentCombo.addActionListener(e -> refreshAppointments.run());

        // initial load
        refreshPatients.run();
        refreshServices.run();

        panel.add(contentPanel, BorderLayout.CENTER);
        return panel;
    }

    // helper
    private class SimpleDocListener implements javax.swing.event.DocumentListener {

        private final Runnable r;

        public SimpleDocListener(Runnable r) {
            this.r = r;
        }

        public void insertUpdate(javax.swing.event.DocumentEvent e) { r.run(); }
        public void removeUpdate(javax.swing.event.DocumentEvent e) { r.run(); }
        public void changedUpdate(javax.swing.event.DocumentEvent e) { r.run(); }
    }

    private JPanel receptionistRescheduleVisitPanel() {

        JPanel panel = createStyledPanel("Przełożenie usługi");
        panel.setLayout(new BorderLayout());

        panel.add(
                createHeaderPanel("Przełożenie wizyty/badania/zabiegu", "ReceptionistDashboard"),
                BorderLayout.NORTH
        );

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // =========================
        // 🔎 WYSZUKIWANIE PACJENTA
        // =========================
        gbc.gridx = 0;
        gbc.gridy = 0;
        contentPanel.add(new JLabel("Szukaj pacjenta:"), gbc);

        JTextField patientSearchField = new JTextField();
        patientSearchField.setPreferredSize(new Dimension(300, 30));
        gbc.gridx = 1;
        contentPanel.add(patientSearchField, gbc);

        // =========================
        // PACJENT COMBO
        // =========================
        gbc.gridx = 0;
        gbc.gridy = 1;
        contentPanel.add(new JLabel("Pacjent:"), gbc);

        JComboBox<String> patientCombo = new JComboBox<>();
        patientCombo.setPreferredSize(new Dimension(300, 30));
        gbc.gridx = 1;
        contentPanel.add(patientCombo, gbc);

        // =========================
        // WIZYTA COMBO
        // =========================
        gbc.gridx = 0;
        gbc.gridy = 2;
        contentPanel.add(new JLabel("Wybierz wizytę do przełożenia:"), gbc);

        JComboBox<Visit> visitCombo = new JComboBox<>();
        visitCombo.setPreferredSize(new Dimension(400, 30));
        gbc.gridx = 1;
        contentPanel.add(visitCombo, gbc);

        // =========================
        // TERMINY
        // =========================
        gbc.gridx = 0;
        gbc.gridy = 3;
        contentPanel.add(new JLabel("Nowy termin:"), gbc);

        JPanel appointmentsPanel = new JPanel();
        appointmentsPanel.setLayout(new BoxLayout(appointmentsPanel, BoxLayout.Y_AXIS));
        appointmentsPanel.setBackground(Color.WHITE);
        appointmentsPanel.setBorder(BorderFactory.createTitledBorder("Dostępne terminy"));

        JScrollPane scrollPane = new JScrollPane(appointmentsPanel);
        scrollPane.setPreferredSize(new Dimension(500, 220));
        gbc.gridx = 1;
        contentPanel.add(scrollPane, gbc);

        // =========================
        // PRZYCISK
        // =========================
        JButton rescheduleBtn = createStyledButton(
                "🔄 Przełóż wizytę",
                new Color(70, 130, 200)
        );

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        contentPanel.add(rescheduleBtn, gbc);

        // =========================
        // 🔎 REFRESH PACJENTÓW (SEARCH)
        // =========================
        Runnable refreshPatients = () -> {

            patientCombo.removeAllItems();

            String query = patientSearchField.getText().trim().toLowerCase();

            for (Patient p : dataManager.getAllPatients().values()) {

                String searchData = (
                        p.getLogin() + " " +
                                p.getName() + " " +
                                p.getPesel()
                ).toLowerCase();

                if (query.isEmpty() || searchData.contains(query)) {
                    patientCombo.addItem(p.getLogin() + " - " + p.getName());
                }
            }
        };

        patientSearchField.getDocument().addDocumentListener(new SimpleDocListener(refreshPatients));

        // =========================
        // PACJENT -> WIZYTY
        // =========================
        patientCombo.addActionListener(e -> {

            visitCombo.removeAllItems();

            String selected = (String) patientCombo.getSelectedItem();
            if (selected == null) return;

            String login = selected.split(" - ")[0];
            Patient patient = dataManager.getPatient(login);

            if (patient != null) {
                for (Visit visit : patient.getRegisteredVisits()) {
                    visitCombo.addItem(visit);
                }
            }
        });

        // =========================
        // WYBÓR TERMINU (twoja logika)
        // =========================
        visitCombo.addActionListener(e -> {

            appointmentsPanel.removeAll();

            Visit selectedVisit = (Visit) visitCombo.getSelectedItem();
            if (selectedVisit == null) return;

            String paymentCode =
                    selectedVisit.getPaymentStatus().equals("NFZ") ? "NFZ" : "PRIVATE";

            Map<String, List<AvailableAppointment>> groupedByDate = new LinkedHashMap<>();

            if (selectedVisit.getType().equals("VISIT")) {

                for (AvailableAppointment app : availableAppointments) {
                    if (app.isAvailable() && app.getPaymentType().equals(paymentCode)) {
                        groupedByDate
                                .computeIfAbsent(app.getDate(), k -> new ArrayList<>())
                                .add(app);
                    }
                }

            } else if (selectedVisit.getType().equals("LAB")) {

                for (AvailableAppointment app : availableLaboratoryTests) {
                    if (app.isAvailable() && app.getPaymentType().equals(paymentCode)) {
                        groupedByDate
                                .computeIfAbsent(app.getDate(), k -> new ArrayList<>())
                                .add(app);
                    }
                }

            } else if (selectedVisit.getType().equals("PROC")) {

                for (AvailableAppointment app : availableProcedures) {
                    if (app.isAvailable() && app.getPaymentType().equals(paymentCode)) {
                        groupedByDate
                                .computeIfAbsent(app.getDate(), k -> new ArrayList<>())
                                .add(app);
                    }
                }
            }

            if (groupedByDate.isEmpty()) {
                appointmentsPanel.add(new JLabel("Brak dostępnych terminów"));
            } else {

                for (var entry : groupedByDate.entrySet()) {

                    JPanel datePanel = new JPanel();
                    datePanel.setLayout(new BoxLayout(datePanel, BoxLayout.Y_AXIS));
                    datePanel.setBorder(BorderFactory.createTitledBorder(entry.getKey()));
                    datePanel.setBackground(Color.WHITE);

                    JPanel hoursPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

                    for (AvailableAppointment app : entry.getValue()) {

                        JButton hourBtn = new JButton(app.getHour());
                        hourBtn.setBackground(new Color(70, 130, 200));
                        hourBtn.setForeground(Color.WHITE);

                        hourBtn.addActionListener(ev -> {

                            panel.putClientProperty("newSelectedDate", app.getDate());
                            panel.putClientProperty("newSelectedHour", app.getHour());
                            panel.putClientProperty("newServiceName", app.getDoctorName());
                            panel.putClientProperty("newVisitType", app.getType());
                            panel.putClientProperty("newPaymentType", paymentCode);

                            JOptionPane.showMessageDialog(
                                    panel,
                                    "Wybrano nowy termin: " +
                                            app.getDate() + " " + app.getHour()
                            );
                        });

                        hoursPanel.add(hourBtn);
                    }

                    datePanel.add(hoursPanel);
                    appointmentsPanel.add(datePanel);
                    appointmentsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                }
            }

            appointmentsPanel.revalidate();
            appointmentsPanel.repaint();
        });

        // =========================
        // PRZYCISK PRZEŁOŻENIA
        // =========================
        rescheduleBtn.addActionListener(e -> {

            Visit selectedVisit = (Visit) visitCombo.getSelectedItem();
            String selectedPatient = (String) patientCombo.getSelectedItem();

            String newDate = (String) panel.getClientProperty("newSelectedDate");
            String newHour = (String) panel.getClientProperty("newSelectedHour");
            String newServiceName = (String) panel.getClientProperty("newServiceName");
            String newVisitType = (String) panel.getClientProperty("newVisitType");
            String newPaymentCode = (String) panel.getClientProperty("newPaymentType");

            if (selectedVisit == null || newDate == null || newHour == null) {
                JOptionPane.showMessageDialog(
                        panel,
                        "Wybierz wizytę i nowy termin!",
                        "Błąd",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            String login = selectedPatient.split(" - ")[0];
            Patient patient = dataManager.getPatient(login);

            if (patient == null) return;

            selectedVisit.setDateTime(newDate + " " + newHour);

            if (newServiceName != null) {
                selectedVisit.setDoctorName(newServiceName);
            }

            if (newVisitType != null) {
                selectedVisit.setType(newVisitType);
            }

            dataManager.saveAllData();

            JOptionPane.showMessageDialog(panel, "Wizyta została przełożona!");
            cardLayout.show(mainPanel, "ReceptionistDashboard");
        });

        panel.add(contentPanel, BorderLayout.CENTER);

        refreshPatients.run();

        return panel;
    }
    private JPanel receptionistCancelVisitPanel() {

        JPanel panel = createStyledPanel("Odwołanie usługi");
        panel.setLayout(new BorderLayout());

        panel.add(
                createHeaderPanel("Odwołanie wizyty/badania/zabiegu", "ReceptionistDashboard"),
                BorderLayout.NORTH
        );

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // =========================
        // 🔎 SEARCH PACJENTA
        // =========================
        gbc.gridx = 0;
        gbc.gridy = 0;
        contentPanel.add(new JLabel("Szukaj pacjenta:"), gbc);

        JTextField searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(300, 30));
        gbc.gridx = 1;
        contentPanel.add(searchField, gbc);

        // =========================
        // PACJENT COMBO
        // =========================
        gbc.gridx = 0;
        gbc.gridy = 1;
        contentPanel.add(new JLabel("Pacjent:"), gbc);

        JComboBox<String> patientCombo = new JComboBox<>();
        patientCombo.setPreferredSize(new Dimension(300, 30));
        gbc.gridx = 1;
        contentPanel.add(patientCombo, gbc);

        // =========================
        // WIZYTA COMBO
        // =========================
        gbc.gridx = 0;
        gbc.gridy = 2;
        contentPanel.add(new JLabel("Wizyta do odwołania:"), gbc);

        JComboBox<Visit> visitCombo = new JComboBox<>();
        visitCombo.setPreferredSize(new Dimension(400, 30));
        gbc.gridx = 1;
        contentPanel.add(visitCombo, gbc);

        // =========================
        // PRZYCISK
        // =========================
        JButton cancelBtn = createStyledButton(
                "❌ Odwołaj wizytę",
                new Color(200, 0, 0)
        );

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        contentPanel.add(cancelBtn, gbc);

        // =========================
        // 🔎 REFRESH PACJENTÓW (SEARCH)
        // =========================
        Runnable refreshPatients = () -> {

            patientCombo.removeAllItems();

            String query = searchField.getText().trim().toLowerCase();

            for (Patient p : dataManager.getAllPatients().values()) {

                String data = (
                        p.getLogin() + " " +
                                p.getName() + " " +
                                p.getPesel()
                ).toLowerCase();

                if (query.isEmpty() || data.contains(query)) {
                    patientCombo.addItem(p.getLogin() + " - " + p.getName());
                }
            }
        };

        searchField.getDocument().addDocumentListener(new SimpleDocListener(refreshPatients));

        // =========================
        // PACJENT -> WIZYTY
        // =========================
        patientCombo.addActionListener(e -> {

            visitCombo.removeAllItems();

            String selected = (String) patientCombo.getSelectedItem();
            if (selected == null) return;

            String login = selected.split(" - ")[0];
            Patient patient = dataManager.getPatient(login);

            if (patient != null) {
                for (Visit visit : patient.getRegisteredVisits()) {
                    visitCombo.addItem(visit);
                }
            }
        });

        // =========================
        // CANCEL LOGIC (twoja)
        // =========================
        cancelBtn.addActionListener(e -> {

            Visit selectedVisit = (Visit) visitCombo.getSelectedItem();
            String selectedPatient = (String) patientCombo.getSelectedItem();

            if (selectedVisit == null) {
                JOptionPane.showMessageDialog(
                        panel,
                        "Wybierz wizytę do odwołania!",
                        "Błąd",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(
                    panel,
                    "Czy na pewno chcesz odwołać tę wizytę?",
                    "Potwierdzenie",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm != JOptionPane.YES_OPTION) return;

            String login = selectedPatient.split(" - ")[0];
            Patient patient = dataManager.getPatient(login);

            if (patient != null) {

                patient.getRegisteredVisits().remove(selectedVisit);

                String[] dateTime = selectedVisit.getDateTime().split(" ");
                String paymentCode =
                        selectedVisit.getPaymentStatus().equals("NFZ") ? "NFZ" : "PRIVATE";

                if (selectedVisit.getType().equals("VISIT")) {

                    for (AvailableAppointment app : availableAppointments) {
                        if (app.getDoctorName().equals(selectedVisit.getDoctorName())
                                && app.getDate().equals(dateTime[0])
                                && app.getHour().equals(dateTime[1])
                                && app.getPaymentType().equals(paymentCode)) {

                            app.setAvailable(true);
                            break;
                        }
                    }

                } else if (selectedVisit.getType().equals("LAB")) {

                    for (AvailableAppointment app : availableLaboratoryTests) {
                        if (app.getDoctorName().equals(selectedVisit.getDoctorName())
                                && app.getDate().equals(dateTime[0])
                                && app.getHour().equals(dateTime[1])
                                && app.getPaymentType().equals(paymentCode)) {

                            app.setAvailable(true);
                            break;
                        }
                    }

                } else if (selectedVisit.getType().equals("PROC")) {

                    for (AvailableAppointment app : availableProcedures) {
                        if (app.getDoctorName().equals(selectedVisit.getDoctorName())
                                && app.getDate().equals(dateTime[0])
                                && app.getHour().equals(dateTime[1])
                                && app.getPaymentType().equals(paymentCode)) {

                            app.setAvailable(true);
                            break;
                        }
                    }
                }

                dataManager.saveAllData();

                JOptionPane.showMessageDialog(panel, "Wizyta została odwołana!");
                cardLayout.show(mainPanel, "ReceptionistDashboard");
            }
        });

        panel.add(contentPanel, BorderLayout.CENTER);

        refreshPatients.run();

        return panel;
    }

    private JPanel allVisitsListPanel() {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        panel.add(
                createHeaderPanel(
                        "Lista wszystkich wizyt, badań i zabiegów",
                        "ReceptionistDashboard"
                ),
                BorderLayout.NORTH
        );

        // =========================
        // FILTER PANEL
        // =========================
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBackground(Color.WHITE);
        filterPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel filterLabel = new JLabel("Szukaj lekarza:");
        filterLabel.setFont(new Font("SansSerif", Font.BOLD, 12));

        JTextField doctorSearchField = new JTextField();
        doctorSearchField.setPreferredSize(new Dimension(200, 28));

        JComboBox<String> doctorCombo = new JComboBox<>();
        doctorCombo.addItem("Wszyscy lekarze");

        filterPanel.add(filterLabel);
        filterPanel.add(doctorSearchField);
        filterPanel.add(doctorCombo);

        // =========================
        // CONTENT PANEL
        // =========================
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // =========================
        // REFRESH DOCTOR LIST (SEARCH)
        // =========================
        Runnable refreshDoctors = () -> {

            doctorCombo.removeAllItems();
            doctorCombo.addItem("Wszyscy lekarze");

            String q = doctorSearchField.getText().trim().toLowerCase();

            for (Doctor doctor : dataManager.getDoctors()) {

                String data = (doctor.getName() + " " + doctor.getSpecialization()).toLowerCase();

                if (q.isEmpty() || data.contains(q)) {
                    doctorCombo.addItem(doctor.getName());
                }
            }
        };

        // =========================
        // REFRESH VISITS LIST
        // =========================
        Runnable refreshList = () -> {

            contentPanel.removeAll();

            String selectedDoctor = (String) doctorCombo.getSelectedItem();

            for (Patient patient : dataManager.getAllPatients().values()) {

                for (Visit visit : patient.getRegisteredVisits()) {

                    boolean match =
                            selectedDoctor == null
                                    || selectedDoctor.equals("Wszyscy lekarze")
                                    || visit.getDoctorName().equals(selectedDoctor);

                    if (match) {

                        JPanel visitCard = createReceptionistVisitCard(visit, patient);
                        contentPanel.add(visitCard);
                        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                    }
                }
            }

            if (contentPanel.getComponentCount() == 0) {

                JLabel empty = new JLabel("Brak wizyt dla wybranego lekarza");
                empty.setFont(new Font("SansSerif", Font.ITALIC, 14));
                empty.setAlignmentX(Component.CENTER_ALIGNMENT);

                contentPanel.add(empty);
            }

            contentPanel.revalidate();
            contentPanel.repaint();
        };

        // =========================
        // EVENTS
        // =========================
        doctorSearchField.getDocument().addDocumentListener(new SimpleDocListener(() -> {
            refreshDoctors.run();
            refreshList.run();
        }));

        doctorCombo.addActionListener(e -> refreshList.run());

        // =========================
        // INITIAL LOAD
        // =========================
        refreshDoctors.run();
        refreshList.run();

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.WHITE);
        centerPanel.add(filterPanel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        panel.add(centerPanel, BorderLayout.CENTER);

        return panel;
    }
    private JPanel createReceptionistVisitCard(Visit visit, Patient patient) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        card.setBackground(new Color(248, 248, 255));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        String icon = visit.getType().equals("LAB") ? "🔬" : (visit.getType().equals("PROC") ? "💉" : "👨‍⚕️");
        infoPanel.add(new JLabel(icon + " Pacjent: " + patient.getName()));
        infoPanel.add(new JLabel("👨‍⚕️ Lekarz: " + visit.getDoctorName()));
        infoPanel.add(new JLabel("📅 Data: " + visit.getDateTime()));
        infoPanel.add(new JLabel("📋 Typ: " + visit.getSpecialization()));
        infoPanel.add(new JLabel("💰 Płatność: " + (visit.getPaymentStatus().equals("NFZ") ? "NFZ" : "Prywatna")));

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonsPanel.setOpaque(false);

        JButton rescheduleBtn = new JButton("🔄 Przełóż");
        rescheduleBtn.setBackground(new Color(70, 130, 200));
        rescheduleBtn.setForeground(Color.WHITE);
        rescheduleBtn.setFocusPainted(false);
        rescheduleBtn.addActionListener(e -> {
            // Przejście do panelu przesunięcia
            cardLayout.show(mainPanel, "ReceptionistRescheduleVisit");
        });

        JButton cancelBtn = new JButton("❌ Odwołaj");
        cancelBtn.setBackground(new Color(200, 0, 0));
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setFocusPainted(false);
        cancelBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(frame,
                    "Czy na pewno chcesz odwołać tę wizytę?",
                    "Potwierdzenie", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                patient.getRegisteredVisits().remove(visit);

                String[] dateTime = visit.getDateTime().split(" ");
                String paymentCode = visit.getPaymentStatus().equals("NFZ") ? "NFZ" : "PRIVATE";

                if (visit.getType().equals("VISIT")) {
                    for (AvailableAppointment app : availableAppointments) {
                        if (app.getDoctorName().equals(visit.getDoctorName()) &&
                                app.getDate().equals(dateTime[0]) && app.getHour().equals(dateTime[1]) &&
                                app.getPaymentType().equals(paymentCode)) {
                            app.setAvailable(true);
                            break;
                        }
                    }
                } else if (visit.getType().equals("LAB")) {
                    for (AvailableAppointment app : availableLaboratoryTests) {
                        if (app.getDoctorName().equals(visit.getDoctorName()) &&
                                app.getDate().equals(dateTime[0]) && app.getHour().equals(dateTime[1]) &&
                                app.getPaymentType().equals(paymentCode)) {
                            app.setAvailable(true);
                            break;
                        }
                    }
                } else if (visit.getType().equals("PROC")) {
                    for (AvailableAppointment app : availableProcedures) {
                        if (app.getDoctorName().equals(visit.getDoctorName()) &&
                                app.getDate().equals(dateTime[0]) && app.getHour().equals(dateTime[1]) &&
                                app.getPaymentType().equals(paymentCode)) {
                            app.setAvailable(true);
                            break;
                        }
                    }
                }

                dataManager.saveAllData();
                refreshReceptionistPanel("AllVisitsList");
                cardLayout.show(mainPanel, "AllVisitsList");
            }
        });

        buttonsPanel.add(rescheduleBtn);
        buttonsPanel.add(cancelBtn);

        card.add(infoPanel, BorderLayout.CENTER);
        card.add(buttonsPanel, BorderLayout.EAST);

        return card;
    }

    // ==================== PANELE PACJENTA ====================

    private JPanel patientDashboard() {
        JPanel panel = createStyledPanel("Panel Pacjenta");
        panel.add(createHeaderPanel("Panel Pacjenta - Dashboard", "Logout"), BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        if (currentUser instanceof Patient) {
            Patient patient = (Patient) currentUser;
            JLabel welcomeLabel = new JLabel("Witaj, " + patient.getName() + "!");
            welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
            welcomeLabel.setForeground(new Color(70, 130, 200));
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 2;
            contentPanel.add(welcomeLabel, gbc);
        }

        String[][] buttons = {
                {"📅 Wizyty, badania i zabiegi", "VisitMenu"},
                {"📊 Wyniki badań", "TestResults"},
                {"💊 Recepty", "Prescriptions"},
                {"📜 Historia wizyt i zabiegów", "History"},
                {"📄 Skierowania", "Referrals"},
                {"👨‍⚕️ Lista lekarzy", "DoctorsList"},
                {"📋 Zwolnienia lekarskie", "SickLeave"}
        };

        int row = 1;
        for (String[] btn : buttons) {
            JButton button = createStyledButton(btn[0], new Color(70, 130, 200));
            button.setPreferredSize(new Dimension(320, 50));
            button.addActionListener(e -> {
                refreshPanel(btn[1]);
                cardLayout.show(mainPanel, btn[1]);
            });
            gbc.gridy = row;
            contentPanel.add(button, gbc);
            row++;
        }

        panel.add(contentPanel, BorderLayout.CENTER);

        JPanel visitsPanel = new JPanel();
        visitsPanel.setLayout(new BoxLayout(visitsPanel, BoxLayout.Y_AXIS));
        visitsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Twoje zarejestrowane usługi"),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        visitsPanel.setBackground(new Color(248, 248, 255));
        visitsPanel.setPreferredSize(new Dimension(380, 500));

        if (currentUser instanceof Patient) {
            Patient patient = (Patient) currentUser;
            if (patient.getRegisteredVisits().isEmpty()) {
                JLabel emptyLabel = new JLabel("Brak zarejestrowanych usług");
                emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                visitsPanel.add(emptyLabel);
            } else {
                for (Visit visit : patient.getRegisteredVisits()) {
                    JPanel visitCard = createPatientVisitCard(visit);
                    visitsPanel.add(visitCard);
                    visitsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                }
            }
        }

        JScrollPane scrollPane = new JScrollPane(visitsPanel);
        scrollPane.setBorder(null);
        panel.add(scrollPane, BorderLayout.EAST);

        return panel;
    }

    private JPanel createPatientVisitCard(Visit visit) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        card.setBackground(Color.WHITE);
        card.setMaximumSize(new Dimension(360, 130));

        String icon = visit.getType().equals("LAB") ? "🔬" : (visit.getType().equals("PROC") ? "💉" : "👨‍⚕️");
        card.add(new JLabel(icon + " " + visit.getDoctorName()));
        card.add(new JLabel("📅 " + visit.getDateTime()));
        card.add(new JLabel("📋 " + visit.getSpecialization()));
        String paymentText = visit.getPaymentStatus().equals("NFZ") ? "💰 NFZ" :
                (visit.getPaymentStatus().equals("PRIVATE_PAID") ? "💰 Opłacona" : "💰 Do zapłaty");
        card.add(new JLabel(paymentText));

        return card;
    }

    private JPanel visitMenuPanel() {
        JPanel panel = createStyledPanel("Zarządzanie usługami");
        panel.add(createHeaderPanel("Wizyty, badania i zabiegi", "Dashboard"), BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JButton registerBtn = createStyledButton("📝 Zarejestruj się", new Color(70, 130, 200));
        JButton rescheduleBtn = createStyledButton("🔄 Przełóż", new Color(70, 130, 200));
        JButton cancelBtn = createStyledButton("❌ Odwołaj", new Color(70, 130, 200));

        registerBtn.addActionListener(e -> {
            refreshPatientServicePanel("RegisterService");
            cardLayout.show(mainPanel, "RegisterService");
        });
        rescheduleBtn.addActionListener(e -> {
            refreshPatientServicePanel("RescheduleVisit");
            cardLayout.show(mainPanel, "RescheduleVisit");
        });
        cancelBtn.addActionListener(e -> {
            refreshPatientServicePanel("CancelVisit");
            cardLayout.show(mainPanel, "CancelVisit");
        });

        gbc.gridx = 0;
        gbc.gridy = 0;
        buttonPanel.add(registerBtn, gbc);
        gbc.gridy = 1;
        buttonPanel.add(rescheduleBtn, gbc);
        gbc.gridy = 2;
        buttonPanel.add(cancelBtn, gbc);

        panel.add(buttonPanel, BorderLayout.CENTER);

        JPanel registeredPanel = new JPanel();
        registeredPanel.setLayout(new BoxLayout(registeredPanel, BoxLayout.Y_AXIS));
        registeredPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Twoje zarejestrowane usługi"),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        registeredPanel.setBackground(new Color(248, 248, 255));
        registeredPanel.setPreferredSize(new Dimension(380, 500));

        if (currentUser instanceof Patient) {
            Patient patient = (Patient) currentUser;
            for (Visit visit : patient.getRegisteredVisits()) {
                JPanel visitCard = createPatientSmallVisitCard(visit);
                registeredPanel.add(visitCard);
                registeredPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            }
        }

        JScrollPane scrollPane = new JScrollPane(registeredPanel);
        scrollPane.setBorder(null);
        panel.add(scrollPane, BorderLayout.EAST);

        return panel;
    }

    private JPanel createPatientSmallVisitCard(Visit visit) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        card.setBackground(Color.WHITE);
        card.setMaximumSize(new Dimension(360, 90));

        String icon = visit.getType().equals("LAB") ? "🔬" : (visit.getType().equals("PROC") ? "💉" : "👨‍⚕️");
        card.add(new JLabel(icon + " " + visit.getDoctorName()));
        card.add(new JLabel("📅 " + visit.getDateTime()));

        return card;
    }

    private JPanel registerServicePanel() {

        JPanel panel = createStyledPanel("Rejestracja usługi");
        panel.setLayout(new BorderLayout());

        panel.add(
                createHeaderPanel("Rejestracja na wizytę/badanie/zabieg", "VisitMenu"),
                BorderLayout.NORTH
        );

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // =========================
        // TYPE
        // =========================
        gbc.gridx = 0;
        gbc.gridy = 0;
        contentPanel.add(new JLabel("Rodzaj usługi:"), gbc);

        JComboBox<String> serviceTypeCombo =
                new JComboBox<>(new String[]{
                        "Wizyta lekarska",
                        "Badanie laboratoryjne",
                        "Zabieg ambulatoryjny"
                });

        serviceTypeCombo.setPreferredSize(new Dimension(200, 30));

        gbc.gridx = 1;
        contentPanel.add(serviceTypeCombo, gbc);

        // =========================
        // 🔎 SEARCH LEKARZA / USŁUGI
        // =========================
        gbc.gridx = 0;
        gbc.gridy = 1;
        contentPanel.add(new JLabel("Szukaj:"), gbc);

        JTextField searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(300, 30));
        gbc.gridx = 1;
        contentPanel.add(searchField, gbc);

        // =========================
        // SERVICE COMBO
        // =========================
        gbc.gridx = 0;
        gbc.gridy = 2;
        contentPanel.add(new JLabel("Wybierz:"), gbc);

        JComboBox<String> serviceCombo = new JComboBox<>();
        serviceCombo.setPreferredSize(new Dimension(300, 30));
        gbc.gridx = 1;
        contentPanel.add(serviceCombo, gbc);

        // =========================
        // PAYMENT
        // =========================
        gbc.gridx = 0;
        gbc.gridy = 3;
        contentPanel.add(new JLabel("Forma płatności:"), gbc);

        JComboBox<String> paymentCombo =
                new JComboBox<>(new String[]{
                        "NFZ (refundowana)",
                        "Płatna prywatnie"
                });

        paymentCombo.setPreferredSize(new Dimension(200, 30));
        gbc.gridx = 1;
        contentPanel.add(paymentCombo, gbc);

        // =========================
        // TERMINY
        // =========================
        JPanel appointmentsPanel = new JPanel();
        appointmentsPanel.setLayout(new BoxLayout(appointmentsPanel, BoxLayout.Y_AXIS));
        appointmentsPanel.setBorder(BorderFactory.createTitledBorder("Dostępne terminy"));
        appointmentsPanel.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(appointmentsPanel);
        scrollPane.setPreferredSize(new Dimension(600, 300));

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        contentPanel.add(scrollPane, gbc);

        // =========================
        // REGISTER BUTTON
        // =========================
        JButton registerBtn = createStyledButton(
                "✅ Zarejestruj wybrany termin",
                new Color(0, 150, 0)
        );

        gbc.gridy = 5;
        contentPanel.add(registerBtn, gbc);

        // =========================
        // 🔎 REFRESH SERVICES (SEARCH LEKARZA)
        // =========================
        Runnable refreshServices = () -> {

            serviceCombo.removeAllItems();

            String type = (String) serviceTypeCombo.getSelectedItem();
            String q = searchField.getText().trim().toLowerCase();

            if (type.equals("Wizyta lekarska")) {

                for (Doctor d : dataManager.getDoctors()) {

                    String data = (d.getName() + " " + d.getSpecialization()).toLowerCase();

                    if (q.isEmpty() || data.contains(q)) {
                        serviceCombo.addItem(d.getName() + " (" + d.getSpecialization() + ")");
                    }
                }

            } else if (type.equals("Badanie laboratoryjne")) {

                String[] lab = {
                        "Pobranie krwi",
                        "EKG",
                        "USG",
                        "Holter EKG",
                        "RTG klatki piersiowej"
                };

                for (String s : lab) {
                    if (q.isEmpty() || s.toLowerCase().contains(q)) {
                        serviceCombo.addItem(s);
                    }
                }

            } else if (type.equals("Zabieg ambulatoryjny")) {

                String[] proc = {
                        "Zastrzyk domięśniowy",
                        "Szczepienie",
                        "Usunięcie szwów",
                        "Opracowanie rany",
                        "Iniekcja dostawowa"
                };

                for (String s : proc) {
                    if (q.isEmpty() || s.toLowerCase().contains(q)) {
                        serviceCombo.addItem(s);
                    }
                }
            }
        };

        // =========================
        // REFRESH APPOINTMENTS (twoja logika bez zmian)
        // =========================
        Runnable refreshAppointments = () -> {

            appointmentsPanel.removeAll();

            String serviceType = (String) serviceTypeCombo.getSelectedItem();
            String selected = (String) serviceCombo.getSelectedItem();
            String paymentType = (String) paymentCombo.getSelectedItem();
            String paymentCode = paymentType.equals("NFZ (refundowana)") ? "NFZ" : "PRIVATE";

            if (selected != null) {

                Map<String, List<AvailableAppointment>> groupedByDate = new LinkedHashMap<>();

                if (serviceType.equals("Wizyta lekarska")) {

                    String doctorName = selected.split(" \\(")[0];

                    for (AvailableAppointment app : availableAppointments) {
                        if (app.getDoctorName().equals(doctorName)
                                && app.isAvailable()
                                && app.getPaymentType().equals(paymentCode)) {

                            groupedByDate.computeIfAbsent(app.getDate(), k -> new ArrayList<>()).add(app);
                        }
                    }

                } else if (serviceType.equals("Badanie laboratoryjne")) {

                    for (AvailableAppointment app : availableLaboratoryTests) {
                        if (app.getDoctorName().equals(selected)
                                && app.isAvailable()
                                && app.getPaymentType().equals(paymentCode)) {

                            groupedByDate.computeIfAbsent(app.getDate(), k -> new ArrayList<>()).add(app);
                        }
                    }

                } else if (serviceType.equals("Zabieg ambulatoryjny")) {

                    for (AvailableAppointment app : availableProcedures) {
                        if (app.getDoctorName().equals(selected)
                                && app.isAvailable()
                                && app.getPaymentType().equals(paymentCode)) {

                            groupedByDate.computeIfAbsent(app.getDate(), k -> new ArrayList<>()).add(app);
                        }
                    }
                }

                if (groupedByDate.isEmpty()) {
                    appointmentsPanel.add(new JLabel("Brak dostępnych terminów"));
                } else {

                    for (var entry : groupedByDate.entrySet()) {

                        JPanel datePanel = new JPanel();
                        datePanel.setLayout(new BoxLayout(datePanel, BoxLayout.Y_AXIS));
                        datePanel.setBorder(BorderFactory.createTitledBorder(entry.getKey()));
                        datePanel.setBackground(Color.WHITE);

                        JPanel hoursPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

                        for (AvailableAppointment app : entry.getValue()) {

                            JButton hourBtn = new JButton(app.getHour());
                            hourBtn.setBackground(new Color(70, 130, 200));
                            hourBtn.setForeground(Color.WHITE);

                            hourBtn.addActionListener(ev -> {

                                panel.putClientProperty("selectedDate", app.getDate());
                                panel.putClientProperty("selectedHour", app.getHour());
                                panel.putClientProperty("selectedServiceName", app.getDoctorName());
                                panel.putClientProperty("selectedSpecialization", app.getSpecialization());
                                panel.putClientProperty("selectedServiceType", serviceType);
                                panel.putClientProperty("selectedVisitType", app.getType());
                                panel.putClientProperty("selectedPaymentType", paymentCode);

                                JOptionPane.showMessageDialog(
                                        frame,
                                        "Wybrano termin: " + app.getDate() + " " + app.getHour()
                                );
                            });

                            hoursPanel.add(hourBtn);
                        }

                        datePanel.add(hoursPanel);
                        appointmentsPanel.add(datePanel);
                        appointmentsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                    }
                }
            }

            appointmentsPanel.revalidate();
            appointmentsPanel.repaint();
        };

        // =========================
        // EVENTS
        // =========================
        searchField.getDocument().addDocumentListener(new SimpleDocListener(() -> {
            refreshServices.run();
            refreshAppointments.run();
        }));

        serviceTypeCombo.addActionListener(e -> {
            refreshServices.run();
            refreshAppointments.run();
        });

        serviceCombo.addActionListener(e -> refreshAppointments.run());
        paymentCombo.addActionListener(e -> refreshAppointments.run());

        // initial load
        refreshServices.run();

        registerBtn.addActionListener(e -> {

            Patient patient = (Patient) currentUser;

            String selectedDate = (String) panel.getClientProperty("selectedDate");
            String selectedHour = (String) panel.getClientProperty("selectedHour");
            String serviceName = (String) panel.getClientProperty("selectedServiceName");
            String specialization = (String) panel.getClientProperty("selectedSpecialization");
            String serviceType = (String) panel.getClientProperty("selectedServiceType");
            String visitType = (String) panel.getClientProperty("selectedVisitType");
            String paymentCode = (String) panel.getClientProperty("selectedPaymentType");

            if (selectedDate == null || selectedHour == null) {
                JOptionPane.showMessageDialog(frame, "Wybierz termin!", "Błąd", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String paymentStatus =
                    paymentCode.equals("NFZ") ? "NFZ" : "PRIVATE_UNPAID";

            int newId = patient.getRegisteredVisits().size() + 100;

            Visit newVisit = new Visit(
                    newId,
                    serviceName,
                    specialization,
                    selectedDate + " " + selectedHour,
                    "Zarejestrowana",
                    "",
                    visitType
            );

            newVisit.setPaymentStatus(paymentStatus);
            patient.addRegisteredVisit(newVisit);

            dataManager.saveAllData();

            JOptionPane.showMessageDialog(frame, "Usługa została zarejestrowana!");
            refreshAppointments.run();
            refreshPatientServicePanel("VisitMenu");
            cardLayout.show(mainPanel, "VisitMenu");
        });

        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel rescheduleVisitPanel() {
        JPanel panel = createStyledPanel("Przełożenie usługi");
        panel.add(createHeaderPanel("Przełożenie wizyty/badania/zabiegu", "VisitMenu"), BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        gbc.gridx = 0;
        gbc.gridy = 0;
        contentPanel.add(new JLabel("Wybierz wizytę do przełożenia:"), gbc);
        JComboBox<Visit> visitCombo = new JComboBox<>();
        visitCombo.setPreferredSize(new Dimension(400, 30));
        gbc.gridx = 1;
        contentPanel.add(visitCombo, gbc);

        Patient patient = (Patient) currentUser;
        for (Visit visit : patient.getRegisteredVisits()) {
            visitCombo.addItem(visit);
        }

        gbc.gridx = 0;
        gbc.gridy = 1;
        contentPanel.add(new JLabel("Nowy termin (wybierz z listy):"), gbc);

        JPanel appointmentsPanel = new JPanel();
        appointmentsPanel.setLayout(new BoxLayout(appointmentsPanel, BoxLayout.Y_AXIS));
        appointmentsPanel.setBorder(BorderFactory.createTitledBorder("Dostępne terminy"));
        appointmentsPanel.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(appointmentsPanel);
        scrollPane.setPreferredSize(new Dimension(500, 200));
        gbc.gridx = 1;
        contentPanel.add(scrollPane, gbc);

        JButton rescheduleBtn = createStyledButton("🔄 Przełóż wizytę", new Color(70, 130, 200));
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        contentPanel.add(rescheduleBtn, gbc);

        visitCombo.addActionListener(e -> {
            appointmentsPanel.removeAll();
            Visit selectedVisit = (Visit) visitCombo.getSelectedItem();
            if (selectedVisit != null) {
                String paymentCode = selectedVisit.getPaymentStatus().equals("NFZ") ? "NFZ" : "PRIVATE";
                Map<String, List<AvailableAppointment>> groupedByDate = new LinkedHashMap<>();

                if (selectedVisit.getType().equals("VISIT")) {
                    for (AvailableAppointment app : availableAppointments) {
                        if (app.isAvailable() && app.getPaymentType().equals(paymentCode)) {
                            groupedByDate.computeIfAbsent(app.getDate(), k -> new ArrayList<>()).add(app);
                        }
                    }
                } else if (selectedVisit.getType().equals("LAB")) {
                    for (AvailableAppointment app : availableLaboratoryTests) {
                        if (app.isAvailable() && app.getPaymentType().equals(paymentCode)) {
                            groupedByDate.computeIfAbsent(app.getDate(), k -> new ArrayList<>()).add(app);
                        }
                    }
                } else if (selectedVisit.getType().equals("PROC")) {
                    for (AvailableAppointment app : availableProcedures) {
                        if (app.isAvailable() && app.getPaymentType().equals(paymentCode)) {
                            groupedByDate.computeIfAbsent(app.getDate(), k -> new ArrayList<>()).add(app);
                        }
                    }
                }

                if (groupedByDate.isEmpty()) {
                    appointmentsPanel.add(new JLabel("Brak dostępnych terminów"));
                } else {
                    for (Map.Entry<String, List<AvailableAppointment>> entry : groupedByDate.entrySet()) {
                        JPanel datePanel = new JPanel();
                        datePanel.setLayout(new BoxLayout(datePanel, BoxLayout.Y_AXIS));
                        datePanel.setBorder(BorderFactory.createTitledBorder(entry.getKey()));
                        datePanel.setBackground(Color.WHITE);

                        JPanel hoursPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                        for (AvailableAppointment app : entry.getValue()) {
                            JButton hourBtn = new JButton(app.getHour());
                            hourBtn.setBackground(new Color(70, 130, 200));
                            hourBtn.setForeground(Color.WHITE);
                            hourBtn.addActionListener(ev -> {
                                panel.putClientProperty("newSelectedDate", app.getDate());
                                panel.putClientProperty("newSelectedHour", app.getHour());
                                panel.putClientProperty("newServiceName", app.getDoctorName());
                                panel.putClientProperty("newPaymentType", app.getPaymentType());
                                JOptionPane.showMessageDialog(frame, "Wybrano nowy termin: " + app.getDate() + " " + app.getHour());
                            });
                            hoursPanel.add(hourBtn);
                        }
                        datePanel.add(hoursPanel);
                        appointmentsPanel.add(datePanel);
                        appointmentsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                    }
                }
                appointmentsPanel.revalidate();
                appointmentsPanel.repaint();
            }
        });

        rescheduleBtn.addActionListener(e -> {
            Visit selectedVisit = (Visit) visitCombo.getSelectedItem();
            String newDate = (String) panel.getClientProperty("newSelectedDate");
            String newHour = (String) panel.getClientProperty("newSelectedHour");
            String newServiceName = (String) panel.getClientProperty("newServiceName");
            String newPaymentCode = (String) panel.getClientProperty("newPaymentType");

            if (selectedVisit == null || newDate == null || newHour == null) {
                JOptionPane.showMessageDialog(frame, "Wybierz wizytę i nowy termin!", "Błąd", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String[] oldDateTime = selectedVisit.getDateTime().split(" ");
            String oldPaymentCode = selectedVisit.getPaymentStatus().equals("NFZ") ? "NFZ" : "PRIVATE";

            if (selectedVisit.getType().equals("VISIT")) {
                for (AvailableAppointment app : availableAppointments) {
                    if (app.getDoctorName().equals(selectedVisit.getDoctorName()) &&
                            app.getDate().equals(oldDateTime[0]) && app.getHour().equals(oldDateTime[1]) &&
                            app.getPaymentType().equals(oldPaymentCode)) {
                        app.setAvailable(true);
                        break;
                    }
                }
            } else if (selectedVisit.getType().equals("LAB")) {
                for (AvailableAppointment app : availableLaboratoryTests) {
                    if (app.getDoctorName().equals(selectedVisit.getDoctorName()) &&
                            app.getDate().equals(oldDateTime[0]) && app.getHour().equals(oldDateTime[1]) &&
                            app.getPaymentType().equals(oldPaymentCode)) {
                        app.setAvailable(true);
                        break;
                    }
                }
            } else if (selectedVisit.getType().equals("PROC")) {
                for (AvailableAppointment app : availableProcedures) {
                    if (app.getDoctorName().equals(selectedVisit.getDoctorName()) &&
                            app.getDate().equals(oldDateTime[0]) && app.getHour().equals(oldDateTime[1]) &&
                            app.getPaymentType().equals(oldPaymentCode)) {
                        app.setAvailable(true);
                        break;
                    }
                }
            }

            selectedVisit.setDateTime(newDate + " " + newHour);
            if (newServiceName != null) {
                selectedVisit.setDoctorName(newServiceName);
            }

            if (selectedVisit.getType().equals("VISIT")) {
                for (AvailableAppointment app : availableAppointments) {
                    if (app.getDoctorName().equals(selectedVisit.getDoctorName()) &&
                            app.getDate().equals(newDate) && app.getHour().equals(newHour) &&
                            app.getPaymentType().equals(newPaymentCode)) {
                        app.setAvailable(false);
                        break;
                    }
                }
            } else if (selectedVisit.getType().equals("LAB")) {
                for (AvailableAppointment app : availableLaboratoryTests) {
                    if (app.getDoctorName().equals(selectedVisit.getDoctorName()) &&
                            app.getDate().equals(newDate) && app.getHour().equals(newHour) &&
                            app.getPaymentType().equals(newPaymentCode)) {
                        app.setAvailable(false);
                        break;
                    }
                }
            } else if (selectedVisit.getType().equals("PROC")) {
                for (AvailableAppointment app : availableProcedures) {
                    if (app.getDoctorName().equals(selectedVisit.getDoctorName()) &&
                            app.getDate().equals(newDate) && app.getHour().equals(newHour) &&
                            app.getPaymentType().equals(newPaymentCode)) {
                        app.setAvailable(false);
                        break;
                    }
                }
            }

            dataManager.saveAllData();
            JOptionPane.showMessageDialog(frame, "Wizyta została przełożona!");
            refreshPatientServicePanel("VisitMenu");
            cardLayout.show(mainPanel, "VisitMenu");
        });

        panel.add(contentPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel cancelVisitPanel() {
        JPanel panel = createStyledPanel("Odwołanie usługi");
        panel.add(createHeaderPanel("Odwołanie wizyty/badania/zabiegu", "VisitMenu"), BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        gbc.gridx = 0;
        gbc.gridy = 0;
        contentPanel.add(new JLabel("Wybierz wizytę do odwołania:"), gbc);
        JComboBox<Visit> visitCombo = new JComboBox<>();
        visitCombo.setPreferredSize(new Dimension(400, 30));
        gbc.gridx = 1;
        contentPanel.add(visitCombo, gbc);

        Patient patient = (Patient) currentUser;
        for (Visit visit : patient.getRegisteredVisits()) {
            visitCombo.addItem(visit);
        }

        JButton cancelBtn = createStyledButton("❌ Odwołaj wizytę", new Color(200, 0, 0));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        contentPanel.add(cancelBtn, gbc);

        cancelBtn.addActionListener(e -> {
            Visit selectedVisit = (Visit) visitCombo.getSelectedItem();

            if (selectedVisit == null) {
                JOptionPane.showMessageDialog(frame, "Wybierz wizytę do odwołania!", "Błąd", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(frame, "Czy na pewno chcesz odwołać tę wizytę?", "Potwierdzenie", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                patient.getRegisteredVisits().remove(selectedVisit);

                String[] dateTime = selectedVisit.getDateTime().split(" ");
                String paymentCode = selectedVisit.getPaymentStatus().equals("NFZ") ? "NFZ" : "PRIVATE";

                if (selectedVisit.getType().equals("VISIT")) {
                    for (AvailableAppointment app : availableAppointments) {
                        if (app.getDoctorName().equals(selectedVisit.getDoctorName()) &&
                                app.getDate().equals(dateTime[0]) && app.getHour().equals(dateTime[1]) &&
                                app.getPaymentType().equals(paymentCode)) {
                            app.setAvailable(true);
                            break;
                        }
                    }
                } else if (selectedVisit.getType().equals("LAB")) {
                    for (AvailableAppointment app : availableLaboratoryTests) {
                        if (app.getDoctorName().equals(selectedVisit.getDoctorName()) &&
                                app.getDate().equals(dateTime[0]) && app.getHour().equals(dateTime[1]) &&
                                app.getPaymentType().equals(paymentCode)) {
                            app.setAvailable(true);
                            break;
                        }
                    }
                } else if (selectedVisit.getType().equals("PROC")) {
                    for (AvailableAppointment app : availableProcedures) {
                        if (app.getDoctorName().equals(selectedVisit.getDoctorName()) &&
                                app.getDate().equals(dateTime[0]) && app.getHour().equals(dateTime[1]) &&
                                app.getPaymentType().equals(paymentCode)) {
                            app.setAvailable(true);
                            break;
                        }
                    }
                }

                dataManager.saveAllData();
                JOptionPane.showMessageDialog(frame, "Wizyta została odwołana!");
                refreshPatientServicePanel("VisitMenu");
                cardLayout.show(mainPanel, "VisitMenu");
            }
        });

        panel.add(contentPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel prescriptionsPanel() {
        JPanel panel = createStyledPanel("Recepty");
        panel.add(createHeaderPanel("Moje recepty", "Dashboard"), BorderLayout.NORTH);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        if (currentUser instanceof Patient) {
            Patient patient = (Patient) currentUser;
            if (patient.getPrescriptions().isEmpty()) {
                JLabel emptyLabel = new JLabel("Brak recept");
                emptyLabel.setFont(new Font("SansSerif", Font.ITALIC, 14));
                emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                contentPanel.add(emptyLabel);
            } else {
                for (Prescription pres : patient.getPrescriptions()) {
                    JPanel presCard = createPrescriptionCard(pres);
                    contentPanel.add(presCard);
                    contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                }
            }
        }

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createPrescriptionCard(Prescription pres) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        card.setBackground(new Color(248, 248, 255));
        card.setMaximumSize(new Dimension(800, 100));

        card.add(new JLabel("💊 Recepta nr: " + pres.getNumber()));
        card.add(new JLabel("📋 " + pres.getMedicines()));
        card.add(new JLabel("📅 Data wystawienia: " + pres.getDate()));
        card.add(new JLabel("👨‍⚕️ Lekarz: " + pres.getDoctor()));

        return card;
    }

    private JPanel historyPanel() {
        JPanel panel = createStyledPanel("Historia");
        panel.add(createHeaderPanel("Historia wizyt, badań i zabiegów", "Dashboard"), BorderLayout.NORTH);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        if (currentUser instanceof Patient) {
            Patient patient = (Patient) currentUser;

            if (!patient.getHistory().isEmpty()) {
                JLabel historyTitle = new JLabel("Zakończone:");
                historyTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
                historyTitle.setForeground(new Color(70, 130, 200));
                historyTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
                contentPanel.add(historyTitle);
                contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));

                for (Visit visit : patient.getHistory()) {
                    JPanel visitCard = createHistoryVisitCard(visit);
                    contentPanel.add(visitCard);
                    contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                }
                contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));
            }

            if (!patient.getRegisteredVisits().isEmpty()) {
                JLabel registeredTitle = new JLabel("Aktualnie zarejestrowane:");
                registeredTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
                registeredTitle.setForeground(new Color(70, 130, 200));
                registeredTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
                contentPanel.add(registeredTitle);
                contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));

                for (Visit visit : patient.getRegisteredVisits()) {
                    JPanel visitCard = createHistoryVisitCard(visit);
                    contentPanel.add(visitCard);
                    contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                }
            }

            if (patient.getHistory().isEmpty() && patient.getRegisteredVisits().isEmpty()) {
                JLabel emptyLabel = new JLabel("Brak historii");
                emptyLabel.setFont(new Font("SansSerif", Font.ITALIC, 14));
                emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                contentPanel.add(emptyLabel);
            }
        }

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createHistoryVisitCard(Visit visit) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        card.setBackground(Color.WHITE);
        card.setMaximumSize(new Dimension(800, 120));

        String icon = visit.getType().equals("LAB") ? "🔬" : (visit.getType().equals("PROC") ? "💉" : "👨‍⚕️");
        card.add(new JLabel(icon + " " + visit.getDoctorName()));
        card.add(new JLabel("📋 " + visit.getSpecialization()));
        card.add(new JLabel("📅 " + visit.getDateTime()));
        card.add(new JLabel("✅ Status: " + visit.getStatus()));
        String paymentText = visit.getPaymentStatus().equals("NFZ") ? "💰 NFZ" :
                (visit.getPaymentStatus().equals("PRIVATE_PAID") ? "💰 Opłacona" : "💰 Do zapłaty");
        card.add(new JLabel(paymentText));
        if (visit.getNotes() != null && !visit.getNotes().isEmpty()) {
            card.add(new JLabel("📝 Uwagi: " + visit.getNotes()));
        }

        return card;
    }

    private JPanel referralsPanel() {
        JPanel panel = createStyledPanel("Skierowania");
        panel.add(createHeaderPanel("Moje skierowania", "Dashboard"), BorderLayout.NORTH);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        if (currentUser instanceof Patient) {
            Patient patient = (Patient) currentUser;
            if (patient.getReferrals().isEmpty()) {
                JLabel emptyLabel = new JLabel("Brak skierowań");
                emptyLabel.setFont(new Font("SansSerif", Font.ITALIC, 14));
                emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                contentPanel.add(emptyLabel);
            } else {
                for (Referral ref : patient.getReferrals()) {
                    JPanel refCard = createReferralCard(ref);
                    contentPanel.add(refCard);
                    contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                }
            }
        }

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createReferralCard(Referral ref) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        card.setBackground(new Color(248, 248, 255));
        card.setMaximumSize(new Dimension(800, 100));

        card.add(new JLabel("📄 Skierowanie nr: " + ref.getNumber()));
        card.add(new JLabel("🔬 Na: " + ref.getType()));
        card.add(new JLabel("📅 Ważne do: " + ref.getValidUntil()));
        card.add(new JLabel("👨‍⚕️ Wystawił: " + ref.getDoctor()));

        return card;
    }

    private JPanel doctorsListPanel() {
        JPanel panel = createStyledPanel("Lista lekarzy");
        panel.add(createHeaderPanel("Lista lekarzy", "Dashboard"), BorderLayout.NORTH);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        for (Doctor doctor : dataManager.getDoctors()) {
            JPanel doctorCard = createDoctorCardForPatient(doctor);
            contentPanel.add(doctorCard);
            contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createDoctorCardForPatient(Doctor doctor) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        card.setBackground(new Color(248, 248, 255));
        card.setMaximumSize(new Dimension(800, 120));

        card.add(new JLabel("👨‍⚕️ " + doctor.getName()));
        card.add(new JLabel("🔬 Specjalizacja: " + doctor.getSpecialization()));
        card.add(new JLabel("⏰ Godziny przyjęć: " + doctor.getHours()));
        card.add(new JLabel("📧 " + doctor.getEmail()));
        card.add(new JLabel("📞 " + doctor.getPhone()));

        return card;
    }

    private JPanel sickLeavePanel() {
        JPanel panel = createStyledPanel("Zwolnienia");
        panel.add(createHeaderPanel("Zwolnienia lekarskie", "Dashboard"), BorderLayout.NORTH);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        if (currentUser instanceof Patient) {
            Patient patient = (Patient) currentUser;
            if (patient.getSickLeaves().isEmpty()) {
                JLabel emptyLabel = new JLabel("Brak zwolnień lekarskich");
                emptyLabel.setFont(new Font("SansSerif", Font.ITALIC, 14));
                emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                contentPanel.add(emptyLabel);
            } else {
                for (SickLeave sl : patient.getSickLeaves()) {
                    JPanel slCard = createSickLeaveCard(sl);
                    contentPanel.add(slCard);
                    contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                }
            }
        }

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createSickLeaveCard(SickLeave sl) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        card.setBackground(new Color(248, 248, 255));
        card.setMaximumSize(new Dimension(800, 100));

        card.add(new JLabel("📋 Zwolnienie L4"));
        card.add(new JLabel("📅 Okres: " + sl.getStartDate() + " - " + sl.getEndDate()));
        card.add(new JLabel("👨‍⚕️ Wystawił: " + sl.getDoctor()));
        card.add(new JLabel("📝 Kod: " + sl.getCode()));

        return card;
    }

    private JPanel testResultsPanel() {
        JPanel panel = createStyledPanel("Wyniki badań");
        panel.add(createHeaderPanel("Wyniki badań", "Dashboard"), BorderLayout.NORTH);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        if (currentUser instanceof Patient) {
            Patient patient = (Patient) currentUser;
            if (patient.getTestResults().isEmpty()) {
                JLabel emptyLabel = new JLabel("Brak wyników badań");
                emptyLabel.setFont(new Font("SansSerif", Font.ITALIC, 14));
                emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                contentPanel.add(emptyLabel);
            } else {
                for (TestResult result : patient.getTestResults()) {
                    JPanel resultCard = createTestResultCard(result);
                    contentPanel.add(resultCard);
                    contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                }
            }
        }

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createTestResultCard(TestResult result) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        card.setBackground(Color.WHITE);
        card.setMaximumSize(new Dimension(800, 100));

        card.add(new JLabel("🔬 " + result.getTestName()));
        card.add(new JLabel("📅 Data: " + result.getDate()));
        card.add(new JLabel("📊 Wynik: " + result.getResult()));
        card.add(new JLabel("👨‍⚕️ Lekarz: " + result.getDoctor()));

        return card;
    }

    // ==================== METODY POMOCNICZE ====================

    private void refreshDashboard() {
        if (panels.containsKey("Dashboard")) {
            mainPanel.remove(panels.get("Dashboard"));
            panels.put("Dashboard", patientDashboard());
            mainPanel.add(panels.get("Dashboard"), "Dashboard");
        }
    }

    private void refreshReceptionistDashboard() {
        if (panels.containsKey("ReceptionistDashboard")) {
            mainPanel.remove(panels.get("ReceptionistDashboard"));
            panels.put("ReceptionistDashboard", receptionistDashboard());
            mainPanel.add(panels.get("ReceptionistDashboard"), "ReceptionistDashboard");
        }
    }

    private void refreshDoctorDashboard() {
        if (panels.containsKey("DoctorDashboard")) {
            mainPanel.remove(panels.get("DoctorDashboard"));
            panels.put("DoctorDashboard", doctorDashboard());
            mainPanel.add(panels.get("DoctorDashboard"), "DoctorDashboard");
        }
    }

    private void refreshAdminDashboard() {
        if (panels.containsKey("AdminDashboard")) {
            mainPanel.remove(panels.get("AdminDashboard"));
            panels.put("AdminDashboard", adminDashboard());
            mainPanel.add(panels.get("AdminDashboard"), "AdminDashboard");
        }
    }

    private void refreshPanel(String panelName) {
        if (panels.containsKey(panelName)) {
            JPanel oldPanel = panels.get(panelName);
            JPanel newPanel = null;

            switch (panelName) {
                case "Dashboard":
                    newPanel = patientDashboard();
                    break;
                case "VisitMenu":
                    newPanel = visitMenuPanel();
                    break;
                case "RegisterService":
                    newPanel = registerServicePanel();
                    break;
                case "RescheduleVisit":
                    newPanel = rescheduleVisitPanel();
                    break;
                case "CancelVisit":
                    newPanel = cancelVisitPanel();
                    break;
                case "Prescriptions":
                    newPanel = prescriptionsPanel();
                    break;
                case "History":
                    newPanel = historyPanel();
                    break;
                case "Referrals":
                    newPanel = referralsPanel();
                    break;
                case "DoctorsList":
                    newPanel = doctorsListPanel();
                    break;
                case "SickLeave":
                    newPanel = sickLeavePanel();
                    break;
                case "TestResults":
                    newPanel = testResultsPanel();
                    break;
            }

            if (newPanel != null) {
                mainPanel.remove(oldPanel);
                panels.put(panelName, newPanel);
                mainPanel.add(newPanel, panelName);
            }
        }
    }

    private void refreshDoctorPanel(String panelName) {
        if (panels.containsKey(panelName)) {
            JPanel oldPanel = panels.get(panelName);
            JPanel newPanel = null;

            switch (panelName) {
                case "DoctorDashboard":
                    newPanel = doctorDashboard();
                    break;
                case "DoctorMyVisits":
                    newPanel = doctorMyVisitsPanel();
                    break;
                case "DoctorCompletedVisits":
                    newPanel = doctorCompletedVisitsPanel();
                    break;
                case "DoctorAddResults":
                    newPanel = doctorAddResultsPanel();
                    break;
                case "DoctorAddPrescription":
                    newPanel = doctorAddPrescriptionPanel();
                    break;
                case "DoctorAddReferral":
                    newPanel = doctorAddReferralPanel();
                    break;
                case "DoctorAddSickLeave":
                    newPanel = doctorAddSickLeavePanel();
                    break;
            }

            if (newPanel != null) {
                mainPanel.remove(oldPanel);
                panels.put(panelName, newPanel);
                mainPanel.add(newPanel, panelName);
            }
        }
    }

    private void refreshReceptionistPanel(String panelName) {
        if (panels.containsKey(panelName)) {
            JPanel oldPanel = panels.get(panelName);
            JPanel newPanel = null;

            switch (panelName) {
                case "ReceptionistDashboard":
                    newPanel = receptionistDashboard();
                    break;
                case "ManagePatients":
                    newPanel = managePatientsPanel();
                    break;
                case "RegisterPatient":
                    newPanel = registerPatientPanel();
                    break;
                case "ReceptionistRegisterService":
                    newPanel = receptionistRegisterServicePanel();
                    break;
                case "ReceptionistRescheduleVisit":
                    newPanel = receptionistRescheduleVisitPanel();
                    break;
                case "ReceptionistCancelVisit":
                    newPanel = receptionistCancelVisitPanel();
                    break;
                case "AllVisitsList":
                    newPanel = allVisitsListPanel();
                    break;
            }

            if (newPanel != null) {
                mainPanel.remove(oldPanel);
                panels.put(panelName, newPanel);
                mainPanel.add(newPanel, panelName);
            }
        }
    }

    private void refreshAdminPanel(String panelName) {
        if (panels.containsKey(panelName)) {
            JPanel oldPanel = panels.get(panelName);
            JPanel newPanel = null;

            switch (panelName) {
                case "AdminDashboard":
                    newPanel = adminDashboard();
                    break;
                case "AdminManageDoctors":
                    newPanel = adminManageDoctorsPanel();
                    break;
                case "AdminManageReceptionists":
                    newPanel = adminManageReceptionistsPanel();
                    break;
                case "AdminManagePatients":
                    newPanel = adminManagePatientsPanel();
                    break;
                case "AdminManageSchedule":
                    newPanel = adminManageSchedulePanel();
                    break;
            }

            if (newPanel != null) {
                mainPanel.remove(oldPanel);
                panels.put(panelName, newPanel);
                mainPanel.add(newPanel, panelName);
            }
        }
    }

    private void refreshPatientServicePanel(String panelName) {
        if (panels.containsKey(panelName)) {
            JPanel oldPanel = panels.get(panelName);
            JPanel newPanel = null;

            switch (panelName) {
                case "RegisterService":
                    newPanel = registerServicePanel();
                    break;
                case "RescheduleVisit":
                    newPanel = rescheduleVisitPanel();
                    break;
                case "CancelVisit":
                    newPanel = cancelVisitPanel();
                    break;
                case "VisitMenu":
                    newPanel = visitMenuPanel();
                    break;
            }

            if (newPanel != null) {
                mainPanel.remove(oldPanel);
                panels.put(panelName, newPanel);
                mainPanel.add(newPanel, panelName);
            }
        }
    }

    private void showPatientVisits(Patient patient) {
        StringBuilder sb = new StringBuilder();
        sb.append("Wizyty pacjenta: ").append(patient.getName()).append("\n\n");

        if (!patient.getRegisteredVisits().isEmpty()) {
            sb.append("AKTYWNE WIZYTY:\n");
            for (Visit visit : patient.getRegisteredVisits()) {
                String icon = visit.getType().equals("LAB") ? "🔬" : (visit.getType().equals("PROC") ? "💉" : "👨‍⚕️");
                sb.append(icon).append(" ").append(visit.getDoctorName()).append("\n");
                sb.append("   Data: ").append(visit.getDateTime()).append("\n");
                sb.append("   Status: ").append(visit.getStatus()).append("\n");
                sb.append("   Płatność: ").append(visit.getPaymentStatus()).append("\n\n");
            }
        }
        if (!patient.getHistory().isEmpty()) {
            sb.append("HISTORIA:\n");
            for (Visit visit : patient.getHistory()) {
                String icon = visit.getType().equals("LAB") ? "🔬" : (visit.getType().equals("PROC") ? "💉" : "👨‍⚕️");
                sb.append(icon).append(" ").append(visit.getDoctorName()).append("\n");
                sb.append("   Data: ").append(visit.getDateTime()).append("\n");
                sb.append("   Status: ").append(visit.getStatus()).append("\n");
                sb.append("   Płatność: ").append(visit.getPaymentStatus()).append("\n\n");
            }
        }

        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 400));
        JOptionPane.showMessageDialog(frame, scrollPane, "Wizyty pacjenta", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showRegisterForPatient(Patient patient) {
        JOptionPane.showMessageDialog(frame, "Przekierowanie do rejestracji dla pacjenta: " + patient.getName());
        cardLayout.show(mainPanel, "ReceptionistRegisterService");
    }

    private void logout() {
        dataManager.saveAllData();
        currentUser = null;
        panels.clear();
        userType = "";
        mainPanel.removeAll();
        mainPanel.add(roleSelectionPanel(), "RoleSelection");
        mainPanel.add(loginPanel(), "Login");
        cardLayout.show(mainPanel, "RoleSelection");
    }
}

// ==================== KLASY DANYCH ====================

interface User extends Serializable {
    String getLogin();
    String getPassword();
    String getName();
}

class Receptionist implements User {
    private static final long serialVersionUID = 1L;
    private String login;
    private String password;
    private String name;
    private String employeeId;

    public Receptionist(String login, String password, String name, String employeeId) {
        this.login = login;
        this.password = password;
        this.name = name;
        this.employeeId = employeeId;
    }

    public String getLogin() { return login; }
    public String getPassword() { return password; }
    public String getName() { return name; }
    public String getEmployeeId() { return employeeId; }

    public void setPassword(String password) { this.password = password; }
    public void setName(String name) { this.name = name; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
}

class Patient implements User {
    private static final long serialVersionUID = 1L;
    private String login;
    private String password;
    private String name;
    private String pesel;
    private String address;
    private List<Visit> history;
    private List<Visit> registeredVisits;
    private List<TestResult> testResults;
    private List<Prescription> prescriptions;
    private List<Referral> referrals;
    private List<SickLeave> sickLeaves;

    public Patient(String login, String password, String name, String pesel, String address) {
        this.login = login;
        this.password = password;
        this.name = name;
        this.pesel = pesel;
        this.address = address;
        this.history = new ArrayList<>();
        this.registeredVisits = new ArrayList<>();
        this.testResults = new ArrayList<>();
        this.prescriptions = new ArrayList<>();
        this.referrals = new ArrayList<>();
        this.sickLeaves = new ArrayList<>();
    }

    public String getLogin() { return login; }
    public String getPassword() { return password; }
    public String getName() { return name; }
    public String getPesel() { return pesel; }
    public String getAddress() { return address; }
    public List<Visit> getHistory() { return history; }
    public List<Visit> getRegisteredVisits() { return registeredVisits; }
    public List<TestResult> getTestResults() { return testResults; }
    public List<Prescription> getPrescriptions() { return prescriptions; }
    public List<Referral> getReferrals() { return referrals; }
    public List<SickLeave> getSickLeaves() { return sickLeaves; }

    public void addToHistory(Visit visit) { history.add(visit); }
    public void addRegisteredVisit(Visit visit) { registeredVisits.add(visit); }
    public void addTestResult(TestResult result) { testResults.add(result); }
    public void addPrescription(Prescription pres) { prescriptions.add(pres); }
    public void addReferral(Referral ref) { referrals.add(ref); }
    public void addSickLeave(SickLeave sl) { sickLeaves.add(sl); }

    public void setName(String name) { this.name = name; }
    public void setPesel(String pesel) { this.pesel = pesel; }
    public void setAddress(String address) { this.address = address; }
    public void setPassword(String password) { this.password = password; }
}

class Doctor implements User, Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private String login;
    private String password;
    private String name;
    private String specialization;
    private String hours;
    private String email;
    private String phone;

    public Doctor(int id, String name, String specialization, String hours, String email, String phone) {
        this.id = id;
        this.name = name;
        this.specialization = specialization;
        this.hours = hours;
        this.email = email;
        this.phone = phone;
    }

    public int getId() { return id; }
    public String getLogin() { return login; }
    public String getPassword() { return password; }
    public String getName() { return name; }
    public String getSpecialization() { return specialization; }
    public String getHours() { return hours; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }

    public void setLogin(String login) { this.login = login; }
    public void setPassword(String password) { this.password = password; }
    public void setName(String name) { this.name = name; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    public void setHours(String hours) { this.hours = hours; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
}

class Admin implements User, Serializable {
    private static final long serialVersionUID = 1L;
    private String login;
    private String password;
    private String name;

    public Admin(String login, String password, String name) {
        this.login = login;
        this.password = password;
        this.name = name;
    }

    public String getLogin() { return login; }
    public String getPassword() { return password; }
    public String getName() { return name; }
}

class Visit implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private String doctorName;
    private String specialization;
    private String dateTime;
    private String status;
    private String notes;
    private String type;
    private String paymentStatus;

    public Visit(int id, String doctorName, String specialization, String dateTime, String status, String notes, String type) {
        this.id = id;
        this.doctorName = doctorName;
        this.specialization = specialization;
        this.dateTime = dateTime;
        this.status = status;
        this.notes = notes;
        this.type = type;
        this.paymentStatus = "NFZ";
    }

    public int getId() { return id; }
    public String getDoctorName() { return doctorName; }
    public String getSpecialization() { return specialization; }
    public String getDateTime() { return dateTime; }
    public String getStatus() { return status; }
    public String getNotes() { return notes; }
    public String getType() { return type; }
    public String getPaymentStatus() { return paymentStatus; }

    public void setDateTime(String dateTime) { this.dateTime = dateTime; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    public void setType(String type) { this.type = type; }
    public void setStatus(String status) { this.status = status; }
    public void setNotes(String notes) { this.notes = notes; }

    @Override
    public String toString() {
        String icon = type.equals("LAB") ? "🔬" : (type.equals("PROC") ? "💉" : "👨‍⚕️");
        return icon + " " + doctorName + " - " + dateTime + " (" + status + ")";
    }
}

class TestResult implements Serializable {
    private static final long serialVersionUID = 1L;
    private String testName;
    private String date;
    private String result;
    private String doctor;

    public TestResult(String testName, String date, String result, String doctor) {
        this.testName = testName;
        this.date = date;
        this.result = result;
        this.doctor = doctor;
    }

    public String getTestName() { return testName; }
    public String getDate() { return date; }
    public String getResult() { return result; }
    public String getDoctor() { return doctor; }
}

class Prescription implements Serializable {
    private static final long serialVersionUID = 1L;
    private String number;
    private String medicines;
    private String date;
    private String doctor;

    public Prescription(String number, String medicines, String date, String doctor) {
        this.number = number;
        this.medicines = medicines;
        this.date = date;
        this.doctor = doctor;
    }

    public String getNumber() { return number; }
    public String getMedicines() { return medicines; }
    public String getDate() { return date; }
    public String getDoctor() { return doctor; }
}

class Referral implements Serializable {
    private static final long serialVersionUID = 1L;
    private String number;
    private String type;
    private String validUntil;
    private String doctor;

    public Referral(String number, String type, String validUntil, String doctor) {
        this.number = number;
        this.type = type;
        this.validUntil = validUntil;
        this.doctor = doctor;
    }

    public String getNumber() { return number; }
    public String getType() { return type; }
    public String getValidUntil() { return validUntil; }
    public String getDoctor() { return doctor; }
}

class SickLeave implements Serializable {
    private static final long serialVersionUID = 1L;
    private String startDate;
    private String endDate;
    private String doctor;
    private String code;

    public SickLeave(String startDate, String endDate, String doctor, String code) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.doctor = doctor;
        this.code = code;
    }

    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public String getDoctor() { return doctor; }
    public String getCode() { return code; }
}

class AvailableAppointment {
    private int doctorId;
    private String doctorName;
    private String specialization;
    private String date;
    private String hour;
    private boolean available;
    private String type;
    private String paymentType;

    public AvailableAppointment(int doctorId, String doctorName, String specialization, String date, String hour, boolean available, String type, String paymentType) {
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.specialization = specialization;
        this.date = date;
        this.hour = hour;
        this.available = available;
        this.type = type;
        this.paymentType = paymentType;
    }

    public int getDoctorId() { return doctorId; }
    public String getDoctorName() { return doctorName; }
    public String getSpecialization() { return specialization; }
    public String getDate() { return date; }
    public String getHour() { return hour; }
    public boolean isAvailable() { return available; }
    public String getType() { return type; }
    public String getPaymentType() { return paymentType; }
    public void setAvailable(boolean available) { this.available = available; }
}

class DataManager {
    private static final String DATA_FOLDER = "data/";

    private static final String PATIENTS_FILE = DATA_FOLDER + "pacjenci.txt";
    private static final String DOCTORS_FILE = DATA_FOLDER + "lekarze.txt";
    private static final String VISITS_FILE = DATA_FOLDER + "wizyty.txt";
    private static final String PRESCRIPTIONS_FILE = DATA_FOLDER + "recepty.txt";
    private static final String REFERRALS_FILE = DATA_FOLDER + "skierowania.txt";
    private static final String SICK_LEAVES_FILE = DATA_FOLDER + "zwolnienia.txt";
    private static final String TEST_RESULTS_FILE = DATA_FOLDER + "wyniki_badan.txt";
    private static final String RECEPTIONISTS_FILE = DATA_FOLDER + "recepcjonisci.txt";
    private static final String ADMINS_FILE = DATA_FOLDER + "administratorzy.txt";

    private Map<String, Patient> patients = new HashMap<>();
    private List<Doctor> doctors = new ArrayList<>();
    private Map<String, Receptionist> receptionists = new HashMap<>();
    private Map<String, Admin> admins = new HashMap<>();

    public DataManager() {
        File folder = new File(DATA_FOLDER);
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    private boolean isAnyDataFileEmpty() {
        File[] filesToCheck = {
                new File(PATIENTS_FILE), new File(DOCTORS_FILE),
                new File(RECEPTIONISTS_FILE), new File(ADMINS_FILE)
        };

        for (File file : filesToCheck) {
            if (!file.exists() || file.length() == 0) {
                System.out.println("Plik " + file.getName() + " jest pusty lub nie istnieje. Tworzenie domyślnych danych...");
                return true;
            }
        }
        return false;
    }

    public void loadAllData() {
        loadDoctors();
        loadPatients();
        loadReceptionists();
        loadAdmins();

        if (isAnyDataFileEmpty() || doctors.isEmpty() || patients.isEmpty()) {
            System.out.println("Inicjalizacja domyślnych danych testowych...");
            initializeDefaultData();
        } else {
            loadVisits();
            loadPrescriptions();
            loadReferrals();
            loadSickLeaves();
            loadTestResults();
        }
    }

    private void initializeDefaultData() {
        doctors.clear();
        patients.clear();
        receptionists.clear();
        admins.clear();

        Doctor doc1 = new Doctor(1, "dr Anna Kowalska", "Internista",
                "poniedziałek, środa, piątek 9:00-15:00", "anna.kowalska@klinika.pl", "123-456-789");
        doc1.setLogin("doctor1");
        doc1.setPassword("pass123");

        Doctor doc2 = new Doctor(2, "dr Jan Nowak", "Kardiolog",
                "wtorek, czwartek 10:00-16:00", "jan.nowak@klinika.pl", "123-456-790");
        doc2.setLogin("doctor2");
        doc2.setPassword("pass123");

        Doctor doc3 = new Doctor(3, "dr Maria Wiśniewska", "Dermatolog",
                "poniedziałek, czwartek 9:00-14:00", "maria.wisniewska@klinika.pl", "123-456-791");
        doc3.setLogin("doctor3");
        doc3.setPassword("pass123");

        Doctor doc4 = new Doctor(4, "dr Piotr Lewandowski", "Okulista",
                "środa, piątek 11:00-17:00", "piotr.lewandowski@klinika.pl", "123-456-792");
        doc4.setLogin("doctor4");
        doc4.setPassword("pass123");

        doctors.add(doc1);
        doctors.add(doc2);
        doctors.add(doc3);
        doctors.add(doc4);

        Patient user1 = new Patient("user1", "pass1", "Jan Kowalski", "12345678901", "ul. Kwiatowa 15, Warszawa");
        user1.addToHistory(new Visit(1, "dr Anna Kowalska", "Internista", "15.02.2024 10:00", "Zakończona",
                "Przeziębienie, zalecenie odpoczynku", "VISIT"));
        user1.addToHistory(new Visit(2, "dr Jan Nowak", "Kardiolog", "20.02.2024 11:30", "Zakończona",
                "Kontrola ciśnienia, wszystko OK", "VISIT"));
        user1.addRegisteredVisit(new Visit(101, "dr Piotr Lewandowski", "Okulista", "10.04.2024 09:00", "Zarejestrowana", "", "VISIT"));
        user1.addTestResult(new TestResult("Morfologia krwi", "15.03.2024", "Wyniki w normie", "dr Anna Kowalska"));
        user1.addTestResult(new TestResult("EKG", "20.03.2024", "Czynność serca prawidłowa", "dr Jan Nowak"));
        user1.addPrescription(new Prescription("RX-001", "Amoksycylina 500mg - 1x dziennie przez 7 dni", "15.02.2024", "dr Anna Kowalska"));
        user1.addReferral(new Referral("SKI-001", "Badanie EKG", "30.06.2024", "dr Jan Nowak"));

        Patient user2 = new Patient("user2", "pass2", "Anna Nowak", "98765432109", "ul. Lipowa 7, Kraków");
        user2.addToHistory(new Visit(3, "dr Maria Wiśniewska", "Dermatolog", "05.02.2024 14:00", "Zakończona",
                "Wysypka alergiczna, przepisano maść", "VISIT"));
        user2.addRegisteredVisit(new Visit(103, "dr Jan Nowak", "Kardiolog", "12.04.2024 13:30", "Zarejestrowana", "", "VISIT"));
        user2.addTestResult(new TestResult("Morfologia krwi", "10.03.2024", "Lekka anemia, zalecana suplementacja żelaza", "dr Anna Kowalska"));
        user2.addPrescription(new Prescription("RX-003", "Maść hydrokortyzonowa - 2x dziennie", "05.02.2024", "dr Maria Wiśniewska"));
        user2.addReferral(new Referral("SKI-003", "Konsultacja alergologiczna", "30.05.2024", "dr Maria Wiśniewska"));

        patients.put("user1", user1);
        patients.put("user2", user2);

        receptionists.put("recepcja1", new Receptionist("recepcja1", "pass123", "Anna Nowak (Recepcjonistka)", "R001"));
        receptionists.put("recepcja2", new Receptionist("recepcja2", "pass123", "Piotr Kowalski (Recepcjonista)", "R002"));

        admins.put("admin", new Admin("admin", "admin123", "Administrator Systemu"));

        saveAllData();

        System.out.println("Domyślne dane testowe zostały utworzone pomyślnie!");
    }

    private void loadDoctors() {
        File file = new File(DOCTORS_FILE);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                doctors.clear();
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split("\\|");
                    if (parts.length >= 8) {
                        Doctor doc = new Doctor(Integer.parseInt(parts[0]), parts[1], parts[2], parts[3], parts[4], parts[5]);
                        doc.setLogin(parts[6]);
                        doc.setPassword(parts[7]);
                        doctors.add(doc);
                    }
                }
            } catch (IOException e) {
                System.err.println("Błąd odczytu pliku lekarzy: " + e.getMessage());
            }
        }
    }

    private void saveDoctors() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DOCTORS_FILE))) {
            for (Doctor doc : doctors) {
                writer.write(doc.getId() + "|" + doc.getName() + "|" + doc.getSpecialization() + "|" +
                        doc.getHours() + "|" + doc.getEmail() + "|" + doc.getPhone() + "|" +
                        doc.getLogin() + "|" + doc.getPassword());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Błąd zapisu pliku lekarzy: " + e.getMessage());
        }
    }

    private void loadPatients() {
        File file = new File(PATIENTS_FILE);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                patients.clear();
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split("\\|");
                    if (parts.length >= 5) {
                        Patient patient = new Patient(parts[0], parts[1], parts[2], parts[3], parts[4]);
                        patients.put(parts[0], patient);
                    }
                }
            } catch (IOException e) {
                System.err.println("Błąd odczytu pliku pacjentów: " + e.getMessage());
            }
        }
    }

    private void savePatients() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(PATIENTS_FILE))) {
            for (Patient patient : patients.values()) {
                writer.write(patient.getLogin() + "|" + patient.getPassword() + "|" +
                        patient.getName() + "|" + patient.getPesel() + "|" + patient.getAddress());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Błąd zapisu pliku pacjentów: " + e.getMessage());
        }
    }

    private void loadReceptionists() {
        File file = new File(RECEPTIONISTS_FILE);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                receptionists.clear();
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split("\\|");
                    if (parts.length >= 4) {
                        Receptionist rec = new Receptionist(parts[0], parts[1], parts[2], parts[3]);
                        receptionists.put(parts[0], rec);
                    }
                }
            } catch (IOException e) {
                System.err.println("Błąd odczytu pliku recepcjonistów: " + e.getMessage());
            }
        }
    }

    private void saveReceptionists() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(RECEPTIONISTS_FILE))) {
            for (Receptionist rec : receptionists.values()) {
                writer.write(rec.getLogin() + "|" + rec.getPassword() + "|" + rec.getName() + "|" + rec.getEmployeeId());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Błąd zapisu pliku recepcjonistów: " + e.getMessage());
        }
    }

    private void loadAdmins() {
        File file = new File(ADMINS_FILE);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                admins.clear();
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split("\\|");
                    if (parts.length >= 3) {
                        Admin admin = new Admin(parts[0], parts[1], parts[2]);
                        admins.put(parts[0], admin);
                    }
                }
            } catch (IOException e) {
                System.err.println("Błąd odczytu pliku administratorów: " + e.getMessage());
            }
        }
    }

    private void saveAdmins() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ADMINS_FILE))) {
            for (Admin admin : admins.values()) {
                writer.write(admin.getLogin() + "|" + admin.getPassword() + "|" + admin.getName());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Błąd zapisu pliku administratorów: " + e.getMessage());
        }
    }

    private void loadVisits() {
        File file = new File(VISITS_FILE);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split("\\|");
                    if (parts.length >= 10) {
                        String login = parts[0];
                        String type = parts[1];
                        int id = Integer.parseInt(parts[2]);
                        String doctorName = parts[3];
                        String specialization = parts[4];
                        String dateTime = parts[5];
                        String status = parts[6];
                        String notes = parts[7];
                        String visitType = parts[8];
                        String paymentStatus = parts[9];

                        Visit visit = new Visit(id, doctorName, specialization, dateTime, status, notes, visitType);
                        visit.setPaymentStatus(paymentStatus);

                        Patient patient = patients.get(login);
                        if (patient != null) {
                            if ("history".equals(type)) {
                                patient.addToHistory(visit);
                            } else if ("registered".equals(type)) {
                                patient.addRegisteredVisit(visit);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Błąd odczytu pliku wizyt: " + e.getMessage());
            }
        }
    }

    private void saveVisits() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(VISITS_FILE))) {
            for (Patient patient : patients.values()) {
                for (Visit visit : patient.getHistory()) {
                    writer.write(patient.getLogin() + "|history|" + visit.getId() + "|" +
                            visit.getDoctorName() + "|" + visit.getSpecialization() + "|" +
                            visit.getDateTime() + "|" + visit.getStatus() + "|" + visit.getNotes() + "|" +
                            visit.getType() + "|" + visit.getPaymentStatus());
                    writer.newLine();
                }
                for (Visit visit : patient.getRegisteredVisits()) {
                    writer.write(patient.getLogin() + "|registered|" + visit.getId() + "|" +
                            visit.getDoctorName() + "|" + visit.getSpecialization() + "|" +
                            visit.getDateTime() + "|" + visit.getStatus() + "|" + visit.getNotes() + "|" +
                            visit.getType() + "|" + visit.getPaymentStatus());
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            System.err.println("Błąd zapisu pliku wizyt: " + e.getMessage());
        }
    }

    private void loadPrescriptions() {
        File file = new File(PRESCRIPTIONS_FILE);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split("\\|");
                    if (parts.length >= 5) {
                        String login = parts[0];
                        Prescription pres = new Prescription(parts[1], parts[2], parts[3], parts[4]);
                        Patient patient = patients.get(login);
                        if (patient != null) {
                            patient.addPrescription(pres);
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Błąd odczytu pliku recept: " + e.getMessage());
            }
        }
    }

    private void savePrescriptions() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(PRESCRIPTIONS_FILE))) {
            for (Patient patient : patients.values()) {
                for (Prescription pres : patient.getPrescriptions()) {
                    writer.write(patient.getLogin() + "|" + pres.getNumber() + "|" +
                            pres.getMedicines() + "|" + pres.getDate() + "|" + pres.getDoctor());
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            System.err.println("Błąd zapisu pliku recept: " + e.getMessage());
        }
    }

    private void loadReferrals() {
        File file = new File(REFERRALS_FILE);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split("\\|");
                    if (parts.length >= 5) {
                        String login = parts[0];
                        Referral ref = new Referral(parts[1], parts[2], parts[3], parts[4]);
                        Patient patient = patients.get(login);
                        if (patient != null) {
                            patient.addReferral(ref);
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Błąd odczytu pliku skierowań: " + e.getMessage());
            }
        }
    }

    private void saveReferrals() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(REFERRALS_FILE))) {
            for (Patient patient : patients.values()) {
                for (Referral ref : patient.getReferrals()) {
                    writer.write(patient.getLogin() + "|" + ref.getNumber() + "|" +
                            ref.getType() + "|" + ref.getValidUntil() + "|" + ref.getDoctor());
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            System.err.println("Błąd zapisu pliku skierowań: " + e.getMessage());
        }
    }

    private void loadSickLeaves() {
        File file = new File(SICK_LEAVES_FILE);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split("\\|");
                    if (parts.length >= 5) {
                        String login = parts[0];
                        SickLeave sl = new SickLeave(parts[1], parts[2], parts[3], parts[4]);
                        Patient patient = patients.get(login);
                        if (patient != null) {
                            patient.addSickLeave(sl);
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Błąd odczytu pliku zwolnień: " + e.getMessage());
            }
        }
    }

    private void saveSickLeaves() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(SICK_LEAVES_FILE))) {
            for (Patient patient : patients.values()) {
                for (SickLeave sl : patient.getSickLeaves()) {
                    writer.write(patient.getLogin() + "|" + sl.getStartDate() + "|" +
                            sl.getEndDate() + "|" + sl.getDoctor() + "|" + sl.getCode());
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            System.err.println("Błąd zapisu pliku zwolnień: " + e.getMessage());
        }
    }

    private void loadTestResults() {
        File file = new File(TEST_RESULTS_FILE);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split("\\|");
                    if (parts.length >= 5) {
                        String login = parts[0];
                        TestResult tr = new TestResult(parts[1], parts[2], parts[3], parts[4]);
                        Patient patient = patients.get(login);
                        if (patient != null) {
                            patient.addTestResult(tr);
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Błąd odczytu pliku wyników badań: " + e.getMessage());
            }
        }
    }

    private void saveTestResults() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(TEST_RESULTS_FILE))) {
            for (Patient patient : patients.values()) {
                for (TestResult tr : patient.getTestResults()) {
                    writer.write(patient.getLogin() + "|" + tr.getTestName() + "|" +
                            tr.getDate() + "|" + tr.getResult() + "|" + tr.getDoctor());
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            System.err.println("Błąd zapisu pliku wyników badań: " + e.getMessage());
        }
    }

    public void saveAllData() {
        saveDoctors();
        savePatients();
        saveReceptionists();
        saveAdmins();
        saveVisits();
        savePrescriptions();
        saveReferrals();
        saveSickLeaves();
        saveTestResults();
    }

    public Patient getPatient(String login) {
        return patients.get(login);
    }

    public Receptionist getReceptionist(String login) {
        return receptionists.get(login);
    }

    public Doctor getDoctor(String login) {
        for (Doctor doctor : doctors) {
            if (doctor.getLogin().equals(login)) {
                return doctor;
            }
        }
        return null;
    }

    public Admin getAdmin(String login) {
        return admins.get(login);
    }

    public void addPatient(Patient patient) {
        patients.put(patient.getLogin(), patient);
        savePatients();
        saveVisits();
        savePrescriptions();
        saveReferrals();
        saveSickLeaves();
        saveTestResults();
    }

    public void addDoctor(Doctor doctor) {
        doctors.add(doctor);
        saveDoctors();
    }

    public void addReceptionist(Receptionist receptionist) {
        receptionists.put(receptionist.getLogin(), receptionist);
        saveReceptionists();
    }

    public void removeDoctor(Doctor doctor) {
        doctors.remove(doctor);
        saveDoctors();
    }

    public void removeReceptionist(String login) {
        receptionists.remove(login);
        saveReceptionists();
    }

    public void removePatient(String login) {
        patients.remove(login);
        savePatients();
    }

    public List<Doctor> getDoctors() {
        return doctors;
    }

    public Map<String, Patient> getAllPatients() {
        return patients;
    }

    public Map<String, Receptionist> getReceptionists() {
        return receptionists;
    }
}