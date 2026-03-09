package main.java.com.ubo.tp.message.ihm;

import main.java.com.ubo.tp.message.controller.UserController;
import main.java.com.ubo.tp.message.core.DataManager;
import main.java.com.ubo.tp.message.core.session.Session;
import main.java.com.ubo.tp.message.datamodel.Channel;
import main.java.com.ubo.tp.message.datamodel.Message;
import main.java.com.ubo.tp.message.datamodel.User;
import main.java.com.ubo.tp.message.ihm.channel.ChannelListPanel;
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
    private JTextField messageField;

    private User selectedUser = null;
    private Channel selectedChannel = null;

    private Set<Message> currentConversation = new HashSet<>();

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

        JLabel lblWelcome = new JLabel(
                "Bienvenue " + user.getName() + " !",
                SwingConstants.CENTER
        );
        lblWelcome.setFont(new Font("Arial", Font.BOLD, 20));

        JButton btnLogout = new JButton("Déconnexion");
        btnLogout.addActionListener(e -> session.disconnect());

        topPanel.add(lblWelcome, BorderLayout.CENTER);
        topPanel.add(btnLogout, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // ===== USERS =====
        userModel = new DefaultListModel<>();

        for (User u : userController.getAllUsers()) {
            userModel.addElement(u);
        }

        userList = new JList<>(userModel);
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane userScroll = new JScrollPane(userList);
        userScroll.setPreferredSize(new Dimension(220, 0));

        add(userScroll, BorderLayout.WEST);

        // ===== CHANNELS =====
        channelListPanel = new ChannelListPanel(dataManager, session);
        add(channelListPanel, BorderLayout.EAST);

        // ===== MESSAGES =====
        messageListPanel = new MessageListPanel();
        add(new JScrollPane(messageListPanel), BorderLayout.CENTER);

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
        JPanel bottomPanel = new JPanel(new BorderLayout(8, 8));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        messageField = new JTextField();

        JButton sendButton = new JButton("Envoyer");
        sendButton.addActionListener(e -> envoyerMessage());

        bottomPanel.add(messageField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    // ================= USER CONVERSATION =================
    private void chargerConversationUser(User user) {

        currentConversation.clear();

        Set<Message> sent = dataManager.getMessagesFrom(
                session.getConnectedUser(),
                user
        );

        Set<Message> received = dataManager.getMessagesFrom(
                user,
                session.getConnectedUser()
        );

        currentConversation.addAll(sent);
        currentConversation.addAll(received);

        messageListPanel.refresh(
                currentConversation,
                session.getConnectedUser().getUserTag()
        );
    }

    // ================= CHANNEL CONVERSATION =================
    private void chargerConversationChannel(Channel channel) {

        currentConversation.clear();

        for (Message m : dataManager.getMessages()) {

            if (m.getRecipient().equals(channel.getUuid())) {
                currentConversation.add(m);
            }

        }

        messageListPanel.refresh(
                currentConversation,
                session.getConnectedUser().getUserTag()
        );
    }

    // ================= SEND MESSAGE =================
    private void envoyerMessage() {

        String content = messageField.getText();

        if (content == null || content.trim().isEmpty()) {
            return;
        }

        Message message;

        // ===== PRIVATE MESSAGE =====
        if (selectedUser != null) {

            message = new Message(
                    session.getConnectedUser(),
                    selectedUser.getUuid(),
                    content.trim()
            );

            dataManager.sendMessage(message);

        }
        // ===== CHANNEL MESSAGE =====
        else if (selectedChannel != null) {

            message = new Message(
                    session.getConnectedUser(),
                    selectedChannel.getUuid(),
                    content.trim()
            );

            dataManager.sendMessage(message);

        }
        // ===== NO DESTINATION =====
        else {

            JOptionPane.showMessageDialog(
                    this,
                    "Sélectionnez un utilisateur ou un canal !"
            );

            return;
        }

        // Ajout immédiat dans la conversation
        currentConversation.add(message);

        messageListPanel.refresh(
                currentConversation,
                session.getConnectedUser().getUserTag()
        );

        messageField.setText("");
    }
}