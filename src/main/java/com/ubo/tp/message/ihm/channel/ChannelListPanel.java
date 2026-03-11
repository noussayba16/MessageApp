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
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2));

        JButton createButton = new JButton("Créer");
        JButton deleteButton = new JButton("Supprimer");

        createButton.addActionListener(e -> createChannel());
        deleteButton.addActionListener(e -> deleteChannel());

        buttonPanel.add(createButton);
        buttonPanel.add(deleteButton);

        add(buttonPanel, BorderLayout.SOUTH);

        dataManager.addObserver(this);
    }

    // ===== CHARGER CANAUX =====
    private void chargerCanaux(String keyword) {

        model.clear();

        String filtre = keyword == null ? "" : keyword.trim().toLowerCase();

        for (Channel channel : dataManager.getChannels()) {

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

        String name = JOptionPane.showInputDialog(
                this,
                "Nom du canal"
        );

        if (name == null || name.trim().isEmpty()) {
            return;
        }

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

        // ===== CANAL PUBLIC =====
        if (type == 0) {

            channel = new Channel(
                    session.getConnectedUser(),
                    name.trim()
            );

        } else {

            // ===== CANAL PRIVE =====

            java.util.List<User> allUsers = new ArrayList<>();

            for (User u : dataManager.getUsers()) {

                // Ignorer l'utilisateur système <Inconnu>
                if (u.getUserTag().equalsIgnoreCase("<Inconnu>")) {
                    continue;
                }

                // Ne pas ajouter l'utilisateur connecté
                if (u.getUuid().equals(session.getConnectedUser().getUuid())) {
                    continue;
                }

                allUsers.add(u);
            }

            // Sélection des utilisateurs pour un canal privé
            JList<User> userJList = new JList<>(allUsers.toArray(new User[0]));
            userJList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

            int result = JOptionPane.showConfirmDialog(
                    this,
                    new JScrollPane(userJList),
                    "Choisir les utilisateurs du canal privé",
                    JOptionPane.OK_CANCEL_OPTION
            );

            if (result != JOptionPane.OK_OPTION) {
                return;
            }

            ArrayList<User> selectedUsers = new ArrayList<>(userJList.getSelectedValuesList());

            // Ajouter le créateur (l'utilisateur connecté) dans la liste des membres du canal
            selectedUsers.add(session.getConnectedUser());

            channel = new Channel(
                    session.getConnectedUser(),
                    name.trim(),
                    selectedUsers
            );
        }

        dataManager.sendChannel(channel);
    }

    // ===== SUPPRESSION CANAL =====
    private void deleteChannel() {

        Channel selected = channelList.getSelectedValue();

        if (selected == null) {

            JOptionPane.showMessageDialog(
                    this,
                    "Sélectionnez un canal"
            );
            return;
        }

        // Vérification : seul le propriétaire peut supprimer
        if (!selected.getCreator().getUuid().equals(session.getConnectedUser().getUuid())) {

            JOptionPane.showMessageDialog(
                    this,
                    "Vous ne pouvez supprimer que les canaux dont vous êtes le propriétaire."
            );
            return;
        }

        dataManager.removeChannel(selected);
    }

    // ===== GETTERS =====
    public Channel getSelectedChannel() {
        return channelList.getSelectedValue();
    }

    public JList<Channel> getChannelList() {
        return channelList;
    }

    // ===== OBSERVER CANAL =====
    @Override
    public void notifyChannelAdded(Channel addedChannel) {

        SwingUtilities.invokeLater(() -> {
            chargerCanaux(searchField.getText());
        });
    }

    @Override
    public void notifyChannelDeleted(Channel deletedChannel) {

        SwingUtilities.invokeLater(() -> {
            chargerCanaux(searchField.getText());
        });
    }

    @Override
    public void notifyChannelModified(Channel modifiedChannel) {

        SwingUtilities.invokeLater(() -> {
            chargerCanaux(searchField.getText());
        });
    }

    // ===== AUTRES EVENTS =====
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
    public void notifyUserModified(User modifiedUser) {}
}