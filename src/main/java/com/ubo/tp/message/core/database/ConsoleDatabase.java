package main.java.com.ubo.tp.message.core.database;

import main.java.com.ubo.tp.message.datamodel.Channel;
import main.java.com.ubo.tp.message.datamodel.Message;
import main.java.com.ubo.tp.message.datamodel.User;

public class ConsoleDatabase implements IDatabaseObserver {

    @Override
    public void notifyUserAdded(User addedUser) {
        System.out.println("[DB] Utilisateur ajouté : " + addedUser.getName());
    }

    @Override
    public void notifyUserDeleted(User deletedUser) {
        System.out.println("[DB] Utilisateur supprimé : " + deletedUser.getName());
    }

    @Override
    public void notifyUserModified(User modifiedUser) {
        System.out.println("[DB] Utilisateur modifié : " + modifiedUser.getName());
    }

    @Override
    public void notifyMessageAdded(Message addedMessage) {
        System.out.println("[DB] Message ajouté : " + addedMessage.getText());
    }

    @Override
    public void notifyMessageDeleted(Message deletedMessage) {
        System.out.println("[DB] Message supprimé");
    }

    @Override
    public void notifyMessageModified(Message modifiedMessage) {
        System.out.println("[DB] Message modifié");
    }

    @Override
    public void notifyChannelAdded(Channel addedChannel) {
        System.out.println("[DB] Channel ajouté : " + addedChannel.getName());
    }

    @Override
    public void notifyChannelDeleted(Channel deletedChannel) {
        System.out.println("[DB] Channel supprimé : " + deletedChannel.getName());
    }

    @Override
    public void notifyChannelModified(Channel modifiedChannel) {
        System.out.println("[DB] Channel modifié : " + modifiedChannel.getName());
    }
}
