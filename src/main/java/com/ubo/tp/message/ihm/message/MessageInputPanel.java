package main.java.com.ubo.tp.message.ihm.message;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class MessageInputPanel extends JPanel {

    private JTextField messageField;
    private JButton sendButton;
    private JButton imageButton;

    private String selectedImagePath;

    public MessageInputPanel() {

        setLayout(new BorderLayout(5,5));

        messageField = new JTextField();

        sendButton = new JButton("Envoyer");
        imageButton = new JButton("📷");

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonsPanel.add(imageButton);
        buttonsPanel.add(sendButton);

        add(messageField, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.EAST);

        imageButton.addActionListener(e -> chooseImage());
    }

    private void chooseImage() {

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choisir une image");

        int result = chooser.showOpenDialog(this);

        if(result == JFileChooser.APPROVE_OPTION) {

            File file = chooser.getSelectedFile();
            selectedImagePath = file.getAbsolutePath();

            JOptionPane.showMessageDialog(
                    this,
                    "Image sélectionnée : " + file.getName()
            );
        }
    }

    public void setSendAction(Runnable action) {

        sendButton.addActionListener(e -> {

            String content = messageField.getText();

            if((content == null || content.trim().isEmpty()) && selectedImagePath == null) {

                JOptionPane.showMessageDialog(
                        this,
                        "Le message est vide"
                );
                return;
            }

            action.run();

            messageField.setText("");
            selectedImagePath = null;
        });
    }

    public String getMessageText() {
        return messageField.getText();
    }

    public String getImagePath() {
        return selectedImagePath;
    }
}