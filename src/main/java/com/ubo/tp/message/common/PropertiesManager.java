package main.java.com.ubo.tp.message.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

/**
 * Classe utilitaire de gestion du chargement et de la sauvegarde des fichier
 * properties
 *
 * @author S.Lucas
 */
public class PropertiesManager {

	/**
	 * Chargement d'un fichier de propriétés
	 */
	public static Properties loadProperties(String propertiesFilePath) {
		Properties properties = new Properties();

		// Si le fichier de configuration existe
		if (new File(propertiesFilePath).exists()) {
			try (FileInputStream in = new FileInputStream(propertiesFilePath)) {
				properties.load(in);
			} catch (Throwable t) {
				t.printStackTrace();
				System.out.println("Impossible de charger le fichier de propriétés");
			}
		}

		return properties;
	}

	/**
	 * Ecriture du fichier de propriétés.
	 *
	 * @param properties         propriétés à enregistrer
	 * @param propertiesFilePath Chemin du fichier de propriété à écrire.
	 */
	public static void writeProperties(Properties properties, String propertiesFilePath) {
		if (properties != null) {
			try (FileOutputStream out = new FileOutputStream(propertiesFilePath)) {
				properties.store(out, "");
			} catch (Throwable t) {
				t.printStackTrace();
				System.err.println("Impossible d'enregistrer les propriétés");
			}
		}
	}
}
