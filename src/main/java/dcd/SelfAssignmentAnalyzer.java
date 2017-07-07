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

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * Classe utilisée pour l'analyse des self assignment sur les fields dans les méthodes.
 * <br/>Algorithme tiré du guide ASM p100 pour les fields.
 * @author evernat
 */
class SelfAssignmentAnalyzer { // NOPMD
	private final MethodNode methodNode;
	private Set<String> selfAssignments;

	SelfAssignmentAnalyzer(MethodNode methodNode) {
		super();
		this.methodNode = methodNode;
	}

	//CHECKSTYLE:OFF
	@SuppressWarnings("unchecked")
	Set<String> analyze() { // NOPMD
		// la complexité de cette méthode est ignorée
		// car c'est un exemple tiré presque tel quel du guide d'asm
		for (final Iterator<AbstractInsnNode> it = methodNode.instructions.iterator(); it
				.hasNext();) {
			final AbstractInsnNode i1 = it.next();
			if (isALOAD0(i1)) {
				final AbstractInsnNode i2 = getNext(it);
				if (i2 != null && isALOAD0(i2)) {
					AbstractInsnNode i3 = getNext(it);
					while (i3 != null && isALOAD0(i3)) {
						// non nécessaire : i1 = i2;
						// non nécessaire : i2 = i3;
						i3 = getNext(it);
					}
					if (i3 != null && i3.getOpcode() == Opcodes.GETFIELD) {
						final AbstractInsnNode i4 = getNext(it);
						if (i4 != null && i4.getOpcode() == Opcodes.PUTFIELD && sameField(i3, i4)) { // NOPMD
							addSelfAssignment(((FieldInsnNode) i3).name);
						}
					}
				}
			} else if (LocalVariablesAnalyzer.isRead(i1.getOpcode())) {
				final AbstractInsnNode i2 = i1.getNext();
				if (i2 != null && LocalVariablesAnalyzer.isStore(i2.getOpcode())
						&& ((VarInsnNode) i1).var == ((VarInsnNode) i2).var) {
					final LocalVariableNode localVariable = getLocalVariable(
							((VarInsnNode) i1).var);
					addSelfAssignment(localVariable.name);
				}
			}
		}
		if (selfAssignments == null) {
			selfAssignments = Collections.emptySet();
		}
		return selfAssignments;
	}

	private void addSelfAssignment(String selfAssignment) {
		if (selfAssignments == null) {
			selfAssignments = new HashSet<>(1);
		}
		selfAssignments.add(selfAssignment);
	}

	@SuppressWarnings("unchecked")
	private LocalVariableNode getLocalVariable(int index) {
		for (final LocalVariableNode localVariable : (List<LocalVariableNode>) methodNode.localVariables) {
			if (localVariable.index == index) {
				return localVariable;
			}
		}
		throw new IllegalArgumentException("Local variable not found for index " + index);
	}

	@SuppressWarnings("all")
	private static AbstractInsnNode getNext(Iterator<AbstractInsnNode> it) {
		while (it.hasNext()) {
			final AbstractInsnNode in = it.next();
			if (!(in instanceof LineNumberNode)) {
				return in;
			}
		}
		return null;
	}

	private static boolean isALOAD0(AbstractInsnNode i) {
		return i.getOpcode() == Opcodes.ALOAD && ((VarInsnNode) i).var == 0;
	}

	private static boolean sameField(AbstractInsnNode i, AbstractInsnNode j) {
		final FieldInsnNode iField = (FieldInsnNode) i;
		final FieldInsnNode jField = (FieldInsnNode) j;
		return iField.name.equals(jField.name) && iField.owner.equals(jField.owner);
	}
	//CHECKSTYLE:ON
}
