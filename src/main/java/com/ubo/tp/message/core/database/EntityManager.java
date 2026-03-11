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

public class EntityManager implements IWatchableDirectoryObserver {

	protected final Database mDatabase;
	protected final DataFilesManager mDataFileManager;
	protected String mDirectoryPath;
	protected final Map<UUID, User> mUserMap;
	protected final Map<String, Message> mMessageFileMap;
	protected final Map<String, User> mUserFileMap;
	protected final Map<String, Channel> mChannelFileMap;

	public EntityManager(Database database) {
		this.mDatabase = database;
		this.mUserMap = new HashMap<>();
		this.mMessageFileMap = new HashMap<>();
		this.mUserFileMap = new HashMap<>();
		this.mChannelFileMap = new HashMap<>();
		this.mDataFileManager = new DataFilesManager();

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

		// ===== USERS =====
		Set<File> userFiles = this.getUserFiles(newFiles);
		for (File userFile : userFiles) {
			User newUser = this.extractUser(userFile);
			if (newUser != null) {
				this.mDatabase.addUser(newUser);
				mUserMap.put(newUser.getUuid(), newUser);
				mUserFileMap.put(userFile.getName(), newUser);
			}
		}

		// ===== MESSAGES =====
		Set<File> messageFiles = this.getMessageFiles(newFiles);
		for (File messageFile : messageFiles) {
			Message newMessage = this.extractMessage(messageFile);
			if (newMessage != null) {
				this.mDatabase.addMessage(newMessage);
				this.mMessageFileMap.put(messageFile.getName(), newMessage);
			}
		}

		// ===== CHANNELS =====
		Set<File> channelFiles = this.getChannelFiles(newFiles);
		for (File channelFile : channelFiles) {
			Channel newChannel = this.extractChannel(channelFile);
			if (newChannel != null) {
				// CORRIGÉ : garantit que le créateur est toujours membre
				newChannel.ensureCreatorIsMember();
				this.mDatabase.addChannel(newChannel);
				this.mChannelFileMap.put(channelFile.getName(), newChannel);
			}
		}
	}

	@Override
	public void notifyDeletedFiles(Set<File> deletedFiles) {

		// ===== USERS =====
		Set<File> userFiles = this.getUserFiles(deletedFiles);
		for (File deletedUserFile : userFiles) {
			User deletedUser = this.mUserFileMap.get(deletedUserFile.getName());
			if (deletedUser != null) {
				this.mDatabase.deleteUser(deletedUser);
				mUserMap.remove(deletedUser.getUuid());
				mUserFileMap.remove(deletedUserFile.getName());
			}
		}

		// ===== MESSAGES =====
		Set<File> deletedMessageFiles = this.getMessageFiles(deletedFiles);
		for (File deletedMessageFile : deletedMessageFiles) {
			Message deletedMessage = this.mMessageFileMap.get(deletedMessageFile.getName());
			if (deletedMessage != null) {
				this.mDatabase.deleteMessage(deletedMessage);
				mMessageFileMap.remove(deletedMessageFile.getName());
			}
		}

		// ===== CHANNELS =====
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

		// ===== USERS =====
		Set<File> userFiles = this.getUserFiles(modifiedFiles);
		for (User modifiedUser : this.extractAllUsers(userFiles)) {
			this.mDatabase.modifiyUser(modifiedUser);
			mUserMap.put(modifiedUser.getUuid(), modifiedUser);
		}

		// ===== MESSAGES =====
		Set<File> messageFiles = this.getMessageFiles(modifiedFiles);
		for (Message modifiedMessage : this.extractAllMessages(messageFiles)) {
			this.mDatabase.modifiyMessage(modifiedMessage);
		}

		// ===== CHANNELS =====
		Set<File> channelFiles = this.getChanelFiles(modifiedFiles);
		for (Channel modifiedChannel : this.extractAllChannel(channelFiles)) {
			// CORRIGÉ : garantit que le créateur est toujours membre
			modifiedChannel.ensureCreatorIsMember();
			this.mDatabase.modifiyChannel(modifiedChannel);
		}
	}

	// ===== EXTRACT =====
	protected Set<Message> extractAllMessages(Set<File> allMessageFiles) {
		Set<Message> allMessages = new HashSet<>();
		for (File messageFile : allMessageFiles) {
			Message message = this.extractMessage(messageFile);
			if (message != null) allMessages.add(message);
		}
		return allMessages;
	}

	protected Set<Channel> extractAllChannel(Set<File> allChannelFiles) {
		Set<Channel> allChannel = new HashSet<>();
		for (File channelFile : allChannelFiles) {
			Channel channel = this.extractChannel(channelFile);
			if (channel != null) allChannel.add(channel);
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
			if (user != null) allUsers.add(user);
		}
		return allUsers;
	}

	protected User extractUser(File userFile) {
		return mDataFileManager.readUser(userFile);
	}

	protected Channel extractChannel(File channelFile) {
		return mDataFileManager.readChannel(channelFile, mUserMap);
	}

	// ===== FILE FILTERS =====
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

	// ===== DIRECTORY =====
	public void setExchangeDirectory(String directoryPath) {
		this.mDirectoryPath = directoryPath;
		this.mDataFileManager.setExchangeDirectory(directoryPath);
	}

	// ===== WRITE =====
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

	// ===== DELETE =====
	public void deleteChannelFile(Channel channel) {
		if (mDirectoryPath == null) {
			throw new RuntimeException("Le répertoire d'échange n'est pas configuré !");
		}
		if (channel == null) return;

		String fileNameToDelete = null;
		for (Map.Entry<String, Channel> entry : mChannelFileMap.entrySet()) {
			if (entry.getValue() != null &&
					entry.getValue().getUuid().equals(channel.getUuid())) {
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
		if (message == null) return;

		String fileNameToDelete = null;
		for (Map.Entry<String, Message> entry : mMessageFileMap.entrySet()) {
			if (entry.getValue() != null &&
					entry.getValue().getUuid().equals(message.getUuid())) {
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
		if (user == null) return;

		String fileNameToDelete = null;
		for (Map.Entry<String, User> entry : mUserFileMap.entrySet()) {
			if (entry.getValue() != null &&
					entry.getValue().getUuid().equals(user.getUuid())) {
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