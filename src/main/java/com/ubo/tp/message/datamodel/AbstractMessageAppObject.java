package main.java.com.ubo.tp.message.datamodel;

import java.util.Observable;
import java.util.UUID;

/**
 * Classe du modèle représentant un utilisateur.
 *
 * @author S.Lucas
 */
public abstract class AbstractMessageAppObject extends Observable {
	/**
	 * Identifiant unique de l'objet.
	 */
	protected final UUID mUuid;

	/**
	 * Constructeur.
	 *
	 * @param uuid , Identifiant unique de l'objet.
	 */
	public AbstractMessageAppObject(UUID uuid) {
		mUuid = uuid;
	}

	/**
	 * Retourne l'identifiant unique de l'utilisateur.
	 */
	public UUID getUuid() {
		return this.mUuid;
	}

	@Override
	public int hashCode() {
		int hashCode = 0;

		if (this.mUuid != null) {
			hashCode = this.mUuid.hashCode();
		}

		return hashCode;
	}

	@Override
	public boolean equals(Object other) {
		boolean equals = false;

		if (other != null && other instanceof AbstractMessageAppObject) {
			equals = (this.getUuid().equals(((AbstractMessageAppObject) other).getUuid()));
		}

		return equals;
	}
}
