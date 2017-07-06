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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.table.AbstractTableModel;

/**
 * Modèle de données des patterns de regexp pour le composant PatternTable.
 * @author evernat
 */
class PatternTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;

	private final List<Pattern> patterns = new ArrayList<Pattern>();
	private final String tableHeader;

	PatternTableModel(String tableHeader) {
		super();
		this.tableHeader = tableHeader;
	}

	void addPatterns(List<Pattern> myPatterns) {
		for (final Pattern pattern : myPatterns) {
			if (!patterns.contains(pattern)) {
				patterns.add(pattern);
			}
		}
		fireTableDataChanged();

	}

	void addPattern(Pattern pattern) {
		if (!patterns.contains(pattern)) {
			patterns.add(pattern);
		}
		fireTableDataChanged();
	}

	void removePatterns(List<Pattern> patternList) {
		patterns.removeAll(patternList);
		fireTableDataChanged();
	}

	/** {@inheritDoc} */
	@Override
	public int getRowCount() {
		return patterns.size();
	}

	/** {@inheritDoc} */
	@Override
	public int getColumnCount() {
		return 1;
	}

	/** {@inheritDoc} */
	@Override
	public String getColumnName(int column) {
		if (column == 0) {
			return tableHeader;
		}
		return "??";
	}

	/** {@inheritDoc} */
	@Override
	public Class<?> getColumnClass(int column) {
		if (column == 0) {
			return Pattern.class;
		}
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		final Pattern pattern = patterns.get(rowIndex);
		if (columnIndex == 0) {
			return pattern.pattern();
		}
		return "??";
	}

	List<Pattern> getPatterns() {
		return patterns;
	}
}
