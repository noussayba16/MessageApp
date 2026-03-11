package main.java.com.ubo.tp.message.ihm.message;

import main.java.com.ubo.tp.message.datamodel.User;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
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

    // ← NOUVEAU : liste des utilisateurs mentionnables
    private List<User> mentionnableUsers;

    public MessageInputPanel() {
        setLayout(new BorderLayout(5, 5));
        messageField = new JTextField();

        messageField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {

                // ===== EMOJI =====
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
                                if (currentText == null) currentText = "";
                                if (currentText.endsWith(":")) {
                                    currentText = currentText.substring(0, currentText.length() - 1);
                                }
                                messageField.setText(currentText + emoji);
                            }
                        }
                    } else {
                        String currentText = messageField.getText();
                        if (currentText != null && currentText.endsWith(":")) {
                            messageField.setText(currentText.substring(0, currentText.length() - 1));
                        }
                    }
                }

                // ===== MENTION @ =====
                if (e.getKeyChar() == '@') {
                    afficherMentions();
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

    // ===== MENTION @ =====
    private void afficherMentions() {

        if (mentionnableUsers == null || mentionnableUsers.isEmpty()) {
            // Pas d'utilisateurs disponibles : on garde juste le @
            return;
        }

        // Affiche la liste des utilisateurs mentionnables
        JList<User> userJList = new JList<>(mentionnableUsers.toArray(new User[0]));
        userJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userJList.setSelectedIndex(0);

        // Renderer pour afficher tag + nom
        userJList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel label = new JLabel("@" + value.getUserTag() + " (" + value.getName() + ")");
            label.setOpaque(true);
            label.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
            if (isSelected) {
                label.setBackground(new Color(51, 153, 255));
                label.setForeground(Color.WHITE);
            } else {
                label.setBackground(Color.WHITE);
                label.setForeground(Color.BLACK);
            }
            return label;
        });

        int result = JOptionPane.showConfirmDialog(
                MessageInputPanel.this,
                new JScrollPane(userJList),
                "Mentionner un utilisateur",
                JOptionPane.OK_CANCEL_OPTION
        );

        String currentText = messageField.getText();
        if (currentText == null) currentText = "";

        if (result == JOptionPane.OK_OPTION) {
            User selectedUser = userJList.getSelectedValue();
            if (selectedUser != null) {
                // Supprime le "@" déjà tapé et insère @tag
                if (currentText.endsWith("@")) {
                    currentText = currentText.substring(0, currentText.length() - 1);
                }
                messageField.setText(currentText + "@" + selectedUser.getUserTag() + " ");
            }
        } else {
            // Annulé : supprime le "@" tapé
            if (currentText.endsWith("@")) {
                messageField.setText(currentText.substring(0, currentText.length() - 1));
            }
        }
    }

    // ===== SETTER liste des utilisateurs mentionnables =====
    /**
     * Appelé depuis MainPanel pour mettre à jour la liste
     * des utilisateurs mentionnables selon le canal/utilisateur sélectionné.
     */
    public void setMentionnableUsers(List<User> users) {
        this.mentionnableUsers = users;
    }

    private void chooseImage() {
        SwingUtilities.invokeLater(() -> {
            Window parentWindow = SwingUtilities.getWindowAncestor(this);
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Choisir une image");
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
                JOptionPane.showMessageDialog(this, "Le message est vide");
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