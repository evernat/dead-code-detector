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

import java.util.Iterator;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Classe utilisée pour l'analyse des appels de toString() sur des String dans les méthodes.
 * @author evernat
 */
class StringToStringAnalyzer {
	private static final String STRING_INTERNAL_NAME = Type.getType(String.class).getInternalName();
	private final MethodNode methodNode;

	StringToStringAnalyzer(MethodNode methodNode) {
		super();
		this.methodNode = methodNode;
	}

	@SuppressWarnings("unchecked")
	boolean analyze() {
		for (final Iterator<AbstractInsnNode> it = methodNode.instructions.iterator(); it
				.hasNext();) {
			//CHECKSTYLE:OFF
			final AbstractInsnNode instruction = it.next();
			//CHECKSTYLE:ON
			if (instruction instanceof MethodInsnNode) {
				final MethodInsnNode methodInsnNode = (MethodInsnNode) instruction;
				if (STRING_INTERNAL_NAME.equals(methodInsnNode.owner)
						&& "toString".equals(methodInsnNode.name)) {
					// il n'y a qu'une méthode toString dans la classe String, inutile de vérifier desc
					return true;
				}
			}
		}
		return false;
	}
}
