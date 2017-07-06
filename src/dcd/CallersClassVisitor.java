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
