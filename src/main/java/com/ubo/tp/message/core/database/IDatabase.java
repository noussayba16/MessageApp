package main.java.com.ubo.tp.message.core.database;

import java.util.Set;

import main.java.com.ubo.tp.message.datamodel.Channel;
import main.java.com.ubo.tp.message.datamodel.Message;
import main.java.com.ubo.tp.message.datamodel.User;

/**
 * Interface de la base de données de l'application.
 *
 * @author S.Lucas
 */
public interface IDatabase {

	/**
	 * Ajoute un observateur sur les modifications de la base de données.
	 *
	 * @param observer
	 */
	void addObserver(IDatabaseObserver observer);

	/**
	 * Supprime un observateur sur les modifications de la base de données.
	 *
	 * @param observer
	 */
	void removeObserver(IDatabaseObserver observer);

	/**
	 * Retourne la liste des utilisateurs.
	 */
	Set<User> getUsers();

	/**
	 * Retourne la liste des messages.
	 */
	Set<Message> getMessages();

	/**
	 * Retourne la liste des cannaux.
	 */
	Set<Channel> getChannels();
}
