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
