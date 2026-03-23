package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ClinicLoginApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ClinicLoginApp().createAndShowGUI());
    }

    private void createAndShowGUI() {
        // Tworzenie głównego okna
        JFrame frame = new JFrame("System Przychodni - Logowanie");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400); // większe okno
        frame.setLocationRelativeTo(null); // wyśrodkowanie okna

        // Panel główny z gradientowym tłem
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                Color color1 = new Color(135, 206, 250); // jasnoniebieski
                Color color2 = new Color(70, 130, 180);  // ciemniejszy niebieski
                GradientPaint gp = new GradientPaint(0, 0, color1, 0, getHeight(), color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Tytuł logowania
        JLabel titleLabel = new JLabel("Witaj w Systemie Przychodni");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 26));
        titleLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;

        // Etykieta Login
        JLabel loginLabel = new JLabel("Login:");
        loginLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        loginLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(loginLabel, gbc);

        // Pole tekstowe Login
        JTextField loginField = new JTextField(20);
        loginField.setFont(new Font("Arial", Font.PLAIN, 16));
        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(loginField, gbc);

        // Etykieta Hasło
        JLabel passwordLabel = new JLabel("Hasło:");
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        passwordLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(passwordLabel, gbc);

        // Pole hasła
        JPasswordField passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 16));
        gbc.gridx = 1;
        gbc.gridy = 2;
        panel.add(passwordField, gbc);

        // Etykieta Rola
        JLabel roleLabel = new JLabel("Rola:");
        roleLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        roleLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(roleLabel, gbc);

        // ComboBox z rolami
        String[] roles = {"Pacjent", "Lekarz", "Admin"};
        JComboBox<String> roleComboBox = new JComboBox<>(roles);
        roleComboBox.setFont(new Font("Arial", Font.PLAIN, 16));
        gbc.gridx = 1;
        gbc.gridy = 3;
        panel.add(roleComboBox, gbc);

        // Przycisk logowania
        JButton loginButton = new JButton("Zaloguj");
        loginButton.setFont(new Font("Arial", Font.BOLD, 18));
        loginButton.setBackground(new Color(70, 130, 180));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(loginButton, gbc);

        // Akcja logowania
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String login = loginField.getText();
                String password = new String(passwordField.getPassword());
                String role = (String) roleComboBox.getSelectedItem();

                JOptionPane.showMessageDialog(frame,
                        "Zalogowano jako: " + role + "\nLogin: " + login,
                        "Logowanie",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        frame.add(panel);
        frame.setVisible(true);
    }
}