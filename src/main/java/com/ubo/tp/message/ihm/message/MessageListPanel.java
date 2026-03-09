package main.java.com.ubo.tp.message.ihm.message;

import main.java.com.ubo.tp.message.datamodel.Message;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class MessageListPanel extends JPanel {

    public MessageListPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    public void refresh(Set<Message> messages, String currentUserTag, Consumer<Message> onDelete) {

        removeAll();

        List<Message> list = new ArrayList<>(messages);
        list.sort(Comparator.comparingLong(Message::getEmissionDate));

        for (Message m : list) {

            boolean isMe = m.getSender()
                    .getUserTag()
                    .equals(currentUserTag);

            add(new main.java.com.ubo.tp.message.ihm.message.MessagePanel(
                    m,
                    isMe,
                    () -> onDelete.accept(m)
            ));

            add(Box.createVerticalStrut(8));
        }

        revalidate();
        repaint();
    }
}