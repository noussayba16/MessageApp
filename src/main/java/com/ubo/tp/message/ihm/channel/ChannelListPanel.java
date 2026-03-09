package main.java.com.ubo.tp.message.ihm.channel;

import main.java.com.ubo.tp.message.core.DataManager;
import main.java.com.ubo.tp.message.core.database.IDatabaseObserver;
import main.java.com.ubo.tp.message.core.session.Session;
import main.java.com.ubo.tp.message.datamodel.Channel;
import main.java.com.ubo.tp.message.datamodel.Message;
import main.java.com.ubo.tp.message.datamodel.User;

import javax.swing.*;
import java.awt.*;

public class ChannelListPanel extends JPanel implements IDatabaseObserver {

    private JList<Channel> channelList;
    private DefaultListModel<Channel> model;

    private final DataManager dataManager;
    private final Session session;

    public ChannelListPanel(DataManager dataManager, Session session) {

        this.dataManager = dataManager;
        this.session = session;

        setLayout(new BorderLayout());

        model = new DefaultListModel<>();
        channelList = new JList<>(model);

        channelList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Renderer avec ChannelPanel
        channelList.setCellRenderer((list, channel, index, isSelected, cellHasFocus) -> {

            ChannelPanel panel = new ChannelPanel(channel);

            if (isSelected) {
                panel.setBackground(new Color(200,220,255));
            }

            return panel;
        });

        JScrollPane scrollPane = new JScrollPane(channelList);
        add(scrollPane, BorderLayout.CENTER);

        // Chargement initial
        for(Channel channel : dataManager.getChannels()){
            model.addElement(channel);
        }

        // ===== BOUTONS =====

        JPanel buttonPanel = new JPanel(new GridLayout(1,2));

        JButton createButton = new JButton("Créer");
        JButton deleteButton = new JButton("Supprimer");

        createButton.addActionListener(e -> createChannel());
        deleteButton.addActionListener(e -> deleteChannel());

        buttonPanel.add(createButton);
        buttonPanel.add(deleteButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // Observer
        dataManager.addObserver(this);
    }

    // ===== CREATION CANAL =====

    private void createChannel() {

        String name = JOptionPane.showInputDialog(
                this,
                "Nom du canal"
        );

        if(name == null || name.trim().isEmpty())
            return;

        Channel channel = new Channel(
                session.getConnectedUser(),
                name.trim()
        );

        dataManager.sendChannel(channel);
    }

    // ===== SUPPRESSION CANAL =====

    private void deleteChannel() {

        Channel selected = channelList.getSelectedValue();

        if(selected == null){

            JOptionPane.showMessageDialog(
                    this,
                    "Sélectionnez un canal"
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

    // ===== OBSERVER CHANNEL =====

    @Override
    public void notifyChannelAdded(Channel addedChannel) {

        SwingUtilities.invokeLater(() -> {

            model.addElement(addedChannel);

            revalidate();
            repaint();
        });
    }

    @Override
    public void notifyChannelDeleted(Channel deletedChannel) {

        SwingUtilities.invokeLater(() -> {

            for (int i = 0; i < model.size(); i++) {

                Channel c = model.getElementAt(i);

                if (c.getUuid().equals(deletedChannel.getUuid())) {

                    model.removeElementAt(i);
                    break;
                }
            }

            revalidate();
            repaint();
        });
    }

    @Override
    public void notifyChannelModified(Channel modifiedChannel) {

        SwingUtilities.invokeLater(() -> {

            for (int i = 0; i < model.size(); i++) {

                Channel c = model.getElementAt(i);

                if (c.getUuid().equals(modifiedChannel.getUuid())) {

                    model.setElementAt(modifiedChannel, i);
                    break;
                }
            }

            revalidate();
            repaint();
        });
    }

    // ===== AUTRES EVENTS (NON UTILISÉS ICI) =====

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