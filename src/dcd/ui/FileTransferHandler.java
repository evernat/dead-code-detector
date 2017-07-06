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
