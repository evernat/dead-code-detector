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

import java.util.LinkedHashSet;
import java.util.Set;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Implémentation de l'interface ClassVisitor d'ASM utilisée lors de l'analyse des initialisations inutiles.
 * @author evernat
 */
class UselessInitClassVisitor extends ClassVisitor {
	final Set<String> possibleFields = new LinkedHashSet<>();
	final Set<String> positiveFields = new LinkedHashSet<>();
	String className;
	boolean lastInstructionIsInitConstant;
	private final UselessInitMethodVisitor methodVisitor = new UselessInitMethodVisitor();

	private class UselessInitMethodVisitor extends MethodVisitor {
		UselessInitMethodVisitor() {
			super(Opcodes.ASM4);
		}

		/** {@inheritDoc} */
		@Override
		public void visitFieldInsn(int opcode, String owner, String name, String desc) {
			if (opcode == Opcodes.PUTFIELD && className.equals(owner)) {
				if (lastInstructionIsInitConstant && possibleFields.contains(name)) {
					positiveFields.add(name);
				}
				possibleFields.remove(name);
				lastInstructionIsInitConstant = false;
			}
		}

		/** {@inheritDoc} */
		@Override
		public void visitInsn(int opcode) {
			// ces opcodes sont ceux possibles pour une initialisation inutile
			lastInstructionIsInitConstant = opcode == Opcodes.ACONST_NULL
					|| opcode == Opcodes.ICONST_0 || opcode == Opcodes.LCONST_0
					|| opcode == Opcodes.FCONST_0 || opcode == Opcodes.DCONST_0;
		}

		/** {@inheritDoc} */
		@Override
		public void visitMethodInsn(int opcode, String owner, String name, String desc,
				boolean itf) {
			lastInstructionIsInitConstant = false;
		}

		/** {@inheritDoc} */
		@Override
		public AnnotationVisitor visitAnnotation(String arg0, boolean arg1) {
			lastInstructionIsInitConstant = false;
			return null;
		}

		/** {@inheritDoc} */
		@Override
		public AnnotationVisitor visitAnnotationDefault() {
			lastInstructionIsInitConstant = false;
			return null;
		}

		/** {@inheritDoc} */
		@Override
		public void visitAttribute(Attribute arg0) {
			lastInstructionIsInitConstant = false;
		}

		/** {@inheritDoc} */
		@Override
		public void visitCode() {
			lastInstructionIsInitConstant = false;
		}

		/** {@inheritDoc} */
		@Override
		public void visitEnd() {
			lastInstructionIsInitConstant = false;
		}

		/** {@inheritDoc} */
		@Override
		public void visitFrame(int arg0, int arg1, Object[] arg2, int arg3, Object[] arg4) {
			lastInstructionIsInitConstant = false;
		}

		/** {@inheritDoc} */
		@Override
		public void visitIincInsn(int arg0, int arg1) {
			lastInstructionIsInitConstant = false;
		}

		/** {@inheritDoc} */
		@Override
		public void visitIntInsn(int arg0, int arg1) {
			lastInstructionIsInitConstant = false;
		}

		/** {@inheritDoc} */
		@Override
		public void visitJumpInsn(int arg0, Label arg1) {
			lastInstructionIsInitConstant = false;
		}

		/** {@inheritDoc} */
		@Override
		public void visitLabel(Label arg0) {
			lastInstructionIsInitConstant = false;
		}

		/** {@inheritDoc} */
		@Override
		public void visitLdcInsn(Object arg0) {
			lastInstructionIsInitConstant = false;
		}

		/** {@inheritDoc} */
		@Override
		public void visitLineNumber(int arg0, Label arg1) {
			lastInstructionIsInitConstant = false;
		}

		/** {@inheritDoc} */
		@Override
		public void visitLocalVariable(String arg0, String arg1, String arg2, Label arg3,
				Label arg4, int arg5) {
			lastInstructionIsInitConstant = false;
		}

		/** {@inheritDoc} */
		@Override
		public void visitLookupSwitchInsn(Label arg0, int[] arg1, Label[] arg2) {
			lastInstructionIsInitConstant = false;
		}

		/** {@inheritDoc} */
		@Override
		public void visitMaxs(int arg0, int arg1) {
			lastInstructionIsInitConstant = false;
		}

		/** {@inheritDoc} */
		@Override
		public void visitMultiANewArrayInsn(String arg0, int arg1) {
			lastInstructionIsInitConstant = false;
		}

		/** {@inheritDoc} */
		@Override
		public AnnotationVisitor visitParameterAnnotation(int arg0, String arg1, boolean arg2) {
			lastInstructionIsInitConstant = false;
			return null;
		}

		/** {@inheritDoc} */
		@Override
		public void visitTableSwitchInsn(int arg0, int arg1, Label arg2, Label... arg3) {
			lastInstructionIsInitConstant = false;
		}

		/** {@inheritDoc} */
		@Override
		public void visitTryCatchBlock(Label arg0, Label arg1, Label arg2, String arg3) {
			lastInstructionIsInitConstant = false;
		}

		/** {@inheritDoc} */
		@Override
		public void visitTypeInsn(int arg0, String arg1) {
			lastInstructionIsInitConstant = false;
		}

		/** {@inheritDoc} */
		@Override
		public void visitVarInsn(int arg0, int arg1) {
			lastInstructionIsInitConstant = false;
		}
	}

	UselessInitClassVisitor() {
		super(Opcodes.ASM4);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(int version, int access, String name, String signature, String superName,
			String[] interfaces) {
		className = name;
		lastInstructionIsInitConstant = false;
		possibleFields.clear();
		positiveFields.clear();
	}

	/** {@inheritDoc} */
	@Override
	public FieldVisitor visitField(int access, String name, String desc, String signature,
			Object value) {
		// si le field est final, il doit être initialisé sinon cela ne compile pas
		// donc on ignore les fields final
		if ((access & Opcodes.ACC_FINAL) == 0) {
			possibleFields.add(name);
		}
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature,
			String[] exceptions) {
		if ("<init>".equals(name)) {
			// c'est un constructeur, on analyse les initialisations dans le code
			return methodVisitor;
		}
		// sinon on n'analyse pas
		return null;
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
	public void visitEnd() {
		// rien
	}
}
