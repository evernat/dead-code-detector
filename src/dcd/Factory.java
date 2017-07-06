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
