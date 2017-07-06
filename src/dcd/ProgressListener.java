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
