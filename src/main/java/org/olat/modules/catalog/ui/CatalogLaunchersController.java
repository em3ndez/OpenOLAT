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
package org.olat.modules.catalog.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Util;
import org.olat.modules.catalog.CatalogEntry;
import org.olat.modules.catalog.CatalogEntrySearchParams;
import org.olat.modules.catalog.CatalogLauncher;
import org.olat.modules.catalog.CatalogLauncherHandler;
import org.olat.modules.catalog.CatalogLauncherSearchParams;
import org.olat.modules.catalog.CatalogSecurityCallback;
import org.olat.modules.catalog.CatalogV2Service;
import org.olat.modules.catalog.launcher.TaxonomyLevelLauncherHandler;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 23 May 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CatalogLaunchersController extends BasicController {
	
	public static final Event OPEN_ADMIN_EVENT = new Event("open.admin");
	public static final Event TAXONOMY_ADMIN_EVENT = new Event("taxonomy.admin");
	
	private Link openAdminLink;
	private Link taxonomyEditLink;
	
	private final VelocityContainer mainVC;
	
	private List<Controller> launcherCtrls;
	
	private final CatalogEntrySearchParams defaultSearchParams;
	private List<CatalogLauncher> launchers;
	private final List<CatalogLauncher> taxonomyLevelCatalogLaunchers = new ArrayList<>(2);

	@Autowired
	private CatalogV2Service catalogService;

	protected CatalogLaunchersController(UserRequest ureq, WindowControl wControl,
			CatalogEntrySearchParams defaultSearchParams, CatalogSecurityCallback secCallback) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(TaxonomyUIFactory.class, getLocale(), getTranslator()));
		this.defaultSearchParams = defaultSearchParams;
		
		mainVC = createVelocityContainer("launchers");
		putInitialPanel(mainVC);
		
		if (secCallback.canEditCatalogAdministration()) {
			openAdminLink = LinkFactory.createLink("open.admin", mainVC, this);
			openAdminLink.setIconLeftCSS("o_icon o_icon_external_link");
		}
		if (secCallback.canEditTaxonomy()) {
			taxonomyEditLink = LinkFactory.createLink("taxonomy.management.open", mainVC, this);
			taxonomyEditLink.setIconLeftCSS("o_icon o_icon_taxonomy");
		}
		
		loadLaunchers();
	}
	
	private void loadLaunchers() {
		cleanUp();
		
		CatalogLauncherSearchParams searchParams = new CatalogLauncherSearchParams();
		if (defaultSearchParams.isWebPublish()) {
			searchParams.setWebEnabled(Boolean.TRUE);
		} else {
			searchParams.setEnabled(Boolean.TRUE);
			searchParams.setLauncherOrganisations(defaultSearchParams.getOfferOrganisations());
		}
		launchers = catalogService.getCatalogLaunchers(searchParams);
		Collections.sort(launchers);
		launcherCtrls = new ArrayList<>(launchers.size());
	}

	public void update(UserRequest ureq, List<CatalogEntry> catalogEntries) {
		if (launchers.isEmpty()) {
			return;
		}
		
		List<String> componentNames = new ArrayList<>(launchers.size());
		mainVC.contextPut("componentNames", componentNames);
		for (CatalogLauncher launcher : launchers) {
			CatalogLauncherHandler handler = catalogService.getCatalogLauncherHandler(launcher.getType());
			if (handler != null && handler.isEnabled()) {
				Controller launcherCtrl = handler.createRunController(ureq, getWindowControl(), getTranslator(),
						launcher, catalogEntries, defaultSearchParams.isWebPublish());
				if (launcherCtrl != null) {
					listenTo(launcherCtrl);
					launcherCtrls.add(launcherCtrl);
					String componentName = "launcher_" + launcher.getKey();
					componentNames.add(componentName);
					mainVC.put(componentName, launcherCtrl.getInitialComponent());
					if (TaxonomyLevelLauncherHandler.TYPE.equals(launcher.getType())) {
						taxonomyLevelCatalogLaunchers.add(launcher);
					}
				}
			}
		}
	}

	private void cleanUp() {
		if (launcherCtrls != null) {
			for (Controller launcherCtrl : launcherCtrls) {
				removeAsListenerAndDispose(launcherCtrl);
			}
		}
	}
	
	public List<CatalogLauncher> getTaxonomyLevelCatalogLaunchers() {
		return taxonomyLevelCatalogLaunchers;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (launcherCtrls.contains(source)) {
			fireEvent(ureq, event);
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == openAdminLink) {
			fireEvent(ureq, OPEN_ADMIN_EVENT);
		} else if (source == taxonomyEditLink) {
			fireEvent(ureq, TAXONOMY_ADMIN_EVENT);
		}
	}

}
