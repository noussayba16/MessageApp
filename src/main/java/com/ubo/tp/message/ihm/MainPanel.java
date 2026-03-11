package main.java.com.ubo.tp.message.ihm;

import com.ubo.tp.message.ihm.channel.ChannelListPanel;
import main.java.com.ubo.tp.message.controller.UserController;
import main.java.com.ubo.tp.message.core.DataManager;
import main.java.com.ubo.tp.message.core.session.Session;
import main.java.com.ubo.tp.message.datamodel.Channel;
import main.java.com.ubo.tp.message.datamodel.Message;
import main.java.com.ubo.tp.message.datamodel.User;
import main.java.com.ubo.tp.message.ihm.channel.*;
import main.java.com.ubo.tp.message.ihm.message.MessageInputPanel;
import main.java.com.ubo.tp.message.ihm.message.MessageListPanel;

import javax.swing.*;
//import java.awt.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class MainPanel extends JPanel {

    private final UserController userController;
    private final DataManager dataManager;
    private final Session session;

    private JList<User> userList;
    private DefaultListModel<User> userModel;

    private ChannelListPanel channelListPanel;

    private MessageListPanel messageListPanel;
    private MessageInputPanel messageInputPanel;

    private JTextField searchField;
    private JButton searchButton;

    private JTextField userSearchField;
    private JButton userSearchButton;

    private JLabel lblWelcome;

    private User selectedUser = null;
    private Channel selectedChannel = null;

    private Set<Message> currentConversation = new HashSet<>();
    private Set<String> notifiedMessages = new HashSet<>();

    private Timer refreshTimer;

    public MainPanel(User user,
                     UserController userController,
                     DataManager dataManager,
                     Session session) {

        this.userController = userController;
        this.dataManager = dataManager;
        this.session = session;

        setLayout(new BorderLayout());

        // ===== HEADER =====
        JPanel topPanel = new JPanel(new BorderLayout());

        lblWelcome = new JLabel(
                "Bienvenue " + user.getName() + " !",
                SwingConstants.CENTER
        );
        lblWelcome.setFont(new Font("Arial", Font.BOLD, 20));

        JPanel topButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton btnModifierNom = new JButton("Modifier mon nom");
        btnModifierNom.addActionListener(e -> modifierMonNom());

        JButton btnSupprimerCompte = new JButton("Supprimer mon compte");
        btnSupprimerCompte.addActionListener(e -> supprimerMonCompte());

        JButton btnLogout = new JButton("Déconnexion");
        btnLogout.addActionListener(e -> {
            stopRefreshTimer();
            session.disconnect();
        });

        topButtonsPanel.add(btnModifierNom);
        topButtonsPanel.add(btnSupprimerCompte);
        topButtonsPanel.add(btnLogout);

        topPanel.add(lblWelcome, BorderLayout.CENTER);
        topPanel.add(topButtonsPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // ===== USERS PANEL =====
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setPreferredSize(new Dimension(240, 0));

        JPanel userSearchPanel = new JPanel(new BorderLayout(5, 5));
        userSearchField = new JTextField();
        userSearchButton = new JButton("Rechercher");

        userSearchButton.addActionListener(e -> rechercherUtilisateur());
        userSearchField.addActionListener(e -> rechercherUtilisateur());

        userSearchPanel.add(userSearchField, BorderLayout.CENTER);
        userSearchPanel.add(userSearchButton, BorderLayout.EAST);

        userModel = new DefaultListModel<>();
        userList = new JList<>(userModel);
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // ===== RENDERER AVEC INDICATEUR EN LIGNE/HORS LIGNE =====
        userList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {

                JPanel panel = new JPanel(new BorderLayout(8, 0));
                panel.setOpaque(true);

                User u = (User) value;
                boolean isOnline = isUserOnline(u);

                // Point indicateur vert/rouge
                JLabel indicator = new JLabel("●") {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(getForeground());
                        g2.fillOval(0, (getHeight() - 12) / 2, 12, 12);
                        g2.dispose();
                    }
                    @Override
                    public Dimension getPreferredSize() {
                        return new Dimension(14, 14);
                    }
                };
                indicator.setForeground(isOnline ? new Color(34, 197, 94) : new Color(200, 200, 200));

                // Nom + tag + statut
                String statut = isOnline ? "en ligne" : "hors ligne";
                JLabel nameLabel = new JLabel(u.getName() + " - " + u.getUserTag()
                        + " (" + statut + ")");
                nameLabel.setFont(new Font("Arial", Font.PLAIN, 12));

                panel.add(indicator, BorderLayout.WEST);
                panel.add(nameLabel, BorderLayout.CENTER);
                panel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

                if (isSelected) {
                    panel.setBackground(new Color(51, 153, 255));
                    nameLabel.setForeground(Color.WHITE);
                } else {
                    panel.setBackground(index % 2 == 0 ? Color.WHITE : new Color(245, 245, 245));
                    nameLabel.setForeground(Color.BLACK);
                }

                return panel;
            }
        });

        chargerUtilisateurs("");

        JScrollPane userScroll = new JScrollPane(userList);

        leftPanel.add(userSearchPanel, BorderLayout.NORTH);
        leftPanel.add(userScroll, BorderLayout.CENTER);

        add(leftPanel, BorderLayout.WEST);

        // ===== CHANNELS =====
        channelListPanel = new ChannelListPanel(dataManager, session);
        add(channelListPanel, BorderLayout.EAST);

        // ===== MESSAGE AREA =====
        JPanel centerPanel = new JPanel(new BorderLayout());

        JPanel searchPanel = new JPanel(new BorderLayout(5, 5));
        searchField = new JTextField();
        searchButton = new JButton("Rechercher");

        searchButton.addActionListener(e -> rechercherMessages());
        searchField.addActionListener(e -> rechercherMessages());

        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);

        centerPanel.add(searchPanel, BorderLayout.NORTH);

        messageListPanel = new MessageListPanel();
        centerPanel.add(new JScrollPane(messageListPanel), BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // ===== USER SELECTION =====
        userList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectedUser = userList.getSelectedValue();
                selectedChannel = null;

                if (selectedUser != null) {
                    chargerConversationUser(selectedUser);
                }
            }
        });

        // ===== CHANNEL SELECTION =====
        channelListPanel.getChannelList().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectedChannel = channelListPanel.getSelectedChannel();
                selectedUser = null;

                if (selectedChannel != null) {
                    chargerConversationChannel(selectedChannel);
                }
            }
        });

        // ===== INPUT =====
        messageInputPanel = new MessageInputPanel();
        messageInputPanel.setSendAction(this::envoyerMessage);
        add(messageInputPanel, BorderLayout.SOUTH);

        // ===== AUTO REFRESH =====
        refreshTimer = new Timer(2000, e -> rafraichirMessages());
        refreshTimer.start();
    }

    // ================= INDICATEUR EN LIGNE =================
    /**
     * Un utilisateur est "en ligne" si son fichier .usr existe dans la base
     * ET qu'il est la session connectée courante, OU si la session active
     * correspond à cet utilisateur.
     * On détecte la présence via le UserController qui lit les fichiers du répertoire.
     */
    private boolean isUserOnline(User user) {
        if (user == null) return false;

        // L'utilisateur connecté actuellement sur CETTE instance
        User connectedUser = session.getConnectedUser();
        if (connectedUser != null && connectedUser.getUuid().equals(user.getUuid())) {
            return true;
        }

        // Détection multi-instance : on vérifie si le tag contient "(en ligne)"
        // tel qu'affiché — sinon on se base sur le UserController
        for (User u : userController.getAllUsers()) {
            if (u.getUuid().equals(user.getUuid())) {
                // Si le nom contient "(en ligne)" c'est qu'une autre instance l'a marqué
                if (u.getName() != null && u.getName().contains("(en ligne)")) {
                    return true;
                }
            }
        }

        return false;
    }

    // ================= HELPER ACCES CANAL =================
    private boolean hasChannelAccess(Channel channel, User user) {
        if (!channel.isPrivate()) {
            return true;
        }
        UUID userId = user.getUuid();
        if (channel.getCreator().getUuid().equals(userId)) {
            return true;
        }
        return channel.getUsers().stream()
                .anyMatch(u -> u.getUuid().equals(userId));
    }

    private void stopRefreshTimer() {
        if (refreshTimer != null && refreshTimer.isRunning()) {
            refreshTimer.stop();
        }
    }

    // ================= AUTO REFRESH =================
    private void rafraichirMessages() {
        if (session.getConnectedUser() == null) {
            stopRefreshTimer();
            return;
        }

        // Rafraîchit aussi la liste utilisateurs pour mettre à jour les indicateurs
        chargerUtilisateurs(userSearchField.getText());

        if (selectedUser != null) {
            chargerConversationUser(selectedUser);
        } else if (selectedChannel != null) {
            chargerConversationChannel(selectedChannel);
        }
    }

    private void chargerUtilisateurs(String keyword) {
        if (session.getConnectedUser() == null) {
            return;
        }

        // Sauvegarde la sélection courante
        User previousSelection = userList.getSelectedValue();

        userModel.clear();

        String filtre = keyword == null ? "" : keyword.trim().toLowerCase();

        for (User u : userController.getAllUsers()) {
            if (u.getUserTag().equalsIgnoreCase("<Inconnu>")) {
                continue;
            }
            if (filtre.isEmpty()
                    || u.getUserTag().toLowerCase().contains(filtre)
                    || u.getName().toLowerCase().contains(filtre)) {
                userModel.addElement(u);
            }
        }

        // Restaure la sélection après rechargement
        if (previousSelection != null) {
            for (int i = 0; i < userModel.getSize(); i++) {
                if (userModel.getElementAt(i).getUuid().equals(previousSelection.getUuid())) {
                    userList.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private void rechercherUtilisateur() {
        chargerUtilisateurs(userSearchField.getText());
    }

    private void modifierMonNom() {
        User connectedUser = session.getConnectedUser();

        if (connectedUser == null) return;

        String nouveauNom = JOptionPane.showInputDialog(
                this,
                "Nouveau nom d'utilisateur :",
                connectedUser.getName()
        );

        if (nouveauNom == null) return;

        nouveauNom = nouveauNom.trim();

        if (nouveauNom.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Le nom ne peut pas être vide.");
            return;
        }

        User userModifie = new User(
                connectedUser.getUuid(),
                connectedUser.getUserTag(),
                connectedUser.getUserPassword(),
                nouveauNom
        );

        dataManager.modifyUser(connectedUser.getUuid(), userModifie);
        lblWelcome.setText("Bienvenue " + nouveauNom + " !");
        JOptionPane.showMessageDialog(this, "Nom modifié avec succès.");
        chargerUtilisateurs(userSearchField.getText());
    }

    private void supprimerMonCompte() {
        User connectedUser = session.getConnectedUser();

        if (connectedUser == null) return;

        int choix = JOptionPane.showConfirmDialog(
                this,
                "Voulez-vous vraiment supprimer votre compte ?",
                "Confirmation",
                JOptionPane.YES_NO_OPTION
        );

        if (choix != JOptionPane.YES_OPTION) return;

        stopRefreshTimer();
        dataManager.deleteUser(connectedUser);
        JOptionPane.showMessageDialog(this, "Votre compte a été supprimé.");
        session.disconnect();
    }

    // ================= USER CONVERSATION =================
    private void chargerConversationUser(User user) {
        User connectedUser = session.getConnectedUser();

        if (connectedUser == null || user == null) return;

        Set<Message> nouvelleConversation = new HashSet<>();

        Set<Message> sent = dataManager.getMessagesFrom(connectedUser, user);
        Set<Message> received = dataManager.getMessagesFrom(user, connectedUser);

        if (sent != null) nouvelleConversation.addAll(sent);
        if (received != null) nouvelleConversation.addAll(received);

        currentConversation = nouvelleConversation;

        messageListPanel.refresh(
                currentConversation,
                connectedUser.getUserTag(),
                this::supprimerMessage
        );

        verifierNotifications(currentConversation);
        // Après currentConversation = nouvelleConversation;
// Pour une conv privée, seuls les 2 participants sont mentionnables
        List<User> mentionnables = new ArrayList<>();
        mentionnables.add(user);
        messageInputPanel.setMentionnableUsers(mentionnables);
    }

    // ================= CHANNEL CONVERSATION =================
    private void chargerConversationChannel(Channel channel) {
        User connectedUser = session.getConnectedUser();

        if (connectedUser == null || channel == null) return;

        if (!hasChannelAccess(channel, connectedUser)) {
            JOptionPane.showMessageDialog(this, "Vous n'avez pas accès à ce canal privé.");
            selectedChannel = null;
            channelListPanel.getChannelList().clearSelection();
            return;
        }

        Set<Message> nouvelleConversation = new HashSet<>();

        for (Message m : dataManager.getMessages()) {
            if (m.getRecipient().equals(channel.getUuid())) {
                nouvelleConversation.add(m);
            }
        }

        currentConversation = nouvelleConversation;

        messageListPanel.refresh(
                currentConversation,
                connectedUser.getUserTag(),
                this::supprimerMessage
        );

        verifierNotifications(currentConversation);

        // ===== MENTIONNABLES =====
        List<User> mentionnables = new ArrayList<>();

        if (channel.isPrivate()) {
            // Canal privé : membres du canal, retrouvés par UUID dans dataManager
            for (User membre : channel.getUsers()) {
                for (User u : dataManager.getUsers()) {
                    if (u.getUuid().equals(membre.getUuid())
                            && !u.getUuid().equals(connectedUser.getUuid())
                            && !u.getUserTag().equalsIgnoreCase("<Inconnu>")) {
                        mentionnables.add(u);
                        break;
                    }
                }
            }
        } else {
            // Canal public : tous les utilisateurs sauf moi et inconnu
            for (User u : dataManager.getUsers()) {
                if (!u.getUuid().equals(connectedUser.getUuid())
                        && !u.getUserTag().equalsIgnoreCase("<Inconnu>")) {
                    mentionnables.add(u);
                }
            }
        }

        messageInputPanel.setMentionnableUsers(mentionnables);
    }

    // ================= SEARCH MESSAGES =================
    private void rechercherMessages() {
        User connectedUser = session.getConnectedUser();

        if (connectedUser == null) return;

        String keyword = searchField.getText();

        if (keyword == null || keyword.trim().isEmpty()) {
            messageListPanel.refresh(
                    currentConversation,
                    connectedUser.getUserTag(),
                    this::supprimerMessage
            );
            return;
        }

        Set<Message> filtered = new HashSet<>();

        for (Message m : new HashSet<>(currentConversation)) {
            if (m.getText() != null
                    && m.getText().toLowerCase().contains(keyword.toLowerCase())) {
                filtered.add(m);
            }
        }

        messageListPanel.refresh(
                filtered,
                connectedUser.getUserTag(),
                this::supprimerMessage
        );
    }

    // ================= ENVOYER MESSAGE =================
    private void envoyerMessage() {
        User connectedUser = session.getConnectedUser();

        if (connectedUser == null) return;

        String content = messageInputPanel.getMessageText();
        String imagePath = messageInputPanel.getImagePath();

        if (content == null) content = "";

        if (content.length() > 200) {
            JOptionPane.showMessageDialog(this, "Un message ne peut pas dépasser 200 caractères.");
            return;
        }

        if (content.trim().isEmpty() && imagePath == null) return;

        if (selectedUser == null && selectedChannel == null) {
            JOptionPane.showMessageDialog(this, "Sélectionnez un utilisateur ou un canal !");
            return;
        }

        if (selectedUser != null) {
            Message message = new Message(
                    connectedUser,
                    selectedUser.getUuid(),
                    content.trim(),
                    imagePath
            );
            dataManager.sendMessage(message);
            currentConversation.add(message);

            messageListPanel.refresh(
                    currentConversation,
                    connectedUser.getUserTag(),
                    this::supprimerMessage
            );

        } else {
            if (!hasChannelAccess(selectedChannel, connectedUser)) {
                JOptionPane.showMessageDialog(
                        this,
                        "Vous n'avez pas l'autorisation d'envoyer des messages dans ce canal privé."
                );
                return;
            }

            Message message = new Message(
                    connectedUser,
                    selectedChannel.getUuid(),
                    content.trim(),
                    imagePath
            );
            dataManager.sendMessage(message);
            chargerConversationChannel(selectedChannel);
        }
    }

    // ================= DELETE MESSAGE =================
    private void supprimerMessage(Message message) {
        User connectedUser = session.getConnectedUser();

        if (connectedUser == null || message == null) return;

        if (!message.getSender().getUuid().equals(connectedUser.getUuid())) {
            JOptionPane.showMessageDialog(this, "Vous ne pouvez supprimer que vos propres messages.");
            return;
        }

        int choix = JOptionPane.showConfirmDialog(
                this,
                "Voulez-vous vraiment supprimer ce message ?",
                "Confirmation",
                JOptionPane.YES_NO_OPTION
        );

        if (choix != JOptionPane.YES_OPTION) return;

        dataManager.deleteMessage(message);
        currentConversation.remove(message);

        messageListPanel.refresh(
                currentConversation,
                connectedUser.getUserTag(),
                this::supprimerMessage
        );
    }

    // ================= NOTIFICATIONS =================
    private void verifierNotifications(Set<Message> messages) {
        User connectedUser = session.getConnectedUser();

        if (connectedUser == null) return;

        for (Message m : new HashSet<>(messages)) {
            if (m.getSender().getUuid().equals(connectedUser.getUuid())) continue;
            if (notifiedMessages.contains(m.getUuid().toString())) continue;

            notifiedMessages.add(m.getUuid().toString());

            // ===== NOTIFICATION MESSAGE PRIVÉ (conversation utilisateur) =====
            if (selectedUser != null
                    && m.getSender().getUuid().equals(selectedUser.getUuid())
                    && m.getRecipient().equals(connectedUser.getUuid())) {

                JOptionPane.showMessageDialog(
                        this,
                        "Nouveau message de " + m.getSender().getUserTag()
                );
            }

            // ===== NOTIFICATION MESSAGE CANAL =====
            if (selectedChannel != null
                    && m.getRecipient().equals(selectedChannel.getUuid())) {

                JOptionPane.showMessageDialog(
                        this,
                        "Nouveau message dans #" + selectedChannel.getName()
                                + " de " + m.getSender().getUserTag()
                );
            }

            // ===== NOTIFICATION MENTION @ =====
            if (m.getText() != null
                    && m.getText().contains("@" + connectedUser.getUserTag())) {

                JOptionPane.showMessageDialog(
                        this,
                        "Vous avez été mentionné par " + m.getSender().getUserTag()
                                + " dans un message."
                );
            }
        }
    }
}