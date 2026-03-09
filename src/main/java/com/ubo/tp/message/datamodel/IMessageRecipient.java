package main.java.com.ubo.tp.message.datamodel;

import java.util.UUID;

/**
 * Interface de la destination d'un message.
 *
 * @author S.Lucas
 */
public interface IMessageRecipient {

	/**
	 * Retourne l'identifiant de la destination.
	 */
	UUID getUuid();
}
