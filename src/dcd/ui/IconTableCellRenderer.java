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
