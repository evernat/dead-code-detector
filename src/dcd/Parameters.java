/*
 * Copyright 2008 by Emeric Vernat
 *
 *     This file is part of Dead Code Detector.
 *
 * Dead Code Detector is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Dead Code Detector is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Dead Code Detector.  If not, see <http://www.gnu.org/licenses/>.
 */
package dcd;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;

/**
 * Objet portant tous les paramètres définis par l'IHM ou par un fichier de configuration
 * (ou bien dans les préférences utilisateur pour les valeurs par défaut sans fichier de configuration).
 * @author evernat
 */
public class Parameters {
	/**
	 * Clé du paramètre "directories".
	 */
	public static final String DIRECTORIES_KEY = "directories";
	private static final String EXCLUDED_CLASSES_KEY = "excludedClasses";
	private static final String EXCLUDED_METHODS_KEY = "excludedMethods";
	private static final String PUBLIC_DEAD_CODE_KEY = "publicDeadCode";
	private static final String PRIVATE_DEAD_CODE_KEY = "privateDeadCode";
	private static final String LOCAL_DEAD_CODE_KEY = "localDeadCode";
	private static final String INIT_DEAD_CODE_KEY = "initDeadCode";
	private static final String XML_REPORT_FILE_KEY = "xmlReportFile";
	private static final String SEPARATORS = "[;,]";
	private final List<File> directories;
	private final List<Pattern> excludedClasses;
	private final List<Pattern> excludedMethods;
	private final boolean publicDeadCode;
	private final boolean privateDeadCode;
	private final boolean localDeadCode;
	private final boolean initDeadCode;
	private File xmlReportFile; // can be null

	/**
	 * Constructeur.
	 * @param properties Properties
	 */
	public Parameters(Properties properties) {
		super();
		directories = new ArrayList<File>();
		if (properties.containsKey(DIRECTORIES_KEY)) {
			for (final String s : properties.getProperty(DIRECTORIES_KEY).split(SEPARATORS)) {
				directories.add(new File(s));
			}
		}
		checkDirectories(directories);

		excludedClasses = extractPatterns(properties, EXCLUDED_CLASSES_KEY);

		excludedMethods = extractPatterns(properties, EXCLUDED_METHODS_KEY);

		publicDeadCode = Boolean.valueOf(properties.getProperty(PUBLIC_DEAD_CODE_KEY));
		privateDeadCode = Boolean.valueOf(properties.getProperty(PRIVATE_DEAD_CODE_KEY))
				|| !properties.containsKey(PUBLIC_DEAD_CODE_KEY)
				&& !properties.containsKey(PRIVATE_DEAD_CODE_KEY);
		localDeadCode = Boolean.valueOf(properties.getProperty(LOCAL_DEAD_CODE_KEY));
		initDeadCode = Boolean.valueOf(properties.getProperty(INIT_DEAD_CODE_KEY));
		xmlReportFile = properties.getProperty(XML_REPORT_FILE_KEY) == null
				|| properties.getProperty(XML_REPORT_FILE_KEY).trim().isEmpty() ? null : new File(
				properties.getProperty(XML_REPORT_FILE_KEY));
	}

	/**
	 * Constructeur.
	 * xmlReportFile n'est pas un paramètre du constructeur (reste null par défaut),
	 * mais il y a une méthode setXmlReportFile.
	 * @param directories Répertoires (ou fichiers jar, wars) à analyser
	 * @param publicDeadCode Si détection du code mort public et protected
	 * @param privateDeadCode Si détection du code mort package-private et private
	 * @param localDeadCode Si détection du code mort des variables locales
	 * @param initDeadCode Si détection des initialisations inutiles
	 * @param excludedClasses Expressions régulières de classes (ou packages) à ne pas auditer (ex : mypackage.*)
	 * @param excludedMethods Expressions régulières de méthodes ou attributs à ne pas auditer (ex : get.*)
	 */
	public Parameters(List<File> directories, boolean publicDeadCode, boolean privateDeadCode,
			boolean localDeadCode, boolean initDeadCode, List<Pattern> excludedClasses,
			List<Pattern> excludedMethods) {
		super();
		if (directories == null) {
			throw new IllegalArgumentException("argument files can't be null");
		}
		checkDirectories(directories);
		this.directories = directories;
		this.publicDeadCode = publicDeadCode;
		this.privateDeadCode = privateDeadCode;
		this.localDeadCode = localDeadCode;
		this.initDeadCode = initDeadCode;
		this.excludedClasses = excludedClasses != null ? excludedClasses : Collections
				.<Pattern> emptyList();
		this.excludedMethods = excludedMethods != null ? excludedMethods : Collections
				.<Pattern> emptyList();
	}

	/**
	 * Charge les paramètres depuis les propriétés systèmes (-Dproperty=value).
	 * @return Parameters
	 */
	public static Parameters createFromSystemProperties() {
		final Properties properties = new Properties();
		properties.put(DIRECTORIES_KEY, System.getProperty(DIRECTORIES_KEY));
		properties.put(EXCLUDED_CLASSES_KEY, System.getProperty(EXCLUDED_CLASSES_KEY));
		properties.put(EXCLUDED_METHODS_KEY, System.getProperty(EXCLUDED_METHODS_KEY));
		properties.put(PRIVATE_DEAD_CODE_KEY, System.getProperty(PRIVATE_DEAD_CODE_KEY));
		properties.put(PUBLIC_DEAD_CODE_KEY, System.getProperty(PUBLIC_DEAD_CODE_KEY));
		properties.put(LOCAL_DEAD_CODE_KEY, System.getProperty(LOCAL_DEAD_CODE_KEY));
		properties.put(INIT_DEAD_CODE_KEY, System.getProperty(INIT_DEAD_CODE_KEY));
		properties.put(XML_REPORT_FILE_KEY, System.getProperty(XML_REPORT_FILE_KEY));
		return new Parameters(properties);
	}

	/**
	 * Charge les paramètres depuis un fichier de configuration au format properties.
	 * @param propertiesFile Fichier
	 * @return Parameters
	 * @throws IOException e
	 */
	public static Parameters createFromPropertiesFile(File propertiesFile) throws IOException {
		checkPropertiesFile(propertiesFile);
		final Properties properties = new Properties();
		final InputStream inputStream = new BufferedInputStream(new FileInputStream(propertiesFile));
		try {
			properties.load(inputStream);
		} finally {
			inputStream.close();
		}
		if (!properties.containsKey(DIRECTORIES_KEY)
				|| properties.getProperty(DIRECTORIES_KEY).isEmpty()) {
			throw new IllegalArgumentException(
					"configuration file must have a line with directory, or list of directories, containing classes to analyze : directories=...");
		}
		return new Parameters(properties);
	}

	/**
	 * Charge les paramètres depuis les préférences utilisateur.
	 * @return Parameters
	 * @throws BackingStoreException e
	 */
	public static Parameters createFromUserPreferences() throws BackingStoreException {
		final Properties properties = new Properties();
		final Preferences prefs = Preferences.userNodeForPackage(Parameters.class);
		for (final String key : prefs.keys()) {
			final String value = prefs.get(key, null);
			if (value != null && !value.isEmpty()) {
				properties.put(key, value);
			}
		}
		return new Parameters(properties);
	}

	/**
	 * Ecrit les paramètres dans les préférences utilisateurs.
	 * @throws BackingStoreException e
	 */
	public void writeToUserPreferences() throws BackingStoreException {
		final Preferences prefs = Preferences.userNodeForPackage(Parameters.class);
		final StringBuilder dirs = new StringBuilder();
		for (final File directory : directories) {
			if (dirs.length() != 0) {
				dirs.append(',');
			}
			dirs.append(directory.toString());
		}
		final StringBuilder classes = new StringBuilder();
		for (final Pattern excluded : excludedClasses) {
			if (classes.length() != 0) {
				classes.append(',');
			}
			classes.append(excluded.pattern());
		}
		final StringBuilder methods = new StringBuilder();
		for (final Pattern excluded : excludedMethods) {
			if (methods.length() != 0) {
				methods.append(',');
			}
			methods.append(excluded.pattern());
		}
		prefs.put(DIRECTORIES_KEY, dirs.toString());
		prefs.put(EXCLUDED_CLASSES_KEY, classes.toString());
		prefs.put(EXCLUDED_METHODS_KEY, methods.toString());
		prefs.put(PUBLIC_DEAD_CODE_KEY, Boolean.toString(publicDeadCode));
		prefs.put(PRIVATE_DEAD_CODE_KEY, Boolean.toString(privateDeadCode));
		prefs.put(LOCAL_DEAD_CODE_KEY, Boolean.toString(localDeadCode));
		prefs.put(INIT_DEAD_CODE_KEY, Boolean.toString(initDeadCode));
		prefs.put(XML_REPORT_FILE_KEY, xmlReportFile == null ? "" : xmlReportFile.getPath());
		prefs.flush();
	}

	private static List<Pattern> extractPatterns(Properties properties, String string) {
		final List<Pattern> result = new ArrayList<Pattern>();
		if (properties.containsKey(string) && !properties.getProperty(string).isEmpty()) {
			for (final String s : properties.getProperty(string).split(SEPARATORS)) {
				result.add(Pattern.compile(s.replace('$', '.')));
			}
		}
		return result;
	}

	private static void checkPropertiesFile(File propertiesFile) throws IOException {
		if (!propertiesFile.exists()) {
			throw new IOException(propertiesFile.toString() + " does not exist");
		}
		if (propertiesFile.isDirectory()) {
			throw new IOException(propertiesFile.toString()
					+ " is a directory and not a properties file");
		}
	}

	private static void checkDirectories(List<File> directories) {
		for (final File directory : directories) {
			if (!directory.exists()) {
				throw new IllegalArgumentException(directory + " does not exist");
			}
			if (!directory.isDirectory() && !DcdHelper.isJarOrWarFile(directory)) {
				throw new IllegalArgumentException(directory
						+ " is not a directory or a jar, war file");
			}
		}
	}

	/**
	 * @return Répertoires (ou fichiers jar, wars) à analyser.
	 */
	public List<File> getDirectories() {
		return directories;
	}

	/**
	 * @return Expressions régulières de classes (ou packages) à ne pas auditer (ex : mypackage.*)
	 */
	public List<Pattern> getExcludedClasses() {
		return excludedClasses;
	}

	/**
	 * @return Expressions régulières de méthodes ou attributs à ne pas auditer (ex : get.*)
	 */
	public List<Pattern> getExcludedMethods() {
		return excludedMethods;
	}

	/**
	 * @return Booléen selon que le code mort public doit être détecté
	 */
	public boolean isPublicDeadCode() {
		return publicDeadCode;
	}

	/**
	 * @return Booléen selon que le code mort private doit être détecté
	 */
	public boolean isPrivateDeadCode() {
		return privateDeadCode;
	}

	/**
	 * @return Booléen selon que le code mort des variables locales doit être détecté
	 */
	public boolean isLocalDeadCode() {
		return localDeadCode;
	}

	/**
	 * @return Booléen selon que les initialisations inutiles doivent être détectées
	 */
	public boolean isInitDeadCode() {
		return initDeadCode;
	}

	int getSizeMultiplier() {
		return (isInitDeadCode() ? 1 : 0) + (isLocalDeadCode() ? 1 : 0)
				+ (isPrivateDeadCode() ? 2 : 0) + (isPublicDeadCode() ? 2 : 0);
	}

	/**
	 * @return Fichier pour export xml ou null sinon
	 */
	public File getXmlReportFile() {
		return xmlReportFile;
	}

	/**
	 * @param xmlReporFile Fichier pour export xml ou null sinon
	 */
	public void setXmlReportFile(File xmlReporFile) {
		this.xmlReportFile = xmlReporFile;
	}

	/**
	 * @param xmlReportFileName Nom du fichier pour export xml ou null sinon
	 */
	public void setXmlReportFileName(String xmlReportFileName) {
		setXmlReportFile(xmlReportFileName == null ? null : new File(xmlReportFileName));
	}

	boolean isPackageExcluded(String packageName) {
		return isClassExcluded(packageName != null ? packageName : "");
	}

	boolean isClassExcluded(String className) {
		for (final Pattern pattern : getExcludedClasses()) {
			if (pattern.matcher(className).matches()) {
				// cette classe est exclue des classes auditées
				return true;
			}
		}
		return false;
	}

	boolean isMethodExcluded(String method) {
		for (final Pattern pattern : getExcludedMethods()) {
			if (pattern.matcher(method).matches()) {
				// cette méthode (ou cet attribut) est exclue des méthodes auditées
				return true;
			}
		}
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		// toString pour affichage en debug
		final StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName());
		sb.append('[');
		sb.append("directories=").append(directories).append(", ");
		sb.append("publicDeadCode=").append(publicDeadCode).append(", ");
		sb.append("privateDeadCode=").append(privateDeadCode).append(", ");
		sb.append("localDeadCode=").append(localDeadCode).append(", ");
		sb.append("initDeadCode=").append(initDeadCode).append(", ");
		sb.append("excludedClasses=").append(excludedClasses).append(", ");
		sb.append("excludedMethods=").append(excludedMethods).append(", ");
		sb.append("xmlReportFile=").append(xmlReportFile == null ? null : xmlReportFile.getPath());
		sb.append(']');
		return sb.toString();
	}
}
