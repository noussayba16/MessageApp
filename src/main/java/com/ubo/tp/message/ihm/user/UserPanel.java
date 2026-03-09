package main.java.com.ubo.tp.message.ihm.user;

import main.java.com.ubo.tp.message.datamodel.User;

import javax.swing.*;
import java.awt.*;

public class UserPanel extends JPanel {

    public UserPanel(User user) {

        setLayout(new BorderLayout());

        JLabel nameLabel = new JLabel(user.getName());

        if (user.isOnline()) {
            nameLabel.setForeground(Color.GREEN);
        } else {
            nameLabel.setForeground(Color.GRAY);
        }

        add(nameLabel, BorderLayout.CENTER);
    }
}
