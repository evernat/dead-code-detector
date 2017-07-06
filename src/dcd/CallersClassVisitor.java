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

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Implémentation de l'interface ClassVisitor d'ASM utilisée lors de l'analyse
 * (parcours des appels de méthodes et d'attributs).
 * @author evernat
 */
class CallersClassVisitor extends ClassVisitor {
	private final MethodVisitor methodVisitor;

	CallersClassVisitor(MethodVisitor methodVisitor) {
		super(Opcodes.ASM4);
		this.methodVisitor = methodVisitor;
	}

	/** {@inheritDoc} */
	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature,
			String[] exceptions) {
		//log(" " + name + desc);
		return methodVisitor;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(int version, int access, String name, String signature, String superName,
			String[] interfaces) {
		//log(name + " extends " + superName + " {");
	}

	/** {@inheritDoc} */
	@Override
	public void visitSource(String source, String debug) {
		// rien
	}

	/** {@inheritDoc} */
	@Override
	public void visitOuterClass(String owner, String name, String desc) {
		// rien
	}

	/** {@inheritDoc} */
	@Override
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public void visitAttribute(Attribute attr) {
		// rien
	}

	/** {@inheritDoc} */
	@Override
	public void visitInnerClass(String name, String outerName, String innerName, int access) {
		// rien
	}

	/** {@inheritDoc} */
	@Override
	public FieldVisitor visitField(int access, String name, String desc, String signature,
			Object value) {
		//log(" " + desc + " " + name);
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public void visitEnd() {
		//log("}");
	}
}
