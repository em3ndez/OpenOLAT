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
package org.olat.modules.catalog.launcher;

import static org.olat.modules.catalog.ui.CatalogLauncherRepositoryEntriesController.PREFERED_NUMBER_CARDS;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.catalog.CatalogLauncher;
import org.olat.modules.catalog.CatalogLauncherHandler;
import org.olat.modules.catalog.CatalogRepositoryEntry;
import org.olat.modules.catalog.CatalogRepositoryEntrySearchParams;
import org.olat.modules.catalog.CatalogRepositoryEntrySearchParams.OrderBy;
import org.olat.modules.catalog.CatalogV2Service;
import org.olat.modules.catalog.ui.CatalogLauncherRepositoryEntriesController;
import org.olat.modules.catalog.ui.CatalogV2UIFactory;
import org.olat.modules.catalog.ui.admin.CatalogLauncherBasicEditController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 8 Jun 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class RandomHandler implements CatalogLauncherHandler {
	
	private static final String TYPE = "random";
	
	@Autowired
	private CatalogV2Service catalogService;
	
	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public int getSortOrder() {
		return 900;
	}

	@Override
	public boolean isMultiInstance() {
		return false;
	}

	@Override
	public String getTypeI18nKey() {
		return "launcher.random.type";
	}

	@Override
	public String getAddI18nKey() {
		return "launcher.random.add";
	}

	@Override
	public String getEditI18nKey() {
		return "launcher.random.edit";
	}

	@Override
	public String getDetails(Translator translator, CatalogLauncher catalogLauncher) {
		return null;
	}

	@Override
	public Controller createEditController(UserRequest ureq, WindowControl wControl, CatalogLauncher catalogLauncher) {
		return new CatalogLauncherBasicEditController(ureq, wControl, this, catalogLauncher);
	}

	@Override
	public Controller createRunController(UserRequest ureq, WindowControl wControl, Translator translator,
			CatalogLauncher catalogLauncher, CatalogRepositoryEntrySearchParams defaultSearchParams) {
		CatalogRepositoryEntrySearchParams searchParams = defaultSearchParams.copy();
		searchParams.setOrderBy(OrderBy.random);
		List<CatalogRepositoryEntry> entries = catalogService.getRepositoryEntries(searchParams, 0, PREFERED_NUMBER_CARDS);
		
		String launcherName = CatalogV2UIFactory.translateLauncherName(translator, this, catalogLauncher);
		return new CatalogLauncherRepositoryEntriesController(ureq, wControl, entries, launcherName, false, null);
	}

}