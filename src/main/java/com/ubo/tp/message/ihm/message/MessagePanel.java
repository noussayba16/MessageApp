package main.java.com.ubo.tp.message.ihm.message;

import main.java.com.ubo.tp.message.datamodel.Message;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MessagePanel extends JPanel {

    public MessagePanel(Message message, boolean isMe, Runnable deleteAction) {

        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        setOpaque(true);

        if (isMe) {
            setBackground(new Color(220, 248, 198));
        } else {
            setBackground(Color.WHITE);
        }

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel senderLabel = new JLabel(isMe ? "Moi" : message.getSender().getUserTag());
        senderLabel.setFont(new Font("Arial", Font.BOLD, 12));
        topPanel.add(senderLabel, BorderLayout.WEST);

        // bouton supprimer seulement pour l'auteur du message
        if (isMe) {
            JButton deleteButton = new JButton("Supprimer");
            deleteButton.addActionListener(e -> deleteAction.run());
            topPanel.add(deleteButton, BorderLayout.EAST);
        }

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        // texte
        if (message.getText() != null && !message.getText().trim().isEmpty()) {
            JTextArea textArea = new JTextArea(message.getText());
            textArea.setEditable(false);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setFont(new Font("Arial", Font.PLAIN, 13));
            textArea.setOpaque(false);
            textArea.setBorder(null);
            contentPanel.add(textArea);
        }

        // image
        if (message.getImagePath() != null && !message.getImagePath().trim().isEmpty()) {
            ImageIcon icon = new ImageIcon(message.getImagePath());
            Image img = icon.getImage().getScaledInstance(250, -1, Image.SCALE_SMOOTH);
            JLabel imageLabel = new JLabel(new ImageIcon(img));
            contentPanel.add(Box.createVerticalStrut(5));
            contentPanel.add(imageLabel);
        }

        // heure
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String time = sdf.format(new Date(message.getEmissionDate()));

        JLabel timeLabel = new JLabel(time);
        timeLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        timeLabel.setForeground(Color.GRAY);

        add(topPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
        add(timeLabel, BorderLayout.SOUTH);

        setMaximumSize(new Dimension(Integer.MAX_VALUE, getPreferredSize().height));
    }
}