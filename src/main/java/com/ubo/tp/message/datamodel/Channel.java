package main.java.com.ubo.tp.message.datamodel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Classe du modèle représentant un canal.
 *
 * @author S.Lucas
 */
public class Channel extends AbstractMessageAppObject implements IMessageRecipient {

	/**
	 * Créateur du canal.
	 */
	protected final User mCreator;

	/**
	 * Nom du canal.
	 */
	protected final String mName;

	/**
	 * Statut privé ou public du canal.
	 */
	protected boolean mPrivate;

	/**
	 * Liste des Utilisateurs du canal.
	 */
	protected final Set<User> mUsers = new HashSet<User>();

	/**
	 * Constructeur.
	 *
	 * @param sender utilisateur à l'origine du canal.
	 * @param name   Nom du canal.
	 */
	public Channel(User creator, String name) {
		this(UUID.randomUUID(), creator, name);
	}

	/**
	 * Constructeur.
	 *
	 * @param channelUuid identifiant du canal.
	 * @param sender      utilisateur à l'origine du canal.
	 * @param name        Nom du canal.
	 */
	public Channel(UUID channelUuid, User creator, String name) {
		super(channelUuid);
		mCreator = creator;
		mName = name;
	}

	/**
	 * Constructeur pour un canal privé.
	 *
	 * @param sender utilisateur à l'origine du canal.
	 * @param name   Nom du canal.
	 */
	public Channel(User creator, String name, List<User> users) {
		this(UUID.randomUUID(), creator, name, users);
	}

	/**
	 * Constructeur pour un canal privé.
	 *
	 * @param channelUuid identifiant du canal.
	 * @param sender      utilisateur à l'origine du canal.
	 * @param name        Nom du canal.
	 * @param users       Liste des utilisateurs du canal privé.
	 * 
	 */
	public Channel(UUID messageUuid, User creator, String name, List<User> users) {
		this(messageUuid, creator, name);
		if (!users.isEmpty()) {
			mPrivate = true;
			mUsers.addAll(users);
		}
	}

	/**
	 * @return l'utilisateur source du canal.
	 */
	public User getCreator() {
		return mCreator;
	}

	/**
	 * @return le corps du message.
	 */
	public String getName() {
		return mName;
	}

	/**
	 * @return la liste des utilisateurs de ce canal.
	 */
	public List<User> getUsers() {
		return new ArrayList<User>(mUsers);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append("[");
		sb.append(this.getClass().getName());
		sb.append("] : ");
		sb.append(this.getUuid());
		sb.append(" {");
		sb.append(this.getName());
		sb.append("}");

		return sb.toString();
	}

}
