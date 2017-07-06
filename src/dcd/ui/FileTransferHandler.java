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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

/**
 * Classe de gestion du drag and drop de fichiers depuis l'explorateur windows.
 * @author evernat
 */
class FileTransferHandler extends TransferHandler {
	private static final long serialVersionUID = 1L;

	private final FileTableModel fileTableModel;

	/**
	 * Constructeur.
	 * @param fileTableModel FileTableModel.
	 */
	FileTransferHandler(FileTableModel fileTableModel) {
		super();
		this.fileTableModel = fileTableModel;
	}

	/** {@inheritDoc} */
	@Override
	public boolean importData(JComponent comp, Transferable t) {
		if (!t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
			return false;
		}

		try {
			@SuppressWarnings("unchecked")
			final List<File> files = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
			// importation
			fileTableModel.addFiles(files);
			return true;
		} catch (final Exception e) {
			DcdUiHelper.printStackTrace(e);
		}
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
		// We only support file lists
		for (final DataFlavor transferFlavor : transferFlavors) {
			if (!DataFlavor.javaFileListFlavor.equals(transferFlavor)) {
				return false;
			}
		}
		return true;
	}
}
