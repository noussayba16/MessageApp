package main.java.com.ubo.tp.message.ihm.channel;

import main.java.com.ubo.tp.message.datamodel.Channel;

import javax.swing.*;
import java.awt.*;

public class ChannelPanel extends JPanel {

    public ChannelPanel(Channel channel) {

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        setBackground(new Color(240, 240, 240));

        JLabel nameLabel = new JLabel("# " + channel.getName());
        nameLabel.setFont(new Font("Arial", Font.BOLD, 13));

        add(nameLabel, BorderLayout.CENTER);

        JLabel typeLabel = new JLabel();

        if(channel.isPrivate()){
            typeLabel.setText("🔒 Privé");
        }else{
            typeLabel.setText("🌐 Public");
        }

        typeLabel.setFont(new Font("Arial", Font.PLAIN, 11));

        add(typeLabel, BorderLayout.EAST);
    }
}