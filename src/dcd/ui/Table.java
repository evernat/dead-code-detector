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

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseEvent;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.text.JTextComponent;

/**
 * Classe héritant de JTable et affichant des tooltips pour les cellules.
 * Les classes filles définiront les implémentations particulières.
 * @author evernat
 */
class Table extends JTable {
	private static final long serialVersionUID = 1L;
	private static final Color BICOLOR_LINE = Color.decode("#E7E7E7");

	/**
	 * Constructeur.
	 * @param model TableModel
	 */
	Table(TableModel model) {
		super(model);
		setAutoCreateRowSorter(true);
	}

	/** {@inheritDoc} */
	@Override
	public java.awt.Component prepareRenderer(TableCellRenderer renderer, int rowIndex,
			int vColIndex) {
		// Surcharge pour la gestion des lignes de couleurs alternées.
		final Component component = super.prepareRenderer(renderer, rowIndex, vColIndex);
		if (!isRowSelected(rowIndex)) {
			if (rowIndex % 2 == 0) {
				component.setBackground(BICOLOR_LINE);
			} else {
				component.setBackground(getBackground());
			}
		}
		return component;
	}

	/**
	 * Retourne le toolTip de la cellule sous la souris.
	 * @return String
	 * @param event java.awt.event.MouseEvent
	 */
	@Override
	public String getToolTipText(MouseEvent event) {
		int row = rowAtPoint(event.getPoint());
		int column = columnAtPoint(event.getPoint());
		if (row == -1 || column == -1) {
			return super.getToolTipText();
		}
		String tip = super.getToolTipText(event);
		if (tip == null) {
			column = convertColumnIndexToModel(column);
			row = convertRowIndexToModel(row);
			tip = getTextAt(row, column);
			if (tip == null || tip.isEmpty()) {
				tip = super.getToolTipText();
			}
		}
		return tip;
	}

	/**
	 * Renvoie le texte à la position demandée.
	 * @return String
	 * @param row int
	 * @param column int
	 */
	private String getTextAt(int row, int column) {
		final Object value = getModel().getValueAt(row, column);

		String text = "";
		if (value != null) {
			final int columnIndex = convertColumnIndexToView(column);
			final TableCellRenderer renderer = getCellRenderer(row, columnIndex);
			final java.awt.Component rendererComponent = renderer.getTableCellRendererComponent(
					this, value, false, false, row, columnIndex);
			text = getTextFrom(value, rendererComponent);
			if (text == null) {
				text = "";
			}
		}
		return text;
	}

	private static String getTextFrom(Object value, java.awt.Component rendererComponent) {
		String text;
		if (rendererComponent instanceof JLabel) {
			text = ((JLabel) rendererComponent).getText();
			if (text == null || text.isEmpty()) {
				text = ((JLabel) rendererComponent).getToolTipText();
			}
		} else if (rendererComponent instanceof JTextComponent) {
			text = ((JTextComponent) rendererComponent).getText();
			if (text == null || text.isEmpty()) {
				text = ((JTextComponent) rendererComponent).getToolTipText();
			}
		} else if (rendererComponent instanceof JCheckBox) {
			text = ((JCheckBox) rendererComponent).isSelected() ? "Yes" : "No";
		} else {
			text = value.toString();
		}
		return text;
	}
}
