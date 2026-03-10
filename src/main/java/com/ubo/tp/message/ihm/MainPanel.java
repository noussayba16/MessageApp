package main.java.com.ubo.tp.message.ihm;

import com.ubo.tp.message.ihm.channel.ChannelListPanel;
import main.java.com.ubo.tp.message.controller.UserController;
import main.java.com.ubo.tp.message.core.DataManager;
import main.java.com.ubo.tp.message.core.session.Session;
import main.java.com.ubo.tp.message.datamodel.Channel;
import main.java.com.ubo.tp.message.datamodel.Message;
import main.java.com.ubo.tp.message.datamodel.User;
import main.java.com.ubo.tp.message.ihm.message.MessageInputPanel;
import main.java.com.ubo.tp.message.ihm.message.MessageListPanel;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;

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

        // ===== USERS PANEL + SEARCH USER =====
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

                if (selectedChannel == null) {
                    return;
                }

                if (!userHasAccess(selectedChannel)) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Vous n'avez pas accès à ce canal privé."
                    );

                    selectedChannel = null;
                    channelListPanel.getChannelList().clearSelection();
                    return;
                }

                chargerConversationChannel(selectedChannel);
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
    }

    private void rechercherUtilisateur() {
        chargerUtilisateurs(userSearchField.getText());
    }

    private void modifierMonNom() {

        User connectedUser = session.getConnectedUser();

        if (connectedUser == null) {
            return;
        }

        String nouveauNom = JOptionPane.showInputDialog(
                this,
                "Nouveau nom d'utilisateur :",
                connectedUser.getName()
        );

        if (nouveauNom == null) {
            return;
        }

        nouveauNom = nouveauNom.trim();

        if (nouveauNom.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Le nom ne peut pas être vide."
            );
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

        JOptionPane.showMessageDialog(
                this,
                "Nom modifié avec succès."
        );

        chargerUtilisateurs(userSearchField.getText());
    }

    private void supprimerMonCompte() {

        User connectedUser = session.getConnectedUser();

        if (connectedUser == null) {
            return;
        }

        int choix = JOptionPane.showConfirmDialog(
                this,
                "Voulez-vous vraiment supprimer votre compte ?",
                "Confirmation",
                JOptionPane.YES_NO_OPTION
        );

        if (choix != JOptionPane.YES_OPTION) {
            return;
        }

        stopRefreshTimer();
        dataManager.deleteUser(connectedUser);

        JOptionPane.showMessageDialog(
                this,
                "Votre compte a été supprimé."
        );

        session.disconnect();
    }

    // ================= USER CONVERSATION =================
    private void chargerConversationUser(User user) {

        User connectedUser = session.getConnectedUser();

        if (connectedUser == null || user == null) {
            return;
        }

        Set<Message> nouvelleConversation = new HashSet<>();

        Set<Message> sent = dataManager.getMessagesFrom(
                connectedUser,
                user
        );

        Set<Message> received = dataManager.getMessagesFrom(
                user,
                connectedUser
        );

        if (sent != null) {
            nouvelleConversation.addAll(sent);
        }

        if (received != null) {
            nouvelleConversation.addAll(received);
        }

        currentConversation = nouvelleConversation;

        messageListPanel.refresh(
                currentConversation,
                connectedUser.getUserTag(),
                this::supprimerMessage
        );

        verifierNotifications(currentConversation);
    }

    // ================= CHANNEL CONVERSATION =================
    private void chargerConversationChannel(Channel channel) {

        User connectedUser = session.getConnectedUser();

        if (connectedUser == null || channel == null) {
            return;
        }

        if (!userHasAccess(channel)) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vous n'avez pas accès à ce canal privé."
            );

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
    }
    private boolean userHasAccess(Channel channel){

        User currentUser = session.getConnectedUser();

        if(currentUser == null || channel == null){
            return false;
        }

        // canal public
        if(!channel.isPrivate()){
            return true;
        }

        // créateur
        if(channel.getCreator().getUuid().equals(currentUser.getUuid())){
            return true;
        }

        // membre du canal
        for(User u : channel.getUsers()){
            if(u != null && u.getUuid().equals(currentUser.getUuid())){
                return true;
            }
        }

        // vérifier si l'utilisateur a déjà un message dans ce canal
        for(Message m : dataManager.getMessages()){

            if(m.getRecipient().equals(channel.getUuid())){

                if(m.getSender().getUuid().equals(currentUser.getUuid())){
                    return true;
                }
            }
        }

        return false;
    }
    // ================= SEARCH MESSAGES =================
    private void rechercherMessages() {

        User connectedUser = session.getConnectedUser();

        if (connectedUser == null) {
            return;
        }

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
            if (m.getText() != null &&
                    m.getText().toLowerCase().contains(keyword.toLowerCase())) {
                filtered.add(m);
            }
        }

        messageListPanel.refresh(
                filtered,
                connectedUser.getUserTag(),
                this::supprimerMessage
        );
    }

    // ================= SEND MESSAGE =================
    private void envoyerMessage() {

        User connectedUser = session.getConnectedUser();

        if (connectedUser == null) {
            return;
        }

        String content = messageInputPanel.getMessageText();
        String imagePath = messageInputPanel.getImagePath();

        if (content == null) {
            content = "";
        }

        if (content.length() > 200) {
            JOptionPane.showMessageDialog(
                    this,
                    "Un message ne peut pas dépasser 200 caractères."
            );
            return;
        }

        if (content.trim().isEmpty() && imagePath == null) {
            return;
        }

        Message message;

        if (selectedUser != null) {
            message = new Message(
                    connectedUser,
                    selectedUser.getUuid(),
                    content.trim(),
                    imagePath
            );
            dataManager.sendMessage(message);

        } else if (selectedChannel != null) {
            message = new Message(
                    connectedUser,
                    selectedChannel.getUuid(),
                    content.trim(),
                    imagePath
            );
            dataManager.sendMessage(message);

        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "Sélectionnez un utilisateur ou un canal !"
            );
            return;
        }

        currentConversation.add(message);

        messageListPanel.refresh(
                currentConversation,
                connectedUser.getUserTag(),
                this::supprimerMessage
        );
    }

    // ================= DELETE MESSAGE =================
    private void supprimerMessage(Message message) {

        User connectedUser = session.getConnectedUser();

        if (connectedUser == null || message == null) {
            return;
        }

        if (!message.getSender().getUuid().equals(connectedUser.getUuid())) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vous ne pouvez supprimer que vos propres messages."
            );
            return;
        }

        int choix = JOptionPane.showConfirmDialog(
                this,
                "Voulez-vous vraiment supprimer ce message ?",
                "Confirmation",
                JOptionPane.YES_NO_OPTION
        );

        if (choix != JOptionPane.YES_OPTION) {
            return;
        }

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

        if (connectedUser == null) {
            return;
        }

        Set<Message> copie = new HashSet<>(messages);

        for (Message m : copie) {

            if (m.getSender().getUuid().equals(connectedUser.getUuid())) {
                continue;
            }

            if (notifiedMessages.contains(m.getUuid().toString())) {
                continue;
            }

            notifiedMessages.add(m.getUuid().toString());

            if (selectedUser != null
                    && m.getSender().getUuid().equals(selectedUser.getUuid())
                    && m.getRecipient().equals(connectedUser.getUuid())) {

                JOptionPane.showMessageDialog(
                        this,
                        "Nouveau message de " + m.getSender().getUserTag()
                );
            }

            if (m.getText() != null
                    && m.getText().contains("@" + connectedUser.getUserTag())) {

                JOptionPane.showMessageDialog(
                        this,
                        "Vous avez été mentionné dans un message."
                );
            }
        }
    }
}