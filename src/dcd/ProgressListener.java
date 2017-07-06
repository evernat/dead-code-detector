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
package dcd;

/**
 * Interface destinée à être implémentée et déclarée à DeadCodeDetector pour
 * connaître l'avancement de l'analyse.
 * Permet en particulier d'afficher la progression dans l'IHM (UI) sans établir
 * de dépendance directe du package dcd vers le package dcd.ui.
 * @author evernat
 */
public interface ProgressListener {
	/**
	 * Méthode appelée sur un évènement de progression.
	 * @param percentOfProgress int
	 */
	void onProgress(int percentOfProgress);
}
