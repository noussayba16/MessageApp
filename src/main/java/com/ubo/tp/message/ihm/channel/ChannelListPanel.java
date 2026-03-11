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
        JPanel buttonPanel = new JPanel(new GridLayout(1, 3));

        JButton createButton = new JButton("Créer");
        JButton deleteButton = new JButton("Supprimer");
        JButton leaveButton  = new JButton("Quitter");

        createButton.addActionListener(e -> createChannel());
        deleteButton.addActionListener(e -> deleteChannel());
        leaveButton.addActionListener(e -> leaveChannel());

        buttonPanel.add(createButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(leaveButton);

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
            // ===== CANAL PUBLIC =====
            channel = new Channel(session.getConnectedUser(), name.trim());

        } else {
            // ===== CANAL PRIVE =====
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

    // ===== SUPPRESSION CANAL (propriétaire uniquement) =====
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

    // ===== QUITTER CANAL (membre non propriétaire) =====
    private void leaveChannel() {

        Channel selected = channelList.getSelectedValue();

        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Sélectionnez un canal.");
            return;
        }

        User connectedUser = session.getConnectedUser();

        // Le propriétaire ne peut pas quitter, seulement supprimer
        if (selected.getCreator().getUuid().equals(connectedUser.getUuid())) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vous êtes le propriétaire de ce canal.\nUtilisez \"Supprimer\" pour le supprimer."
            );
            return;
        }

        // Vérifier que l'utilisateur est bien membre
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

        // CORRIGÉ : retire l'utilisateur de la liste des membres
        selected.removeUser(connectedUser);

        // Sauvegarde le canal mis à jour sur le disque
        dataManager.modifyChannel(selected);

        // Recharger la liste (le canal disparaît puisqu'on n'y a plus accès)
        chargerCanaux(searchField.getText());

        JOptionPane.showMessageDialog(
                this,
                "Vous avez quitté le canal \"" + selected.getName() + "\"."
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