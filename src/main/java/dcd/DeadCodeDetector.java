/*
 * Copyright 2008 by Emeric Vernat
 *
 *     This file is part of Dead Code Detector.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dcd;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InnerClassNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Classe principale de DCD en mode texte avec la méthode main.
 * @author evernat
 */
public class DeadCodeDetector {
	/**
	 * Nom et copyright de DCD.
	 */
	public static final String APPLICATION_NAME = DcdHelper.APPLICATION_NAME;
	private final Parameters parameters;
	private final Map<File, File> tmpDirectoriesByJarOrWar = new HashMap<>();
	private final Report report;
	private final Result result;
	private int suspectCount;
	private int analyzedClassCount;
	private ProgressListener progressListener;
	private long totalSize;
	private long progressSize;
	private int lastPercentOfProgress;
	private Step currentStep;

	/**
	 * Enumération des différentes étapes possibles.
	 */
	private static enum Step {
		/**
		 * Etape 0 : analyse des initialisations inutiles.
		 */
		INIT_ANALYSIS("Analysing init"),
		/**
		 * Etape 1 : analyse des variables locales.
		 */
		LOCAL_ANALYSIS("Analysing local"),
		/**
		 * Etape 2 : indexation et analyse des méthodes et champs private ou package-private.
		 */
		PRIVATE_INDEXATION_AND_ANALYSIS("Analysing private"),
		/**
		 * Etape 3 : indexation des méthodes et champs public ou protected.
		 */
		PUBLIC_INDEXATION("Indexing public"),
		/**
		 * Etape 4 : analyse des méthodes et champs public ou protected.
		 */
		PUBLIC_ANALYSIS("Analysing public");

		private final String desc;

		private Step(String desc) {
			this.desc = desc;
		}

		String getMessage(File file) {
			return desc + " in " + file.getPath() + " ...";
		}
	}

	/**
	 * Constructeur.
	 * @param directory Répertoire à analyser
	 * @throws IOException e
	 * @throws XMLStreamException e
	 */
	public DeadCodeDetector(File directory) throws IOException, XMLStreamException {
		this(Collections.singletonList(directory));
	}

	/**
	 * Constructeur.
	 * @param directories Répertoires (ou fichiers jar, wars) à analyser
	 * @throws IOException e
	 * @throws XMLStreamException e
	 */
	public DeadCodeDetector(List<File> directories) throws IOException, XMLStreamException {
		this(new Parameters(directories, false, true, false, false, null, null));
	}

	/**
	 * Constructeur.
	 * @param parameters Parameters
	 * @throws IOException e
	 * @throws XMLStreamException e
	 */
	public DeadCodeDetector(Parameters parameters) throws IOException, XMLStreamException {
		super();
		this.parameters = parameters;
		report = new Report(parameters.getXmlReportFile());
		result = new Result(report);
	}

	/**
	 * @return Instance de l'interface de publication de l'avancement.
	 */
	public ProgressListener getProgressListener() {
		return progressListener;
	}

	/**
	 * @param progressListener Instance de l'interface de publication de l'avancement.
	 */
	public void setProgressListener(ProgressListener progressListener) {
		this.progressListener = progressListener;
	}

	private void launchAnalyze(String dir) throws IOException, XMLStreamException {
		analyzeDirectory(dir, null);
		if (isPublicIndexationStep()) {
			result.filterJavaMethods();
		}
	}

	private void analyzeDirectory(String dir, String packageName)
			throws IOException, XMLStreamException {
		final Set<String> classNameList = listClassesAndAnalyzeSubDirectories(dir, packageName);

		switch (currentStep) {
		case PRIVATE_INDEXATION_AND_ANALYSIS:
			if (parameters.isPackageExcluded(packageName)) {
				final long tmp = this.progressSize;
				// package exclu, on compte la progression et on passe au suivant
				addProgressSize(dir, packageName);
				// on ajoute la progression une 2ème fois comme si indexation puis analyse
				this.progressSize += this.progressSize - tmp;
				break;
			}

			indexClasses(dir, classNameList);

			analyzeClasses(dir, classNameList);

			suspectCount += result.reportDeadCode(false);

			// après chaque analyse de répertoire on supprime les données inutiles pour ne pas perturber
			// l'analyse du répertoire suivant et pour économiser la mémoire à la fin
			result.clear();
			break;
		case INIT_ANALYSIS:
			if (parameters.isPackageExcluded(packageName)) {
				// package exclu, on compte la progression et on passe au suivant
				addProgressSize(dir, packageName);
				break;
			}
			analyzeClassesForUselessInit(dir, classNameList);
			break;
		case LOCAL_ANALYSIS:
			if (parameters.isPackageExcluded(packageName)) {
				// package exclu, on compte la progression et on passe au suivant
				addProgressSize(dir, packageName);
				break;
			}
			analyzeClassesForLocalDeadCode(dir, classNameList);
			break;
		case PUBLIC_INDEXATION:
			indexClasses(dir, classNameList);
			break;
		case PUBLIC_ANALYSIS:
			analyzeClasses(dir, classNameList);
			break;
		default:
			break;
		}
		countAnalyzedClassesIfNeeded(classNameList.size());
	}

	private void countAnalyzedClassesIfNeeded(int classCount) {
		final boolean noPublic = !parameters.isPublicDeadCode();
		final boolean noPublicOrPrivate = noPublic && !parameters.isPrivateDeadCode();
		final boolean noPublicPrivateOrLocal = noPublicOrPrivate && !parameters.isLocalDeadCode();
		if (currentStep == Step.PUBLIC_ANALYSIS
				|| currentStep == Step.PRIVATE_INDEXATION_AND_ANALYSIS && noPublic
				|| currentStep == Step.LOCAL_ANALYSIS && noPublicOrPrivate
				|| currentStep == Step.INIT_ANALYSIS && noPublicPrivateOrLocal) {
			// si il faut analyser les classes en plusieurs étapes, inutile de compter les classes plusieurs fois
			analyzedClassCount += classCount;
		}
	}

	private Set<String> listClassesAndAnalyzeSubDirectories(String dir, String packageName)
			throws IOException, XMLStreamException {
		final Set<String> classNameList = new LinkedHashSet<>();
		String directory;
		if (packageName == null) {
			directory = dir;
		} else {
			directory = dir + File.separatorChar + packageName.replace('.', File.separatorChar);
		}
		for (final File file : DcdHelper.listFiles(new File(directory))) {
			if (isInterrupted()) {
				break;
			}
			final String name = packageName != null ? packageName + '.' + file.getName()
					: file.getName();
			if (file.isDirectory() && file.getName().indexOf('.') == -1) {
				analyzeDirectory(dir, name);
			} else if (file.getName().endsWith(".class")) {
				classNameList.add(name.substring(0, name.length() - ".class".length()));
			}
		}
		return classNameList;
	}

	private ClassReader createClassReader(String dir, String className) throws IOException {
		final String fileName = dir + File.separatorChar
				+ className.replace('.', File.separatorChar) + ".class";
		final File file = new File(fileName);
		addProgressSize(file);
		return Factory.createClassReader(file);
	}

	private void addProgressSize(String dir, String packageName) {
		final File packageFile = new File(
				dir + File.separatorChar + packageName.replace('.', File.separatorChar));
		addProgressSize(packageFile);
	}

	private void addProgressSize(File file) {
		if (getProgressListener() != null) {
			if (file.isDirectory()) {
				// c'est un package ignoré, on ajoute sa taille à la progression sans être récursif
				progressSize += DcdHelper.getClassTotalSize(file, false);
			} else {
				progressSize += file.length();
			}
			if (lastPercentOfProgress + 2 < 100 * progressSize / totalSize) {
				lastPercentOfProgress = (int) (100 * progressSize / totalSize);
				getProgressListener().onProgress(Math.min(lastPercentOfProgress, 100));
			}
		}
	}

	private void indexClasses(String dir, Set<String> classNameList) throws IOException {
		for (final String className : classNameList) {
			if (isInterrupted()) {
				break;
			}
			final ClassReader classReader = createClassReader(dir, className);
			registerHierarchyOfClass(classReader);
			if (parameters.isClassExcluded(className)) {
				continue;
			}
			final Set<String> methods = new LinkedHashSet<>();
			final Set<String> fields = new LinkedHashSet<>();
			final ClassVisitor classVisitor = Factory.createCalledClassVisitor(methods, fields,
					isPublicIndexationStep());
			classReader.accept(classVisitor,
					ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

			if (isPublicIndexationStep() && !methods.isEmpty()) {
				result.excludeJavaMethods(classReader, methods);
			}

			excludeFilteredMethods(methods);
			excludeFilteredMethods(fields);

			result.registerMethods(classReader.getClassName(), methods);
			result.registerFields(classReader.getClassName(), fields);
		}
	}

	private void excludeFilteredMethods(Set<String> methods) {
		if (!methods.isEmpty() && !parameters.getExcludedMethods().isEmpty()) {
			for (final Iterator<String> it = methods.iterator(); it.hasNext();) {
				final String method = it.next();
				if (parameters.isMethodExcluded(DcdHelper.getMethodName(method))) {
					it.remove();
				}
			}
		}
	}

	private void registerHierarchyOfClass(ClassReader classReader) {
		final String asmClassName = classReader.getClassName();
		final String asmSuperClassName = classReader.getSuperName();
		if (isPublicIndexationStep() || !DcdHelper.isJavaClass(asmSuperClassName)) {
			// les classes java et javax ne sont pas auditées
			result.registerSuperClass(asmSuperClassName, asmClassName);
			result.registerSubClass(asmSuperClassName, asmClassName);
			if (isPublicIndexationStep()) {
				for (final String asmInterfaceName : classReader.getInterfaces()) {
					result.registerSubClass(asmInterfaceName, asmClassName);
				}
			}
		}
	}

	private void analyzeClasses(String dir, Set<String> classesToVisit) throws IOException {
		final ClassVisitor classVisitor = Factory
				.createCallersClassVisitor(result.createCallersMethodVisitor());
		for (final String className : classesToVisit) {
			if (isInterrupted()) {
				break;
			}
			final ClassReader classReader = createClassReader(dir, className);
			classReader.accept(classVisitor, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
		}
	}

	private void analyzeClassesForUselessInit(String dir, Set<String> classesToVisit)
			throws IOException, XMLStreamException {
		final UselessInitClassVisitor uselessInitClassVisitor = Factory
				.createUselessInitClassVisitor();
		for (final String className : classesToVisit) {
			if (isInterrupted()) {
				break;
			}
			final ClassReader classReader = createClassReader(dir, className);
			classReader.accept(uselessInitClassVisitor,
					ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
			final Set<String> positiveFields = uselessInitClassVisitor.positiveFields;
			if (!positiveFields.isEmpty()) {
				// les classes *ServiceLocator générées par Apache Axis
				// contiennent une initialisation "ports" inutile,
				// mais il est inutile de le signaler puisque c'est généré par Axis
				if (className.endsWith("ServiceLocator") && positiveFields.size() == 1
						&& positiveFields.contains("ports")) {
					continue;
				}
				report.reportUselessInitializations(className, positiveFields);
				suspectCount += positiveFields.size();
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void analyzeClassesForLocalDeadCode(String dir, Set<String> classesToVisit)
			throws IOException, XMLStreamException {
		for (final String className : classesToVisit) {
			if (isInterrupted()) {
				break;
			}
			// les classes *SoapBindingStub générées par Apache Axis
			// contiennent beaucoup des variables locales non utilisées,
			// mais il est inutile de le signaler puisque c'est généré par Axis
			if (!className.endsWith("SoapBindingStub")) {
				final ClassNode classNode = new ClassNode();
				final ClassReader classReader = createClassReader(dir, className);
				classReader.accept(classNode, ClassReader.EXPAND_FRAMES);

				for (final MethodNode methodNode : (List<MethodNode>) classNode.methods) {
					analyzeMethodForLocalDeadCode(dir, className, classNode, methodNode);
					analyzeMethodForSelfAssignments(className, methodNode);
					if (Factory.createStringToStringAnalyzer(methodNode).analyze()) {
						report.reportStringToString(className, methodNode);
						suspectCount++;
					}
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void analyzeMethodForLocalDeadCode(String dir, String className, ClassNode classNode,
			MethodNode methodNode) throws IOException, XMLStreamException {
		final LocalVariablesAnalyzer localVariablesAnalyzer = Factory
				.createLocalVariablesAnalyzer(methodNode);
		final Set<LocalVariableNode> localVariables = localVariablesAnalyzer.analyzeMethod();
		if (localVariables.isEmpty()) {
			return;
		}
		// on exclue éventuellement les variables avec les mêmes filtres que les méthodes
		for (final Iterator<LocalVariableNode> it = localVariables.iterator(); it.hasNext();) {
			final LocalVariableNode localVariable = it.next();
			if (parameters.isMethodExcluded(localVariable.name)) {
				it.remove();
			}
		}
		// s'il reste des variables on regarde s'il y a des classes internes à la méthode
		for (final InnerClassNode innerClassNode : (List<InnerClassNode>) classNode.innerClasses) {
			if (innerClassNode.outerName != null
					&& !innerClassNode.outerName.equals(classNode.name)) {
				// des classes internes n'ont parfois pas la même classe externe ???
				// (on ignore car la classe interne n'est alors pas forcément dans le même répertoire)
				continue;
			}
			final ClassNode innerClass = new ClassNode();
			final ClassReader innerClassReader = createClassReader(dir, innerClassNode.name);
			innerClassReader.accept(innerClass, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
			localVariablesAnalyzer.analyzeInnerClass(innerClass);
			if (localVariables.isEmpty()) {
				// si toutes les variables ont été utilisées, inutile de continuer à lire les classes internes
				break;
			}
		}
		if (!localVariables.isEmpty()) {
			report.reportDeadLocalVariables(className, methodNode, localVariables);
			suspectCount += localVariables.size();
		}
	}

	private void analyzeMethodForSelfAssignments(String className, MethodNode methodNode)
			throws XMLStreamException {
		final Set<String> selfAssignments = Factory.createSelfAssignmentAnalyzer(methodNode)
				.analyze();
		if (!selfAssignments.isEmpty()) {
			report.reportSelfAssignments(className, methodNode, selfAssignments);
			suspectCount += selfAssignments.size();
		}
	}

	/**
	 * Méthode exécutée pour lancer l'audit.
	 * @throws IOException e
	 * @throws XMLStreamException e
	 */
	public void run() throws IOException, XMLStreamException {
		final long start = System.currentTimeMillis();
		try {
			totalSize = 0;
			progressSize = 0;
			lastPercentOfProgress = -1;
			for (final File file : parameters.getDirectories()) {
				if (isInterrupted()) {
					break;
				}
				if (DcdHelper.isJarOrWarFile(file)) {
					// décompression du fichier jar ou war

					// TODO Si le JRE utilisé est en v7, on pourrait peut-être éviter de décompresser les jars
					// en utilisant un In-memory filesystem (shrinkwrap nio2 par exemple),
					// http://exitcondition.alrubinger.com/2012/08/17/shrinkwrap-nio2/
					//					final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "myArchive.jar");
					//					final FileSystem fileSystem = ShrinkWrapFileSystems.newFileSystem(archive);

					log("Uncompressing " + file.getPath() + " ...");
					final File tmpDirectory = DcdHelper.unzipIntoTempDirectory(file);
					totalSize += getProgressListener() != null
							? DcdHelper.getClassTotalSize(tmpDirectory, true)
							: 0;
					tmpDirectoriesByJarOrWar.put(file, tmpDirectory);
				} else {
					totalSize += getProgressListener() != null
							? DcdHelper.getClassTotalSize(file, true)
							: 0;
				}
			}
			// les classes étant analysées à chaque étape (2 fois si mode public ou private),
			// hors filtres que l'on ignore et classes internes analysées plusieurs fois si local également,
			// on multiplie totalSize par les étapes
			totalSize *= parameters.getSizeMultiplier();

			// c'est parti
			launchStepAnalyses();
		} finally {
			if (isInterrupted()) {
				log("Interrupted");
			}
			// après l'analyse on supprime les données inutiles
			// pour économiser la mémoire même s'il y a une exception
			result.clear();

			// on supprime les répertoires temporaires
			if (!tmpDirectoriesByJarOrWar.isEmpty()) {
				log("Deleting temporary directories");
				for (final File tmpDirectory : tmpDirectoriesByJarOrWar.values()) {
					DcdHelper.rmdir(tmpDirectory);
				}
				tmpDirectoriesByJarOrWar.clear();
			}

			// et on termine le rapport (résumé et fermeture flux xml)
			final long end = System.currentTimeMillis();
			report.close(end - start, suspectCount, analyzedClassCount,
					parameters.getXmlReportFile());
		}
	}

	private void launchStepAnalyses() throws IOException, XMLStreamException {
		if (parameters.isInitDeadCode()) {
			currentStep = Step.INIT_ANALYSIS;
			launchAllAnalyses();
		}
		if (parameters.isLocalDeadCode()) {
			currentStep = Step.LOCAL_ANALYSIS;
			launchAllAnalyses();
		}
		if (parameters.isPrivateDeadCode()) {
			currentStep = Step.PRIVATE_INDEXATION_AND_ANALYSIS;
			launchAllAnalyses();
		}
		if (parameters.isPublicDeadCode()) {
			currentStep = Step.PUBLIC_INDEXATION;
			launchAllAnalyses();
			currentStep = Step.PUBLIC_ANALYSIS;
			launchAllAnalyses();
			// si mode public, rapport à la fin de l'analyse du domaine
			log("");
			suspectCount += result.reportDeadCode(true);
		}
	}

	private void launchAllAnalyses() throws IOException, XMLStreamException {
		for (final File file : parameters.getDirectories()) {
			if (isInterrupted()) {
				break;
			}
			log(currentStep.getMessage(file));
			if (DcdHelper.isJarOrWarFile(file)) {
				// analyse du fichier jar ou war
				final File tmpDirectory = tmpDirectoriesByJarOrWar.get(file);
				if (file.getName().endsWith(".war")) {
					final String webInfClasses = File.separatorChar + "WEB-INF" + File.separatorChar
							+ "classes";
					if (new File(tmpDirectory, webInfClasses).exists()) {
						// analyse des classes du répertoire WEB-INF/classes du war
						launchAnalyze(tmpDirectory.getPath() + webInfClasses);
					}
				} else {
					// analyse des classes du jar
					launchAnalyze(tmpDirectory.getPath());
				}
			} else {
				// analyse des classes du répertoire
				launchAnalyze(file.getPath());
			}
		}
	}

	private boolean isPublicIndexationStep() {
		return currentStep == Step.PUBLIC_INDEXATION;
	}

	private static boolean isInterrupted() {
		return Thread.currentThread().isInterrupted();
	}

	private static void log(String msg) {
		DcdHelper.log(msg);
	}

	// ESCA-JAVA0139:
	/**
	 * Méthode exécutée pour lancer l'audit.
	 * @param args String[]
	 * @throws IOException e
	 * @throws XMLStreamException e
	 */
	public static void main(String[] args) throws IOException, XMLStreamException {
		log(APPLICATION_NAME);
		if (System.getProperty("java.version").compareTo("1.6") < 0) {
			log("You must use a JRE version >= 1.6. Download it from http://java.com");
			return;
		}
		try {
			Class.forName("org.objectweb.asm.ClassReader");
		} catch (final ClassNotFoundException e) {
			log("ASM classes not found. Add the asm and asm-tree jar files in the classpath and run again.");
			return;
		}
		final Parameters parameters;
		if (System.getProperty(Parameters.DIRECTORIES_KEY) != null) {
			parameters = Parameters.createFromSystemProperties();
		} else {
			if (args.length < 1) {
				log("Argument required: configuration file in properties format (key=value) containing:");
				log("directories = directory, or list of directories, containing classes to analyze");
				log("excludedClasses = [optional] regexp, or list of regexp, of classes to exclude (ex : mypackage.*)");
				log("excludedMethods = [optional] regexp, or list of regexp, of methods or fields to exclude (ex : get.*)");
				log("privateDeadCode = [optional] detect private and package-private dead code (true by default)");
				log("publicDeadCode = [optional] detect also public and protected dead code (false by default)");
				log("localDeadCode = [optional] detect also dead local variables (and self assignments and toString on String, false by default)");
				log("initDeadCode = [optional] detect also useless initializations (false by default)");
				log("xmlReportFile = [optional] report to a file in xml format and not to standard output (standard output and no xml by default)");
				return;
			}
			parameters = Parameters.createFromPropertiesFile(new File(args[0]));
		}
		new DeadCodeDetector(parameters).run();
	}
}
