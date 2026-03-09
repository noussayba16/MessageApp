package main.java.com.ubo.tp.message.ihm;

import main.java.com.ubo.tp.message.controller.LoginController;
import main.java.com.ubo.tp.message.controller.UserController;
import main.java.com.ubo.tp.message.core.DataManager;
import main.java.com.ubo.tp.message.core.session.ISessionObserver;
import main.java.com.ubo.tp.message.core.session.Session;
import main.java.com.ubo.tp.message.datamodel.User;
import main.java.com.ubo.tp.message.ihm.login.LoginPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;

/**
 * Classe de la vue principale de l'application.
 */
public class MessageAppMainView extends JFrame implements ISessionObserver {

    private MessageApp application;
    private LoginPanel loginPanel;
    private LoginController loginController;
    private JPanel mainPanel;

    /**
     * Constructeur.
     */
    public MessageAppMainView(MessageApp app) {
        this.application = app;
        initComponents();
    }

    /**
     * Initialisation des composants.
     */
    private void initComponents() {

        setTitle("Message App - M2 TIIL");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        createMenuBar();

        // ===== LOGIN VIEW =====
        loginPanel = new LoginPanel();

        loginController = new LoginController(
                application.getDataManager(),
                application.getSession()
        );

        loginPanel.setController(loginController);

        // Afficher login au démarrage
        setContentPane(loginPanel);
    }

    /**
     * Affiche le login.
     */
    public void showLoginPanel() {
        setContentPane(loginPanel);
        revalidate();
        repaint();
    }

    /**
     * Affiche l'interface principale après connexion.
     */
    public void showMainInterface(User user) {

        UserController userController = new UserController(
                application.getDataManager(),
                application.getSession()
        );

        mainPanel = new MainPanel(
                user,
                userController,
                application.getDataManager(),
                application.getSession()
        );

        setContentPane(mainPanel);
        revalidate();
        repaint();
    }
    /**
     * Création de la barre de menu.
     */
    private void createMenuBar() {

        JMenuBar menuBar = new JMenuBar();

        JMenu menuFichier = new JMenu("Fichier");

        JMenuItem itemChoisirRepertoire =
                new JMenuItem("Choisir répertoire d'échange");

        itemChoisirRepertoire.addActionListener(e -> chooseExchangeDirectory());

        menuFichier.add(itemChoisirRepertoire);
        menuFichier.addSeparator();

        JMenuItem itemQuitter = new JMenuItem("Quitter");
        itemQuitter.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK)
        );

        itemQuitter.addActionListener(e -> application.exit());

        menuFichier.add(itemQuitter);

        JMenu menuAide = new JMenu("?");

        JMenuItem itemAPropos = new JMenuItem("À propos");
        itemAPropos.addActionListener(e -> showAboutDialog());

        menuAide.add(itemAPropos);

        menuBar.add(menuFichier);
        menuBar.add(menuAide);

        setJMenuBar(menuBar);
    }

    /**
     * Choix du répertoire d'échange.
     */
    private void chooseExchangeDirectory() {

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Sélectionner le répertoire d'échange");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {

            File selectedDirectory = fileChooser.getSelectedFile();

            if (application.isValidExchangeDirectory(selectedDirectory)) {

                application.initDirectory(
                        selectedDirectory.getAbsolutePath()
                );

                JOptionPane.showMessageDialog(
                        this,
                        "Répertoire configuré : " +
                                selectedDirectory.getAbsolutePath(),
                        "Succès",
                        JOptionPane.INFORMATION_MESSAGE
                );

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        "Répertoire invalide.",
                        "Erreur",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    /**
     * Boîte À propos.
     */
    private void showAboutDialog() {

        JOptionPane.showMessageDialog(
                this,
                "Message App\nUBO M2 TIIL\nVersion 1.0",
                "À propos",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    // ===== OBSERVER SESSION =====

    @Override
    public void notifyLogin(User user) {
        showMainInterface(user);
    }

    @Override
    public void notifyLogout() {
        showLoginPanel();
    }
}