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
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.border.Border;

import dcd.Parameters;

/**
 * Vue principale en JPanel Swing pour l'IHM (design pattern Modèle-Vue-Contrôleur).
 * Le modèle est la classe Parameters dans le package dcd.
 * @author evernat
 */
class ParametersPanel extends JPanel {
	private static final Border EMPTY_BORDER = DcdUiHelper.EMPTY_BORDER;
	private static final long serialVersionUID = 1L;
	private final FileTable filesTable = new FileTable();
	private final JCheckBox publicDeadCodeCheckBox = new JCheckBox("public & protected");
	private final JLabel publicWarningLabel = new JLabel(
			DcdUiHelper.createIcon("/images/warning.gif"));
	private final JCheckBox privateDeadCodeCheckBox = new JCheckBox("package-private & private");
	private final JCheckBox localDeadCodeCheckBox = new JCheckBox("local");
	private final JCheckBox initDeadCodeCheckBox = new JCheckBox("init");
	private final JProgressBar progressBar = new JProgressBar(0, 100);
	private final JTextArea textArea = new JTextArea(10, 88);
	private final JPanel resultsPanel = new JPanel();
	private final JButton advancedButton = createButton("Advanced", "/images/down.gif", "advanced");
	private final JPanel advancedPanel = new JPanel(DcdUiHelper.createBorderLayout());
	private final PatternPanel classesPatternPanel = new PatternPanel("Classes filters",
			"Type a regular expression of classes to exclude (ex : mypackage.*)");
	private final PatternPanel methodsPatternPanel = new PatternPanel("Methods & fields filters",
			"Type a regular expression of methods or fields to exclude (ex : get.*)");
	private final JTextField xmlReportFileTextField = new JTextField(50);
	private final ParametersController controller = new ParametersController(filesTable, textArea,
			progressBar);

	ParametersPanel(Parameters parameters) {
		super();
		init();
		if (parameters != null) {
			filesTable.getFileTableModel().addFiles(parameters.getDirectories());
			classesPatternPanel.getPatternTableModel().addPatterns(parameters.getExcludedClasses());
			methodsPatternPanel.getPatternTableModel().addPatterns(parameters.getExcludedMethods());
			if (!parameters.getExcludedClasses().isEmpty()
					|| !parameters.getExcludedMethods().isEmpty()
					|| parameters.getXmlReportFile() != null) {
				hideOrShowAdvancedPanel();
			}
			publicDeadCodeCheckBox.setSelected(parameters.isPublicDeadCode());
			privateDeadCodeCheckBox.setSelected(parameters.isPrivateDeadCode());
			localDeadCodeCheckBox.setSelected(parameters.isLocalDeadCode());
			initDeadCodeCheckBox.setSelected(parameters.isInitDeadCode());
			xmlReportFileTextField.setText(parameters.getXmlReportFile() == null ? "" : parameters
					.getXmlReportFile().getPath());
		}
	}

	ParametersController getController() {
		return controller;
	}

	private void init() {
		initTransferHandler();

		filesTable.addKeyListener(new KeyAdapter() {
			/** {@inheritDoc} */
			@Override
			public void keyPressed(KeyEvent event) {
				final int keyCode = event.getKeyCode();
				onKeyPressed(keyCode);
			}
		});

		setBorder(EMPTY_BORDER);
		setLayout(DcdUiHelper.createBorderLayout());
		setOpaque(false);

		add(getNorthPanel(), BorderLayout.NORTH);

		add(getCenterPanel(), BorderLayout.CENTER);
	}

	final void onKeyPressed(final int keyCode) {
		if (keyCode == KeyEvent.VK_DELETE) {
			controller.actionRemove();
		} else if (keyCode == KeyEvent.VK_INSERT) {
			controller.actionAddDirectory();
		}
	}

	private JPanel getNorthPanel() {
		final JPanel parametersPanel = getParametersPanel();

		final JPanel directoryButtonsPanel = new JPanel(DcdUiHelper.createGridLayout(-1, 1, 10, 10));
		directoryButtonsPanel.setBorder(EMPTY_BORDER);
		final JButton addDirectoryButton = createButton("Add a classes directory...",
				"/images/add.gif", "addDirectory");
		final JButton addJarOrWarButton = createButton("Add a jar or war file...",
				"/images/add.gif", "addJarOrWar");
		final JButton removeButton = createButton("Remove", "/images/remove.gif", "remove");
		directoryButtonsPanel.add(addDirectoryButton);
		directoryButtonsPanel.add(addJarOrWarButton);
		directoryButtonsPanel.add(removeButton);
		directoryButtonsPanel.setOpaque(false);
		final JPanel buttonsPanel = new JPanel(DcdUiHelper.createBorderLayout());
		buttonsPanel.setOpaque(false);
		buttonsPanel.add(directoryButtonsPanel, BorderLayout.NORTH);
		final JPanel advancedButtonPanel = new JPanel(DcdUiHelper.createBorderLayout());
		advancedButtonPanel.setBorder(EMPTY_BORDER);
		advancedButtonPanel.setOpaque(false);
		advancedButtonPanel.add(advancedButton, BorderLayout.CENTER);
		buttonsPanel.add(advancedButtonPanel, BorderLayout.SOUTH);

		final JPanel northPanel = new JPanel(DcdUiHelper.createBorderLayout());
		northPanel.setOpaque(false);
		final JLabel label = new JLabel(
				"<html><b>Add a directory (or list of directories, or jar or war files) containing classes to analyze"
						+ "<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;then choose the type(s) of dead code to detect and click the launch button.");
		label.setFont(label.getFont().deriveFont(label.getFont().getSize2D() * 1.2f));

		final JPanel runPanel = new JPanel(DcdUiHelper.CENTERED_FLOW_LAYOUT);
		runPanel.setBorder(EMPTY_BORDER);
		runPanel.setOpaque(false);
		final JButton runButton = createButton("Detect dead code", "/images/run.gif", "run");
		runButton.setHorizontalAlignment(SwingConstants.CENTER);
		runButton.setFont(runButton.getFont().deriveFont(Font.BOLD));
		runButton.setMnemonic(KeyEvent.VK_D);
		runPanel.add(runButton);

		northPanel.add(label, BorderLayout.NORTH);
		northPanel.add(parametersPanel, BorderLayout.CENTER);
		northPanel.add(buttonsPanel, BorderLayout.EAST);
		northPanel.add(runPanel, BorderLayout.SOUTH);

		return northPanel;
	}

	private JPanel getParametersPanel() {
		final JPanel parametersPanel = new JPanel(DcdUiHelper.createBorderLayout());
		parametersPanel.setBorder(EMPTY_BORDER);
		parametersPanel.setOpaque(false);
		parametersPanel.add(new JScrollPane(filesTable), BorderLayout.NORTH);
		final JPanel modePanel = new JPanel(DcdUiHelper.LEADING_FLOW_LAYOUT);
		modePanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
		privateDeadCodeCheckBox.setSelected(true);
		final String publicWarning = "<html><b>Warning :</b"
				+ "<br/>Public dead code analysis is useless for libraries with a public api for unknown applications."
				+ "<br/>For applications, remember to add all users of classes, including libraries, and to add classes filters for these libraries otherwise you will have a lot of false positives.";
		publicWarningLabel.setText("     ");
		publicWarningLabel.setToolTipText(publicWarning);
		publicWarningLabel.setVisible(false);
		publicDeadCodeCheckBox.setToolTipText(publicWarning);
		publicDeadCodeCheckBox.setOpaque(false);
		publicDeadCodeCheckBox.addItemListener(new ItemListener() {
			/** {@inheritDoc} */
			@Override
			public void itemStateChanged(ItemEvent event) {
				setWarningVisible(event.getStateChange() == ItemEvent.SELECTED);
			}
		});
		privateDeadCodeCheckBox.setOpaque(false);
		localDeadCodeCheckBox.setOpaque(false);
		initDeadCodeCheckBox.setOpaque(false);
		modePanel.add(new JLabel("Dead code types : "));
		modePanel.add(publicDeadCodeCheckBox);
		modePanel.add(publicWarningLabel);
		modePanel.add(privateDeadCodeCheckBox);
		modePanel.add(localDeadCodeCheckBox);
		modePanel.add(initDeadCodeCheckBox);
		modePanel.setOpaque(false);
		parametersPanel.add(modePanel, BorderLayout.CENTER);
		//		advancedPanel.setOpaque(false);
		// fond non transparent mais semi-transparent pour distinguer le bloc "Advanced"
		advancedPanel.setBackground(DcdUiHelper.SEMI_TRANSPARENT_COLOR);
		advancedPanel.setVisible(false);
		final JPanel xmlReportFilePanel = new JPanel(DcdUiHelper.LEADING_FLOW_LAYOUT);
		xmlReportFilePanel.setOpaque(false);
		xmlReportFilePanel.setBorder(EMPTY_BORDER);
		xmlReportFilePanel.add(new JLabel("Xml report file   "));
		xmlReportFilePanel.add(xmlReportFileTextField);
		xmlReportFilePanel.add(new JLabel("   "));

		advancedPanel.add(classesPatternPanel, BorderLayout.WEST);
		advancedPanel.add(methodsPatternPanel, BorderLayout.EAST);
		advancedPanel.add(xmlReportFilePanel, BorderLayout.SOUTH);

		parametersPanel.add(advancedPanel, BorderLayout.SOUTH);
		return parametersPanel;
	}

	private JPanel getCenterPanel() {
		textArea.setEditable(false);
		textArea.setFont(textArea.getFont().deriveFont(textArea.getFont().getSize2D() * 0.9f));
		final JScrollPane scrollPane = new JScrollPane(textArea);
		// scrollbar vertical à always pour éviter d'avoir la scrollbar horizontal
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		final JPanel scrollPanePanel = new JPanel(DcdUiHelper.createBorderLayout());
		scrollPanePanel.setBorder(EMPTY_BORDER);
		scrollPanePanel.setOpaque(false);
		scrollPanePanel.add(scrollPane, BorderLayout.CENTER);
		final JPanel resultsButtonsPanel = new JPanel(DcdUiHelper.createGridLayout(-1, 1, 10, 10));
		resultsButtonsPanel.setBorder(EMPTY_BORDER);
		resultsButtonsPanel.setOpaque(false);
		final JButton cancelButton = createButton("Cancel", "/images/cancel.gif", "cancel");
		final JButton copyButton = createButton("Copy", "/images/copy.gif", "copy");
		copyButton.setMnemonic(KeyEvent.VK_C);
		final JButton clearButton = createButton("Clear", "/images/clear.gif", "clear");
		resultsButtonsPanel.add(cancelButton);
		resultsButtonsPanel.add(copyButton);
		resultsButtonsPanel.add(clearButton);
		resultsButtonsPanel.add(progressBar);
		// progressBar.setStringPainted(true);
		final JPanel resultsButtonsNorthPanel = new JPanel(DcdUiHelper.createBorderLayout());
		resultsButtonsNorthPanel.setOpaque(false);
		resultsButtonsNorthPanel.add(resultsButtonsPanel, BorderLayout.NORTH);
		resultsPanel.setLayout(DcdUiHelper.createBorderLayout());
		resultsPanel.setOpaque(false);
		resultsPanel.setVisible(false);
		resultsPanel.add(scrollPanePanel, BorderLayout.CENTER);
		resultsPanel.add(resultsButtonsNorthPanel, BorderLayout.EAST);

		return resultsPanel;
	}

	private JButton createButton(String text, String imageName, String actionCommand) {
		final JButton button = DcdUiHelper.createButton(text, imageName, actionCommand);
		button.addActionListener(new ActionListener() {
			/** {@inheritDoc} */
			@Override
			public void actionPerformed(ActionEvent event) {
				onAction(event);
			}
		});
		return button;
	}

	private void initTransferHandler() {
		setTransferHandler(controller.createFileTransferHandler());
		filesTable.setTransferHandler(getTransferHandler());
		final Action pasteAction = TransferHandler.getPasteAction();
		getInputMap().put(KeyStroke.getKeyStroke("ctrl V"), pasteAction.getValue(Action.NAME));
		getActionMap().put(pasteAction.getValue(Action.NAME), pasteAction);
	}

	final void onAction(ActionEvent event) {
		final JButton source = (JButton) event.getSource();
		try {
			onAction(source.getActionCommand());
		} catch (final Exception e) {
			DcdUiHelper.handleException(e, this);
		}
	}

	private void onAction(String actionCommand) {
		if ("addDirectory".equals(actionCommand)) {
			controller.actionAddDirectory();
		} else if ("addJarOrWar".equals(actionCommand)) {
			controller.actionAddJarOrWarFile();
		} else if ("remove".equals(actionCommand)) {
			controller.actionRemove();
		} else if ("run".equals(actionCommand)) {
			beforeRun();
			controller.actionRun(createParameters());
		} else if ("cancel".equals(actionCommand)) {
			controller.actionCancel();
		} else if ("copy".equals(actionCommand)) {
			controller.actionCopy();
		} else if ("clear".equals(actionCommand)) {
			controller.actionClear();
		} else if ("advanced".equals(actionCommand)) {
			hideOrShowAdvancedPanel();
		}
	}

	final Parameters createParameters() {
		final Parameters parameters = new Parameters(filesTable.getFileTableModel().getFiles(),
				publicDeadCodeCheckBox.isSelected(), privateDeadCodeCheckBox.isSelected(),
				localDeadCodeCheckBox.isSelected(), initDeadCodeCheckBox.isSelected(),
				classesPatternPanel.getExclusionPatterns(),
				methodsPatternPanel.getExclusionPatterns());
		parameters.setXmlReportFileName(xmlReportFileTextField.getText().trim().isEmpty() ? null
				: xmlReportFileTextField.getText().trim());
		return parameters;
	}

	private void hideOrShowAdvancedPanel() {
		final boolean visible = !advancedPanel.isVisible();
		advancedPanel.setVisible(visible);
		advancedButton.setIcon(DcdUiHelper.createIcon(visible ? "/images/up.gif"
				: "/images/down.gif"));
		DcdUiHelper.packIfNotMaximized(SwingUtilities.getWindowAncestor(resultsPanel));
	}

	private void beforeRun() {
		if (controller.getFileTableModel().getFiles().isEmpty()) {
			throw new IllegalStateException("There is nothing to analyze.");
		}
		if (!publicDeadCodeCheckBox.isSelected() && !privateDeadCodeCheckBox.isSelected()
				&& !localDeadCodeCheckBox.isSelected() && !initDeadCodeCheckBox.isSelected()) {
			throw new IllegalStateException("Choose public, private, local or init dead code type.");
		}
		if (!resultsPanel.isVisible()) {
			resultsPanel.setVisible(true);
			DcdUiHelper.packIfNotMaximized(SwingUtilities.getWindowAncestor(resultsPanel));
		}
	}

	final void setWarningVisible(boolean visible) {
		publicWarningLabel.setVisible(visible);
	}
}
