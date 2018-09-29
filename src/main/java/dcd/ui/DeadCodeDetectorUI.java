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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import dcd.DeadCodeDetector;
import dcd.Parameters;

/**
 * Classe principale de DCD en mode graphique avec la méthode main.
 * @author evernat
 */
public class DeadCodeDetectorUI extends JPanel {
	private static final long serialVersionUID = 1L;
	private static final Color GRADIENT_COLOR = new Color(166, 202, 240);
	final ParametersPanel parametersPanel;

	/**
	 * Constructeur du panel.
	 * Ce constructeur peut être utilisé à la place de main
	 * pour instancier ce panel et le placer dans une JFrame à afficher.
	 * @param parameters Parameters
	 */
	public DeadCodeDetectorUI(Parameters parameters) {
		super();
		setLayout(new BorderLayout());
		parametersPanel = new ParametersPanel(parameters);
		add(parametersPanel, BorderLayout.CENTER);
	}

	static void showFrame(final File propertiesFile) {
		try {
			if (System.getProperty("java.version").compareTo("1.7") < 0) {
				throw new IllegalStateException(
						"You must use a JRE version >= 1.7. Download it from http://java.com");
			}
			try {
				Class.forName("org.objectweb.asm.ClassReader");
			} catch (final ClassNotFoundException e) {
				throw new IllegalStateException(
						"ASM classes not found. Add the asm jar file in the classpath and run again.",
						e);
			}

			// look and feel
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

			Parameters parameters;
			try {
				if (System.getProperty(Parameters.DIRECTORIES_KEY) != null) {
					parameters = Parameters.createFromSystemProperties();
				} else if (propertiesFile != null) {
					parameters = Parameters.createFromPropertiesFile(propertiesFile);
				} else {
					parameters = Parameters.createFromUserPreferences();
				}
			} catch (final Exception e) {
				DcdUiHelper.printStackTrace(e);
				JOptionPane.showMessageDialog(null, e.getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
				parameters = null;
			}
			// instanciation après look and feel
			final DeadCodeDetectorUI ui = new DeadCodeDetectorUI(parameters);
			final JFrame frame = new JFrame(DeadCodeDetector.APPLICATION_NAME);
			frame.add(ui);
			frame.pack();
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.addWindowListener(new WindowAdapter() {
				/** {@inheritDoc} */
				@Override
				public void windowClosing(WindowEvent e) {
					ui.parametersPanel.getController().actionCancel();
					if (propertiesFile == null) {
						try {
							ui.parametersPanel.createParameters().writeToUserPreferences();
						} catch (final Exception ex) {
							DcdUiHelper.printStackTrace(ex);
						}
					}
				}
			});
			final Dimension dimension = frame.getToolkit().getScreenSize();
			frame.setLocation(dimension.width / 2 - frame.getWidth() / 2,
					dimension.height / 2 - frame.getHeight() / 2);
			frame.setVisible(true);
		} catch (final Exception e) {
			DcdUiHelper.printStackTrace(e);
			JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	/** {@inheritDoc} */
	@Override
	protected void paintComponent(Graphics graphics) {
		// Surchargée pour dessiner le fond avec gradient
		final LookAndFeel lookAndFeel = UIManager.getLookAndFeel();
		final String lafName = lookAndFeel != null ? lookAndFeel.getName() : null;
		if ("Substance".equals(lafName)) {
			super.paintComponent(graphics); // le gradient fonctionne mal en substance ?
		}

		final Color startColor = getBackground();
		final Color endColor = GRADIENT_COLOR;
		final int w = getWidth();
		final int h = getHeight();

		// l'image du gradient pourrait être mise en cache, mais ce n'est pas grave
		final Paint paint = new GradientPaint(0, h / 2f, startColor, 1, h, endColor, false);
		final Graphics2D graphix = (Graphics2D) graphics.create();
		graphix.setPaint(paint);
		graphix.fillRect(0, 0, w, h);
		graphix.dispose();
	}

	/**
	 * Méthode exécutée pour lancer l'IHM.
	 * @param args String[]
	 */
	public static void main(final String[] args) {
		SwingUtilities.invokeLater(new Runnable() { // NOPMD
			/** {@inheritDoc} */
			@Override
			public void run() {
				final File propertiesFile;
				if (args.length > 0) {
					propertiesFile = new File(args[0]);
				} else {
					propertiesFile = null;
				}
				showFrame(propertiesFile);
			}
		});
	}
}
