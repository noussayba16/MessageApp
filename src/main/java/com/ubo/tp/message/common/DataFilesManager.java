package main.java.com.ubo.tp.message.common;

import java.io.File;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import main.java.com.ubo.tp.message.datamodel.Channel;
import main.java.com.ubo.tp.message.datamodel.Message;
import main.java.com.ubo.tp.message.datamodel.User;

public class DataFilesManager {

	protected static final String PROPERTY_KEY_UUID = "UUID";
	protected static final String PROPERTY_KEY_USER_TAG = "Tag";
	protected static final String PROPERTY_KEY_USER_PASSWORD = "This_is_not_the_password";
	protected static final String PROPERTY_KEY_NAME = "Name";
	protected static final String PROPERTY_KEY_MESSAGE_SENDER = "Sender";
	protected static final String PROPERTY_KEY_MESSAGE_RECIPIENT = "Recipient";
	protected static final String PROPERTY_KEY_MESSAGE_DATE = "Date";
	protected static final String PROPERTY_KEY_MESSAGE_TEXT = "Text";
	protected static final String PROPERTY_KEY_CHANNEL_CREATOR = "Creator";
	protected static final String PROPERTY_KEY_CHANNEL_USERS = "Users";
	protected static final String PROPERTY_KEY_CHANNEL_PRIVATE = "Private";
	protected static final String USER_SEPARATOR = ";";

	protected String mDirectoryPath;

	// ===== USER =====
	public User readUser(File userFile) {
		User user = null;

		if (userFile != null
				&& userFile.getName().endsWith(Constants.USER_FILE_EXTENSION)
				&& userFile.exists()) {

			Properties properties = PropertiesManager.loadProperties(userFile.getAbsolutePath());

			String uuid     = properties.getProperty(PROPERTY_KEY_UUID, UUID.randomUUID().toString());
			String tag      = properties.getProperty(PROPERTY_KEY_USER_TAG, "NoTag");
			String password = decrypt(properties.getProperty(PROPERTY_KEY_USER_PASSWORD, "NoPassword"));
			String name     = properties.getProperty(PROPERTY_KEY_NAME, "NoName");

			user = new User(UUID.fromString(uuid), tag, password, name);
		}

		return user;
	}

	public void writeUserFile(User user) {
		Properties properties = new Properties();
		String destFileName = this.getFileName(user.getUuid(), Constants.USER_FILE_EXTENSION);

		properties.setProperty(PROPERTY_KEY_UUID, user.getUuid().toString());
		properties.setProperty(PROPERTY_KEY_USER_TAG, user.getUserTag());
		properties.setProperty(PROPERTY_KEY_USER_PASSWORD, encrypt(user.getUserPassword()));
		properties.setProperty(PROPERTY_KEY_NAME, user.getName());

		PropertiesManager.writeProperties(properties, destFileName);
	}

	// ===== CHANNEL =====
	public void writeChannelFile(Channel channel) {
		Properties properties = new Properties();
		String destFileName = this.getFileName(channel.getUuid(), Constants.CHANNEL_FILE_EXTENSION);

		properties.setProperty(PROPERTY_KEY_UUID, channel.getUuid().toString());
		properties.setProperty(PROPERTY_KEY_NAME, channel.getName());
		properties.setProperty(PROPERTY_KEY_CHANNEL_CREATOR, channel.getCreator().getUuid().toString());

		// CORRIGÉ : sauvegarde les UUIDs des membres (pas leur toString())
		properties.setProperty(PROPERTY_KEY_CHANNEL_USERS, this.getUsersAsString(channel.getUsers()));

		// CORRIGÉ : sauvegarde le flag private pour pouvoir le relire
		properties.setProperty(PROPERTY_KEY_CHANNEL_PRIVATE, String.valueOf(channel.isPrivate()));

		PropertiesManager.writeProperties(properties, destFileName);
	}

	public Channel readChannel(File channelFile, Map<UUID, User> userMap) {
		Channel channel = null;

		if (channelFile != null
				&& channelFile.getName().endsWith(Constants.CHANNEL_FILE_EXTENSION)
				&& channelFile.exists()) {

			Properties properties = PropertiesManager.loadProperties(channelFile.getAbsolutePath());

			String uuid         = properties.getProperty(PROPERTY_KEY_UUID, UUID.randomUUID().toString());
			String channelName  = properties.getProperty(PROPERTY_KEY_NAME, "NoName");
			String channelCreator = properties.getProperty(PROPERTY_KEY_CHANNEL_CREATOR,
					Constants.UNKNONWN_USER_UUID.toString());
			String channelUsers = properties.getProperty(PROPERTY_KEY_CHANNEL_USERS, "");
			boolean isPrivate   = Boolean.parseBoolean(
					properties.getProperty(PROPERTY_KEY_CHANNEL_PRIVATE, "false"));

			User creator = getUserFromUuid(channelCreator, userMap);
			List<User> allUsers = this.getUsersFromString(channelUsers, userMap);

			if (isPrivate) {
				// Canal privé : utilise le constructeur avec liste de membres
				channel = new Channel(UUID.fromString(uuid), creator, channelName, allUsers);
			} else {
				// Canal public
				channel = new Channel(UUID.fromString(uuid), creator, channelName);
			}

			// CORRIGÉ : garantit que le créateur est toujours dans mUsers
			channel.ensureCreatorIsMember();
		}

		return channel;
	}

	// ===== MESSAGE =====
	public Message readMessage(File messageFile, Map<UUID, User> userMap) {
		Message message = null;

		if (messageFile != null
				&& messageFile.getName().endsWith(Constants.MESSAGE_FILE_EXTENSION)
				&& messageFile.exists()) {

			Properties properties = PropertiesManager.loadProperties(messageFile.getAbsolutePath());

			String uuid           = properties.getProperty(PROPERTY_KEY_UUID, UUID.randomUUID().toString());
			String senderUuid     = properties.getProperty(PROPERTY_KEY_MESSAGE_SENDER,
					Constants.UNKNONWN_USER_UUID.toString());
			String recipientUuid  = properties.getProperty(PROPERTY_KEY_MESSAGE_RECIPIENT,
					Constants.UNKNONWN_USER_UUID.toString());
			String emissionDateStr = properties.getProperty(PROPERTY_KEY_MESSAGE_DATE, "0");
			String text           = properties.getProperty(PROPERTY_KEY_MESSAGE_TEXT, "NoText");

			User sender = getUserFromUuid(senderUuid, userMap);
			long emissionDate = Long.valueOf(emissionDateStr);

			message = new Message(UUID.fromString(uuid), sender, UUID.fromString(recipientUuid), emissionDate, text);
		}

		return message;
	}

	public void writeMessageFile(Message message) {
		Properties properties = new Properties();
		String destFileName = this.getFileName(message.getUuid(), Constants.MESSAGE_FILE_EXTENSION);

		properties.setProperty(PROPERTY_KEY_UUID, message.getUuid().toString());
		properties.setProperty(PROPERTY_KEY_MESSAGE_SENDER, message.getSender().getUuid().toString());
		properties.setProperty(PROPERTY_KEY_MESSAGE_RECIPIENT, message.getRecipient().toString());
		properties.setProperty(PROPERTY_KEY_MESSAGE_DATE, String.valueOf(message.getEmissionDate()));
		properties.setProperty(PROPERTY_KEY_MESSAGE_TEXT, message.getText());

		PropertiesManager.writeProperties(properties, destFileName);
	}

	// ===== HELPERS =====
	protected User getUserFromUuid(String uuid, Map<UUID, User> userMap) {
		User user = userMap.get(UUID.fromString(uuid));
		if (user == null) {
			user = userMap.get(Constants.UNKNONWN_USER_UUID);
		}
		return user;
	}

	protected String getFileName(UUID objectUuid, String fileExtension) {
		return mDirectoryPath + Constants.SYSTEM_FILE_SEPARATOR + objectUuid + "." + fileExtension;
	}

	public void setExchangeDirectory(String directoryPath) {
		this.mDirectoryPath = directoryPath;
	}

	/**
	 * CORRIGÉ : sauvegarde les UUID des membres, pas leur toString()
	 */
	protected String getUsersAsString(List<User> users) {
		StringBuilder sb = new StringBuilder();
		Iterator<User> iterator = users.iterator();
		while (iterator.hasNext()) {
			sb.append(iterator.next().getUuid().toString()); // ← était user.toString() = BUG
			if (iterator.hasNext()) {
				sb.append(USER_SEPARATOR);
			}
		}
		return sb.toString();
	}

	protected List<User> getUsersFromString(String users, Map<UUID, User> userMap) {
		List<User> userList = new ArrayList<>();

		if (users == null || users.trim().isEmpty()) {
			return userList;
		}

		String[] splittedUsers = users.split(USER_SEPARATOR);
		for (String userId : splittedUsers) {
			if (!userId.trim().isEmpty()) {
				User user;
				try {
					user = getUserFromUuid(userId.trim(), userMap);
				} catch (Exception e) {
					user = Constants.UNKNOWN_USER;
				}
				userList.add(user);
			}
		}

		return userList;
	}

	public static String encrypt(String data) {
		return Base64.getEncoder().encodeToString(data.getBytes());
	}

	public static String decrypt(String encryptedData) {
		byte[] decodedBytes = Base64.getDecoder().decode(encryptedData);
		return new String(decodedBytes);
	}
}