package main.java.com.ubo.tp.message.common;

import java.util.UUID;

import main.java.com.ubo.tp.message.datamodel.User;

/**
 * Classe de contantes de l'appli.
 *
 * @author S.Lucas
 */
public class Constants {
	/**
	 * Extension des fichiers de propriété des User
	 */
	public static final String USER_FILE_EXTENSION = "usr";

	/**
	 * Extension des fichiers de propriété des Message
	 */
	public static final String MESSAGE_FILE_EXTENSION = "msg";

	/**
	 * Extension des fichiers de propriété des Channel
	 */
	public static final String CHANNEL_FILE_EXTENSION = "chn";

	/**
	 * Répertoire des fichiers temporaires du système.
	 */
	public static final String SYSTEM_TMP_DIR = System.getProperty("java.io.tmpdir");

	/**
	 * Séparateur de fichier du système.
	 */
	public static final String SYSTEM_FILE_SEPARATOR = System.getProperty("file.separator");

	/**
	 * Identifiant de l'utilisateur inconnu.
	 */
	public static final UUID UNKNONWN_USER_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

	/**
	 * Clé de configuration pour la sauvegarde du répertoire d'échange.
	 */
	public static final String CONFIGURATION_KEY_EXCHANGE_DIRECTORY = "EXCHANGE_DIRECTORY";

	/**
	 * Clé de configuration pour l'UI
	 */
	public static final String CONFIGURATION_KEY_UI_CLASS_NAME = "UI_CLASS_NAME";

	/**
	 * Clé de configuration pour le mode bouchoné
	 */
	public static final String CONFIGURATION_KEY_MOCK_ENABLED = "MOCK_ENABLED";

	/**
	 * Utilisateur inconnu.
	 */
	public static final User UNKNOWN_USER = new User(Constants.UNKNONWN_USER_UUID, "<Inconnu>", "--", "<Inconnu>");

}
