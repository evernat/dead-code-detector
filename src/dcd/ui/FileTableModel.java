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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.AbstractTableModel;

/**
 * Modèle de données des fichiers pour le composant FileTable.
 * @author evernat
 */
class FileTableModel extends AbstractTableModel {
	static final String TAILLE_HEADER = "Size (KB)";

	static final String TYPE_ICON_HEADER = "T";

	private static final String DIRECTORY_HEADER = "Directory (or jar, war file)";

	private static final long serialVersionUID = 1L;

	private final List<File> files = new ArrayList<File>();

	void addFiles(List<File> fileList) {
		for (final File file : fileList) {
			if ((file.isDirectory() || file.getName().endsWith(".jar") || file.getName().endsWith(
					".war"))
					&& !files.contains(file)) {
				files.add(file);
			}
		}
		fireTableDataChanged();
	}

	void removeFiles(List<File> fileList) {
		files.removeAll(fileList);
		fireTableDataChanged();
	}

	/** {@inheritDoc} */
	@Override
	public int getRowCount() {
		return files.size();
	}

	/** {@inheritDoc} */
	@Override
	public int getColumnCount() {
		return 3;
	}

	/** {@inheritDoc} */
	@Override
	public String getColumnName(int column) {
		switch (column) {
		case 0:
			return DIRECTORY_HEADER;
		case 1:
			return TYPE_ICON_HEADER;
		case 2:
			return TAILLE_HEADER;
		default:
			return "??";
		}
	}

	/** {@inheritDoc} */
	@Override
	public Class<?> getColumnClass(int column) {
		switch (column) {
		case 0:
			return String.class;
		case 1:
			return Icon.class;
		case 2:
			return Integer.class;
		default:
			return null;
		}
	}

	/** {@inheritDoc} */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		final File file = files.get(rowIndex);
		switch (columnIndex) {
		case 0:
			return file.getPath();
		case 1:
			return FileSystemView.getFileSystemView().getSystemIcon(file);
		case 2:
			return file.isDirectory() ? null : Math.round(file.length() / 1024d);
		default:
			return "??";
		}
	}

	List<File> getFiles() {
		return files;
	}
}
