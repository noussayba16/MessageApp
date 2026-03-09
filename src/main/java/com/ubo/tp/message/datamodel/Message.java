package main.java.com.ubo.tp.message.datamodel;

import java.util.UUID;

/**
 * Classe du modèle représentant un message.
 *
 * @author S.Lucas
 */
public class Message extends AbstractMessageAppObject {

	/**
	 * Utilisateur source du message.
	 */
	protected final User mSender;

	/**
	 * Destinataire du message.
	 */
	protected final UUID mRecipient;

	/**
	 * Date d'émission du message.
	 */
	protected final long mEmissionDate;

	/**
	 * Corps du message.
	 */
	protected final String mText;

	/**
	 * Constructeur.
	 *
	 * @param sender    utilisateur à l'origine du message.
	 * @param recipient destinataire du message.
	 * @param text      corps du message.
	 */
	public Message(User sender, UUID recipient, String text) {
		this(UUID.randomUUID(), sender, recipient, System.currentTimeMillis(), text);
	}

	/**
	 * Constructeur.
	 *
	 * @param messageUuid  identifiant du message.
	 * @param sender       utilisateur à l'origine du message.
	 * @param recipient    destinataire du message.
	 * @param emissionDate date d'émission du message.
	 * @param text         corps du message.
	 */
	public Message(UUID messageUuid, User sender, UUID recipient, long emissionDate, String text) {
		super(messageUuid);
		mSender = sender;
		mRecipient = recipient;
		mEmissionDate = emissionDate;
		mText = text;
	}

	/**
	 * @return l'utilisateur source du message.
	 */
	public User getSender() {
		return mSender;
	}

	/**
	 * @return le destinataire du message.
	 */
	public UUID getRecipient() {
		return mRecipient;
	}

	/**
	 * @return le corps du message.
	 */
	public String getText() {
		return mText;
	}

	/**
	 * Retourne la date d'émission.
	 */
	public long getEmissionDate() {
		return this.mEmissionDate;
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
		sb.append(this.getText());
		sb.append("}");

		return sb.toString();
	}

    public String getContent() {
        return mText;
    }
}
