package main.java.com.ubo.tp.message.datamodel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class Channel extends AbstractMessageAppObject implements IMessageRecipient {

	protected final User mCreator;
	protected final String mName;
	protected boolean mPrivate;
	protected final Set<User> mUsers = new HashSet<>();

	public Channel(User creator, String name) {
		this(UUID.randomUUID(), creator, name);
	}

	public Channel(UUID channelUuid, User creator, String name) {
		super(channelUuid);
		mCreator = creator;
		mName = name;
	}

	public Channel(User creator, String name, List<User> users) {
		this(UUID.randomUUID(), creator, name, users);
	}

	public Channel(UUID channelUuid, User creator, String name, List<User> users) {
		this(channelUuid, creator, name);
		mUsers.add(creator); // ← créateur toujours ajouté en premier
		if (!users.isEmpty()) {
			mPrivate = true;
			mUsers.addAll(users);
		}
	}

	/**
	 * CORRECTIF : garantit que le créateur est toujours dans mUsers.
	 * Appelé après chargement depuis le disque pour réparer les anciens canaux.
	 */
	public void ensureCreatorIsMember() {
		if (mCreator != null) {
			mUsers.add(mCreator);
		}
	}

	public User getCreator() { return mCreator; }
	public String getName() { return mName; }
	public List<User> getUsers() { return new ArrayList<>(mUsers); }
	public boolean isPrivate() { return mPrivate; }

	@Override
	public String toString() {
		return "[" + this.getClass().getName() + "] : " + this.getUuid() + " {" + this.getName() + "}";
	}
	public void removeUser(User user) {
		mUsers.removeIf(u -> u.getUuid().equals(user.getUuid()));
		// Si après retrait il ne reste que le créateur ou personne → rester privé
	}

}