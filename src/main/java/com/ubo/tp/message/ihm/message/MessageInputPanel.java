package main.java.com.ubo.tp.message.ihm.message;

import javax.swing.*;
import java.awt.*;

public class MessageInputPanel extends JPanel {

    private JTextField messageField;
    private JButton sendButton;

    public MessageInputPanel() {

        setLayout(new BorderLayout(5,5));

        messageField = new JTextField();
        sendButton = new JButton("Envoyer");

        add(messageField, BorderLayout.CENTER);
        add(sendButton, BorderLayout.EAST);
    }

    /**
     * Permet au MainPanel d'attacher l'action d'envoi
     */
    public void setSendAction(Runnable action) {

        sendButton.addActionListener(e -> {

            String content = messageField.getText();

            if(content == null || content.trim().isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Le message est vide"
                );
                return;
            }

            action.run();
            messageField.setText("");
        });
    }

    /**
     * Retourne le texte saisi
     */
    public String getMessageText() {
        return messageField.getText();
    }
}