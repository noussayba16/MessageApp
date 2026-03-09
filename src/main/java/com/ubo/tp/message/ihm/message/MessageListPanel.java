package main.java.com.ubo.tp.message.ihm.message;

import main.java.com.ubo.tp.message.datamodel.Message;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class MessageListPanel extends JPanel {

    public MessageListPanel() {

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    public void refresh(Set<Message> messages, String currentUserTag) {

        removeAll();

        // Convertir en liste
        List<Message> list = new ArrayList<>(messages);

        // Trier les messages par date
        list.sort(Comparator.comparingLong(Message::getEmissionDate));

        for(Message m : list) {

            boolean isMe = m.getSender()
                    .getUserTag()
                    .equals(currentUserTag);

            add(new MessagePanel(m, isMe));

            add(Box.createVerticalStrut(8));
        }

        revalidate();
        repaint();
    }
}