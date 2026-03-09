package main.java.com.ubo.tp.message.datamodel;

import java.util.UUID;

public class Message extends AbstractMessageAppObject {

	protected final User mSender;
	protected final UUID mRecipient;
	protected final long mEmissionDate;
	protected final String mText;

	// 🔹 nouveau champ pour l'image
	protected final String mImagePath;

	/**
	 * Constructeur utilisé dans l'application (texte)
	 */
	public Message(User sender, UUID recipient, String text) {
		this(UUID.randomUUID(), sender, recipient, System.currentTimeMillis(), text, null);
	}

	/**
	 * Constructeur utilisé pour lecture fichier (important pour DataFilesManager)
	 */
	public Message(UUID messageUuid, User sender, UUID recipient, long emissionDate, String text) {
		this(messageUuid, sender, recipient, emissionDate, text, null);
	}

	/**
	 * Constructeur texte + image
	 */
	public Message(User sender, UUID recipient, String text, String imagePath) {
		this(UUID.randomUUID(), sender, recipient, System.currentTimeMillis(), text, imagePath);
	}

	/**
	 * Constructeur complet
	 */
	public Message(UUID messageUuid, User sender, UUID recipient, long emissionDate, String text, String imagePath) {
		super(messageUuid);
		mSender = sender;
		mRecipient = recipient;
		mEmissionDate = emissionDate;
		mText = text;
		mImagePath = imagePath;
	}

	public User getSender() {
		return mSender;
	}

	public UUID getRecipient() {
		return mRecipient;
	}

	public String getText() {
		return mText;
	}

	public long getEmissionDate() {
		return mEmissionDate;
	}

	// 🔹 getter image
	public String getImagePath() {
		return mImagePath;
	}

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