package main.java.com.ubo.tp.message.core;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import main.java.com.ubo.tp.message.core.database.Database;
import main.java.com.ubo.tp.message.core.database.EntityManager;
import main.java.com.ubo.tp.message.core.database.IDatabase;
import main.java.com.ubo.tp.message.core.database.IDatabaseObserver;
import main.java.com.ubo.tp.message.core.directory.IWatchableDirectory;
import main.java.com.ubo.tp.message.core.directory.WatchableDirectory;
import main.java.com.ubo.tp.message.datamodel.Channel;
import main.java.com.ubo.tp.message.datamodel.IMessageRecipient;
import main.java.com.ubo.tp.message.datamodel.Message;
import main.java.com.ubo.tp.message.datamodel.User;

public class DataManager {

    protected final IDatabase mDatabase;
    protected final EntityManager mEntityManager;
    protected IWatchableDirectory mWatchableDirectory;

    public DataManager(IDatabase database, EntityManager entityManager) {
        mDatabase = database;
        mEntityManager = entityManager;
    }

    public void addObserver(IDatabaseObserver observer) {
        mDatabase.addObserver(observer);
    }

    public void removeObserver(IDatabaseObserver observer) {
        mDatabase.removeObserver(observer);
    }

    public Set<User> getUsers() {
        return mDatabase.getUsers();
    }

    public Set<Message> getMessages() {
        return mDatabase.getMessages();
    }

    public Set<Channel> getChannels() {
        return mDatabase.getChannels();
    }

    public void sendMessage(Message message) {
        mEntityManager.writeMessageFile(message);
    }

    public void sendUser(User user) {
        mEntityManager.writeUserFile(user);
    }

    public void sendChannel(Channel channel) {
        mEntityManager.writeChannelFile(channel);
    }

    public Set<Message> getMessagesFrom(User user) {

        Set<Message> userMessages = new HashSet<>();

        for (Message message : getMessages()) {
            if (message.getSender().equals(user)) {
                userMessages.add(message);
            }
        }

        return userMessages;
    }

    public Set<Message> getMessagesFrom(User sender, IMessageRecipient recipient) {

        Set<Message> userMessages = new HashSet<>();

        for (Message message : getMessagesFrom(sender)) {
            if (message.getRecipient().equals(recipient.getUuid())) {
                userMessages.add(message);
            }
        }

        return userMessages;
    }

    public Set<Message> getMessagesTo(User user) {

        Set<Message> userMessages = new HashSet<>();

        for (Message message : getMessages()) {
            if (message.getRecipient().equals(user.getUuid())) {
                userMessages.add(message);
            }
        }

        return userMessages;
    }

    public void setExchangeDirectory(String directoryPath) {

        mEntityManager.setExchangeDirectory(directoryPath);

        mWatchableDirectory = new WatchableDirectory(directoryPath);
        mWatchableDirectory.initWatching();
        mWatchableDirectory.addObserver(mEntityManager);
    }

    public User createUser(String tag, String name, String password) {

        if (tag == null || tag.trim().isEmpty()) {
            throw new IllegalArgumentException("Le tag est obligatoire.");
        }

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom est obligatoire.");
        }

        if (getUser(tag) != null) {
            throw new IllegalArgumentException("Ce tag existe déjà.");
        }

        User newUser = new User(tag, password, name);
        mEntityManager.writeUserFile(newUser);

        return newUser;
    }

    public User getUser(String tag) {

        for (User user : getUsers()) {
            if (user.getUserTag().equals(tag)) {
                return user;
            }
        }

        return null;
    }

    public void modifyUser(UUID uuid, User modifiedUser) {

        if (modifiedUser == null) {
            return;
        }

        mEntityManager.writeUserFile(modifiedUser);

        if (mDatabase instanceof Database db) {
            db.modifiyUser(modifiedUser);
        }
    }

    public void deleteUser(User user) {

        if (user == null) {
            return;
        }

        mEntityManager.deleteUserFile(user);

        if (mDatabase instanceof Database db) {
            db.deleteUser(user);
        }
    }

    // ===== CHANNEL =====

    public void addChannel(Channel channel) {

        if (channel == null) {
            return;
        }

        sendChannel(channel);
    }

    public void removeChannel(Channel channel) {

        if (channel == null) {
            return;
        }

        mEntityManager.deleteChannelFile(channel);

        if (mDatabase instanceof Database db) {
            db.deleteChannel(channel);
        }
    }

    // ===== MESSAGE =====

    public void deleteMessage(Message message) {

        if (message == null) {
            return;
        }

        mEntityManager.deleteMessageFile(message);

        if (mDatabase instanceof Database db) {
            db.deleteMessage(message);
        }
    }
    public void modifyChannel(UUID uuid, Channel modifiedChannel) {

        if (modifiedChannel == null) {
            return;
        }

        // écrire le canal modifié dans le dossier d'échange
        mEntityManager.writeChannelFile(modifiedChannel);

        // mettre à jour la base en mémoire
        if (mDatabase instanceof Database db) {
            db.modifyChannel(modifiedChannel);
        }
    }
}