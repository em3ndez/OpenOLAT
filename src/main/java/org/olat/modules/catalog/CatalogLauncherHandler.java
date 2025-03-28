/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.catalog;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 8 Jun 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface CatalogLauncherHandler {
	
	public String getType();
	
	public boolean isEnabled();
	
	public int getSortOrder();
	
	public boolean isMultiInstance();

	public String getTypeI18nKey();

	public String getAddI18nKey();

	public String getEditI18nKey();

	public String getDetails(Translator translator, CatalogLauncher catalogLauncher);
	
	public Controller createEditController(UserRequest ureq, WindowControl wControl, CatalogLauncher catalogLauncher);

	public Controller createRunController(UserRequest ureq, WindowControl wControl, Translator translator,
			CatalogLauncher catalogLauncher, List<CatalogEntry> catalogEntries, boolean webPublish);

	/**
	 * Delete the data of a catalog launcher.
	 * 
	 * @param catalogLauncher deleted catalog launcher.
	 */
	public default void deleteLauncherData(CatalogLauncher catalogLauncher) {}
	
}
