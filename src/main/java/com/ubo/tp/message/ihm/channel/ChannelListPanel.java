package com.ubo.tp.message.ihm.channel;

import main.java.com.ubo.tp.message.datamodel.Channel;
import main.java.com.ubo.tp.message.core.DataManager;
import main.java.com.ubo.tp.message.core.database.IDatabaseObserver;
import main.java.com.ubo.tp.message.core.session.Session;
import main.java.com.ubo.tp.message.datamodel.Message;
import main.java.com.ubo.tp.message.datamodel.User;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChannelListPanel extends JPanel implements IDatabaseObserver {

    private JList<Channel> channelList;
    private DefaultListModel<Channel> model;

    private final DataManager dataManager;
    private final Session session;

    private JTextField searchField;
    private JButton searchButton;

    public ChannelListPanel(DataManager dataManager, Session session) {

        this.dataManager = dataManager;
        this.session = session;

        setLayout(new BorderLayout());

        // ===== BARRE DE RECHERCHE =====
        JPanel searchPanel = new JPanel(new BorderLayout(5, 5));

        searchField = new JTextField();
        searchButton = new JButton("Rechercher");

        searchButton.addActionListener(e -> rechercherCanaux());
        searchField.addActionListener(e -> rechercherCanaux());

        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);

        add(searchPanel, BorderLayout.NORTH);

        // ===== LISTE DES CANAUX =====
        model = new DefaultListModel<>();
        channelList = new JList<>(model);
        channelList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        channelList.setCellRenderer((list, channel, index, isSelected, cellHasFocus) -> {
            main.java.com.ubo.tp.message.ihm.channel.ChannelPanel panel = new main.java.com.ubo.tp.message.ihm.channel.ChannelPanel(channel);
            if (isSelected) {
                panel.setBackground(new Color(200, 220, 255));
            }
            return panel;
        });

        JScrollPane scrollPane = new JScrollPane(channelList);
        add(scrollPane, BorderLayout.CENTER);

        chargerCanaux("");

        // ===== BOUTONS =====
        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 4, 4));

        JButton createButton  = new JButton("Créer");
        JButton deleteButton  = new JButton("Supprimer");
        JButton leaveButton   = new JButton("Quitter");
        JButton manageButton  = new JButton("Gérer membres");

        createButton.addActionListener(e -> createChannel());
        deleteButton.addActionListener(e -> deleteChannel());
        leaveButton.addActionListener(e -> leaveChannel());
        manageButton.addActionListener(e -> gererMembres());

        buttonPanel.add(createButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(leaveButton);
        buttonPanel.add(manageButton);

        add(buttonPanel, BorderLayout.SOUTH);

        dataManager.addObserver(this);
    }

    // ===== HELPER ACCES CANAL =====
    private boolean canAccess(Channel channel) {
        if (!channel.isPrivate()) return true;

        User connectedUser = session.getConnectedUser();
        if (connectedUser == null) return false;

        UUID userId = connectedUser.getUuid();

        if (channel.getCreator().getUuid().equals(userId)) return true;

        return channel.getUsers().stream()
                .anyMatch(u -> u.getUuid().equals(userId));
    }

    // ===== CHARGER CANAUX =====
    private void chargerCanaux(String keyword) {
        model.clear();

        String filtre = keyword == null ? "" : keyword.trim().toLowerCase();

        for (Channel channel : dataManager.getChannels()) {
            if (!canAccess(channel)) continue;

            if (filtre.isEmpty() || channel.getName().toLowerCase().contains(filtre)) {
                model.addElement(channel);
            }
        }
    }

    // ===== RECHERCHE =====
    private void rechercherCanaux() {
        chargerCanaux(searchField.getText());
    }

    // ===== CREATION CANAL =====
    private void createChannel() {

        String name = JOptionPane.showInputDialog(this, "Nom du canal");

        if (name == null || name.trim().isEmpty()) return;

        Object[] options = {"Public", "Privé"};

        int type = JOptionPane.showOptionDialog(
                this,
                "Type du canal",
                "Créer un canal",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        Channel channel;

        if (type == 0) {
            channel = new Channel(session.getConnectedUser(), name.trim());
        } else {
            List<User> allUsers = new ArrayList<>();

            for (User u : dataManager.getUsers()) {
                if (u.getUserTag().equalsIgnoreCase("<Inconnu>")) continue;
                if (u.getUuid().equals(session.getConnectedUser().getUuid())) continue;
                allUsers.add(u);
            }

            JList<User> userJList = new JList<>(allUsers.toArray(new User[0]));
            userJList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

            int result = JOptionPane.showConfirmDialog(
                    this,
                    new JScrollPane(userJList),
                    "Choisir les utilisateurs du canal privé",
                    JOptionPane.OK_CANCEL_OPTION
            );

            if (result != JOptionPane.OK_OPTION) return;

            ArrayList<User> selectedUsers = new ArrayList<>(userJList.getSelectedValuesList());
            selectedUsers.add(session.getConnectedUser());

            channel = new Channel(session.getConnectedUser(), name.trim(), selectedUsers);
        }

        dataManager.sendChannel(channel);
    }

    // ===== SUPPRESSION CANAL =====
    private void deleteChannel() {

        Channel selected = channelList.getSelectedValue();

        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Sélectionnez un canal.");
            return;
        }

        User connectedUser = session.getConnectedUser();

        if (!selected.getCreator().getUuid().equals(connectedUser.getUuid())) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vous ne pouvez supprimer que les canaux dont vous êtes le propriétaire."
            );
            return;
        }

        int choix = JOptionPane.showConfirmDialog(
                this,
                "Voulez-vous vraiment supprimer le canal \"" + selected.getName() + "\" ?",
                "Confirmation",
                JOptionPane.YES_NO_OPTION
        );

        if (choix != JOptionPane.YES_OPTION) return;

        dataManager.removeChannel(selected);
    }

    // ===== QUITTER CANAL =====
    private void leaveChannel() {

        Channel selected = channelList.getSelectedValue();

        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Sélectionnez un canal.");
            return;
        }

        User connectedUser = session.getConnectedUser();

        if (selected.getCreator().getUuid().equals(connectedUser.getUuid())) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vous êtes le propriétaire de ce canal.\nUtilisez \"Supprimer\" pour le supprimer."
            );
            return;
        }

        boolean isMember = selected.getUsers().stream()
                .anyMatch(u -> u.getUuid().equals(connectedUser.getUuid()));

        if (!isMember) {
            JOptionPane.showMessageDialog(this, "Vous n'êtes pas membre de ce canal.");
            return;
        }

        int choix = JOptionPane.showConfirmDialog(
                this,
                "Voulez-vous vraiment quitter le canal \"" + selected.getName() + "\" ?",
                "Confirmation",
                JOptionPane.YES_NO_OPTION
        );

        if (choix != JOptionPane.YES_OPTION) return;

        selected.removeUser(connectedUser);
        dataManager.modifyChannel(selected);
        chargerCanaux(searchField.getText());

        JOptionPane.showMessageDialog(
                this,
                "Vous avez quitté le canal \"" + selected.getName() + "\"."
        );
    }

    // ===== GERER MEMBRES (propriétaire uniquement) =====
    private void gererMembres() {

        Channel selected = channelList.getSelectedValue();

        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Sélectionnez un canal.");
            return;
        }

        User connectedUser = session.getConnectedUser();

        if (!selected.getCreator().getUuid().equals(connectedUser.getUuid())) {
            JOptionPane.showMessageDialog(
                    this,
                    "Seul le propriétaire peut gérer les membres."
            );
            return;
        }

        if (!selected.isPrivate()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Ce canal est public, il n'a pas de membres à gérer."
            );
            return;
        }

        // ===== CHOIX ACTION =====
        Object[] options = {"Ajouter un membre", "Retirer un membre"};

        int action = JOptionPane.showOptionDialog(
                this,
                "Que voulez-vous faire ?",
                "Gérer les membres de #" + selected.getName(),
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (action == 0) {
            // ===== AJOUTER =====
            ajouterMembre(selected);
        } else if (action == 1) {
            // ===== RETIRER =====
            retirerMembre(selected);
        }
    }

    // ===== RETIRER MEMBRE =====
    private void retirerMembre(Channel channel) {

        User connectedUser = session.getConnectedUser();

        // Membres sauf le propriétaire
        java.util.List<User> membres = new java.util.ArrayList<>();
        for (User u : channel.getUsers()) {
            if (!u.getUuid().equals(connectedUser.getUuid())) {
                membres.add(u);
            }
        }

        if (membres.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Ce canal n'a aucun autre membre."
            );
            return;
        }

        JList<User> membreJList = new JList<>(membres.toArray(new User[0]));
        membreJList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        int result = JOptionPane.showConfirmDialog(
                this,
                new JScrollPane(membreJList),
                "Sélectionnez les membres à retirer",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (result != JOptionPane.OK_OPTION) return;

        java.util.List<User> aRetirer = membreJList.getSelectedValuesList();

        if (aRetirer.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Aucun membre sélectionné.");
            return;
        }

        for (User u : aRetirer) {
            channel.removeUser(u);
        }

        dataManager.modifyChannel(channel);
        chargerCanaux(searchField.getText());

        JOptionPane.showMessageDialog(
                this,
                aRetirer.size() + " membre(s) retiré(s) du canal."
        );
    }
    private void ajouterMembre(Channel channel) {

        User connectedUser = session.getConnectedUser();

        // Utilisateurs qui ne sont PAS encore membres
        java.util.List<User> nonMembres = new java.util.ArrayList<>();

        for (User u : dataManager.getUsers()) {
            if (u.getUserTag().equalsIgnoreCase("<Inconnu>")) continue;
            if (u.getUuid().equals(connectedUser.getUuid())) continue;

            // Vérifier qu'il n'est pas déjà membre
            boolean dejaMembre = channel.getUsers().stream()
                    .anyMatch(m -> m.getUuid().equals(u.getUuid()));

            if (!dejaMembre) {
                nonMembres.add(u);
            }
        }

        if (nonMembres.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Tous les utilisateurs sont déjà membres de ce canal."
            );
            return;
        }

        JList<User> userJList = new JList<>(nonMembres.toArray(new User[0]));
        userJList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        int result = JOptionPane.showConfirmDialog(
                this,
                new JScrollPane(userJList),
                "Choisir les membres à ajouter",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (result != JOptionPane.OK_OPTION) return;

        java.util.List<User> aAjouter = userJList.getSelectedValuesList();

        if (aAjouter.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Aucun membre sélectionné.");
            return;
        }

        // Ajoute chaque membre sélectionné
        for (User u : aAjouter) {
            channel.addUser(u);
        }

        dataManager.modifyChannel(channel);
        chargerCanaux(searchField.getText());

        JOptionPane.showMessageDialog(
                this,
                aAjouter.size() + " membre(s) ajouté(s) au canal."
        );
    }

    // ===== GETTERS =====
    public Channel getSelectedChannel() {
        return channelList.getSelectedValue();
    }

    public JList<Channel> getChannelList() {
        return channelList;
    }

    // ===== OBSERVER =====
    @Override
    public void notifyChannelAdded(Channel addedChannel) {
        SwingUtilities.invokeLater(() -> chargerCanaux(searchField.getText()));
    }

    @Override
    public void notifyChannelDeleted(Channel deletedChannel) {
        SwingUtilities.invokeLater(() -> chargerCanaux(searchField.getText()));
    }

    @Override
    public void notifyChannelModified(Channel modifiedChannel) {
        SwingUtilities.invokeLater(() -> chargerCanaux(searchField.getText()));
    }

    @Override
    public void notifyMessageAdded(Message addedMessage) {}

    @Override
    public void notifyMessageDeleted(Message deletedMessage) {}

    @Override
    public void notifyMessageModified(Message modifiedMessage) {}

    @Override
    public void notifyUserAdded(User addedUser) {}

    @Override
    public void notifyUserDeleted(User deletedUser) {}

    @Override
    public void notifyUserModified(User modifiedUser) {
        SwingUtilities.invokeLater(() -> chargerCanaux(searchField.getText()));
    }
}