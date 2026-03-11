package main.java.com.ubo.tp.message.ihm.message;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

public class MessageInputPanel extends JPanel {

    private static final String[] EMOJI_CODES = {
            ":smile:",
            ":smirk:",
            ":sad:"
    };

    private static final Map<String, String> EMOJI_MAP = new LinkedHashMap<>();

    static {
        EMOJI_MAP.put(":smile:", "😄");
        EMOJI_MAP.put(":smirk:", "😏");
        EMOJI_MAP.put(":sad:", "😢");
    }

    private JTextField messageField;
    private JButton sendButton;
    private JButton imageButton;

    private String selectedImagePath;

    public MessageInputPanel() {

        setLayout(new BorderLayout(5, 5));

        messageField = new JTextField();

        messageField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {

                if (e.getKeyChar() == ':') {

                    JList<String> emojiList = new JList<>(EMOJI_CODES);
                    emojiList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                    emojiList.setSelectedIndex(0);

                    int result = JOptionPane.showConfirmDialog(
                            MessageInputPanel.this,
                            new JScrollPane(emojiList),
                            "Choisir un emoji",
                            JOptionPane.OK_CANCEL_OPTION
                    );

                    if (result == JOptionPane.OK_OPTION) {
                        String selectedCode = emojiList.getSelectedValue();

                        if (selectedCode != null) {
                            String emoji = EMOJI_MAP.get(selectedCode);

                            if (emoji != null) {
                                String currentText = messageField.getText();

                                if (currentText == null) {
                                    currentText = "";
                                }

                                // Supprime le ":" tapé avant d'ajouter l'emoji
                                if (currentText.endsWith(":")) {
                                    currentText = currentText.substring(0, currentText.length() - 1);
                                }

                                messageField.setText(currentText + emoji);
                            }
                        }
                    } else {
                        // Annulé : supprime le ":" tapé
                        String currentText = messageField.getText();
                        if (currentText != null && currentText.endsWith(":")) {
                            messageField.setText(currentText.substring(0, currentText.length() - 1));
                        }
                    }
                }
            }
        });

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
        // CORRIGÉ : utilise SwingUtilities.invokeLater + parent Window
        // pour éviter le NullPointerException du JFileChooser sur Windows
        SwingUtilities.invokeLater(() -> {

            // Récupère la fenêtre parente pour éviter le bug de focus
            Window parentWindow = SwingUtilities.getWindowAncestor(this);

            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Choisir une image");

            // Filtre pour n'afficher que les images
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                    "Images (jpg, png, gif, bmp)", "jpg", "jpeg", "png", "gif", "bmp"
            ));

            int result = chooser.showOpenDialog(parentWindow);

            if (result == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                selectedImagePath = file.getAbsolutePath();

                JOptionPane.showMessageDialog(
                        parentWindow,
                        "Image sélectionnée : " + file.getName()
                );
            }
        });
    }

    public void setSendAction(Runnable action) {

        sendButton.addActionListener(e -> {

            String content = messageField.getText();

            if ((content == null || content.trim().isEmpty()) && selectedImagePath == null) {
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