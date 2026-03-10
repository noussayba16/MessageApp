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

public class ChannelListPanel extends JPanel implements IDatabaseObserver {

    private JList<Channel> channelList;
    private DefaultListModel<Channel> model;

    private final DataManager dataManager;
    private final Session session;

    private JTextField searchField;

    public ChannelListPanel(DataManager dataManager, Session session) {

        this.dataManager = dataManager;
        this.session = session;

        setLayout(new BorderLayout());

        // ===== BARRE RECHERCHE =====
        JPanel searchPanel = new JPanel(new BorderLayout(5,5));

        searchField = new JTextField();
        JButton searchButton = new JButton("Rechercher");

        searchButton.addActionListener(e -> rechercherCanaux());
        searchField.addActionListener(e -> rechercherCanaux());

        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);

        add(searchPanel, BorderLayout.NORTH);

        // ===== LISTE CANAUX =====
        model = new DefaultListModel<>();
        channelList = new JList<>(model);

        channelList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        channelList.setCellRenderer((list, channel, index, isSelected, cellHasFocus) -> {

            main.java.com.ubo.tp.message.ihm.channel.ChannelPanel panel = new main.java.com.ubo.tp.message.ihm.channel.ChannelPanel(channel);

            if (isSelected) {
                panel.setBackground(new Color(200,220,255));
            }

            return panel;
        });

        add(new JScrollPane(channelList), BorderLayout.CENTER);

        chargerCanaux("");

        // ===== BOUTONS =====
        JPanel buttonPanel = new JPanel(new GridLayout(1,3,5,5));

        JButton createButton = new JButton("Créer");
        JButton deleteButton = new JButton("Supprimer");
        JButton removeUserButton = new JButton("Retirer utilisateur");

        createButton.addActionListener(e -> createChannel());
        deleteButton.addActionListener(e -> deleteChannel());
        removeUserButton.addActionListener(e -> removeUserFromChannel());

        buttonPanel.add(createButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(removeUserButton);

        add(buttonPanel, BorderLayout.SOUTH);

        dataManager.addObserver(this);
    }

    // ===== CHARGER CANAUX =====
    private void chargerCanaux(String keyword){

        model.clear();

        String filtre = keyword == null ? "" : keyword.trim().toLowerCase();

        for(Channel channel : dataManager.getChannels()){

            if(filtre.isEmpty() ||
                    channel.getName().toLowerCase().contains(filtre)){

                model.addElement(channel);
            }
        }
    }

    private void rechercherCanaux(){
        chargerCanaux(searchField.getText());
    }

    // ===== CREATION CANAL =====
    private void createChannel() {

        String name = JOptionPane.showInputDialog(this,"Nom du canal");

        if (name == null || name.trim().isEmpty()) return;

        Object[] options = {"Public","Privé"};

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

        if(type == 0){

            channel = new Channel(
                    session.getConnectedUser(),
                    name.trim()
            );

        }else{

            List<User> allUsers = new ArrayList<>();

            for(User u : dataManager.getUsers()){

                if(u.getUserTag().equalsIgnoreCase("<Inconnu>")) continue;
                if(u.getUuid().equals(session.getConnectedUser().getUuid())) continue;

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

            if(result != JOptionPane.OK_OPTION) return;

            ArrayList<User> selectedUsers = new ArrayList<>(userJList.getSelectedValuesList());

            selectedUsers.add(session.getConnectedUser());

            channel = new Channel(
                    session.getConnectedUser(),
                    name.trim(),
                    selectedUsers
            );
        }

        dataManager.sendChannel(channel);
    }

    private void removeUserFromChannel(){

        Channel selected = channelList.getSelectedValue();

        if(selected == null){
            JOptionPane.showMessageDialog(this,"Sélectionnez un canal");
            return;
        }

        if(!selected.isPrivate()){
            JOptionPane.showMessageDialog(this,"Ce canal n'est pas privé");
            return;
        }

        if(!selected.getCreator().getUuid().equals(session.getConnectedUser().getUuid())){
            JOptionPane.showMessageDialog(this,"Seul le propriétaire peut modifier ce canal");
            return;
        }

        // construire la liste des utilisateurs supprimables
        java.util.List<User> members = new ArrayList<>();

        for(User u : dataManager.getUsers()){

            // ignorer utilisateur inconnu
            if(u.getUserTag().equalsIgnoreCase("<Inconnu>")){
                continue;
            }

            // ignorer le créateur
            if(u.getUuid().equals(selected.getCreator().getUuid())){
                continue;
            }

            members.add(u);
        }

        if(members.isEmpty()){
            JOptionPane.showMessageDialog(this,"Aucun utilisateur à supprimer");
            return;
        }

        JList<User> userList = new JList<>(members.toArray(new User[0]));

        int result = JOptionPane.showConfirmDialog(
                this,
                new JScrollPane(userList),
                "Supprimer un utilisateur",
                JOptionPane.OK_CANCEL_OPTION
        );

        if(result == JOptionPane.OK_OPTION){

            User userToRemove = userList.getSelectedValue();

            if(userToRemove != null){

                members.remove(userToRemove);

                Channel updatedChannel = new Channel(
                        selected.getUuid(),
                        selected.getCreator(),
                        selected.getName(),
                        members
                );

                dataManager.modifyChannel(selected.getUuid(), updatedChannel);
            }
        }
    }

    // ===== SUPPRIMER CANAL =====
    private void deleteChannel(){

        Channel selected = channelList.getSelectedValue();

        if(selected == null){
            JOptionPane.showMessageDialog(this,"Sélectionnez un canal");
            return;
        }

        if(!selected.getCreator().getUuid().equals(session.getConnectedUser().getUuid())){
            JOptionPane.showMessageDialog(this,
                    "Vous ne pouvez supprimer que les canaux dont vous êtes le propriétaire.");
            return;
        }

        dataManager.removeChannel(selected);
    }

    // ===== GETTERS =====
    public Channel getSelectedChannel(){
        return channelList.getSelectedValue();
    }

    public JList<Channel> getChannelList(){
        return channelList;
    }

    // ===== OBSERVER =====
    @Override
    public void notifyChannelAdded(Channel addedChannel){
        SwingUtilities.invokeLater(() -> chargerCanaux(searchField.getText()));
    }

    @Override
    public void notifyChannelDeleted(Channel deletedChannel){
        SwingUtilities.invokeLater(() -> chargerCanaux(searchField.getText()));
    }

    @Override
    public void notifyChannelModified(Channel modifiedChannel){
        SwingUtilities.invokeLater(() -> chargerCanaux(searchField.getText()));
    }

    @Override public void notifyMessageAdded(Message addedMessage){}
    @Override public void notifyMessageDeleted(Message deletedMessage){}
    @Override public void notifyMessageModified(Message modifiedMessage){}
    @Override public void notifyUserAdded(User addedUser){}
    @Override public void notifyUserDeleted(User deletedUser){}
    @Override public void notifyUserModified(User modifiedUser){}
}