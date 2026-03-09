package main.java.com.ubo.tp.message.ihm.login;

import main.java.com.ubo.tp.message.controller.LoginController;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Vue du panneau de connexion/création de compte.
 * Architecture MVC - VIEW
 */
public class LoginPanel extends JPanel {

    private JTextField txtTag;
    private JTextField txtName;
    private JPasswordField txtPassword;

    private JButton btnLogin;
    private JButton btnRegister;

    private LoginController controller;

    public LoginPanel() {
        initComponents();
    }

    /**
     * Injection du contrôleur
     */
    public void setController(LoginController controller) {
        this.controller = controller;
    }
    private void initComponents() {

        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        GridBagConstraints gbc;

        JLabel lblTitle = new JLabel("Connexion / Création de compte");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));

        txtTag = new JTextField(15);
        txtName = new JTextField(15);
        txtPassword = new JPasswordField(15);

        btnLogin = new JButton("Se connecter");
        btnRegister = new JButton("Créer un compte");

        // ===== Ligne 0 : Titre =====
        gbc = new GridBagConstraints(
                0, 0, 2, 1,
                1.0, 0,
                GridBagConstraints.CENTER,
                GridBagConstraints.NONE,
                new Insets(10, 10, 20, 10),
                0, 0);
        add(lblTitle, gbc);

        // ===== Ligne 1 : Tag =====
        gbc = new GridBagConstraints(
                0, 1, 1, 1,
                0, 0,
                GridBagConstraints.EAST,
                GridBagConstraints.NONE,
                new Insets(5, 5, 5, 5),
                0, 0);
        add(new JLabel("Tag : "), gbc);

        gbc = new GridBagConstraints(
                1, 1, 1, 1,
                1.0, 0,
                GridBagConstraints.WEST,
                GridBagConstraints.HORIZONTAL,
                new Insets(5, 5, 5, 5),
                0, 0);
        add(txtTag, gbc);

        // ===== Ligne 2 : Nom =====
        gbc = new GridBagConstraints(
                0, 2, 1, 1,
                0, 0,
                GridBagConstraints.EAST,
                GridBagConstraints.NONE,
                new Insets(5, 5, 5, 5),
                0, 0);
        add(new JLabel("Nom : "), gbc);

        gbc = new GridBagConstraints(
                1, 2, 1, 1,
                1.0, 0,
                GridBagConstraints.WEST,
                GridBagConstraints.HORIZONTAL,
                new Insets(5, 5, 5, 5),
                0, 0);
        add(txtName, gbc);

        // ===== Ligne 3 : Mot de passe =====
        gbc = new GridBagConstraints(
                0, 3, 1, 1,
                0, 0,
                GridBagConstraints.EAST,
                GridBagConstraints.NONE,
                new Insets(5, 5, 5, 5),
                0, 0);
        add(new JLabel("Mot de passe : "), gbc);

        gbc = new GridBagConstraints(
                1, 3, 1, 1,
                1.0, 0,
                GridBagConstraints.WEST,
                GridBagConstraints.HORIZONTAL,
                new Insets(5, 5, 5, 5),
                0, 0);
        add(txtPassword, gbc);

        // ===== Ligne 4 : Boutons =====
        gbc = new GridBagConstraints(
                0, 4, 1, 1,
                1.0, 0,
                GridBagConstraints.EAST,
                GridBagConstraints.NONE,
                new Insets(20, 5, 5, 5),
                0, 0);
        add(btnRegister, gbc);

        gbc = new GridBagConstraints(
                1, 4, 1, 1,
                1.0, 0,
                GridBagConstraints.WEST,
                GridBagConstraints.NONE,
                new Insets(20, 5, 5, 5),
                0, 0);
        add(btnLogin, gbc);

        btnLogin.addActionListener(e -> handleLogin());
        btnRegister.addActionListener(e -> handleRegister());

    }

    private void handleLogin() {

        if (controller == null) return;

        String result = controller.login(
                txtTag.getText().trim(),
                new String(txtPassword.getPassword())
        );

        if ("SUCCESS".equals(result)) {
            JOptionPane.showMessageDialog(this,
                    "Connexion réussie !");
            clearFields();
        } else {
            JOptionPane.showMessageDialog(this,
                    result,
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleRegister() {

        if (controller == null) return;

        String result = controller.register(
                txtTag.getText().trim(),
                txtName.getText().trim(),
                new String(txtPassword.getPassword())
        );

        if ("SUCCESS".equals(result)) {
            JOptionPane.showMessageDialog(this,
                    "Compte créé avec succès !");
            clearFields();
        } else {
            JOptionPane.showMessageDialog(this,
                    result,
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void clearFields() {
        txtTag.setText("");
        txtName.setText("");
        txtPassword.setText("");
    }
}
