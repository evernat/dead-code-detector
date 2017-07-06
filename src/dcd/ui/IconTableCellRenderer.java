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
package dcd.ui;

import javax.swing.Icon;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Définit un renderer pour représenter une Icon dans une JTable.
 * @author Emeric Vernat
 */
class IconTableCellRenderer extends DefaultTableCellRenderer {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructeur.
	 */
	IconTableCellRenderer() {
		super();
		setHorizontalAlignment(SwingConstants.CENTER);
	}

	/**
	 * Cette méthode est appelée par l'affichage de la table pour définir la valeur affichée.
	 * @param value java.lang.Object
	 */
	@Override
	protected void setValue(Object value) {
		if (value == null) {
			setIcon(null);
		} else if (value instanceof Icon) {
			setIcon((Icon) value);
		}
	}
}
