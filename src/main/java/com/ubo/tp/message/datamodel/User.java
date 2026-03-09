package main.java.com.ubo.tp.message.datamodel;

import java.util.UUID;

/**
 * Classe du modèle représentant un utilisateur.
 *
 * @author S.Lucas
 */
public class User extends AbstractMessageAppObject implements IMessageRecipient {

	/**
	 * Tag non modifiable correspondant à l'utilisateur. <br/>
	 * <i>Doit être unique dans le système</i>
	 */
	protected final String mUserTag;

	/**
	 * Mot de passe de l'utilisateur.
	 */
	protected String mUserPassword;

	/**
	 * Nom de l'utilisateur.
	 */
	protected String mName;

	/**
	 * Booléen indiquant si l'utilisateur est connecté.
	 */
	protected boolean mOnline = false;

	/**
	 * Constructeur.
	 *
	 * @param userTag      Tag correspondant à l'utilisateur.
	 * @param userTag      Tag correspondant à l'utilisateur.
	 * @param userPassword mot de passe de l'utilisateur.
	 * @param name         Nom de l'utilisateur.
	 */
	public User(String userTag, String userPassword, String name) {
		this(UUID.randomUUID(), userTag, userPassword, name);
	}

	/**
	 * Constructeur.
	 *
	 * @param uuid         Identifiant unique de l'utilisateur.
	 * @param userTag      Tag correspondant à l'utilisateur.
	 * @param userPassword mot de passe de l'utilisateur.
	 * @param name         Nom de l'utilisateur.
	 */
	public User(UUID uuid, String userTag, String userPassword, String name) {
		super(uuid);
		mUserTag = userTag;
		mUserPassword = userPassword;
		mName = name;
	}

	/**
	 * Retourne le nom de l'utilisateur.
	 */
	public String getName() {
		return mName;
	}

	/**
	 * Assigne le nom de l'utilisateur.
	 *
	 * @param name
	 */
	public void setName(String name) {
		this.mName = name;
	}

	/**
	 * Retourne le tag correspondant à l'utilisateur.
	 */
	public String getUserTag() {
		return this.mUserTag;
	}

	/**
	 * Retourne le mot de passe de l'utilisateur.
	 */
	public String getUserPassword() {
		return this.mUserPassword;
	}

	/**
	 * Assigne le mot de passe de l'utilisateur.
	 *
	 * @param userPassword
	 */
	public void setUserPassword(String userPassword) {
		this.mUserPassword = userPassword;
	}

	/**
	 * Retourne le statut de connection.
	 */
	public boolean isOnline() {
		return this.mOnline;
	}

	/**
	 * Assigne le statut de connection.
	 *
	 * @param online
	 */
	public void setOnline(boolean online) {
		this.mOnline = online;
	}

    @Override
    public String toString() {
        String status = mOnline ? " (en ligne)" : " (hors ligne)";
        return mUserTag + " - " + mName + status;
    }
}
