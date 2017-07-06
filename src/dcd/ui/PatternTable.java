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

/**
 * Tableau pour la gestion des filtres.
 * @author evernat
 */
class PatternTable extends Table {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructeur.
	 * @param tableHeader Titre de la table
	 */
	PatternTable(String tableHeader) {
		super(new PatternTableModel(tableHeader));
	}

	PatternTableModel getPatternTableModel() {
		return (PatternTableModel) getModel();
	}
}
