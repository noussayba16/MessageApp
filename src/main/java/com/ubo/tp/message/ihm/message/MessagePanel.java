package main.java.com.ubo.tp.message.ihm.message;

import main.java.com.ubo.tp.message.datamodel.Message;

import javax.swing.*;
import java.awt.*;

public class MessagePanel extends JPanel {

    public MessagePanel(Message message, boolean isMe) {

        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        setOpaque(true);

        // Couleur différente selon expéditeur
        if (isMe) {
            setBackground(new Color(220, 248, 198)); // vert clair
        } else {
            setBackground(Color.WHITE);
        }

        JLabel senderLabel = new JLabel(isMe ? "Moi" : message.getSender().getUserTag());
        senderLabel.setFont(new Font("Arial", Font.BOLD, 12));

        JTextArea textArea = new JTextArea(message.getText());
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font("Arial", Font.PLAIN, 13));
        textArea.setOpaque(false);

        add(senderLabel, BorderLayout.NORTH);
        add(textArea, BorderLayout.CENTER);

        // 🔥 Empêche l’étirement vertical
        setMaximumSize(new Dimension(Integer.MAX_VALUE, getPreferredSize().height));
    }
}