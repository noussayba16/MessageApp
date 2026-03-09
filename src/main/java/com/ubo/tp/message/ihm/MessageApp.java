package main.java.com.ubo.tp.message.ihm;

import java.io.File;
import javax.swing.UIManager;

import main.java.com.ubo.tp.message.core.DataManager;
import main.java.com.ubo.tp.message.core.session.Session;

/**
 * Classe principale l'application.
 *
 * @author S.Lucas
 */
public class MessageApp {
	protected Session mSession;

	/**
	 * Base de données.
	 */
	protected DataManager mDataManager;

	/**
	 * Vue principale de l'application.
	 */
	protected MessageAppMainView mMainView;

	/**
	 * Constructeur.
	 *
	 * @param dataManager
	 */
	public MessageApp(DataManager dataManager) {
		this.mDataManager = dataManager;
	}

	/**
	 * Initialisation de l'application.
	 */
	public void init() {
		this.initLookAndFeel();
		this.mSession = new Session();
		this.initGui();

		File defaultDir = new File("exchange");
		if (!defaultDir.exists()) {
			defaultDir.mkdirs();
		}

		mDataManager.setExchangeDirectory(defaultDir.getAbsolutePath());
	}


	/**
	 * Initialisation du look and feel de l'application.
	 */
	protected void initLookAndFeel() {
		try {
			// Utilisation du Look and Feel système pour une meilleure intégration
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			System.err.println("Erreur lors de l'initialisation du Look and Feel : " + e.getMessage());
		}
	}

	/**
	 * Initialisation de l'interface graphique.
	 */
	protected void initGui() {
		this.mMainView = new MessageAppMainView(this);
		this.mSession.addObserver(mMainView);
	}

	/**
	 * Initialisation du répertoire d'échange (depuis la conf ou depuis un file
	 * chooser). <br/>
	 * <b>Le chemin doit obligatoirement avoir été saisi et être valide avant de
	 * pouvoir utiliser l'application</b>
	 */
	protected void initDirectory() {
		// Ne rien faire ici - le répertoire sera choisi via le menu
	}

	/**
	 * Indique si le fichier donné est valide pour servir de répertoire d'échange
	 *
	 * @param directory , Répertoire à tester.
	 */
	protected boolean isValidExchangeDirectory(File directory) {
		// Valide si répertoire disponible en lecture et écriture
		return directory != null && directory.exists() && directory.isDirectory() && directory.canRead()
				&& directory.canWrite();
	}

	/**
	 * Initialisation du répertoire d'échange.
	 *
	 * @param directoryPath
	 */
	protected void initDirectory(String directoryPath) {
		mDataManager.setExchangeDirectory(directoryPath);
	}

	public void show() {
		if (mMainView != null) {
			mMainView.setVisible(true);
		}
	}

	/**
	 * Quitter l'application.
	 */
	public void exit() {
		System.exit(0);
	}

	/**
	 * Obtenir le DataManager.
	 */
	public DataManager getDataManager() {
		return mDataManager;
	}

	public Session getSession() {
		return mSession;
	}

}