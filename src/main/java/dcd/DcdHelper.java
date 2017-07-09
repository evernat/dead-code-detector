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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.objectweb.asm.Type;

/**
 * Classe utilitaire pour le package dcd.
 * @author evernat
 */
final class DcdHelper {
	/**
	 * Nom et copyright de DCD.
	 */
	static final String APPLICATION_NAME = "Dead Code Detector, Copyright 2008-2017 by Emeric Vernat";
	private static final char METHOD_SEPARATOR = ' ';
	private static final int VISIBILITY_PUBLIC_OR_PROTECTED = Modifier.PUBLIC | Modifier.PROTECTED;

	private DcdHelper() {
		super();
	}

	static boolean isJavaClass(String asmClassName) {
		// si commence par java/, javax/ ou org/xml/sax/ (packages java.* ou javax.*
		// ou org.xml.sax.* et pas javassist.*) alors c'est une classe du core java
		return asmClassName.startsWith("java/") || asmClassName.startsWith("javax/")
				|| asmClassName.startsWith("org/xml/sax/");
	}

	static Set<String> getJavaMethods(String asmClassName) {
		Class<?> clazz;
		try {
			// TODO serait-il plus performant de lire classe Java et super-classes avec Class.getResourceAsStream puis de parser avec ASM ?
			clazz = Class.forName(Type.getObjectType(asmClassName).getClassName());
		} catch (final Throwable t) { // NOPMD
			final String msg = "[DCD] Can not load "
					+ Type.getObjectType(asmClassName).getClassName();
			log(msg);
			return Collections.emptySet();
		}
		final Set<Class<?>> classes = new HashSet<>();
		while (clazz != null) {
			classes.add(clazz);
			if ((clazz.getModifiers() & Modifier.ABSTRACT) != 0) {
				for (final Class<?> clazz2 : clazz.getInterfaces()) {
					classes.add(clazz2);
					classes.addAll(Arrays.asList(clazz2.getInterfaces()));
				}
			}
			clazz = clazz.getSuperclass();
		}
		final Set<String> methods = new HashSet<>();
		for (final Class<?> clazz2 : classes) {
			for (final Method method : clazz2.getDeclaredMethods()) {
				if ((method.getModifiers() & VISIBILITY_PUBLIC_OR_PROTECTED) != 0) {
					final String methodKey = DcdHelper.getMethodKey(method.getName(),
							Type.getMethodDescriptor(method));
					if (!methods.contains(methodKey)) {
						methods.add(methodKey);
					}
				}
			}
		}
		return methods;
	}

	static String getMethodKey(String name, String desc) {
		return name + METHOD_SEPARATOR + desc;
	}

	static String getMethodName(String methodKey) {
		final int index = methodKey.indexOf(METHOD_SEPARATOR);
		return methodKey.substring(0, index);
	}

	static String getMethodDescription(String methodKey) {
		final int index = methodKey.indexOf(METHOD_SEPARATOR);
		final String methodName = methodKey.substring(0, index);
		final String desc = methodKey.substring(index + 1);
		final Type returnType = Type.getReturnType(desc);
		final Type[] argumentTypes = Type.getArgumentTypes(desc);
		final StringBuilder methodDesc = new StringBuilder();
		final String javalang = "java.lang.";
		if (returnType != Type.VOID_TYPE) {
			String className = returnType.getClassName();
			if (className.startsWith(javalang)) {
				className = className.substring(javalang.length());
			}
			methodDesc.append(className).append(' ');
		}
		methodDesc.append(methodName).append('(');
		for (final Type argumentType : argumentTypes) {
			String className = argumentType.getClassName();
			if (className.startsWith(javalang)) {
				className = className.substring(javalang.length());
			}
			methodDesc.append(className).append(", ");
		}
		if (argumentTypes.length > 0) {
			methodDesc.delete(methodDesc.length() - 2, methodDesc.length());
		}
		methodDesc.append(')');
		return methodDesc.toString();
	}

	static String getFieldDescription(String field) {
		final int index = field.indexOf(METHOD_SEPARATOR);
		return field.substring(0, index);
	}

	static File unzipIntoTempDirectory(File file) throws IOException {
		final int random = new SecureRandom().nextInt();
		final File tmpDirectory = new File(System.getProperty("java.io.tmpdir"), "tmpdcd" + random);
		// on ajoute random au répertoire temporaire pour l'utilisation d'instances en parallèle
		if (!tmpDirectory.exists() && !tmpDirectory.mkdirs()) {
			throw new IOException(tmpDirectory + " can't be created");
		}
		final ZipFile zipFile = new ZipFile(file);
		try {
			final byte[] buffer = new byte[64 * 1024]; // buffer de 64 Ko pour la décompression
			final Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements() && !Thread.currentThread().isInterrupted()) {
				final ZipEntry zipEntry = entries.nextElement();
				final boolean toBeCopied = zipEntry.getName().endsWith(".class")
						|| isViewFile(zipEntry.getName());
				if (toBeCopied && !zipEntry.isDirectory()) {
					final File tmpFile = new File(tmpDirectory, zipEntry.getName());
					if (!tmpFile.getParentFile().exists() && !tmpFile.getParentFile().mkdirs()) {
						throw new IOException(tmpFile.getParentFile() + " can't be created");
					}
					final InputStream input = zipFile.getInputStream(zipEntry);
					final OutputStream output = new BufferedOutputStream(
							new FileOutputStream(tmpFile), (int) zipEntry.getSize());
					try {
						copy(buffer, input, output);
					} finally {
						input.close();
						output.close();
					}
				}
			}
		} finally {
			zipFile.close();
		}
		return tmpDirectory;
	}

	static boolean isViewFile(String fileName) {
		return fileName.endsWith(".xhtml") || fileName.endsWith(".jsp");
	}

	private static void copy(byte[] buffer, InputStream input, OutputStream output)
			throws IOException {
		int read = input.read(buffer);
		while (read != -1) {
			output.write(buffer, 0, read);
			read = input.read(buffer);
		}
	}

	static long getClassTotalSize(File directory, boolean recursive) {
		long result = 0;
		for (final File file : listFiles(directory)) {
			if (recursive && file.isDirectory() && file.getName().indexOf('.') == -1) {
				result += getClassTotalSize(file, recursive);
			} else if (file.getName().endsWith(".class")) {
				result += file.length();
			}
		}
		return result;
	}

	static void rmdir(File directory) {
		for (final File file : listFiles(directory)) {
			if (file.isDirectory()) {
				rmdir(file);
			} else if (!file.delete()) {
				file.deleteOnExit();
			}
		}
		if (!directory.delete()) {
			directory.deleteOnExit();
		}
	}

	static File[] listFiles(File directory) {
		final File[] files = directory.listFiles();
		if (files == null) {
			return new File[] {};
		}
		return files;
	}

	static boolean isJarOrWarFile(File file) {
		return file.getName().endsWith(".jar") || file.getName().endsWith(".war");
	}

	static void log(String msg) {
		// ESCA-JAVA0266:
		System.out.println(msg); // NOPMD
	}
}
