package org.example;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class ClinicLoginApp {

    private JFrame frame;
    private JPanel mainPanel;
    private CardLayout cardLayout;
    private User currentUser;
    private DataManager dataManager;
    private Map<String, JPanel> panels = new HashMap<>();
    private String userType = "";
    private List<AvailableAppointment> availableAppointments = new ArrayList<>(); // VISIT
    private List<AvailableAppointment> availableLaboratoryTests = new ArrayList<>(); // LAB
    private List<AvailableAppointment> availableProcedures = new ArrayList<>(); // PROC

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ClinicLoginApp().createGUI());
    }

    private void createGUI() {
        dataManager = new DataManager();
        dataManager.loadAllData();

        frame = new JFrame("System Opieki Zdrowotnej");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 850);
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

    private JPanel roleSelectionPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(240, 248, 255));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 200), 2),
                "Wybierz typ konta",
                TitledBorder.CENTER,
                TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 18),
                new Color(70, 130, 200)
        ));

        GridBagConstraints cardGbc = new GridBagConstraints();
        cardGbc.insets = new Insets(15, 15, 15, 15);

        JLabel title = new JLabel("System Opieki Zdrowotnej");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(new Color(70, 130, 200));
        cardGbc.gridx = 0; cardGbc.gridy = 0; cardGbc.gridwidth = 2;
        card.add(title, cardGbc);

        JButton patientBtn = new JButton("👤 Pacjent");
        patientBtn.setFont(new Font("SansSerif", Font.BOLD, 16));
        patientBtn.setBackground(new Color(100, 150, 200));
        patientBtn.setForeground(Color.WHITE);
        patientBtn.setPreferredSize(new Dimension(200, 60));

        JButton doctorBtn = new JButton("👨‍⚕️ Lekarz");
        doctorBtn.setFont(new Font("SansSerif", Font.BOLD, 16));
        doctorBtn.setBackground(new Color(100, 150, 200));
        doctorBtn.setForeground(Color.WHITE);
        doctorBtn.setPreferredSize(new Dimension(200, 60));

        JButton adminBtn = new JButton("🔧 Administrator");
        adminBtn.setFont(new Font("SansSerif", Font.BOLD, 16));
        adminBtn.setBackground(new Color(100, 150, 200));
        adminBtn.setForeground(Color.WHITE);
        adminBtn.setPreferredSize(new Dimension(200, 60));

        patientBtn.addActionListener(e -> {
            userType = "PATIENT";
            cardLayout.show(mainPanel, "Login");
        });

        doctorBtn.addActionListener(e -> {
            userType = "DOCTOR";
            JOptionPane.showMessageDialog(frame, "Panel lekarza w przygotowaniu...\nNa razie zaloguj się jako pacjent.", "Informacja", JOptionPane.INFORMATION_MESSAGE);
        });

        adminBtn.addActionListener(e -> {
            userType = "ADMIN";
            JOptionPane.showMessageDialog(frame, "Panel administratora w przygotowaniu...\nNa razie zaloguj się jako pacjent.", "Informacja", JOptionPane.INFORMATION_MESSAGE);
        });

        cardGbc.gridy = 1; cardGbc.gridwidth = 2;
        card.add(patientBtn, cardGbc);
        cardGbc.gridy = 2;
        card.add(doctorBtn, cardGbc);
        cardGbc.gridy = 3;
        card.add(adminBtn, cardGbc);

        gbc.gridx = 0; gbc.gridy = 0;
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
        loginCard.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 200), 2),
                "Logowanie - Pacjent",
                TitledBorder.CENTER,
                TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 16),
                new Color(70, 130, 200)
        ));

        GridBagConstraints cardGbc = new GridBagConstraints();
        cardGbc.insets = new Insets(10, 15, 10, 15);

        JLabel titleLabel = new JLabel("Panel Pacjenta");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(new Color(70, 130, 200));
        cardGbc.gridx = 0; cardGbc.gridy = 0; cardGbc.gridwidth = 2;
        loginCard.add(titleLabel, cardGbc);

        JLabel userLabel = new JLabel("Login:");
        userLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        cardGbc.gridy = 1; cardGbc.gridwidth = 1;
        loginCard.add(userLabel, cardGbc);

        JTextField userField = new JTextField(15);
        cardGbc.gridx = 1;
        loginCard.add(userField, cardGbc);

        JLabel passLabel = new JLabel("Hasło:");
        cardGbc.gridx = 0; cardGbc.gridy = 2;
        loginCard.add(passLabel, cardGbc);

        JPasswordField passField = new JPasswordField(15);
        cardGbc.gridx = 1;
        loginCard.add(passField, cardGbc);

        JButton loginBtn = new JButton("Zaloguj się");
        loginBtn.setBackground(new Color(70, 130, 200));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        cardGbc.gridx = 0; cardGbc.gridy = 3; cardGbc.gridwidth = 2;
        loginCard.add(loginBtn, cardGbc);

        JButton backBtn = new JButton("◀ Powrót do wyboru roli");
        cardGbc.gridy = 4;
        loginCard.add(backBtn, cardGbc);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(new Color(255, 255, 200));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Przykładowe konta pacjentów"));

        infoPanel.add(new JLabel("• user1 / pass1 - Jan Kowalski"));
        infoPanel.add(new JLabel("• user2 / pass2 - Anna Nowak"));

        cardGbc.gridy = 5;
        loginCard.add(infoPanel, cardGbc);

        java.awt.event.ActionListener loginAction = e -> {
            String login = userField.getText();
            String password = new String(passField.getPassword());

            if (userType.equals("PATIENT")) {
                Patient patient = dataManager.getPatient(login);
                if (patient != null && patient.getPassword().equals(password)) {
                    currentUser = patient;
                    if (panels.isEmpty()) {
                        initializeAvailableAppointments();
                        initializePanelsAfterLogin();
                    }
                    refreshDashboard();
                    cardLayout.show(mainPanel, "Dashboard");
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

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(loginCard, gbc);

        return panel;
    }

    private void initializeAvailableAppointments() {
        availableAppointments.clear();
        availableLaboratoryTests.clear();
        availableProcedures.clear();

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        LocalDate today = LocalDate.now();

        // Godziny dla NFZ (mniej godzin)
        String[] nfzHours = {"09:00", "10:00", "11:00", "12:00", "14:00"};
        // Godziny dla prywatnych (więcej godzin)
        String[] privateHours = {"08:00", "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00"};

        for (int i = 1; i <= 21; i++) {
            LocalDate date = today.plusDays(i);
            if (date.getDayOfWeek().getValue() >= 6) continue;

            String dateStr = date.format(dateFormatter);

            // Wizyty lekarskie
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

            // Badania laboratoryjne
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

            // Zabiegi ambulatoryjne
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

        // Oznacz zajęte terminy
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
        panels.put("Dashboard", patientDashboard());
        panels.put("VisitMenu", visitMenuPanel());
        panels.put("RegisterService", registerServicePanel()); // Połączona rejestracja
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

    private JPanel patientDashboard() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(240, 248, 255));

        JPanel welcomePanel = new JPanel();
        welcomePanel.setBackground(new Color(70, 130, 200));
        JLabel welcomeLabel = new JLabel();
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        welcomeLabel.setForeground(Color.WHITE);
        welcomePanel.add(welcomeLabel);
        panel.add(welcomePanel, BorderLayout.NORTH);

        JPanel buttonsPanel = new JPanel(new GridBagLayout());
        buttonsPanel.setBackground(new Color(240, 248, 255));
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        String[][] buttons = {
                {"📅 Wizyty, badania i zabiegi", "VisitMenu"},
                {"📊 Wyniki badań", "TestResults"},
                {"💊 Recepty", "Prescriptions"},
                {"📜 Historia wizyt i zabiegów", "History"},
                {"📄 Skierowania", "Referrals"},
                {"👨‍⚕️ Lista lekarzy", "DoctorsList"},
                {"📋 Zwolnienia lekarskie", "SickLeave"},
                {"🚪 Wyloguj", "Login"}
        };

        int row = 0;
        for (String[] btn : buttons) {
            gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
            JButton button = new JButton(btn[0]);
            button.setPreferredSize(new Dimension(320, 50));
            button.setFont(new Font("SansSerif", Font.BOLD, 14));
            button.setBackground(Color.WHITE);
            button.setFocusPainted(false);

            if (btn[1].equals("Login")) {
                button.addActionListener(e -> {
                    dataManager.saveAllData();
                    currentUser = null;
                    panels.clear();
                    mainPanel.removeAll();
                    mainPanel.add(roleSelectionPanel(), "RoleSelection");
                    mainPanel.add(loginPanel(), "Login");
                    cardLayout.show(mainPanel, "RoleSelection");
                });
            } else {
                button.addActionListener(e -> {
                    refreshPanel(btn[1]);
                    cardLayout.show(mainPanel, btn[1]);
                });
            }

            buttonsPanel.add(button, gbc);
            row++;
        }

        panel.add(buttonsPanel, BorderLayout.CENTER);

        JPanel visitsPanel = new JPanel(new BorderLayout());
        visitsPanel.setBorder(BorderFactory.createTitledBorder("Twoje zarejestrowane wizyty, badania i zabiegi"));
        visitsPanel.setBackground(Color.WHITE);
        visitsPanel.setPreferredSize(new Dimension(400, 550));

        JPanel currentVisits = new JPanel();
        currentVisits.setLayout(new BoxLayout(currentVisits, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(currentVisits);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        visitsPanel.add(scrollPane, BorderLayout.CENTER);

        panel.add(visitsPanel, BorderLayout.EAST);

        panel.putClientProperty("welcomeLabel", welcomeLabel);
        panel.putClientProperty("currentVisits", currentVisits);

        return panel;
    }

    private void refreshDashboard() {
        if (currentUser == null || !(currentUser instanceof Patient)) return;

        Patient patient = (Patient) currentUser;
        JPanel dashboard = panels.get("Dashboard");
        if (dashboard == null) return;

        JLabel welcomeLabel = (JLabel) dashboard.getClientProperty("welcomeLabel");
        JPanel currentVisits = (JPanel) dashboard.getClientProperty("currentVisits");

        if (welcomeLabel != null) {
            welcomeLabel.setText("Witaj, " + patient.getName() + "!");
        }

        if (currentVisits != null) {
            currentVisits.removeAll();
            if (patient.getRegisteredVisits().isEmpty()) {
                JLabel emptyLabel = new JLabel("Brak zarejestrowanych wizyt, badań i zabiegów");
                emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                currentVisits.add(emptyLabel);
            } else {
                for (Visit visit : patient.getRegisteredVisits()) {
                    JPanel visitCard = createVisitCard(visit);
                    currentVisits.add(visitCard);
                    currentVisits.add(Box.createRigidArea(new Dimension(0, 10)));
                }
            }
            currentVisits.revalidate();
            currentVisits.repaint();
        }
    }

    private JPanel createVisitCard(Visit visit) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 200)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        card.setBackground(new Color(240, 248, 255));
        card.setMaximumSize(new Dimension(380, 150));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);

        String icon = visit.getType().equals("LAB") ? "🔬 " : (visit.getType().equals("PROC") ? "💉 " : "👨‍⚕️ ");
        JLabel typeLabel = new JLabel(icon + visit.getDoctorName());
        typeLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        JLabel specLabel = new JLabel("📋 " + visit.getSpecialization());
        JLabel dateLabel = new JLabel("📅 " + visit.getDateTime());

        String paymentText = visit.getPaymentStatus().equals("NFZ") ? "💰 NFZ (refundowana)" :
                (visit.getPaymentStatus().equals("PRIVATE_PAID") ? "💰 Opłacona prywatnie" : "💰 Do zapłaty");
        JLabel paymentLabel = new JLabel(paymentText);
        paymentLabel.setForeground(visit.getPaymentStatus().equals("NFZ") ? new Color(0, 150, 0) :
                (visit.getPaymentStatus().equals("PRIVATE_PAID") ? new Color(0, 150, 0) : new Color(200, 100, 0)));
        JLabel statusLabel = new JLabel("✅ " + visit.getStatus());
        statusLabel.setForeground(new Color(0, 150, 0));

        card.add(typeLabel);
        card.add(specLabel);
        card.add(dateLabel);
        card.add(paymentLabel);
        card.add(statusLabel);

        return card;
    }

    private void refreshPanel(String panelName) {
        if (currentUser == null || !(currentUser instanceof Patient)) return;

        switch (panelName) {
            case "History":
                refreshHistoryPanel();
                break;
            case "TestResults":
                refreshTestResultsPanel();
                break;
            case "VisitMenu":
                refreshVisitMenu();
                break;
            case "RegisterService":
                refreshRegisterServicePanel();
                break;
            case "RescheduleVisit":
                refreshRescheduleVisitPanel();
                break;
            case "CancelVisit":
                refreshCancelVisitPanel();
                break;
        }
    }

    private void refreshHistoryPanel() {
        Patient patient = (Patient) currentUser;
        JPanel historyPanel = panels.get("History");
        if (historyPanel == null) return;

        JPanel content = (JPanel) historyPanel.getClientProperty("contentPanel");
        if (content != null) {
            content.removeAll();

            JLabel titleLabel = new JLabel("HISTORIA WSZYSTKICH WIZYT, BADAŃ I ZABIEGÓW");
            titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
            titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            content.add(titleLabel);
            content.add(Box.createRigidArea(new Dimension(0, 20)));

            if (!patient.getHistory().isEmpty()) {
                JLabel historyTitle = new JLabel("Zakończone:");
                historyTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
                historyTitle.setForeground(new Color(70, 130, 200));
                historyTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
                content.add(historyTitle);
                content.add(Box.createRigidArea(new Dimension(0, 10)));

                for (Visit visit : patient.getHistory()) {
                    JPanel visitCard = createHistoryVisitCard(visit);
                    content.add(visitCard);
                    content.add(Box.createRigidArea(new Dimension(0, 10)));
                }
                content.add(Box.createRigidArea(new Dimension(0, 20)));
            }

            if (!patient.getRegisteredVisits().isEmpty()) {
                JLabel registeredTitle = new JLabel("Aktualnie zarejestrowane:");
                registeredTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
                registeredTitle.setForeground(new Color(70, 130, 200));
                registeredTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
                content.add(registeredTitle);
                content.add(Box.createRigidArea(new Dimension(0, 10)));

                for (Visit visit : patient.getRegisteredVisits()) {
                    JPanel visitCard = createHistoryVisitCard(visit);
                    content.add(visitCard);
                    content.add(Box.createRigidArea(new Dimension(0, 10)));
                }
            }

            if (patient.getHistory().isEmpty() && patient.getRegisteredVisits().isEmpty()) {
                content.add(new JLabel("Brak historii"));
            }

            JButton backBtn = new JButton("Powrót");
            backBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            backBtn.addActionListener(e -> cardLayout.show(mainPanel, "Dashboard"));
            content.add(Box.createRigidArea(new Dimension(0, 20)));
            content.add(backBtn);
            content.revalidate();
            content.repaint();
        }
    }

    private JPanel createHistoryVisitCard(Visit visit) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        card.setBackground(Color.WHITE);
        card.setMaximumSize(new Dimension(650, 130));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        String icon = visit.getType().equals("LAB") ? "🔬 " : (visit.getType().equals("PROC") ? "💉 " : "👨‍⚕️ ");
        card.add(new JLabel(icon + visit.getDoctorName()));
        card.add(new JLabel("Typ: " + visit.getSpecialization()));
        card.add(new JLabel("Data: " + visit.getDateTime()));
        card.add(new JLabel("Status: " + visit.getStatus()));
        String paymentText = visit.getPaymentStatus().equals("NFZ") ? "NFZ (refundowana)" :
                (visit.getPaymentStatus().equals("PRIVATE_PAID") ? "Opłacona prywatnie" : "Do zapłaty");
        card.add(new JLabel("Płatność: " + paymentText));
        if (visit.getNotes() != null && !visit.getNotes().isEmpty()) {
            card.add(new JLabel("Uwagi: " + visit.getNotes()));
        }

        return card;
    }

    private void refreshTestResultsPanel() {
        Patient patient = (Patient) currentUser;
        JPanel resultsPanel = panels.get("TestResults");
        if (resultsPanel == null) return;

        JPanel content = (JPanel) resultsPanel.getClientProperty("contentPanel");
        if (content != null) {
            content.removeAll();
            if (patient.getTestResults().isEmpty()) {
                content.add(new JLabel("Brak wyników badań"));
            } else {
                for (TestResult result : patient.getTestResults()) {
                    JPanel resultCard = createResultCard(result);
                    content.add(resultCard);
                    content.add(Box.createRigidArea(new Dimension(0, 10)));
                }
            }
            JButton backBtn = new JButton("Powrót");
            backBtn.addActionListener(e -> cardLayout.show(mainPanel, "Dashboard"));
            content.add(backBtn);
            content.revalidate();
            content.repaint();
        }
    }

    private JPanel createResultCard(TestResult result) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 200)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        card.setBackground(Color.WHITE);
        card.setMaximumSize(new Dimension(600, 100));

        card.add(new JLabel("🔬 " + result.getTestName()));
        card.add(new JLabel("📅 Data: " + result.getDate()));
        card.add(new JLabel("📊 Wynik: " + result.getResult()));
        card.add(new JLabel("👨‍⚕️ Lekarz: " + result.getDoctor()));

        return card;
    }

    private void refreshVisitMenu() {
        Patient patient = (Patient) currentUser;
        JPanel visitPanel = panels.get("VisitMenu");
        if (visitPanel == null) return;

        JPanel registeredPanel = (JPanel) visitPanel.getClientProperty("registeredVisitsPanel");
        if (registeredPanel != null) {
            registeredPanel.removeAll();
            if (patient.getRegisteredVisits().isEmpty()) {
                registeredPanel.add(new JLabel("Brak zarejestrowanych"));
            } else {
                for (Visit visit : patient.getRegisteredVisits()) {
                    JPanel visitCard = createSmallVisitCard(visit);
                    registeredPanel.add(visitCard);
                    registeredPanel.add(Box.createRigidArea(new Dimension(0, 5)));
                }
            }
            registeredPanel.revalidate();
            registeredPanel.repaint();
        }
    }

    private JPanel createSmallVisitCard(Visit visit) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        card.setBackground(new Color(240, 248, 255));
        card.setMaximumSize(new Dimension(350, 100));

        String icon = visit.getType().equals("LAB") ? "🔬" : (visit.getType().equals("PROC") ? "💉" : "👨‍⚕️");
        card.add(new JLabel(icon + " " + visit.getDoctorName()));
        card.add(new JLabel(visit.getDateTime()));
        card.add(new JLabel(visit.getStatus()));
        String paymentText = visit.getPaymentStatus().equals("NFZ") ? "NFZ" :
                (visit.getPaymentStatus().equals("PRIVATE_PAID") ? "Opłacona" : "Do zapłaty");
        card.add(new JLabel(paymentText));

        return card;
    }

    private void refreshRegisterServicePanel() {
        JPanel registerPanel = panels.get("RegisterService");
        if (registerPanel == null) return;

        JComboBox<String> serviceTypeCombo = (JComboBox<String>) registerPanel.getClientProperty("serviceTypeCombo");
        JComboBox<String> serviceCombo = (JComboBox<String>) registerPanel.getClientProperty("serviceCombo");

        if (serviceTypeCombo != null && serviceCombo != null) {
            String selectedType = (String) serviceTypeCombo.getSelectedItem();
            serviceCombo.removeAllItems();

            if (selectedType.equals("Wizyta lekarska")) {
                for (Doctor doctor : dataManager.getDoctors()) {
                    serviceCombo.addItem(doctor.getName() + " (" + doctor.getSpecialization() + ")");
                }
            } else if (selectedType.equals("Badanie laboratoryjne")) {
                String[] labTests = {"Pobranie krwi", "EKG", "USG", "Holter EKG", "RTG klatki piersiowej"};
                for (String test : labTests) {
                    serviceCombo.addItem(test);
                }
            } else if (selectedType.equals("Zabieg ambulatoryjny")) {
                String[] procedures = {"Zastrzyk domięśniowy", "Szczepienie", "Usunięcie szwów", "Opracowanie rany", "Iniekcja dostawowa"};
                for (String proc : procedures) {
                    serviceCombo.addItem(proc);
                }
            }
        }
        refreshAvailableServices();
    }

    private void refreshAvailableServices() {
        JPanel registerPanel = panels.get("RegisterService");
        if (registerPanel == null) return;

        JPanel appointmentsPanel = (JPanel) registerPanel.getClientProperty("appointmentsPanel");
        JComboBox<String> serviceTypeCombo = (JComboBox<String>) registerPanel.getClientProperty("serviceTypeCombo");
        JComboBox<String> serviceCombo = (JComboBox<String>) registerPanel.getClientProperty("serviceCombo");
        JComboBox<String> paymentCombo = (JComboBox<String>) registerPanel.getClientProperty("paymentCombo");

        if (appointmentsPanel != null && serviceTypeCombo != null && serviceCombo != null && paymentCombo != null) {
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
                        if (app.getDoctorName().equals(doctorName) && app.isAvailable() && app.getPaymentType().equals(paymentCode)) {
                            groupedByDate.computeIfAbsent(app.getDate(), k -> new ArrayList<>()).add(app);
                        }
                    }
                } else if (serviceType.equals("Badanie laboratoryjne")) {
                    for (AvailableAppointment app : availableLaboratoryTests) {
                        if (app.getDoctorName().equals(selected) && app.isAvailable() && app.getPaymentType().equals(paymentCode)) {
                            groupedByDate.computeIfAbsent(app.getDate(), k -> new ArrayList<>()).add(app);
                        }
                    }
                } else if (serviceType.equals("Zabieg ambulatoryjny")) {
                    for (AvailableAppointment app : availableProcedures) {
                        if (app.getDoctorName().equals(selected) && app.isAvailable() && app.getPaymentType().equals(paymentCode)) {
                            groupedByDate.computeIfAbsent(app.getDate(), k -> new ArrayList<>()).add(app);
                        }
                    }
                }

                if (groupedByDate.isEmpty()) {
                    appointmentsPanel.add(new JLabel("Brak dostępnych terminów dla wybranej usługi i formy płatności"));
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
                            hourBtn.addActionListener(e -> {
                                registerPanel.putClientProperty("selectedDate", app.getDate());
                                registerPanel.putClientProperty("selectedHour", app.getHour());
                                registerPanel.putClientProperty("selectedServiceType", serviceType);
                                registerPanel.putClientProperty("selectedServiceName", app.getDoctorName());
                                registerPanel.putClientProperty("selectedSpecialization", app.getSpecialization());
                                registerPanel.putClientProperty("selectedType", app.getType());
                                registerPanel.putClientProperty("selectedPaymentType", paymentCode);
                                JOptionPane.showMessageDialog(frame,
                                        "Wybrano termin: " + app.getDate() + " " + app.getHour() + "\n" + paymentType,
                                        "Wybór terminu",
                                        JOptionPane.INFORMATION_MESSAGE);
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
        }
    }

    private void refreshRescheduleVisitPanel() {
        Patient patient = (Patient) currentUser;
        JPanel reschedulePanel = panels.get("RescheduleVisit");
        if (reschedulePanel == null) return;

        JComboBox<Visit> visitCombo = (JComboBox<Visit>) reschedulePanel.getClientProperty("visitCombo");
        if (visitCombo != null) {
            visitCombo.removeAllItems();
            for (Visit visit : patient.getRegisteredVisits()) {
                visitCombo.addItem(visit);
            }
        }
    }

    private void refreshCancelVisitPanel() {
        Patient patient = (Patient) currentUser;
        JPanel cancelPanel = panels.get("CancelVisit");
        if (cancelPanel == null) return;

        JComboBox<Visit> visitCombo = (JComboBox<Visit>) cancelPanel.getClientProperty("visitCombo");
        if (visitCombo != null) {
            visitCombo.removeAllItems();
            for (Visit visit : patient.getRegisteredVisits()) {
                visitCombo.addItem(visit);
            }
        }
    }

    private JPanel visitMenuPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(240, 248, 255));

        JPanel buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.setBackground(new Color(240, 248, 255));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JButton registerBtn = new JButton("📝 Zarejestruj się na wizytę");
        JButton rescheduleBtn = new JButton("🔄 Przełóż wizytę");
        JButton cancelBtn = new JButton("❌ Odwołaj wizytę");
        JButton backBtn = new JButton("◀ Powrót");

        registerBtn.addActionListener(e -> {
            refreshPanel("RegisterService");
            cardLayout.show(mainPanel, "RegisterService");
        });
        rescheduleBtn.addActionListener(e -> {
            refreshPanel("RescheduleVisit");
            cardLayout.show(mainPanel, "RescheduleVisit");
        });
        cancelBtn.addActionListener(e -> {
            refreshPanel("CancelVisit");
            cardLayout.show(mainPanel, "CancelVisit");
        });
        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "Dashboard"));

        gbc.gridx = 0; gbc.gridy = 0;
        buttonPanel.add(registerBtn, gbc);
        gbc.gridy = 1;
        buttonPanel.add(rescheduleBtn, gbc);
        gbc.gridy = 2;
        buttonPanel.add(cancelBtn, gbc);
        gbc.gridy = 3;
        buttonPanel.add(backBtn, gbc);

        JPanel registeredPanel = new JPanel();
        registeredPanel.setLayout(new BoxLayout(registeredPanel, BoxLayout.Y_AXIS));
        registeredPanel.setBorder(BorderFactory.createTitledBorder("Twoje zarejestrowane usługi"));
        registeredPanel.setBackground(Color.WHITE);
        registeredPanel.setPreferredSize(new Dimension(380, 500));

        JScrollPane scrollPane = new JScrollPane(registeredPanel);

        panel.add(buttonPanel, BorderLayout.CENTER);
        panel.add(scrollPane, BorderLayout.EAST);

        panel.putClientProperty("registeredVisitsPanel", registeredPanel);

        return panel;
    }

    private JPanel registerServicePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JPanel topPanel = new JPanel(new GridBagLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        topPanel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel title = new JLabel("Rejestracja na wizytę / badanie / zabieg");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        topPanel.add(title, gbc);

        JLabel typeLabel = new JLabel("Rodzaj usługi:");
        typeLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        gbc.gridy = 1; gbc.gridwidth = 1;
        topPanel.add(typeLabel, gbc);

        JComboBox<String> serviceTypeCombo = new JComboBox<>(new String[]{"Wizyta lekarska", "Badanie laboratoryjne", "Zabieg ambulatoryjny"});
        serviceTypeCombo.setPreferredSize(new Dimension(250, 30));
        gbc.gridx = 1;
        topPanel.add(serviceTypeCombo, gbc);

        JLabel serviceLabel = new JLabel("Wybierz:");
        serviceLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        gbc.gridx = 0; gbc.gridy = 2;
        topPanel.add(serviceLabel, gbc);

        JComboBox<String> serviceCombo = new JComboBox<>();
        serviceCombo.setPreferredSize(new Dimension(300, 30));
        gbc.gridx = 1;
        topPanel.add(serviceCombo, gbc);

        JLabel paymentLabel = new JLabel("Forma płatności:");
        gbc.gridx = 0; gbc.gridy = 3;
        topPanel.add(paymentLabel, gbc);

        JComboBox<String> paymentCombo = new JComboBox<>(new String[]{"NFZ (refundowana)", "Płatna prywatnie"});
        paymentCombo.setPreferredSize(new Dimension(200, 30));
        gbc.gridx = 1;
        topPanel.add(paymentCombo, gbc);

        JButton refreshBtn = new JButton("Odśwież dostępne terminy");
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        topPanel.add(refreshBtn, gbc);

        JPanel appointmentsPanel = new JPanel();
        appointmentsPanel.setLayout(new BoxLayout(appointmentsPanel, BoxLayout.Y_AXIS));
        appointmentsPanel.setBorder(BorderFactory.createTitledBorder("Dostępne terminy"));
        appointmentsPanel.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(appointmentsPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        bottomPanel.setBackground(Color.WHITE);

        JButton registerBtn = new JButton("✅ Zarejestruj wybrany termin");
        registerBtn.setBackground(new Color(70, 130, 200));
        registerBtn.setForeground(Color.WHITE);
        registerBtn.setFont(new Font("SansSerif", Font.BOLD, 14));

        JButton backBtn = new JButton("Powrót");

        bottomPanel.add(registerBtn);
        bottomPanel.add(backBtn);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        serviceTypeCombo.addActionListener(e -> refreshRegisterServicePanel());
        serviceCombo.addActionListener(e -> refreshAvailableServices());
        paymentCombo.addActionListener(e -> refreshAvailableServices());
        refreshBtn.addActionListener(e -> refreshAvailableServices());

        registerBtn.addActionListener(e -> {
            Patient patient = (Patient) currentUser;
            String selectedDate = (String) panel.getClientProperty("selectedDate");
            String selectedHour = (String) panel.getClientProperty("selectedHour");
            String serviceName = (String) panel.getClientProperty("selectedServiceName");
            String specialization = (String) panel.getClientProperty("selectedSpecialization");
            String serviceType = (String) panel.getClientProperty("selectedServiceType");
            String visitType = (String) panel.getClientProperty("selectedType");
            String paymentCode = (String) panel.getClientProperty("selectedPaymentType");
            String paymentType = (String) paymentCombo.getSelectedItem();

            // SPRAWDZENIE CZY WYBRANO TERMIN
            if (selectedDate == null || selectedHour == null) {
                JOptionPane.showMessageDialog(frame,
                        "Najpierw wybierz termin z listy dostępnych godzin!\n" +
                                "Kliknij na konkretną godzinę, aby wybrać termin.",
                        "Brak wybranego terminu",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (serviceName == null) {
                JOptionPane.showMessageDialog(frame,
                        "Najpierw wybierz rodzaj usługi i konkretną usługę z listy!",
                        "Brak wybranej usługi",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (selectedDate != null && selectedHour != null && serviceName != null) {
                String paymentStatus = "NFZ";

                if (paymentCode.equals("PRIVATE")) {
                    String[] options = {"Zapłać BLIK teraz", "Zapłać w rejestracji później"};
                    int choice = JOptionPane.showOptionDialog(frame,
                            "Wybierz sposób płatności:", "Płatność za usługę",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

                    if (choice == 0) {
                        String blik = JOptionPane.showInputDialog(frame, "Podaj kod BLIK (6 cyfr):");
                        if (blik != null && blik.matches("\\d{6}")) {
                            paymentStatus = "PRIVATE_PAID";
                            JOptionPane.showMessageDialog(frame, "Płatność została potwierdzona!", "Sukces", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(frame, "Nieprawidłowy kod BLIK. Rejestracja nie została dokonana.", "Błąd", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    } else {
                        paymentStatus = "PRIVATE_UNPAID";
                        JOptionPane.showMessageDialog(frame, "Prosimy o uregulowanie płatności w rejestracji przed usługą.", "Informacja", JOptionPane.INFORMATION_MESSAGE);
                    }
                }

                int newId = patient.getRegisteredVisits().size() + 100;
                Visit newVisit = new Visit(newId, serviceName, specialization,
                        selectedDate + " " + selectedHour, "Zarejestrowana", "", visitType);
                newVisit.setPaymentStatus(paymentStatus);
                patient.addRegisteredVisit(newVisit);

                if (visitType.equals("VISIT")) {
                    for (AvailableAppointment app : availableAppointments) {
                        if (app.getDoctorName().equals(serviceName) &&
                                app.getDate().equals(selectedDate) &&
                                app.getHour().equals(selectedHour) &&
                                app.getPaymentType().equals(paymentCode)) {
                            app.setAvailable(false);
                            break;
                        }
                    }
                } else if (visitType.equals("LAB")) {
                    for (AvailableAppointment app : availableLaboratoryTests) {
                        if (app.getDoctorName().equals(serviceName) &&
                                app.getDate().equals(selectedDate) &&
                                app.getHour().equals(selectedHour) &&
                                app.getPaymentType().equals(paymentCode)) {
                            app.setAvailable(false);
                            break;
                        }
                    }
                } else if (visitType.equals("PROC")) {
                    for (AvailableAppointment app : availableProcedures) {
                        if (app.getDoctorName().equals(serviceName) &&
                                app.getDate().equals(selectedDate) &&
                                app.getHour().equals(selectedHour) &&
                                app.getPaymentType().equals(paymentCode)) {
                            app.setAvailable(false);
                            break;
                        }
                    }
                }

                dataManager.saveAllData();
                JOptionPane.showMessageDialog(frame, "Usługa została zarejestrowana!");
                refreshDashboard();
                refreshVisitMenu();
                refreshAvailableServices();
                cardLayout.show(mainPanel, "VisitMenu");
            }
        });

        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "VisitMenu"));

        panel.putClientProperty("serviceTypeCombo", serviceTypeCombo);
        panel.putClientProperty("serviceCombo", serviceCombo);
        panel.putClientProperty("paymentCombo", paymentCombo);
        panel.putClientProperty("appointmentsPanel", appointmentsPanel);

        return panel;
    }

    private JPanel rescheduleVisitPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel title = new JLabel("Przełożenie wizyty/badania/zabiegu");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(title, gbc);

        JLabel visitLabel = new JLabel("Wybierz usługę do przełożenia:");
        gbc.gridy = 1; gbc.gridwidth = 1;
        panel.add(visitLabel, gbc);

        JComboBox<Visit> visitCombo = new JComboBox<>();
        visitCombo.setPreferredSize(new Dimension(350, 30));
        gbc.gridx = 1;
        panel.add(visitCombo, gbc);

        JLabel newServiceLabel = new JLabel("Nowa usługa (możesz zmienić typ):");
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(newServiceLabel, gbc);

        JComboBox<String> newServiceTypeCombo = new JComboBox<>(new String[]{"Wizyta lekarska", "Badanie laboratoryjne", "Zabieg ambulatoryjny"});
        newServiceTypeCombo.setPreferredSize(new Dimension(200, 30));
        gbc.gridx = 1;
        panel.add(newServiceTypeCombo, gbc);

        JComboBox<String> newServiceCombo = new JComboBox<>();
        newServiceCombo.setPreferredSize(new Dimension(300, 30));
        gbc.gridy = 3;
        panel.add(newServiceCombo, gbc);

        JLabel newDateLabel = new JLabel("Nowy termin (wybierz z listy):");
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(newDateLabel, gbc);

        JPanel newAppointmentsPanel = new JPanel();
        newAppointmentsPanel.setLayout(new BoxLayout(newAppointmentsPanel, BoxLayout.Y_AXIS));
        newAppointmentsPanel.setBorder(BorderFactory.createTitledBorder("Dostępne terminy"));
        newAppointmentsPanel.setBackground(Color.WHITE);
        newAppointmentsPanel.setPreferredSize(new Dimension(400, 200));

        JScrollPane scrollPane = new JScrollPane(newAppointmentsPanel);
        scrollPane.setPreferredSize(new Dimension(450, 200));
        gbc.gridx = 1; gbc.gridy = 4;
        panel.add(scrollPane, gbc);

        JButton rescheduleBtn = new JButton("Przełóż");
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        panel.add(rescheduleBtn, gbc);

        JButton backBtn = new JButton("Powrót");
        gbc.gridy = 6;
        panel.add(backBtn, gbc);

        // Aktualizacja listy nowych usług po zmianie typu
        newServiceTypeCombo.addActionListener(e -> {
            String selectedType = (String) newServiceTypeCombo.getSelectedItem();
            newServiceCombo.removeAllItems();

            if (selectedType.equals("Wizyta lekarska")) {
                for (Doctor doctor : dataManager.getDoctors()) {
                    newServiceCombo.addItem(doctor.getName() + " (" + doctor.getSpecialization() + ")");
                }
            } else if (selectedType.equals("Badanie laboratoryjne")) {
                String[] labTests = {"Pobranie krwi", "EKG", "USG", "Holter EKG", "RTG klatki piersiowej"};
                for (String test : labTests) {
                    newServiceCombo.addItem(test);
                }
            } else if (selectedType.equals("Zabieg ambulatoryjny")) {
                String[] procedures = {"Zastrzyk domięśniowy", "Szczepienie", "Usunięcie szwów", "Opracowanie rany", "Iniekcja dostawowa"};
                for (String proc : procedures) {
                    newServiceCombo.addItem(proc);
                }
            }
            refreshNewAppointments(newServiceTypeCombo, newServiceCombo, newAppointmentsPanel, panel);
        });

        newServiceCombo.addActionListener(e -> {
            refreshNewAppointments(newServiceTypeCombo, newServiceCombo, newAppointmentsPanel, panel);
        });

        rescheduleBtn.addActionListener(e -> {
            Patient patient = (Patient) currentUser;
            Visit selected = (Visit) visitCombo.getSelectedItem();
            String newDate = (String) panel.getClientProperty("newSelectedDate");
            String newHour = (String) panel.getClientProperty("newSelectedHour");
            String newServiceName = (String) panel.getClientProperty("newServiceName");
            String newSpecialization = (String) panel.getClientProperty("newSpecialization");
            String newVisitType = (String) panel.getClientProperty("newVisitType");
            String newPaymentCode = (String) panel.getClientProperty("newPaymentType");

            // SPRAWDZENIE CZY WYBRANO STARĄ USŁUGĘ
            if (selected == null) {
                JOptionPane.showMessageDialog(frame,
                        "Najpierw wybierz usługę, którą chcesz przełożyć!",
                        "Brak wybranej usługi",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // SPRAWDZENIE CZY WYBRANO NOWY TERMIN
            if (newDate == null || newHour == null) {
                JOptionPane.showMessageDialog(frame,
                        "Najpierw wybierz nowy termin z listy dostępnych godzin!\n" +
                                "Kliknij na konkretną godzinę, aby wybrać nowy termin.",
                        "Brak wybranego nowego terminu",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (newServiceName == null) {
                JOptionPane.showMessageDialog(frame,
                        "Najpierw wybierz nową usługę z listy!",
                        "Brak wybranej nowej usługi",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (selected != null && newDate != null && newHour != null && newServiceName != null) {
                // Przywróć stary termin
                String[] oldDateTime = selected.getDateTime().split(" ");
                String oldPaymentCode = selected.getPaymentStatus().equals("NFZ") ? "NFZ" : "PRIVATE";

                if (selected.getType().equals("VISIT")) {
                    for (AvailableAppointment app : availableAppointments) {
                        if (app.getDoctorName().equals(selected.getDoctorName()) &&
                                app.getDate().equals(oldDateTime[0]) &&
                                app.getHour().equals(oldDateTime[1]) &&
                                app.getPaymentType().equals(oldPaymentCode)) {
                            app.setAvailable(true);
                            break;
                        }
                    }
                } else if (selected.getType().equals("LAB")) {
                    for (AvailableAppointment app : availableLaboratoryTests) {
                        if (app.getDoctorName().equals(selected.getDoctorName()) &&
                                app.getDate().equals(oldDateTime[0]) &&
                                app.getHour().equals(oldDateTime[1]) &&
                                app.getPaymentType().equals(oldPaymentCode)) {
                            app.setAvailable(true);
                            break;
                        }
                    }
                } else if (selected.getType().equals("PROC")) {
                    for (AvailableAppointment app : availableProcedures) {
                        if (app.getDoctorName().equals(selected.getDoctorName()) &&
                                app.getDate().equals(oldDateTime[0]) &&
                                app.getHour().equals(oldDateTime[1]) &&
                                app.getPaymentType().equals(oldPaymentCode)) {
                            app.setAvailable(true);
                            break;
                        }
                    }
                }

                // Aktualizuj wizytę
                selected.setDoctorName(newServiceName);
                selected.setSpecialization(newSpecialization);
                selected.setType(newVisitType);
                selected.setDateTime(newDate + " " + newHour);

                // Zaznacz nowy termin jako zajęty
                if (newVisitType.equals("VISIT")) {
                    for (AvailableAppointment app : availableAppointments) {
                        if (app.getDoctorName().equals(newServiceName) &&
                                app.getDate().equals(newDate) &&
                                app.getHour().equals(newHour) &&
                                app.getPaymentType().equals(newPaymentCode)) {
                            app.setAvailable(false);
                            break;
                        }
                    }
                } else if (newVisitType.equals("LAB")) {
                    for (AvailableAppointment app : availableLaboratoryTests) {
                        if (app.getDoctorName().equals(newServiceName) &&
                                app.getDate().equals(newDate) &&
                                app.getHour().equals(newHour) &&
                                app.getPaymentType().equals(newPaymentCode)) {
                            app.setAvailable(false);
                            break;
                        }
                    }
                } else if (newVisitType.equals("PROC")) {
                    for (AvailableAppointment app : availableProcedures) {
                        if (app.getDoctorName().equals(newServiceName) &&
                                app.getDate().equals(newDate) &&
                                app.getHour().equals(newHour) &&
                                app.getPaymentType().equals(newPaymentCode)) {
                            app.setAvailable(false);
                            break;
                        }
                    }
                }

                dataManager.saveAllData();
                JOptionPane.showMessageDialog(frame, "Usługa została przełożona!");
                refreshDashboard();
                refreshVisitMenu();
                cardLayout.show(mainPanel, "VisitMenu");
            }
        });

        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "VisitMenu"));

        panel.putClientProperty("visitCombo", visitCombo);
        panel.putClientProperty("newServiceTypeCombo", newServiceTypeCombo);
        panel.putClientProperty("newServiceCombo", newServiceCombo);
        panel.putClientProperty("newAppointmentsPanel", newAppointmentsPanel);

        return panel;
    }

    private void refreshNewAppointments(JComboBox<String> serviceTypeCombo, JComboBox<String> serviceCombo,
                                        JPanel appointmentsPanel, JPanel parentPanel) {
        appointmentsPanel.removeAll();

        String serviceType = (String) serviceTypeCombo.getSelectedItem();
        String selected = (String) serviceCombo.getSelectedItem();

        if (selected != null) {
            Map<String, List<AvailableAppointment>> groupedByDate = new LinkedHashMap<>();

            if (serviceType.equals("Wizyta lekarska")) {
                String doctorName = selected.split(" \\(")[0];
                for (AvailableAppointment app : availableAppointments) {
                    if (app.getDoctorName().equals(doctorName) && app.isAvailable()) {
                        groupedByDate.computeIfAbsent(app.getDate(), k -> new ArrayList<>()).add(app);
                    }
                }
            } else if (serviceType.equals("Badanie laboratoryjne")) {
                for (AvailableAppointment app : availableLaboratoryTests) {
                    if (app.getDoctorName().equals(selected) && app.isAvailable()) {
                        groupedByDate.computeIfAbsent(app.getDate(), k -> new ArrayList<>()).add(app);
                    }
                }
            } else if (serviceType.equals("Zabieg ambulatoryjny")) {
                for (AvailableAppointment app : availableProcedures) {
                    if (app.getDoctorName().equals(selected) && app.isAvailable()) {
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
                        JButton hourBtn = new JButton(app.getHour() + " (" + (app.getPaymentType().equals("NFZ") ? "NFZ" : "Prywatna") + ")");
                        hourBtn.setBackground(new Color(70, 130, 200));
                        hourBtn.setForeground(Color.WHITE);
                        hourBtn.addActionListener(e -> {
                            parentPanel.putClientProperty("newSelectedDate", app.getDate());
                            parentPanel.putClientProperty("newSelectedHour", app.getHour());
                            parentPanel.putClientProperty("newServiceName", app.getDoctorName());
                            parentPanel.putClientProperty("newSpecialization", app.getSpecialization());
                            parentPanel.putClientProperty("newVisitType", app.getType());
                            parentPanel.putClientProperty("newPaymentType", app.getPaymentType());
                            JOptionPane.showMessageDialog(frame,
                                    "Wybrano termin: " + app.getDate() + " " + app.getHour() + "\n" +
                                            (app.getPaymentType().equals("NFZ") ? "NFZ" : "Prywatna"),
                                    "Wybór terminu",
                                    JOptionPane.INFORMATION_MESSAGE);
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
    }

    private JPanel cancelVisitPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel title = new JLabel("Odwołanie wizyty/badania/zabiegu");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(title, gbc);

        JLabel visitLabel = new JLabel("Wybierz usługę do odwołania:");
        gbc.gridy = 1; gbc.gridwidth = 1;
        panel.add(visitLabel, gbc);

        JComboBox<Visit> visitCombo = new JComboBox<>();
        visitCombo.setPreferredSize(new Dimension(350, 30));
        gbc.gridx = 1;
        panel.add(visitCombo, gbc);

        JButton cancelBtn = new JButton("Odwołaj");
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        panel.add(cancelBtn, gbc);

        JButton backBtn = new JButton("Powrót");
        gbc.gridy = 3;
        panel.add(backBtn, gbc);

        cancelBtn.addActionListener(e -> {
            Patient patient = (Patient) currentUser;
            Visit selected = (Visit) visitCombo.getSelectedItem();

            // SPRAWDZENIE CZY WYBRANO USŁUGĘ
            if (selected == null) {
                JOptionPane.showMessageDialog(frame,
                        "Najpierw wybierz usługę, którą chcesz odwołać!",
                        "Brak wybranej usługi",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (selected != null) {
                int confirm = JOptionPane.showConfirmDialog(frame,
                        "Czy na pewno chcesz odwołać:\n" +
                                selected.getDoctorName() + "\n" +
                                selected.getDateTime() + "\n" +
                                "Ta operacja jest nieodwracalna!",
                        "Potwierdzenie odwołania",
                        JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    patient.getRegisteredVisits().remove(selected);

                    String[] dateTime = selected.getDateTime().split(" ");
                    String paymentCode = selected.getPaymentStatus().equals("NFZ") ? "NFZ" : "PRIVATE";

                    if (selected.getType().equals("VISIT")) {
                        for (AvailableAppointment app : availableAppointments) {
                            if (app.getDoctorName().equals(selected.getDoctorName()) &&
                                    app.getDate().equals(dateTime[0]) &&
                                    app.getHour().equals(dateTime[1]) &&
                                    app.getPaymentType().equals(paymentCode)) {
                                app.setAvailable(true);
                                break;
                            }
                        }
                    } else if (selected.getType().equals("LAB")) {
                        for (AvailableAppointment app : availableLaboratoryTests) {
                            if (app.getDoctorName().equals(selected.getDoctorName()) &&
                                    app.getDate().equals(dateTime[0]) &&
                                    app.getHour().equals(dateTime[1]) &&
                                    app.getPaymentType().equals(paymentCode)) {
                                app.setAvailable(true);
                                break;
                            }
                        }
                    } else if (selected.getType().equals("PROC")) {
                        for (AvailableAppointment app : availableProcedures) {
                            if (app.getDoctorName().equals(selected.getDoctorName()) &&
                                    app.getDate().equals(dateTime[0]) &&
                                    app.getHour().equals(dateTime[1]) &&
                                    app.getPaymentType().equals(paymentCode)) {
                                app.setAvailable(true);
                                break;
                            }
                        }
                    }

                    dataManager.saveAllData();
                    JOptionPane.showMessageDialog(frame, "Usługa została odwołana!");
                    refreshDashboard();
                    refreshVisitMenu();
                    cardLayout.show(mainPanel, "VisitMenu");
                }
            }
        });

        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "VisitMenu"));

        panel.putClientProperty("visitCombo", visitCombo);

        return panel;
    }

    private JPanel prescriptionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JLabel title = new JLabel("Recepty", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        panel.add(title, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        if (currentUser != null && currentUser instanceof Patient) {
            Patient patient = (Patient) currentUser;
            for (Prescription pres : patient.getPrescriptions()) {
                JPanel presCard = new JPanel();
                presCard.setLayout(new BoxLayout(presCard, BoxLayout.Y_AXIS));
                presCard.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(70, 130, 200)),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)
                ));
                presCard.setBackground(new Color(240, 248, 255));
                presCard.setMaximumSize(new Dimension(600, 100));

                presCard.add(new JLabel("💊 Recepta nr: " + pres.getNumber()));
                presCard.add(new JLabel("📋 " + pres.getMedicines()));
                presCard.add(new JLabel("📅 Data wystawienia: " + pres.getDate()));
                presCard.add(new JLabel("👨‍⚕️ Lekarz: " + pres.getDoctor()));

                content.add(presCard);
                content.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        }

        JButton backBtn = new JButton("Powrót");
        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "Dashboard"));
        content.add(backBtn);

        JScrollPane scrollPane = new JScrollPane(content);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel historyPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JLabel title = new JLabel("Historia wizyt, badań i zabiegów", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        panel.add(title, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        panel.add(scrollPane, BorderLayout.CENTER);

        panel.putClientProperty("contentPanel", content);

        return panel;
    }

    private JPanel referralsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JLabel title = new JLabel("Skierowania", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        panel.add(title, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        if (currentUser != null && currentUser instanceof Patient) {
            Patient patient = (Patient) currentUser;
            for (Referral ref : patient.getReferrals()) {
                JPanel refCard = new JPanel();
                refCard.setLayout(new BoxLayout(refCard, BoxLayout.Y_AXIS));
                refCard.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(70, 130, 200)),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)
                ));
                refCard.setBackground(new Color(240, 248, 255));
                refCard.setMaximumSize(new Dimension(600, 100));

                refCard.add(new JLabel("📄 Skierowanie nr: " + ref.getNumber()));
                refCard.add(new JLabel("🔬 Na: " + ref.getType()));
                refCard.add(new JLabel("📅 Ważne do: " + ref.getValidUntil()));
                refCard.add(new JLabel("👨‍⚕️ Wystawił: " + ref.getDoctor()));

                content.add(refCard);
                content.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        }

        JButton backBtn = new JButton("Powrót");
        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "Dashboard"));
        content.add(backBtn);

        JScrollPane scrollPane = new JScrollPane(content);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel doctorsListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JLabel title = new JLabel("Lista lekarzy", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        panel.add(title, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        for (Doctor doctor : dataManager.getDoctors()) {
            JPanel doctorCard = new JPanel();
            doctorCard.setLayout(new BoxLayout(doctorCard, BoxLayout.Y_AXIS));
            doctorCard.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(70, 130, 200)),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ));
            doctorCard.setBackground(new Color(240, 248, 255));
            doctorCard.setMaximumSize(new Dimension(600, 120));

            doctorCard.add(new JLabel("👨‍⚕️ " + doctor.getName()));
            doctorCard.add(new JLabel("Specjalizacja: " + doctor.getSpecialization()));
            doctorCard.add(new JLabel("Godziny przyjęć: " + doctor.getHours()));
            doctorCard.add(new JLabel("📧 " + doctor.getEmail()));
            doctorCard.add(new JLabel("📞 " + doctor.getPhone()));

            content.add(doctorCard);
            content.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        JButton backBtn = new JButton("Powrót");
        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "Dashboard"));
        content.add(backBtn);

        JScrollPane scrollPane = new JScrollPane(content);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel sickLeavePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JLabel title = new JLabel("Zwolnienia lekarskie", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        panel.add(title, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        if (currentUser != null && currentUser instanceof Patient) {
            Patient patient = (Patient) currentUser;
            for (SickLeave sl : patient.getSickLeaves()) {
                JPanel slCard = new JPanel();
                slCard.setLayout(new BoxLayout(slCard, BoxLayout.Y_AXIS));
                slCard.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(70, 130, 200)),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)
                ));
                slCard.setBackground(new Color(240, 248, 255));
                slCard.setMaximumSize(new Dimension(600, 100));

                slCard.add(new JLabel("📋 Zwolnienie L4"));
                slCard.add(new JLabel("📅 Okres: " + sl.getStartDate() + " - " + sl.getEndDate()));
                slCard.add(new JLabel("👨‍⚕️ Wystawił: " + sl.getDoctor()));
                slCard.add(new JLabel("📝 Kod: " + sl.getCode()));

                content.add(slCard);
                content.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        }

        JButton backBtn = new JButton("Powrót");
        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "Dashboard"));
        content.add(backBtn);

        JScrollPane scrollPane = new JScrollPane(content);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel testResultsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JLabel title = new JLabel("Wyniki badań", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        panel.add(title, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JScrollPane scrollPane = new JScrollPane(content);
        panel.add(scrollPane, BorderLayout.CENTER);

        panel.putClientProperty("contentPanel", content);

        return panel;
    }
}

interface User extends Serializable {
    String getLogin();
    String getPassword();
    String getName();
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
}

class Doctor implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
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
    public String getName() { return name; }
    public String getSpecialization() { return specialization; }
    public String getHours() { return hours; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
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

    @Override
    public String toString() {
        String icon = type.equals("LAB") ? "🔬 " : (type.equals("PROC") ? "💉 " : "👨‍⚕️ ");
        return icon + doctorName + " - " + dateTime + " (" + status + ")";
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
    private static final String PATIENTS_FILE = "pacjenci.txt";
    private static final String DOCTORS_FILE = "lekarze.txt";
    private static final String VISITS_FILE = "wizyty.txt";
    private static final String PRESCRIPTIONS_FILE = "recepty.txt";
    private static final String REFERRALS_FILE = "skierowania.txt";
    private static final String SICK_LEAVES_FILE = "zwolnienia.txt";
    private static final String TEST_RESULTS_FILE = "wyniki_badan.txt";

    private Map<String, Patient> patients = new HashMap<>();
    private List<Doctor> doctors = new ArrayList<>();

    public DataManager() {
        initializeDefaultData();
    }

    private void initializeDefaultData() {
        doctors.add(new Doctor(1, "dr Anna Kowalska", "Internista", "poniedziałek, środa, piątek 9:00-15:00", "anna.kowalska@klinika.pl", "123-456-789"));
        doctors.add(new Doctor(2, "dr Jan Nowak", "Kardiolog", "wtorek, czwartek 10:00-16:00", "jan.nowak@klinika.pl", "123-456-790"));
        doctors.add(new Doctor(3, "dr Maria Wiśniewska", "Dermatolog", "poniedziałek, czwartek 9:00-14:00", "maria.wisniewska@klinika.pl", "123-456-791"));
        doctors.add(new Doctor(4, "dr Piotr Lewandowski", "Okulista", "środa, piątek 11:00-17:00", "piotr.lewandowski@klinika.pl", "123-456-792"));

        Patient user1 = new Patient("user1", "pass1", "Jan Kowalski", "12345678901", "ul. Kwiatowa 15, Warszawa");
        user1.addToHistory(new Visit(1, "dr Anna Kowalska", "Internista", "15.02.2024 10:00", "Zakończona", "Przeziębienie, zalecenie odpoczynku", "VISIT"));
        user1.addToHistory(new Visit(2, "dr Jan Nowak", "Kardiolog", "20.02.2024 11:30", "Zakończona", "Kontrola ciśnienia, wszystko OK", "VISIT"));
        user1.addRegisteredVisit(new Visit(101, "dr Piotr Lewandowski", "Okulista", "10.04.2024 09:00", "Zarejestrowana", "", "VISIT"));
        user1.addRegisteredVisit(new Visit(102, "dr Anna Kowalska", "Internista", "15.04.2024 11:00", "Zarejestrowana", "", "VISIT"));
        user1.addTestResult(new TestResult("Morfologia krwi", "15.03.2024", "Wyniki w normie", "dr Anna Kowalska"));
        user1.addTestResult(new TestResult("EKG", "20.03.2024", "Czynność serca prawidłowa", "dr Jan Nowak"));
        user1.addTestResult(new TestResult("Poziom glukozy", "25.03.2024", "95 mg/dL - prawidłowy", "dr Anna Kowalska"));
        user1.addPrescription(new Prescription("RX-001", "Amoksycylina 500mg - 1x dziennie przez 7 dni", "15.02.2024", "dr Anna Kowalska"));
        user1.addPrescription(new Prescription("RX-002", "Ibuprofen 400mg - w razie bólu", "20.02.2024", "dr Jan Nowak"));
        user1.addReferral(new Referral("SKI-001", "Badanie EKG", "30.06.2024", "dr Jan Nowak"));
        user1.addReferral(new Referral("SKI-002", "USG jamy brzusznej", "15.05.2024", "dr Anna Kowalska"));
        user1.addSickLeave(new SickLeave("10.03.2024", "17.03.2024", "dr Anna Kowalska", "A"));

        Patient user2 = new Patient("user2", "pass2", "Anna Nowak", "98765432109", "ul. Lipowa 7, Kraków");
        user2.addToHistory(new Visit(3, "dr Maria Wiśniewska", "Dermatolog", "05.02.2024 14:00", "Zakończona", "Wysypka alergiczna, przepisano maść", "VISIT"));
        user2.addRegisteredVisit(new Visit(103, "dr Jan Nowak", "Kardiolog", "12.04.2024 13:30", "Zarejestrowana", "", "VISIT"));
        user2.addTestResult(new TestResult("Morfologia krwi", "10.03.2024", "Lekka anemia, zalecana suplementacja żelaza", "dr Anna Kowalska"));
        user2.addTestResult(new TestResult("Ciśnienie krwi", "12.03.2024", "130/85 - w normie", "dr Jan Nowak"));
        user2.addPrescription(new Prescription("RX-003", "Maść hydrokortyzonowa - 2x dziennie", "05.02.2024", "dr Maria Wiśniewska"));
        user2.addPrescription(new Prescription("RX-004", "Żelazo 50mg - 1x dziennie", "10.03.2024", "dr Anna Kowalska"));
        user2.addReferral(new Referral("SKI-003", "Konsultacja alergologiczna", "30.05.2024", "dr Maria Wiśniewska"));

        patients.put("user1", user1);
        patients.put("user2", user2);
    }

    public void loadAllData() {
        File visitsFile = new File(VISITS_FILE);
        if (visitsFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(visitsFile))) {
                String firstLine = reader.readLine();
                if (firstLine != null) {
                    String[] parts = firstLine.split("\\|");
                    if (parts.length < 10) {
                        System.out.println("Stary format pliku wizyt. Usuwam i tworzę nowy.");
                        visitsFile.delete();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        loadDoctors();
        loadPatients();
        loadVisits();
        loadPrescriptions();
        loadReferrals();
        loadSickLeaves();
        loadTestResults();
    }

    private void loadDoctors() {
        File file = new File(DOCTORS_FILE);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                doctors.clear();
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split("\\|");
                    if (parts.length >= 6) {
                        Doctor doc = new Doctor(Integer.parseInt(parts[0]), parts[1], parts[2], parts[3], parts[4], parts[5]);
                        doctors.add(doc);
                    }
                }
                System.out.println("Wczytano " + doctors.size() + " lekarzy z pliku " + DOCTORS_FILE);
            } catch (IOException e) {
                System.err.println("Błąd odczytu pliku lekarzy: " + e.getMessage());
            }
        } else {
            saveDoctors();
        }
    }

    private void saveDoctors() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DOCTORS_FILE))) {
            for (Doctor doc : doctors) {
                writer.write(doc.getId() + "|" + doc.getName() + "|" + doc.getSpecialization() + "|" +
                        doc.getHours() + "|" + doc.getEmail() + "|" + doc.getPhone());
                writer.newLine();
            }
            System.out.println("Zapisano " + doctors.size() + " lekarzy do pliku " + DOCTORS_FILE);
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
                System.out.println("Wczytano " + patients.size() + " pacjentów z pliku " + PATIENTS_FILE);
            } catch (IOException e) {
                System.err.println("Błąd odczytu pliku pacjentów: " + e.getMessage());
            }
        } else {
            savePatients();
        }
    }

    private void savePatients() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(PATIENTS_FILE))) {
            for (Patient patient : patients.values()) {
                writer.write(patient.getLogin() + "|" + patient.getPassword() + "|" +
                        patient.getName() + "|" + patient.getPesel() + "|" + patient.getAddress());
                writer.newLine();
            }
            System.out.println("Zapisano " + patients.size() + " pacjentów do pliku " + PATIENTS_FILE);
        } catch (IOException e) {
            System.err.println("Błąd zapisu pliku pacjentów: " + e.getMessage());
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
                System.out.println("Wczytano wizyty z pliku " + VISITS_FILE);
            } catch (IOException e) {
                System.err.println("Błąd odczytu pliku wizyt: " + e.getMessage());
            }
        } else {
            saveVisits();
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
            System.out.println("Zapisano wizyty do pliku " + VISITS_FILE);
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
                System.out.println("Wczytano recepty z pliku " + PRESCRIPTIONS_FILE);
            } catch (IOException e) {
                System.err.println("Błąd odczytu pliku recept: " + e.getMessage());
            }
        } else {
            savePrescriptions();
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
            System.out.println("Zapisano recepty do pliku " + PRESCRIPTIONS_FILE);
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
                System.out.println("Wczytano skierowania z pliku " + REFERRALS_FILE);
            } catch (IOException e) {
                System.err.println("Błąd odczytu pliku skierowań: " + e.getMessage());
            }
        } else {
            saveReferrals();
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
            System.out.println("Zapisano skierowania do pliku " + REFERRALS_FILE);
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
                System.out.println("Wczytano zwolnienia z pliku " + SICK_LEAVES_FILE);
            } catch (IOException e) {
                System.err.println("Błąd odczytu pliku zwolnień: " + e.getMessage());
            }
        } else {
            saveSickLeaves();
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
            System.out.println("Zapisano zwolnienia do pliku " + SICK_LEAVES_FILE);
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
                System.out.println("Wczytano wyniki badań z pliku " + TEST_RESULTS_FILE);
            } catch (IOException e) {
                System.err.println("Błąd odczytu pliku wyników badań: " + e.getMessage());
            }
        } else {
            saveTestResults();
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
            System.out.println("Zapisano wyniki badań do pliku " + TEST_RESULTS_FILE);
        } catch (IOException e) {
            System.err.println("Błąd zapisu pliku wyników badań: " + e.getMessage());
        }
    }

    public void saveAllData() {
        saveDoctors();
        savePatients();
        saveVisits();
        savePrescriptions();
        saveReferrals();
        saveSickLeaves();
        saveTestResults();
        System.out.println("Wszystkie dane zostały zapisane do plików tekstowych!");
    }

    public Patient getPatient(String login) {
        return patients.get(login);
    }

    public List<Doctor> getDoctors() {
        return doctors;
    }

    public Map<String, Patient> getAllPatients() {
        return patients;
    }
}