package main.java.com.ubo.tp.message.core.database;

import main.java.com.ubo.tp.message.datamodel.Channel;
import main.java.com.ubo.tp.message.datamodel.Message;
import main.java.com.ubo.tp.message.datamodel.User;

/**
 * Classe pour adresser la BDD directement.
 *
 * @author S.Lucas
 */
public class DbConnector {

	protected Database mDatabase;

	/**
	 * Constructeur.
	 */
	public DbConnector(Database database) {
		mDatabase = database;
	}

	/**
	 * @return la BDD liée.
	 */
	public IDatabase getDatabase() {
		return this.mDatabase;
	}

	/**
	 * Ajoute un message à la base de données.
	 *
	 * @param messageToAdd
	 */
	public void addMessage(Message messageToAdd) {
		this.mDatabase.addMessage(messageToAdd);
	}

	/**
	 * Supprime un message de la base de données.
	 *
	 * @param messageToRemove
	 */
	public void deleteMessage(Message messageToRemove) {
		this.mDatabase.deleteMessage(messageToRemove);
	}

	/**
	 * Modification d'un message de la base de données.
	 *
	 * @param messageToModify
	 */
	public void modifiyMessage(Message messageToModify) {
		this.mDatabase.modifiyMessage(messageToModify);
	}

	/**
	 * Ajoute un utilisateur à la base de données.
	 *
	 * @param userToAdd
	 */
	public void addUser(User userToAdd) {
		// Ajout de l'utilisateur
		this.mDatabase.addUser(userToAdd);
	}

	/**
	 * Supprime un utilisateur de la base de données.
	 *
	 * @param userToRemove
	 */
	public void deleteUser(User userToRemove) {
		// Suppression de l'utilisateur
		this.mDatabase.deleteUser(userToRemove);
	}

	/**
	 * Modification d'un utilisateur de la base de données.
	 *
	 * @param userToModify
	 */
	public void modifiyUser(User userToModify) {
		this.mDatabase.modifiyUser(userToModify);
	}

	/**
	 * Ajoute un canal à la base de données.
	 *
	 * @param newChannel
	 */
	public void addChannel(Channel channelToAdd) {
		this.mDatabase.addChannel(channelToAdd);
	}

	/**
	 * Supprime un canal de la base de données.
	 *
	 * @param deletedChannel
	 */
	public void deleteChannel(Channel channelToRemove) {
		this.mDatabase.deleteChannel(channelToRemove);
	}

	/**
	 * Modification d'un canal de la base de données.
	 *
	 * @param modifiedChannel
	 */
	public void modifiyChannel(Channel channelToModify) {
		this.mDatabase.modifiyChannel(channelToModify);
	}
}
