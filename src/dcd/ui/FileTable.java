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

import java.awt.Dimension;

import javax.swing.table.TableColumn;

/**
 * Tableau des fichiers à analyser.
 * @author evernat
 */
class FileTable extends Table {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructeur.
	 */
	FileTable() {
		super(new FileTableModel());
		initTable();
	}

	FileTableModel getFileTableModel() {
		return (FileTableModel) getModel();
	}

	private void initTable() {
		final TableColumn tailleColumn = getColumn(FileTableModel.TAILLE_HEADER);
		tailleColumn.setCellRenderer(new IntegerTableCellRenderer());
		tailleColumn.setMaxWidth(60);
		final TableColumn typeIconColumn = getColumn(FileTableModel.TYPE_ICON_HEADER);
		typeIconColumn.setCellRenderer(new IconTableCellRenderer());
		typeIconColumn.setMaxWidth(30);

		setPreferredScrollableViewportSize(new Dimension(530, 150));
	}
}
