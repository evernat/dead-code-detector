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
import java.util.Set;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.MethodNode;

/**
 * Design pattern Factory pour instancier des classes (class visitors, analyzers, ...).
 * @author evernat
 */
final class Factory {
	private Factory() {
		super();
	}

	static ClassReader createClassReader(File file) throws IOException {
		final InputStream input = new BufferedInputStream(new FileInputStream(file),
				(int) file.length());
		try {
			return new ClassReader(input);
		} finally {
			input.close();
		}
	}

	static ClassVisitor createCalledClassVisitor(Set<String> methods, Set<String> fields,
			boolean publicIndexation) {
		return new CalledClassVisitor(methods, fields, publicIndexation);
	}

	static ClassVisitor createCallersClassVisitor(MethodVisitor methodVisitor) {
		return new CallersClassVisitor(methodVisitor);
	}

	static UselessInitClassVisitor createUselessInitClassVisitor() {
		return new UselessInitClassVisitor();
	}

	static LocalVariablesAnalyzer createLocalVariablesAnalyzer(MethodNode methodNode) {
		return new LocalVariablesAnalyzer(methodNode);
	}

	static SelfAssignmentAnalyzer createSelfAssignmentAnalyzer(MethodNode methodNode) {
		return new SelfAssignmentAnalyzer(methodNode);
	}

	static StringToStringAnalyzer createStringToStringAnalyzer(MethodNode methodNode) {
		return new StringToStringAnalyzer(methodNode);
	}
}
