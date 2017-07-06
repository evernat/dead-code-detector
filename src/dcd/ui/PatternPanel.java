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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.Border;

/**
 * Vue en JPanel Swing pour la gestion des filtres (design pattern MVC).
 * @author evernat
 */
class PatternPanel extends JPanel {
	private static final Border EMPTY_BORDER = DcdUiHelper.EMPTY_BORDER;
	private static final long serialVersionUID = 1L;

	private final PatternTable patternTable;
	private final JButton addPatternButton = new JButton("Add a filter...");
	private final JButton removePatternButton = new JButton("Remove");
	private final PatternController controller;

	PatternPanel(String tableHeader, String addMessage) {
		super();
		this.patternTable = new PatternTable(tableHeader);
		this.controller = new PatternController(patternTable, addMessage);
		init();
	}

	List<Pattern> getExclusionPatterns() {
		return getPatternTableModel().getPatterns();
	}

	PatternTableModel getPatternTableModel() {
		return patternTable.getPatternTableModel();
	}

	private void init() {
		initButtons();

		setLayout(new BorderLayout());
		setOpaque(false);
		patternTable.setPreferredScrollableViewportSize(new Dimension(150, 70));
		final JScrollPane scrollPane = new JScrollPane(patternTable);
		final JPanel scrollPanePanel = new JPanel(new BorderLayout());
		scrollPanePanel.setBorder(EMPTY_BORDER);
		scrollPanePanel.setOpaque(false);
		scrollPanePanel.add(scrollPane, BorderLayout.CENTER);
		add(scrollPanePanel, BorderLayout.CENTER);

		final JPanel buttonsPanel = new JPanel(new GridLayout(-1, 1, 10, 10));
		buttonsPanel.setBorder(EMPTY_BORDER);
		buttonsPanel.setOpaque(false);
		buttonsPanel.add(addPatternButton);
		buttonsPanel.add(removePatternButton);
		final JPanel buttonsNorthPanel = new JPanel(new BorderLayout());
		buttonsNorthPanel.setOpaque(false);
		buttonsNorthPanel.add(buttonsPanel, BorderLayout.NORTH);
		add(buttonsNorthPanel, BorderLayout.EAST);
	}

	private void initButtons() {
		final Insets margin = new Insets(2, 2, 2, 2);
		addPatternButton.setMargin(margin);
		removePatternButton.setMargin(margin);
		addPatternButton.setIcon(DcdUiHelper.createIcon("/images/add.gif"));
		removePatternButton.setIcon(DcdUiHelper.createIcon("/images/remove.gif"));
		addPatternButton.setHorizontalAlignment(SwingConstants.LEADING);
		removePatternButton.setHorizontalAlignment(SwingConstants.LEADING);
		addPatternButton.setOpaque(false);
		removePatternButton.setOpaque(false);
		final ActionListener actionHandler = new ActionListener() {
			/** {@inheritDoc} */
			@Override
			public void actionPerformed(ActionEvent event) {
				onAction(event);
			}
		};
		addPatternButton.addActionListener(actionHandler);
		removePatternButton.addActionListener(actionHandler);
	}

	final void onAction(ActionEvent event) {
		final Object source = event.getSource();
		try {
			if (source.equals(addPatternButton)) {
				controller.actionAddPattern();
			} else if (source.equals(removePatternButton)) {
				controller.actionRemovePattern();
			}
		} catch (final Exception e) {
			DcdUiHelper.handleException(e, this);
		}
	}

	/**
	 * Pour test.
	 * @param args String[]
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() { // NOPMD
					/** {@inheritDoc} */
					@Override
					public void run() {
						final JFrame frame = new JFrame();
						frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
						frame.add(new PatternPanel("Classes filters",
								"Type a regular expression of classes to exclude (ex : mypackage.*)"));
						frame.pack();
						frame.setVisible(true);
					}
				});
	}
}
