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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * Classe utilisée pour l'analyse des variables locales dans les méthodes.
 * @author evernat
 */
class LocalVariablesAnalyzer {
	private static final String UNCHECKED = "unchecked";
	private final MethodNode methodNode;
	private final Set<LocalVariableNode> localVariables;
	private final Map<Object, VarInsnNode> varInstructionsByConstantesMap;

	LocalVariablesAnalyzer(MethodNode methodNode) {
		super();
		this.methodNode = methodNode;
		this.localVariables = extractLocalVariables();
		filterCatchVariables();
		filterDuplicates();
		this.varInstructionsByConstantesMap = this.localVariables.isEmpty() ? null
				: new HashMap<Object, VarInsnNode>();
	}

	@SuppressWarnings(UNCHECKED)
	Set<LocalVariableNode> analyzeMethod() {
		// si seulement 1 variable locale ("this") ou si seulement des "variables locales" pour les paramètres et pour "this",
		// alors on passe à la méthode suivante
		if (localVariables.isEmpty()) {
			return Collections.emptySet();
		}
		for (final Iterator<AbstractInsnNode> it = methodNode.instructions.iterator(); it.hasNext();) {
			analyzeInstruction(it.next());
			if (localVariables.isEmpty()) {
				// si toutes les variables ont été utilisées, inutile de continuer à lire les instructions
				return Collections.emptySet();
			}
		}
		return localVariables;
	}

	@SuppressWarnings(UNCHECKED)
	void analyzeInnerClass(ClassNode innerClass) {
		if (methodNode.name.equals(innerClass.outerMethod)
				&& methodNode.desc.equals(innerClass.outerMethodDesc)) {
			// s'il y a une classe interne créée dans cette méthode
			// utilisant éventuellement une variable finale de cette méthode,
			// alors on cherche les constantes de variables (et uniquement celles-ci) dans toutes ses méthodes
			// (si ce n'est pas une constante, alors elle serait déjà détectée utilisée dans la méthode)
			for (final MethodNode innerMethodNode : (List<MethodNode>) innerClass.methods) {
				for (final Iterator<AbstractInsnNode> it = innerMethodNode.instructions.iterator(); it
						.hasNext();) {
					// CHECKSTYLE:OFF
					final AbstractInsnNode instruction = it.next();
					// CHECKSTYLE:ON
					analyzeConstantInstruction(instruction);
					if (localVariables.isEmpty()) {
						// si toutes les variables ont été utilisées, inutile de continuer à lire les instructions
						return;
					}
				}
			}
		}

	}

	@SuppressWarnings(UNCHECKED)
	private Set<LocalVariableNode> extractLocalVariables() {
		if ((methodNode.access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_SYNTHETIC)) != 0) {
			return Collections.emptySet();
		}
		final int oneIfThisExists = (methodNode.access & Opcodes.ACC_STATIC) != 0 ? 0 : 1;
		if (methodNode.localVariables.size() <= oneIfThisExists
				|| methodNode.localVariables.size() <= Type.getArgumentTypes(methodNode.desc).length
						+ oneIfThisExists) {
			return Collections.emptySet();
		}
		final Set<LocalVariableNode> variables = new LinkedHashSet<LocalVariableNode>(
				methodNode.localVariables);
		// on ignore les variables locales "this" et celles des paramètres
		// (attention les variables ne sont pas forcément dans l'ordre des index, en eclipse 3.1 ou 3.2 ?)
		final int nbParameters = Type.getArgumentTypes(methodNode.desc).length + oneIfThisExists;
		for (final Iterator<LocalVariableNode> it = variables.iterator(); it.hasNext();) {
			final int index = it.next().index;
			if (index < nbParameters) {
				it.remove();
			}
		}
		return variables;
	}

	@SuppressWarnings(UNCHECKED)
	private void filterCatchVariables() {
		// on supprime les variables des blocs catchs (comme eclipse, etc...),
		// avant de supprimer les doublons car les blocs catchs provoquent parfois des variables de même index
		for (final TryCatchBlockNode tryCatchBlock : (List<TryCatchBlockNode>) methodNode.tryCatchBlocks) {
			// TODO est-ce qu'il y a un meilleur moyen d'identifier la variable de l'exception autrement que par son type ?
			final String type = tryCatchBlock.type;
			// type est null si finally
			if (type != null) {
				for (final Iterator<LocalVariableNode> it = localVariables.iterator(); it.hasNext();) {
					final LocalVariableNode localVariable = it.next();
					final Type typeLocalVariable = Type.getType(localVariable.desc);
					if (typeLocalVariable.getSort() == Type.OBJECT
							&& type.equals(typeLocalVariable.getInternalName())) {
						it.remove();
						break;
					}
				}
			}
		}
	}

	private void filterDuplicates() {
		// et on supprime les doublons,
		// qui arrivent avec le code suivant : final String s; if (b) s = "t"; else s = "f";,
		// Rq: du coup on peut avoir des faux négatifs avec le code suivant, mais tant pis :
		// if (b) { Object o = new Object(); test(o); } else { Object o = new Object(); }
		// (attention les variables ne sont pas forcément dans l'ordre des index, en eclipse 3.1 ou 3.2 ?)
		for (final Iterator<LocalVariableNode> it = localVariables.iterator(); it.hasNext();) {
			final LocalVariableNode localVariable = it.next();
			for (final LocalVariableNode localVariable2 : localVariables) {
				if (localVariable.index == localVariable2.index
						&& !localVariable.equals(localVariable2)) {
					it.remove();
					break;
				}
			}
		}
	}

	private void analyzeInstruction(Object instructionObject) {
		// CHECKSTYLE:OFF
		final AbstractInsnNode instruction = (AbstractInsnNode) instructionObject;
		// CHECKSTYLE:ON
		// rq : on ne considère pas une instruction d'incrémentation (opcode IINC, type
		// IincInsnNode) comme une instruction de lecture car elle ne lit pas elle-même la variable,
		// IINC équivaut à une écriture par incrémentation (store) de la variable
		if (isRead(instruction.getOpcode())) {
			// si c'est une lecture de variable, alors la variable est utilisée
			// (une instruction d'opcode xLOAD est forcément de type VarInsnNode)
			removeLocalVariable((VarInsnNode) instruction);
		} else if (isStore(instruction.getOpcode())) {
			if (instruction.getPrevious().getOpcode() == Opcodes.LDC) {
				// si c'est une écriture de variable avec une constante de méthode
				// alors la variable est utilisée si la même constante est lue ensuite
				// (une instruction d'opcode xSTORE est forcément de type VarInsnNode,
				// une instruction d'opcode LDC est forcément de type LdcInsnNode)
				varInstructionsByConstantesMap.put(((LdcInsnNode) instruction.getPrevious()).cst,
						(VarInsnNode) instruction);
			} else if (isSimpleConstant(instruction.getPrevious())) {
				// si c'est une écriture de variable avec une constante telle false, 0 ou 57
				// alors la variable est considérée comme utilisée
				removeLocalVariable((VarInsnNode) instruction);
			}
		} else {
			analyzeConstantInstruction(instruction);
		}
	}

	private void analyzeConstantInstruction(Object instructionObject) {
		// CHECKSTYLE:OFF
		final AbstractInsnNode instruction = (AbstractInsnNode) instructionObject;
		// CHECKSTYLE:ON
		if (instruction.getOpcode() == Opcodes.LDC) {
			// une instruction d'opcode LDC est forcément de type LdcInsnNode
			final VarInsnNode varInstruction = varInstructionsByConstantesMap
					.get(((LdcInsnNode) instruction).cst);
			// varInstruction peut être null si cette constante n'est pas dans une variable
			if (varInstruction != null) {
				removeLocalVariable(varInstruction);
			}
		}
	}

	private void removeLocalVariable(VarInsnNode varInstruction) {
		final int localVariableIndex = varInstruction.var;
		for (final Iterator<LocalVariableNode> it = localVariables.iterator(); it.hasNext();) {
			final LocalVariableNode localVariable = it.next();
			if (localVariable.index == localVariableIndex) {
				it.remove();
				break;
			}
		}
	}

	static boolean isRead(int opcode) {
		return opcode == Opcodes.ALOAD || opcode == Opcodes.ILOAD || opcode == Opcodes.FLOAD
				|| opcode == Opcodes.LLOAD || opcode == Opcodes.DLOAD;
	}

	static boolean isStore(int opcode) {
		return opcode == Opcodes.ASTORE || opcode == Opcodes.ISTORE || opcode == Opcodes.FSTORE
				|| opcode == Opcodes.LSTORE || opcode == Opcodes.DSTORE;
	}

	private static boolean isSimpleConstant(Object previousInstructionObject) {
		// CHECKSTYLE:OFF
		final AbstractInsnNode previousInstruction = (AbstractInsnNode) previousInstructionObject;
		// CHECKSTYLE:ON
		final int opcode = previousInstruction.getOpcode();
		return (opcode >= Opcodes.ACONST_NULL && opcode <= Opcodes.DCONST_1
				|| opcode == Opcodes.SIPUSH || opcode == Opcodes.BIPUSH)
				&& (previousInstruction instanceof InsnNode || previousInstruction instanceof IntInsnNode);
	}
}
