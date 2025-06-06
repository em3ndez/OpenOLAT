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
package org.olat.modules.catalog.ui.admin;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.catalog.CatalogSecurityCallback;
import org.olat.modules.catalog.ui.CatalogV2UIFactory;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomySecurityCallback;
import org.olat.modules.taxonomy.ui.TaxonomyOverviewController;
import org.olat.modules.taxonomy.ui.TaxonomySelectionController;
import org.olat.modules.taxonomy.ui.TaxonomySelectionController.TaxonomySelectionEvent;
import org.olat.repository.RepositoryModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 1 Sep 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CatalogTaxonomyEditController extends BasicController implements Activateable2 {
	
	public static final Event OPEN_ADMIN_EVENT = new Event("open.admin");

	private static final String ORES_TYPE_TAXONOMY = "Taxonomy";
	
	private TooledStackedPanel stackPanel;
	private VelocityContainer mainVC;
	private Link openAdminLink;

	private TaxonomySelectionController taxonomySelectionCtrl;
	private TaxonomyOverviewController taxonomyCtrl;
	
	private CatalogSecurityCallback secCallback;

	@Autowired
	private RepositoryModule repositoryModule;

	public CatalogTaxonomyEditController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			CatalogSecurityCallback secCallback) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(CatalogV2UIFactory.class, getLocale(), getTranslator()));
		this.stackPanel = stackPanel;
		this.secCallback = secCallback;
		
		mainVC = createVelocityContainer("taxonomy_edit");
		
		if (secCallback.canEditCatalogAdministration()) {
			openAdminLink = LinkFactory.createLink("open.admin", mainVC, this);
			openAdminLink.setIconLeftCSS("o_icon o_icon_external_link");
		}
		
		taxonomySelectionCtrl = new TaxonomySelectionController(ureq, wControl, repositoryModule.getTaxonomyRefs());
		listenTo(taxonomySelectionCtrl);
		mainVC.put("taxonomy.selection", taxonomySelectionCtrl.getInitialComponent());
		
		putInitialPanel(mainVC);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if (entries == null || entries.isEmpty()) {
			return;
		}
		
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if (ORES_TYPE_TAXONOMY.equalsIgnoreCase(type)) {
			Long taxonomyKey = entries.get(0).getOLATResourceable().getResourceableId();
			Taxonomy taxonomy = taxonomySelectionCtrl.getTaxonomy(taxonomyKey);
			if (taxonomy != null) {
				doSelectTaxonomy(ureq, taxonomy);
				List<ContextEntry> subEntries = entries.subList(1, entries.size());
				taxonomyCtrl.activate(ureq, subEntries, state);
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == openAdminLink) {
			fireEvent(ureq, OPEN_ADMIN_EVENT);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == taxonomySelectionCtrl) {
			if (event instanceof TaxonomySelectionEvent) {
				doSelectTaxonomy(ureq, ((TaxonomySelectionEvent)event).getTaxonomy());
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void doDispose() {
		super.doDispose();
		stackPanel.removeListener(this);
	}

	private void doSelectTaxonomy(UserRequest ureq, Taxonomy taxonomy) {
		removeAsListenerAndDispose(taxonomyCtrl);
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(ORES_TYPE_TAXONOMY, taxonomy.getKey());
		WindowControl swControl = addToHistory(ureq, ores, null);
		
		TaxonomySecurityCallback taxonomySecCallback = secCallback.canEditFullTaxonomies()
				? TaxonomySecurityCallback.FULL
				: new CatalogTaxonomySecurityCallback(taxonomy, getIdentity());
		
		taxonomyCtrl = new TaxonomyOverviewController(ureq, swControl, taxonomySecCallback, taxonomy);
		taxonomyCtrl.setBreadcrumbPanel(stackPanel);
		stackPanel.pushController(StringHelper.escapeHtml(taxonomy.getDisplayName()), taxonomyCtrl);
	}

}
