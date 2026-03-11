package main.java.com.ubo.tp.message.core.database;

import java.util.HashSet;
import java.util.Set;

import main.java.com.ubo.tp.message.common.Constants;
import main.java.com.ubo.tp.message.datamodel.Channel;
import main.java.com.ubo.tp.message.datamodel.Message;
import main.java.com.ubo.tp.message.datamodel.User;

/**
 * Classe représentant les données chargées dans l'application.
 *
 * @author S.Lucas
 */
public class Database implements IDatabase {
	/**
	 * Liste des utilisateurs enregistrés.
	 */
	protected final Set<User> mUsers;

	/**
	 * Liste des Message enregistrés.
	 */
	protected final Set<Message> mMessages;

	/**
	 * Liste des canaux enregistrés.
	 */
	protected final Set<Channel> mChannels;

	/**
	 * Liste des observateurs de modifications de la base.
	 */
	protected final Set<IDatabaseObserver> mObservers;

	/**
	 * Constructeur.
	 */
	public Database() {
		mUsers = new HashSet<>();
		mMessages = new HashSet<>();
		mObservers = new HashSet<>();
		mChannels = new HashSet<>();
	}

	@Override
	public Set<User> getUsers() {
		// Clonage pour éviter les modifications extérieures.
		return new HashSet<>(this.mUsers);
	}

	@Override
	public Set<Message> getMessages() {
		// Clonage pour éviter les modifications extérieures.
		return new HashSet<>(this.mMessages);
	}

	@Override
	public Set<Channel> getChannels() {
		// Clonage pour éviter les modifications extérieures.
		return new HashSet<>(this.mChannels);
	}

	/**
	 * Ajoute un message à la base de données.
	 *
	 * @param messageToAdd
	 */
	protected void addMessage(Message messageToAdd) {
		// Ajout du message
		this.mMessages.add(messageToAdd);

		// Notification des observateurs
		for (IDatabaseObserver observer : mObservers) {
			observer.notifyMessageAdded(messageToAdd);
		}
	}

	/**
	 * Supprime un message de la base de données.
	 *
	 * @param messageToRemove
	 */
	public void deleteMessage(Message messageToRemove) {
		// Suppression du message
		this.mMessages.remove(messageToRemove);

		// Notification des observateurs
		for (IDatabaseObserver observer : mObservers) {
			observer.notifyMessageDeleted(messageToRemove);
		}
	}

	/**
	 * Modification d'un message de la base de données.
	 *
	 * @param messageToModify
	 */
	protected void modifiyMessage(Message messageToModify) {
		// Ré-ajout pour écraser l'ancienne copie.
		this.mMessages.remove(messageToModify);
		this.mMessages.add(messageToModify);

		// Notification des observateurs
		for (IDatabaseObserver observer : mObservers) {
			observer.notifyMessageModified(messageToModify);
		}
	}

	/**
	 * Ajoute un utilisateur à la base de données.
	 *
	 * @param userToAdd
	 */
	protected void addUser(User userToAdd) {
		// Ajout de l'utilisateur
		this.mUsers.add(userToAdd);

		// Notification des observateurs
		for (IDatabaseObserver observer : mObservers) {
			observer.notifyUserAdded(userToAdd);
		}
	}

	/**
	 * Supprime un utilisateur de la base de données.
	 *
	 * @param userToRemove
	 */
    public void deleteUser(User userToRemove) {
		// Suppression de l'utilisateur
		this.mUsers.remove(userToRemove);

		// Notification des observateurs
		for (IDatabaseObserver observer : mObservers) {
			observer.notifyUserDeleted(userToRemove);
		}
	}

	/**
	 * Modification d'un utilisateur de la base de données.
	 *
	 * @param userToModify
	 */
    public void modifiyUser(User userToModify) {
		// Ré-ajout pour écraser l'ancienne copie.
		this.mUsers.remove(userToModify);
		this.mUsers.add(userToModify);

		// Notification des observateurs
		for (IDatabaseObserver observer : mObservers) {
			observer.notifyUserModified(userToModify);
		}
	}

	/**
	 * Ajoute un canal à la base de données.
	 *
	 * @param newChannel
	 */
	protected void addChannel(Channel channelToAdd) {
		// Ajout du canal
		this.mChannels.add(channelToAdd);

		// Notification des observateurs
		for (IDatabaseObserver observer : mObservers) {
			observer.notifyChannelAdded(channelToAdd);
		}
	}

	/**
	 * Supprime un canal de la base de données.
	 *
	 * @param deletedChannel
	 */
    public void deleteChannel(Channel channelToRemove) {

        this.mChannels.remove(channelToRemove);

        for (IDatabaseObserver observer : mObservers) {
            observer.notifyChannelDeleted(channelToRemove);
        }
    }

	/**
	 * Modification d'un canal de la base de données.
	 *
	 * @param modifiedChannel
	 */
	public void modifiyChannel(Channel channelToModify) {
		// Ré-ajout pour écraser l'ancienne copie.
		this.mChannels.remove(channelToModify);
		this.mChannels.add(channelToModify);

		// Notification des observateurs
		for (IDatabaseObserver observer : mObservers) {
			observer.notifyChannelModified(channelToModify);
		}
	}

	@Override
	public void addObserver(IDatabaseObserver observer) {
		this.mObservers.add(observer);

		// Notification pour le nouvel observateur
		for (Message message : this.getMessages()) {
			observer.notifyMessageAdded(message);
		}

		// Notification pour le nouvel observateur
		for (User user : this.getUsers()) {
			// Pas de notification pour l'utilisateur inconnu
			if (!user.getUuid().equals(Constants.UNKNONWN_USER_UUID)) {
				observer.notifyUserAdded(user);
			}
		}
	}

	@Override
	public void removeObserver(main.java.com.ubo.tp.message.core.database.IDatabaseObserver observer) {
		this.mObservers.remove(observer);
	}



}
