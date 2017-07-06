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
		for (final Iterator<AbstractInsnNode> it = methodNode.instructions.iterator(); it.hasNext();) {
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
