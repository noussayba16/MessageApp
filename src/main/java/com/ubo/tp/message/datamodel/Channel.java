package main.java.com.ubo.tp.message.datamodel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Classe du modèle représentant un canal.
 */
public class Channel extends AbstractMessageAppObject implements IMessageRecipient {

	protected final User mCreator;
	protected final String mName;
	protected final boolean mPrivate;
	protected final Set<User> mUsers = new HashSet<>();

	// ===== CANAL PUBLIC =====
	public Channel(User creator, String name) {
		this(UUID.randomUUID(), creator, name, false, new ArrayList<>());
	}

	public Channel(UUID channelUuid, User creator, String name) {
		this(channelUuid, creator, name, false, new ArrayList<>());
	}

	// ===== CANAL PRIVE =====
	public Channel(User creator, String name, List<User> users) {
		this(UUID.randomUUID(), creator, name, true, users);
	}

	public Channel(UUID channelUuid, User creator, String name, List<User> users) {
		this(channelUuid, creator, name, true, users);
	}

	// ===== CONSTRUCTEUR CENTRAL =====
	public Channel(UUID channelUuid, User creator, String name, boolean isPrivate, List<User> users) {
		super(channelUuid);

		this.mCreator = creator;
		this.mName = name;
		this.mPrivate = isPrivate;

		if (isPrivate) {
			mUsers.add(creator);

			if (users != null) {
				for (User u : users) {
					if (u != null && !u.getUuid().equals(creator.getUuid())) {
						mUsers.add(u);
					}
				}
			}
		}
	}

	public User getCreator() {
		return mCreator;
	}

	public String getName() {
		return mName;
	}

	public List<User> getUsers() {
		return new ArrayList<>(mUsers);
	}

	public boolean isPrivate() {
		return mPrivate;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

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