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
 */package dcd.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Window;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

/**
 * Classe utilitaire pour le package dcd.ui.
 * @author evernat
 */
final class DcdUiHelper {
	// ces constantes utilisent le design pattern poids-mouche du GoF (fly-weight)
	static final Border EMPTY_BORDER = BorderFactory.createEmptyBorder(5, 5, 5, 5);
	static final FlowLayout CENTERED_FLOW_LAYOUT = new FlowLayout(FlowLayout.CENTER, 0, 0);
	static final FlowLayout LEADING_FLOW_LAYOUT = new FlowLayout(FlowLayout.LEADING, 0, 0);
	static final Color SEMI_TRANSPARENT_COLOR = new Color(255, 255, 255, 75);

	private DcdUiHelper() {
		super();
	}

	static void handleException(final Exception e, final Component component) {
		printStackTrace(e);
		SwingUtilities.invokeLater(new Runnable() { // NOPMD
					/** {@inheritDoc} */
					@Override
					public void run() {
						final Window window = SwingUtilities.getWindowAncestor(component);
						JOptionPane.showMessageDialog(window, e.getMessage(), "Error",
								JOptionPane.ERROR_MESSAGE);
					}
				});
	}

	static void printStackTrace(final Exception e) {
		// ESCA-JAVA0267:
		e.printStackTrace(System.err);
	}

	static JButton createButton(String text, String imageName, String actionCommand) {
		final JButton button = new JButton();
		button.setOpaque(false);
		button.setHorizontalAlignment(SwingConstants.LEADING);
		if (text != null) {
			button.setText(text);
		}
		if (imageName != null) {
			button.setIcon(DcdUiHelper.createIcon(imageName));
		}
		if (actionCommand != null) {
			button.setActionCommand(actionCommand);
		}
		return button;
	}

	static BorderLayout createBorderLayout() {
		return new BorderLayout();
	}

	static GridLayout createGridLayout(int rows, int cols, int hgap, int vgap) {
		return new GridLayout(rows, cols, hgap, vgap);
	}

	static Icon createIcon(String path) {
		return new ImageIcon(DcdUiHelper.class.getResource(path));
	}

	static void packIfNotMaximized(Window window) {
		// window peut être null lors de l'initialisation
		if (window instanceof Frame
				&& (((Frame) window).getExtendedState() & Frame.MAXIMIZED_BOTH) == 0) {
			window.pack();
		}
	}
}
