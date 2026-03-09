package main.java.com.ubo.tp.message.core.session;

import main.java.com.ubo.tp.message.datamodel.User;

/**
 * Interface d'observation de la session.
 *
 * @author S.Lucas
 */
public interface ISessionObserver {

	/**
	 * Notification de connexion d'un utilisateur.
	 *
	 * @param connectedUser, utilisateur nouvellement connecté.
	 */
	void notifyLogin(User connectedUser);

	/**
	 * Notification de déconnexion.
	 */
	void notifyLogout();
}
