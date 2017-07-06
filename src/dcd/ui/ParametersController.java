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

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import dcd.DeadCodeDetector;
import dcd.Parameters;
import dcd.ProgressListener;

/**
 * Contrôleur de l'IHM (design pattern Modèle-Vue-Contrôleur).
 * @author evernat
 */
class ParametersController implements ProgressListener, Serializable {
	private static final long serialVersionUID = 1L;
	transient Thread thread; // NOPMD
	final JProgressBar progressBar;
	private final JFileChooser fileChooser = new JFileChooser();
	private final FileTable fileTable;
	private final JTextArea textArea;

	private static class TextAreaOutputStream extends OutputStream {
		JTextArea textArea;

		TextAreaOutputStream(JTextArea textArea) {
			super();
			this.textArea = textArea;
		}

		/** {@inheritDoc} */
		@Override
		public void write(byte[] buf) throws UnsupportedEncodingException {
			append(new String(buf, "UTF-8"));
		}

		/** {@inheritDoc} */
		@Override
		public void write(byte[] buf, int offset, int length) throws UnsupportedEncodingException {
			append(new String(buf, offset, length, "UTF-8"));
		}

		/** {@inheritDoc} */
		@Override
		public void write(int b) {
			append(String.valueOf((char) b));
		}

		private void append(final String string) {
			SwingUtilities.invokeLater(new Runnable() { // NOPMD
						/** {@inheritDoc} */
						@Override
						public void run() {
							textArea.append(string);
							// Make sure the last line is always visible
							textArea.setCaretPosition(textArea.getDocument().getLength());
						}
					});
		}
	}

	ParametersController(FileTable fileTable, JTextArea textArea, JProgressBar progressBar) {
		super();
		this.fileTable = fileTable;
		this.textArea = textArea;
		this.progressBar = progressBar;
		textArea.append(DeadCodeDetector.APPLICATION_NAME + '\n');
		try {
			System.setOut(new PrintStream(new TextAreaOutputStream(textArea), true, "UTF-8"));
		} catch (final UnsupportedEncodingException e) {
			// ne peut pas arriver
			throw new IllegalStateException(e);
		}
		fileChooser.setMultiSelectionEnabled(true);
		fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
	}

	void actionAddDirectory() {
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setFileFilter(fileChooser.getAcceptAllFileFilter());
		final boolean ok = fileChooser.showDialog(fileTable, "Add directory(ies)") == JFileChooser.APPROVE_OPTION;
		if (ok) {
			getFileTableModel().addFiles(Arrays.asList(fileChooser.getSelectedFiles()));
		}
	}

	void actionAddJarOrWarFile() {
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		final FileNameExtensionFilter jarOrWarFileFilter = new FileNameExtensionFilter(
				"Jar or war files", "jar", "war");
		fileChooser.setFileFilter(jarOrWarFileFilter);
		try {
			final boolean ok = fileChooser.showDialog(fileTable, "Add jar or war file(s)") == JFileChooser.APPROVE_OPTION;
			if (ok) {
				getFileTableModel().addFiles(Arrays.asList(fileChooser.getSelectedFiles()));
			}
		} finally {
			fileChooser.removeChoosableFileFilter(jarOrWarFileFilter);
		}
	}

	void actionRemove() {
		final List<File> files = getFileTableModel().getFiles();
		if (files.isEmpty()) {
			throw new IllegalStateException("There is nothing to remove.");
		}
		final int[] selectedRows = fileTable.getSelectedRows();
		if (selectedRows.length == 0) {
			throw new IllegalStateException("Select a directory or file to remove.");
		}
		final List<File> tmp = new ArrayList<File>(selectedRows.length);
		for (final int row : selectedRows) {
			tmp.add(files.get(fileTable.convertRowIndexToModel(row)));
		}
		getFileTableModel().removeFiles(tmp);
	}

	void actionRun(final Parameters parameters) {
		progressBar.setVisible(true);
		onProgress(0);
		final FileTable table = fileTable;
		thread = new Thread("DCD") { // NOPMD
			@Override
			public void run() {
				try {
					final DeadCodeDetector dcd = new DeadCodeDetector(parameters);
					dcd.setProgressListener(ParametersController.this);
					dcd.run();
				} catch (final Exception e) {
					DcdUiHelper.handleException(e, table);
				} finally {
					thread = null;
					SwingUtilities.invokeLater(new Runnable() { // NOPMD
								@Override
								public void run() {
									progressBar.setVisible(false);
								}
							});
				}
			}
		};
		thread.setDaemon(true);
		thread.start();
	}

	/** {@inheritDoc} */
	@Override
	public void onProgress(final int percentOfProgress) {
		SwingUtilities.invokeLater(new Runnable() { // NOPMD
					/** {@inheritDoc} */
					@Override
					public void run() {
						progressBar.setValue(percentOfProgress);
						if (progressBar.isStringPainted()) {
							progressBar.setString(percentOfProgress + " %");
						}
					}
				});
	}

	void actionCancel() {
		progressBar.setVisible(false);

		final Thread myThread = thread; // NOPMD
		if (myThread != null) {
			// isInterrupted sera testée dans DeadCodeDetector
			myThread.interrupt();
			// on attend la fin du thread,
			// notamment dans le cas où l'application est en cours de fermeture
			try {
				myThread.join();
			} catch (final Exception e) {
				DcdUiHelper.printStackTrace(e);
			}
		}
	}

	void actionCopy() {
		final StringSelection stringSelection = new StringSelection(textArea.getText());
		Toolkit.getDefaultToolkit().getSystemClipboard()
				.setContents(stringSelection, stringSelection);
	}

	void actionClear() {
		textArea.setText("");
	}

	FileTableModel getFileTableModel() {
		return fileTable.getFileTableModel();
	}

	FileTransferHandler createFileTransferHandler() {
		return new FileTransferHandler(getFileTableModel());
	}
}
