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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

/**
 * Contrôleur pour la gestion des filtres (design pattern MVC).
 * @author evernat
 */
class PatternController implements Serializable {
	private static final long serialVersionUID = 1L;
	private final PatternTable patternTable;
	private final String addMessage;

	PatternController(PatternTable patternTable, String addMessage) {
		super();
		this.patternTable = patternTable;
		this.addMessage = addMessage;
	}

	void actionAddPattern() {
		addPattern("");
	}

	private void addPattern(String initialValue) {
		final String regexp = (String) JOptionPane.showInputDialog(patternTable, addMessage,
				"Add a filter", JOptionPane.PLAIN_MESSAGE, null, null, initialValue);
		// annulé si regexp null
		if (regexp != null) {
			if (regexp.isEmpty()) {
				DcdUiHelper.handleException(new IllegalStateException(
						"Type a regular expression to add id or cancel"), patternTable);
				addPattern("");
			} else {
				try {
					getPatternTableModel().addPattern(Pattern.compile(regexp));
				} catch (final Exception e) {
					DcdUiHelper.handleException(new IllegalStateException(
							"Type a valid regular expression", e), patternTable);
					addPattern(regexp);
				}
			}
		}
	}

	void actionRemovePattern() {
		final List<Pattern> patterns = getPatternTableModel().getPatterns();
		if (patterns.isEmpty()) {
			throw new IllegalStateException("There is nothing to remove.");
		}
		final int[] selectedRows = patternTable.getSelectedRows();
		if (selectedRows.length == 0) {
			throw new IllegalStateException("Select a filter to remove.");
		}
		final List<Pattern> tmp = new ArrayList<Pattern>(selectedRows.length);
		for (final int row : selectedRows) {
			tmp.add(patterns.get(patternTable.convertRowIndexToModel(row)));
		}
		getPatternTableModel().removePatterns(tmp);
	}

	private PatternTableModel getPatternTableModel() {
		return patternTable.getPatternTableModel();
	}
}
