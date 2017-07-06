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

import java.text.NumberFormat;

import javax.swing.table.DefaultTableCellRenderer;

/**
 * Définit un renderer pour représenter un Integer dans une JTable.
 * @author Emeric Vernat
 */
class IntegerTableCellRenderer extends DefaultTableCellRenderer {
	private static final long serialVersionUID = 1L;
	private final NumberFormat numberFormat = NumberFormat.getIntegerInstance();

	/**
	 * Constructeur.
	 */
	IntegerTableCellRenderer() {
		super();
		setHorizontalAlignment(RIGHT);
	}

	/**
	 * Cette méthode est appelée par l'affichage de la table pour définir la valeur affichée.
	 * @param value java.lang.Object
	 */
	@Override
	public void setValue(Object value) {
		if (value == null) {
			this.setText(null);
		} else {
			if (value instanceof Integer) {
				this.setText(numberFormat.format(value));
			} else if (value instanceof Number) {

				// Number inclue Integer, Long, BigInteger, Double, Float ...
				this.setText(numberFormat.format(((Number) value).longValue()));
			} else {
				this.setText("??");
			}
		}
	}
}
