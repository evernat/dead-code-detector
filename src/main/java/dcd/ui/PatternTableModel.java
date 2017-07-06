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
