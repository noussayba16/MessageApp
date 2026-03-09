package main.java.com.ubo.tp.message.ihm.user;

import main.java.com.ubo.tp.message.controller.UserController;
import main.java.com.ubo.tp.message.datamodel.User;

import javax.swing.*;
import java.awt.*;

public class UserListPanel extends JPanel {

    private JList<User> userList;
    private DefaultListModel<User> model;

    public UserListPanel(UserController controller) {

        setLayout(new BorderLayout());

        model = new DefaultListModel<>();

        for (User user : controller.getAllUsers()) {
            model.addElement(user);
        }

        userList = new JList<>(model);

        add(new JScrollPane(userList), BorderLayout.CENTER);
    }

    public User getSelectedUser() {
        return userList.getSelectedValue();
    }
}
