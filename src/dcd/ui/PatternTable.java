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
