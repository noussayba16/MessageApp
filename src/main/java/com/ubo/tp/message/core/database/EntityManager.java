package main.java.com.ubo.tp.message.core.database;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import main.java.com.ubo.tp.message.common.Constants;
import main.java.com.ubo.tp.message.common.DataFilesManager;
import main.java.com.ubo.tp.message.core.directory.IWatchableDirectoryObserver;
import main.java.com.ubo.tp.message.datamodel.Channel;
import main.java.com.ubo.tp.message.datamodel.Message;
import main.java.com.ubo.tp.message.datamodel.User;

/**
 * Classe de gestion de la mise à jour de la base de données et de génération
 * des fichiers
 *
 * @author S.Lucas
 */
public class EntityManager implements IWatchableDirectoryObserver {

	/**
	 * Base de donnée de l'application.
	 */
	protected final Database mDatabase;

	/**
	 * Gestionnaire de fichier.
	 */
	protected final DataFilesManager mDataFileManager;

	/**
	 * Chemin d'accès au répertoire d'échange.
	 */
	protected String mDirectoryPath;

	/**
	 * Map reliant les UUID aux utilisateurs associés.
	 */
	protected final Map<UUID, User> mUserMap;

	/**
	 * Map reliant les noms de fichiers aux messages associés.
	 */
	protected final Map<String, Message> mMessageFileMap;

	/**
	 * Map reliant les noms de fichiers aux utilisateurs associés.
	 */
	protected final Map<String, User> mUserFileMap;

	/**
	 * Map reliant les noms de fichiers aux canaux associés.
	 */
	protected final Map<String, Channel> mChannelFileMap;

	/**
	 * Constructeur.
	 */
	public EntityManager(Database database) {
		this.mDatabase = database;
		this.mUserMap = new HashMap<>();
		this.mMessageFileMap = new HashMap<>();
		this.mUserFileMap = new HashMap<>();
		this.mChannelFileMap = new HashMap<>();
		this.mDataFileManager = new DataFilesManager();

		// Ajout de l'utilisateur inconnu
		User unknowUser = Constants.UNKNOWN_USER;
		this.mUserMap.put(unknowUser.getUuid(), unknowUser);
		this.mDatabase.addUser(unknowUser);
	}

	@Override
	public void notifyPresentFiles(Set<File> presentFiles) {
		this.notifyNewFiles(presentFiles);
	}

	@Override
	public void notifyNewFiles(Set<File> newFiles) {
		Set<File> userFiles = this.getUserFiles(newFiles);

		for (File userFile : userFiles) {
			User newUser = this.extractUser(userFile);

			if (newUser != null) {
				this.mDatabase.addUser(newUser);
				mUserMap.put(newUser.getUuid(), newUser);
				mUserFileMap.put(userFile.getName(), newUser);
			}
		}

		Set<File> messageFiles = this.getMessageFiles(newFiles);

		for (File messageFile : messageFiles) {
			Message newMessage = this.extractMessage(messageFile);

			if (newMessage != null) {
				this.mDatabase.addMessage(newMessage);
				this.mMessageFileMap.put(messageFile.getName(), newMessage);
			}
		}

		Set<File> channelFiles = this.getChannelFiles(newFiles);

		for (File channelFile : channelFiles) {
			Channel newChannel = this.extractChannel(channelFile);

			if (newChannel != null) {
				this.mDatabase.addChannel(newChannel);
				this.mChannelFileMap.put(channelFile.getName(), newChannel);
			}
		}
	}

	@Override
	public void notifyDeletedFiles(Set<File> deletedFiles) {
		Set<File> userFiles = this.getUserFiles(deletedFiles);

		for (File deletedUserFile : userFiles) {
			User deletedUser = this.mUserFileMap.get(deletedUserFile.getName());

			if (deletedUser != null) {
				this.mDatabase.deleteUser(deletedUser);
				mUserMap.remove(deletedUser.getUuid());
				mUserFileMap.remove(deletedUserFile.getName());
			}
		}

		Set<File> deletedMessageFiles = this.getMessageFiles(deletedFiles);

		for (File deletedMessageFile : deletedMessageFiles) {
			Message deletedMessage = this.mMessageFileMap.get(deletedMessageFile.getName());

			if (deletedMessage != null) {
				this.mDatabase.deleteMessage(deletedMessage);
				mMessageFileMap.remove(deletedMessageFile.getName());
			}
		}

		Set<File> deletedChannelFiles = this.getChanelFiles(deletedFiles);

		for (File deletedChannelFile : deletedChannelFiles) {
			Channel deletedChannel = this.mChannelFileMap.get(deletedChannelFile.getName());

			if (deletedChannel != null) {
				this.mDatabase.deleteChannel(deletedChannel);
				mChannelFileMap.remove(deletedChannelFile.getName());
			}
		}
	}

	@Override
	public void notifyModifiedFiles(Set<File> modifiedFiles) {
		Set<File> userFiles = this.getUserFiles(modifiedFiles);

		for (User modifiedUser : this.extractAllUsers(userFiles)) {
			this.mDatabase.modifiyUser(modifiedUser);
			mUserMap.put(modifiedUser.getUuid(), modifiedUser);
		}

		Set<File> messageFiles = this.getMessageFiles(modifiedFiles);

		for (Message modifiedMessage : this.extractAllMessages(messageFiles)) {
			this.mDatabase.modifiyMessage(modifiedMessage);
		}

		Set<File> channelFiles = this.getChanelFiles(modifiedFiles);

		for (Channel modifiedChannel : this.extractAllChannel(channelFiles)) {
			this.mDatabase.modifiyChannel(modifiedChannel);
		}
	}

	protected Set<Message> extractAllMessages(Set<File> allMessageFiles) {
		Set<Message> allMessages = new HashSet<>();

		for (File messageFile : allMessageFiles) {
			Message message = this.extractMessage(messageFile);

			if (message != null) {
				allMessages.add(message);
			}
		}

		return allMessages;
	}

	protected Set<Channel> extractAllChannel(Set<File> allChannelFiles) {
		Set<Channel> allChannel = new HashSet<>();

		for (File channelFile : allChannelFiles) {
			Channel channel = this.extractChannel(channelFile);

			if (channel != null) {
				allChannel.add(channel);
			}
		}

		return allChannel;
	}

	protected Message extractMessage(File messageFile) {
		return mDataFileManager.readMessage(messageFile, this.mUserMap);
	}

	protected Set<User> extractAllUsers(Set<File> allUserFiles) {
		Set<User> allUsers = new HashSet<>();

		for (File userFile : allUserFiles) {
			User user = this.extractUser(userFile);

			if (user != null) {
				allUsers.add(user);
			}
		}

		return allUsers;
	}

	protected User extractUser(File userFile) {
		return mDataFileManager.readUser(userFile);
	}

	protected Channel extractChannel(File userFile) {
		return mDataFileManager.readChannel(userFile, mUserMap);
	}

	protected Set<File> getUserFiles(Set<File> allFiles) {
		return this.getSpecificFiles(allFiles, Constants.USER_FILE_EXTENSION);
	}

	protected Set<File> getMessageFiles(Set<File> allFiles) {
		return this.getSpecificFiles(allFiles, Constants.MESSAGE_FILE_EXTENSION);
	}

	protected Set<File> getChanelFiles(Set<File> allFiles) {
		return this.getSpecificFiles(allFiles, Constants.CHANNEL_FILE_EXTENSION);
	}

	protected Set<File> getChannelFiles(Set<File> allFiles) {
		return this.getSpecificFiles(allFiles, Constants.CHANNEL_FILE_EXTENSION);
	}

	protected Set<File> getSpecificFiles(Set<File> allFiles, String extension) {
		Set<File> specificFiles = new HashSet<>();

		for (File file : allFiles) {
			if (file.getName().endsWith(extension)) {
				specificFiles.add(file);
			}
		}

		return specificFiles;
	}

	public void setExchangeDirectory(String directoryPath) {
		this.mDirectoryPath = directoryPath;
		this.mDataFileManager.setExchangeDirectory(directoryPath);
	}

	public void writeMessageFile(Message message) {
		if (mDirectoryPath != null) {
			mDataFileManager.writeMessageFile(message);
		} else {
			throw new RuntimeException("Le répertoire d'échange n'est pas configuré !");
		}
	}

	public void writeUserFile(User user) {
		if (mDirectoryPath != null) {
			mDataFileManager.writeUserFile(user);
		} else {
			throw new RuntimeException("Le répertoire d'échange n'est pas configuré !");
		}
	}

	public void writeChannelFile(Channel channel) {
		if (mDirectoryPath != null) {
			mDataFileManager.writeChannelFile(channel);
		} else {
			throw new RuntimeException("Le répertoire d'échange n'est pas configuré !");
		}
	}

	public void deleteChannelFile(Channel channel) {

		if (mDirectoryPath == null) {
			throw new RuntimeException("Le répertoire d'échange n'est pas configuré !");
		}

		if (channel == null) {
			return;
		}

		String fileNameToDelete = null;

		for (Map.Entry<String, Channel> entry : mChannelFileMap.entrySet()) {
			Channel storedChannel = entry.getValue();

			if (storedChannel != null &&
					storedChannel.getUuid().equals(channel.getUuid())) {

				fileNameToDelete = entry.getKey();
				break;
			}
		}

		if (fileNameToDelete == null) {
			fileNameToDelete = channel.getUuid().toString() + "." + Constants.CHANNEL_FILE_EXTENSION;
		}

		File file = new File(mDirectoryPath, fileNameToDelete);

		if (file.exists()) {
			boolean deleted = file.delete();
			System.out.println("[EntityManager] Canal supprimé : " + fileNameToDelete + " -> " + deleted);
		}

		mChannelFileMap.remove(fileNameToDelete);
	}

	public void deleteMessageFile(Message message) {

		if (mDirectoryPath == null) {
			throw new RuntimeException("Le répertoire d'échange n'est pas configuré !");
		}

		if (message == null) {
			return;
		}

		String fileNameToDelete = null;

		for (Map.Entry<String, Message> entry : mMessageFileMap.entrySet()) {
			Message storedMessage = entry.getValue();

			if (storedMessage != null &&
					storedMessage.getUuid().equals(message.getUuid())) {

				fileNameToDelete = entry.getKey();
				break;
			}
		}

		if (fileNameToDelete == null) {
			fileNameToDelete = message.getUuid().toString() + "." + Constants.MESSAGE_FILE_EXTENSION;
		}

		File file = new File(mDirectoryPath, fileNameToDelete);

		if (file.exists()) {
			boolean deleted = file.delete();
			System.out.println("[EntityManager] Message supprimé : " + fileNameToDelete + " -> " + deleted);
		}

		mMessageFileMap.remove(fileNameToDelete);
	}
	public void deleteUserFile(User user) {

		if (mDirectoryPath == null) {
			throw new RuntimeException("Le répertoire d'échange n'est pas configuré !");
		}

		if (user == null) {
			return;
		}

		String fileNameToDelete = null;

		for (Map.Entry<String, User> entry : mUserFileMap.entrySet()) {

			User storedUser = entry.getValue();

			if (storedUser != null &&
					storedUser.getUuid().equals(user.getUuid())) {

				fileNameToDelete = entry.getKey();
				break;
			}
		}

		if (fileNameToDelete == null) {
			fileNameToDelete = user.getUuid().toString() + "." + Constants.USER_FILE_EXTENSION;
		}

		File file = new File(mDirectoryPath, fileNameToDelete);

		if (file.exists()) {
			boolean deleted = file.delete();
			System.out.println("[EntityManager] Utilisateur supprimé : " + fileNameToDelete + " -> " + deleted);
		}

		mUserFileMap.remove(fileNameToDelete);
		mUserMap.remove(user.getUuid());
	}
}